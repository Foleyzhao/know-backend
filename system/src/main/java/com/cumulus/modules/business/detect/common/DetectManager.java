package com.cumulus.modules.business.detect.common;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.cumulus.enums.MessageTypeEnum;
import com.cumulus.exception.BadRequestException;
import com.cumulus.modules.business.detect.dto.DetectRequest;
import com.cumulus.modules.business.detect.dto.DetectTaskIpDto;
import com.cumulus.modules.business.detect.entity.DetectRecord;
import com.cumulus.modules.business.detect.entity.DetectTask;
import com.cumulus.modules.business.detect.repository.DetectRecordRepository;
import com.cumulus.modules.business.detect.repository.DetectTaskRepository;
import com.cumulus.modules.business.detect.service.impl.DetectTaskServiceImpl;
import com.cumulus.modules.business.repository.IpLibraryRepository;
import com.cumulus.modules.system.entity.Dept;
import com.cumulus.modules.system.entity.Menu;
import com.cumulus.modules.system.repository.DeptRepository;
import com.cumulus.modules.system.service.MessageService;
import com.cumulus.utils.DateUtils;
import com.cumulus.utils.RegexUtil;
import com.cumulus.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 发现任务管理类
 *
 * @author zhangxq
 */
@Slf4j
@Component
public class DetectManager {

    /**
     * 解析掩码Map
     */
    private final static Map<Integer, Integer> IP_MASK_MAP = new HashMap<>();

    /**
     * 解析掩码格式
     */
    static {
        IP_MASK_MAP.put(0, 0);
        IP_MASK_MAP.put(1, 128);
        IP_MASK_MAP.put(2, 192);
        IP_MASK_MAP.put(3, 224);
        IP_MASK_MAP.put(4, 240);
        IP_MASK_MAP.put(5, 248);
        IP_MASK_MAP.put(6, 252);
        IP_MASK_MAP.put(7, 254);
        IP_MASK_MAP.put(8, 255);
    }

    /**
     * redis模板
     */
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发现任务数据访问接口
     */
    @Autowired
    private DetectTaskRepository detectTaskRepository;

    /**
     * 任务记录数据访问接口
     */
    @Autowired
    private DetectRecordRepository detectRecordRepository;

    /**
     * ip库数据访问接口
     */
    @Autowired
    private IpLibraryRepository ipLibraryRepository;

    /**
     * 部门数据访问接口
     */
    @Autowired
    private DeptRepository deptRepository;

    /**
     * 消息通知服务
     */
    @Autowired
    private MessageService messageService;

