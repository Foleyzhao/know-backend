package com.cumulus.modules.security.service;

import com.cumulus.modules.security.config.bean.SecurityProperties;
import com.cumulus.modules.security.dto.JwtUserDto;
import com.cumulus.modules.security.dto.OnlineUserDto;
import com.cumulus.utils.EncryptUtils;
import com.cumulus.utils.FileUtils;
import com.cumulus.utils.PageUtils;
import com.cumulus.utils.RedisUtils;
import com.cumulus.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 在线用户服务
 */
@Service
@Slf4j
public class OnlineUserService {

    /**
     * Jwt参数配置
     */
    @Autowired
    private SecurityProperties properties;

    /**
     * Redis工具类
     */
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 保存在线用户信息
     *
     * @param jwtUserDto JWT用户传输对象
     * @param token      token
     * @param request    请求
     */
    public void save(JwtUserDto jwtUserDto, String token, HttpServletRequest request) {
        String dept = jwtUserDto.getUser().getDept().getName();
        String ip = StringUtils.getIp(request);
        String browser = StringUtils.getBrowser(request);
        String address = StringUtils.getCityInfo(ip);
        OnlineUserDto onlineUserDto = null;
        try {
            onlineUserDto = new OnlineUserDto(jwtUserDto.getUsername(), jwtUserDto.getUser().getNickName(), dept,
                    browser, ip, address, EncryptUtils.desEncrypt(token), new Date());
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        redisUtils.set(properties.getOnlineKey() + token, onlineUserDto,
                properties.getTokenValidityInSeconds() / 1000);
    }

    /**
     * 分页查询在线用户
     *
     * @param filter   查询参数
     * @param pageable 分页参数
     * @return 在线用户列表
     */
    public Map<String, Object> getAll(String filter, Pageable pageable) {
        List<OnlineUserDto> onlineUserDtos = getAll(filter);
        return PageUtils.toPage(PageUtils.toPage(pageable.getPageNumber(), pageable.getPageSize(), onlineUserDtos),
                onlineUserDtos.size());
    }

    /**
     * 查询在线用户
     *
     * @param filter 查询参数
     * @return 在线用户列表
     */
    public List<OnlineUserDto> getAll(String filter) {
        List<String> keys = redisUtils.scan(properties.getOnlineKey() + "*");
        Collections.reverse(keys);
        List<OnlineUserDto> onlineUserDtos = new ArrayList<>();
        for (String key : keys) {
            OnlineUserDto onlineUserDto = (OnlineUserDto) redisUtils.get(key);
            if (StringUtils.isNotBlank(filter)) {
                if (onlineUserDto.toString().contains(filter)) {
                    onlineUserDtos.add(onlineUserDto);
                }
            } else {
                onlineUserDtos.add(onlineUserDto);
            }
        }
        onlineUserDtos.sort((o1, o2) -> o2.getLoginTime().compareTo(o1.getLoginTime()));
        return onlineUserDtos;
    }

    /**
     * 踢出在线用户
     *
     * @param key 在线用户token
     */
    public void kickOut(String key) {
        key = properties.getOnlineKey() + key;
        redisUtils.del(key);
    }

    /**
     * 退出登录
     *
     * @param token token
     */
    public void logout(String token) {
        String key = properties.getOnlineKey() + token;
        redisUtils.del(key);
    }

    /**
     * 导出在线用户列表
     *
     * @param all      待导出在线用户列表
     * @param response 响应
     * @throws IOException 异常
     */
    public void download(List<OnlineUserDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (OnlineUserDto user : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", user.getUserName());
            map.put("部门", user.getDept());
            map.put("登录IP", user.getIp());
            map.put("登录地点", user.getAddress());
            map.put("浏览器", user.getBrowser());
            map.put("登录日期", user.getLoginTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

    /**
     * 查询在线用户
     *
     * @param key 在线用户redis key
     * @return 在线用户
     */
    public OnlineUserDto getOne(String key) {
        return (OnlineUserDto) redisUtils.get(key);
    }

    /**
     * 检测用户是否在之前已经登录（配置单用户登录时调用）
     *
     * @param userName   用户名
     * @param igoreToken token
     */
    public void checkLoginOnUser(String userName, String igoreToken) {
        List<OnlineUserDto> onlineUserDtos = getAll(userName);
        if (null == onlineUserDtos || onlineUserDtos.isEmpty()) {
            return;
        }
        for (OnlineUserDto onlineUserDto : onlineUserDtos) {
            if (onlineUserDto.getUserName().equals(userName)) {
                try {
                    String token = EncryptUtils.desDecrypt(onlineUserDto.getKey());
                    if (StringUtils.isNotBlank(igoreToken) && !igoreToken.equals(token)) {
                        this.kickOut(token);
                    } else if (StringUtils.isBlank(igoreToken)) {
                        this.kickOut(token);
                    }
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("CheckUser is error", e);
                    }
                }
            }
        }
    }

    /**
     * 根据用户名强退用户
     *
     * @param username 用户名
     */
    @Async
    public void kickOutForUsername(String username) throws Exception {
        List<OnlineUserDto> onlineUsers = getAll(username);
        for (OnlineUserDto onlineUser : onlineUsers) {
            if (onlineUser.getUserName().equals(username)) {
                String token = EncryptUtils.desDecrypt(onlineUser.getKey());
                kickOut(token);
            }
        }
    }

}