    /**
     * 转为引擎参数
     *
     * @param detectTask 发现任务
     * @return 引擎参数传输对象
     */
    public List<DetectRequest> toParam(DetectTask detectTask, DetectTaskIpDto detectTaskIpDto) throws InterruptedException {
        DetectRequest detectRequest = new DetectRequest();
        Long detectTaskId = detectTask.getId();
        //任务id
        detectRequest.setId(detectTaskId.toString());
        //端口
        switch (detectTask.getPortRange()) {
            case DetectConstant.PORT_ALL:
                //全部
                detectRequest.setPort("1-65535");
                break;
            case DetectConstant.PORT_TOP:
                //常见top
                detectRequest.setPort_top(Integer.parseInt(detectTask.getPortList()));
                break;
            case DetectConstant.PORT_DIY:
                //自定义
                detectRequest.setPort(detectTask.getPortList());
                break;
            default:
        }
        //发包速率
        if (detectTask.getSendSpeed() != 0) {
            detectRequest.setRate(detectTask.getSendSpeed());
        }
        //ip范围
        List<String> ipList = new ArrayList<>();
        switch (detectTask.getIpRange()) {
            case DetectConstant.IP_ALL:
                //全部
                ipList = ipLibraryRepository.queryIp();
                detectTaskIpDto.setFLAG(true);
                break;
            case DetectConstant.IP_DEPT:
                //按部门查询
                String[] deptIds = StringUtils.split(detectTask.getDeptList(), RegexUtil.SEPARATOR_COMMA);
                Set<Long> set = new HashSet<>();
                for (String deptId : deptIds) {
                    set.add(Long.parseLong(deptId));
                }
                ipList = ipLibraryRepository.queryIpByDept(set);
                detectTaskIpDto.setFLAG(true);
                break;
            case DetectConstant.IP_DIY:
                //统计ip个数做任务进度
                ipList = ipParse(detectTask.getIpList(), detectTaskIpDto);
                break;
            default:
        }
        //存入redis总数和当前完成数
//        redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + detectTaskId).
//                put(DetectConstant.REDIS_KEY_NUM, ipList.size());
        redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + detectTaskId).
                put(DetectConstant.REDIS_KEY_DONE, 0);
        redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + detectTaskId).
                put(DetectConstant.REDIS_KEY_ONLINE, 0);
        redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + detectTaskId).
                put(DetectConstant.REDIS_KEY_OFFLINE, 0);
        List<DetectRequest> requestList = new ArrayList<>();
        StringBuffer str = new StringBuffer("");
        for (String ip : ipList) {
            if ("".equals(str.toString())) {
                str.append(ip);
                continue;
            }
            str.append(",").append(ip);
        }
        DetectRequest detectRequestIp = new DetectRequest();
        BeanUtils.copyProperties(detectRequest, detectRequestIp);
        detectRequestIp.setAssets(str.toString());
        requestList.add(detectRequestIp);
        if (!"".equals(requestList.get(0).getAssets())) {
            DetectTaskServiceImpl.taskResult.offer(requestList);
        }
        return requestList;
    }

    /**
     * 解析
     *
     * @param iplist
     * @param detectTaskIpDto
     * @return
     */
    public static List<String> ipParse(String iplist, DetectTaskIpDto detectTaskIpDto) {
        if (null == iplist) {
            return new ArrayList<>();
        }
        List<String> resList = new ArrayList<>();
        // 掩码格式
        if (null != detectTaskIpDto.getIpMaskList()) {
            if (detectTaskIpDto.getIpMaskList().size() != 0) {
                List<DetectTaskIpDto.IpSegmentList> ipSegmentLists = ipMaskToList(detectTaskIpDto);
                detectTaskIpDto.setIpSegmentList(ipSegmentLists);
                detectTaskIpDto.getIpMaskList().remove(0);
            }
        }
        //0-255.0-255.0-255.0-255
        if (null != detectTaskIpDto.getIpSegmentList()) {
            if (detectTaskIpDto.getIpSegmentList().size() != 0) {
                List<String> ipList = ipSegmentToList(detectTaskIpDto, resList);
                resList.clear();
                resList.addAll(ipList);
                if (resList.size() != 0) {
                    if (resList.size() != 50) {
                        detectTaskIpDto.setTO(false);
                        detectTaskIpDto.getIpSegmentList().remove(0);
                    } else {
                        return resList;
                    }
                }
            }
        }
        // 其他 如单个ip ipv6
        if (null != detectTaskIpDto.getIpList()) {
            List<String> list = RegexUtil.ipStrToList(iplist);
            resList.addAll(list);
            detectTaskIpDto.setIpList(null);
        }
        if (null == detectTaskIpDto.getIpSegmentList() && null == detectTaskIpDto.getIpMaskList() && null == detectTaskIpDto.getIpList()) {
            detectTaskIpDto.setFLAG(true);
        }
        return resList;
    }

    /**
     * 掩码转换
     *
     * @param detectTaskIpDto
     * @return
     */
    public static List<DetectTaskIpDto.IpSegmentList> ipMaskToList(DetectTaskIpDto detectTaskIpDto) {
        for (String ipMask : detectTaskIpDto.getIpMaskList()) {
            detectTaskIpDto.setIp(ipMask);
            String[] split = ipMask.split("/");
            String[] ipSplit = split[0].split("\\.");
            if (Integer.parseInt(split[1]) == 8) {
                List<DetectTaskIpDto.IpSegmentList> manySegmentList = new ArrayList<>();
                DetectTaskIpDto.IpSegmentList ipSegment = new DetectTaskIpDto.IpSegmentList();
                DetectTaskIpDto.IpSegmentInterval ip1 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip2 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip3 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip4 = new DetectTaskIpDto.IpSegmentInterval();
                ip1.setMax(Integer.parseInt(ipSplit[0]));
                ip1.setMin(Integer.parseInt(ipSplit[0]));
                ip2.setMax(255);
                ip2.setMin(0);
                ip3.setMax(255);
                ip3.setMin(0);
                ip4.setMax(255);
                ip4.setMin(0);
                ipSegment.setIp1(ip1);
                ipSegment.setIp2(ip2);
                ipSegment.setIp3(ip3);
                ipSegment.setIp4(ip4);
                ipSegment.setIpSegment(ipMask);
                ipSegment.setOne(ip1.getMin());
                ipSegment.setTwo(ip2.getMin());
                ipSegment.setThree(ip3.getMin());
                ipSegment.setFour(ip4.getMin());
                manySegmentList.add(ipSegment);
                return manySegmentList;
            } else if (Integer.parseInt(split[1]) == 16) {
                List<DetectTaskIpDto.IpSegmentList> manySegmentList = new ArrayList<>();
                DetectTaskIpDto.IpSegmentList ipSegment = new DetectTaskIpDto.IpSegmentList();
                DetectTaskIpDto.IpSegmentInterval ip1 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip2 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip3 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip4 = new DetectTaskIpDto.IpSegmentInterval();
                ip1.setMax(Integer.parseInt(ipSplit[0]));
                ip1.setMin(Integer.parseInt(ipSplit[0]));
                ip2.setMax(Integer.parseInt(ipSplit[1]));
                ip2.setMin(Integer.parseInt(ipSplit[1]));
                ip3.setMax(255);
                ip3.setMin(0);
                ip4.setMax(255);
                ip4.setMin(0);
                ipSegment.setIp1(ip1);
                ipSegment.setIp2(ip2);
                ipSegment.setIp3(ip3);
                ipSegment.setIp4(ip4);
                ipSegment.setIpSegment(ipMask);
                ipSegment.setOne(ip1.getMin());
                ipSegment.setTwo(ip2.getMin());
                ipSegment.setThree(ip3.getMin());
                ipSegment.setFour(ip4.getMin());
                manySegmentList.add(ipSegment);
                return manySegmentList;
            } else if (Integer.parseInt(split[1]) == 24) {
                List<DetectTaskIpDto.IpSegmentList> manySegmentList = new ArrayList<>();
                DetectTaskIpDto.IpSegmentList ipSegment = new DetectTaskIpDto.IpSegmentList();
                DetectTaskIpDto.IpSegmentInterval ip1 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip2 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip3 = new DetectTaskIpDto.IpSegmentInterval();
                DetectTaskIpDto.IpSegmentInterval ip4 = new DetectTaskIpDto.IpSegmentInterval();
                ip1.setMax(Integer.parseInt(ipSplit[0]));
                ip1.setMin(Integer.parseInt(ipSplit[0]));
                ip2.setMax(Integer.parseInt(ipSplit[1]));
                ip2.setMin(Integer.parseInt(ipSplit[1]));
                ip3.setMax(Integer.parseInt(ipSplit[2]));
                ip3.setMin(Integer.parseInt(ipSplit[2]));
                ip4.setMax(255);
                ip4.setMin(0);
                ipSegment.setIpSegment(ipMask);
                ipSegment.setIp1(ip1);
                ipSegment.setIp2(ip2);
                ipSegment.setIp3(ip3);
                ipSegment.setIp4(ip4);
                ipSegment.setOne(ip1.getMin());
                ipSegment.setTwo(ip2.getMin());
                ipSegment.setThree(ip3.getMin());
                ipSegment.setFour(ip4.getMin());
                manySegmentList.add(ipSegment);
                return manySegmentList;
            }
        }
        return null;
    }

    /**
     * 0-255.0-255.0-255.0-255 转换
     *
     * @param resList
     * @return
     */
    public static List<String> ipSegmentToList(DetectTaskIpDto detectTaskIpDto, List<String> resList) {
        List<String> iplist = new ArrayList<>(resList);
        for (DetectTaskIpDto.IpSegmentList segmentList : detectTaskIpDto.getIpSegmentList()) {
            if (!segmentList.getIpSegment().equals(detectTaskIpDto.getIpSegmentList().get(0).getIpSegment())) {
                return iplist;
            }
            segmentList.setIpSegment(detectTaskIpDto.getIpSegmentList().get(0).getIpSegment());
            if (segmentList.getOne() == 0 && segmentList.getTwo() == 0 && segmentList.getThree() == 0 && segmentList.getFour() == 0) {
                segmentList.setOne(segmentList.getIp1().getMin());
                segmentList.setTwo(segmentList.getIp2().getMin());
                segmentList.setThree(segmentList.getIp3().getMin());
                segmentList.setFour(segmentList.getIp4().getMin());
            }
            for (int i = segmentList.getOne(); i <= segmentList.getIp1().getMax(); i++) {
                if (iplist.size() != 0) {
                    segmentList.setTwo(segmentList.getIp2().getMin());
                }
                for (int j = segmentList.getTwo(); j <= segmentList.getIp2().getMax(); j++) {
                    if (iplist.size() != 0) {
                        segmentList.setThree(segmentList.getIp3().getMin());
                    }
                    for (int k = segmentList.getThree(); k <= segmentList.getIp3().getMax(); k++) {
                        if (iplist.size() != 0) {
                            segmentList.setFour(segmentList.getIp4().getMin());
                        }
                        for (int l = segmentList.getFour(); l <= segmentList.getIp4().getMax(); l++) {
                            if (iplist.size() == 50 && detectTaskIpDto.isIpLib()) {
                                segmentList.setOne(i);
                                segmentList.setTwo(j);
                                segmentList.setThree(k);
                                segmentList.setFour(l);
                                return iplist;
                            }
                            if (iplist.size() > 100000) {
                                throw new BadRequestException("Too many IP addresses");
                            }
                            iplist.add(i + "." + j + "." + k + "." + l);
                        }
                    }
                }
            }
        }
        return iplist;
    }


    /**
     * 开始任务 添加记录
     *
     * @param detectTask 发现任务
     */
    public void startRecord(DetectTask detectTask) {
        Long id = detectTask.getId();
        //如果不存在任务id 证明不是暂停继续 是重新开始的任务
        if (!redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + id).
                hasKey(DetectConstant.REDIS_KEY_RECORDID)) {
            DetectRecord detectRecord = new DetectRecord();
            detectRecord.setDetectTaskId(id);
            switch (detectTask.getIpRange()) {
                case DetectConstant.IP_ALL:
                    detectRecord.setIpList("全部");
                    break;
                case DetectConstant.IP_DEPT:
                    String[] ids = StringUtils.split(detectTask.getIpList(), RegexUtil.SEPARATOR_COMMA);
                    Set<Long> idSet = new HashSet<>();
                    for (String deptId : ids) {
                        idSet.add(Long.parseLong(deptId));
                    }
                    List<Dept> depts = deptRepository.findAllById(idSet);
                    StringBuilder builder = new StringBuilder("");
                    depts.forEach(e -> {
                        if ("".equals(builder.toString())) {
                            builder.append(e.getName());
                        } else {
                            builder.append(",").append(e.getName());
                        }
                    });
                    detectRecord.setIpList(builder.toString());
                    break;
                case DetectConstant.IP_DIY:
                    detectRecord.setIpList(detectTask.getIpList());
                    break;
                default:
            }
            detectRecord.setStartTime(new Timestamp(System.currentTimeMillis()));
            DetectRecord record = detectRecordRepository.save(detectRecord);
            //存入redis任务信息
            redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + id).
                    put(DetectConstant.REDIS_KEY_RECORDID, record.getId());
        }
    }

    /**
     * 任务执行完成 记录
     */
    public void endRecord(DetectTask detectTask) {
        Long taskId = detectTask.getId();
        Long recordId = Long.parseLong(redisTemplate.boundHashOps(
                DetectConstant.REDIS_KEY_DETECT + taskId).get(DetectConstant.REDIS_KEY_RECORDID).toString());
        DetectRecord record = detectRecordRepository.findById(recordId).orElse(null);
        if (null == record) {
            log.info("当前任务记录不存在");
            return;
        }
        //更新任务记录
        record.setEndTime(new Timestamp(System.currentTimeMillis()));
        record.setOnline(Long.parseLong(redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + taskId).
                get(DetectConstant.REDIS_KEY_ONLINE).toString()));
        record.setOffline(Long.parseLong(redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + taskId).
                get(DetectConstant.REDIS_KEY_OFFLINE).toString()));
        detectRecordRepository.save(record);
        //更新发现任务
        detectTask.setOnline(record.getOnline());
        detectTask.setOffline(record.getOffline());
        detectTask.setTaskStatus(DetectConstant.TASK_STATUS_END);
        detectTaskRepository.save(detectTask);
        //删除redis任务id
        redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + taskId).
                delete(DetectConstant.REDIS_KEY_RECORDID);
        //发送系统通知
//        messageService.sendMessage(MessageTypeEnum.DETECT_END_TYPE, Menu.DEFAULT_DETECT_MENU_ID,
//                Collections.singletonList(detectTask.getId().toString()), detectTask.getDetectTaskName(),
//                DateUtils.SIMPLE_DFY_MD_HMS.format(new Date()), record.getResult());
    }

    /**
     * 取消任务
     *
     * @param id 发现任务id
     */
    public void cancelRecord(Long id) {
        Long recordId = Long.parseLong(redisTemplate.boundHashOps(
                DetectConstant.REDIS_KEY_DETECT + id).get(DetectConstant.REDIS_KEY_RECORDID).toString());
        DetectRecord record = detectRecordRepository.findById(recordId).orElse(null);
        if (null == record) {
            log.info("当前任务记录不存在");
            return;
        }
        //更新任务记录
        record.setEndTime(new Timestamp(System.currentTimeMillis()));
        record.setCancel(true);
        detectRecordRepository.save(record);
        //更新发现任务
        DetectTask detectTask = detectTaskRepository.findById(id).orElse(null);
        if (detectTask == null) {
            log.info("当前任务不存在");
            return;
        }
        detectTask.setCancel(true);
        detectTask.setLastResult("任务已被取消");
        detectTask.setTaskStatus(DetectConstant.TASK_STATUS_END);
        detectTaskRepository.save(detectTask);
        //删除redis任务id
        redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + id).
                delete(DetectConstant.REDIS_KEY_RECORDID);
    }

    /**
     * redis存放 添加 key +1
     *
     * @param id 发现任务id
     */
    public void redisAdd(Long id, String key) {
        redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + id).put(key, Integer.parseInt(
                redisTemplate.boundHashOps(DetectConstant.REDIS_KEY_DETECT + id).get(key).toString()) + 1);
    }

    /**
     * 清除发现任务key
     *
     * @param id 任务id
     */
    public void delRedisDetectKey(Long id) {
        if (redisTemplate.hasKey(DetectConstant.REDIS_KEY_DETECT + id)) {
            redisTemplate.delete(DetectConstant.REDIS_KEY_DETECT + id);
        }
    }
}
