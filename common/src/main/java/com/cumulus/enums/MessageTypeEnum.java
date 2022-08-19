package com.cumulus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统消息类型枚举
 *
 * @author : shenjc
 */
@Getter
@AllArgsConstructor
public enum MessageTypeEnum {

    /**
     * 系统消息类型枚举
     */
    VUL_SCAN_END_TYPE(1, "漏洞扫描任务执行结果", "漏洞扫描任务:%s在%s执行结束", new String[]{"idIs"}),
    DETECT_END_TYPE(2, "资产发现任务执行结果", "资产发现任务:%s在%s执行结束,新发现%s条资产", new String[]{"id"}),
    GATHER_END_TYPE(3, "资产采集任务执行结果", "数据采集任务:%s在%s执行结束", new String[]{"id"}),
    RISK_CLOSE_TYPE(4, "风险关闭结果通知", "您提交的风险已关闭:%s", null),
    RISK_ISSUED_SUCCESS_TYPE(5, "风险下发结果通知", "您提交的风险下发已通过:%s", null),
    RISK_ISSUED_OVERRULE_TYPE(6, "风险下发结果通知", "您提交的风险下发被驳回:%s", null),
    RISK_REPAIR_SUCCESS_TYPE(7, "风险修复结果通知", "您提交的风险修复已通过:%s", null),
    RISK_REPAIR_OVERRULE_TYPE(8, "风险修复结果通知", "您提交的风险修复被驳回:%s", null),
    RISK_RETEST_SUCCESS_TYPE(9, "风险复测结果通知", "您提交的风险复测已通过:%s", null),
    RISK_RETEST_OVERRULE_TYPE(10, "风险复测结果通知", "您提交的风险复测被驳回:%s", null),
    NEW_WAIT_TO_ISSUED_RISK_TYPE(11, "新增待下发风险", "收到一条待下发风险申请:%s", new String[]{"id"}),
    NEW_WAIT_TO_REPAIR_RISK_TYPE(12, "新增待修复风险", "收到一条待修复风险申请:%s", new String[]{"id"}),
    NEW_WAIT_TO_RETEST_RISK_TYPE(13, "新增待复测风险", "收到一条待复测风险申请:%s", new String[]{"id"}),
    NEW_WAIT_TO_CLOSE_RISK_TYPE(14, "新增待关闭风险", "收到一条待关闭风险申请:%s", new String[]{"id"});

    /**
     * 类型id
     */
    private final int type;

    /**
     * 类型名
     */
    private final String name;

    /**
     * 消息内容
     */
    private final String messageContext;

    /**
     * 跳转用参数 null 代表没有
     */
    private final String[] jumpParameters;

    /**
     * 占位符
     */
    private static final String PLACEHOLDER = "%s";

    /**
     * 根据 type 获取 name
     *
     * @param type 类型
     * @return 返回name
     */
    public static String getNameByType(Integer type) {
        for (MessageTypeEnum value : MessageTypeEnum.values()) {
            if (((Integer) value.getType()).equals(type)) {
                return value.getName();
            }
        }
        return null;
    }

    /**
     * 生成结果消息
     */
    public String generateMessageContext(String... paramList) {
        return generateString(messageContext, Arrays.asList(paramList.clone()));
    }

    /**
     * 生成跳转参数
     *
     * @param paramList 参数列表
     * @return 返回结果
     */
    public Map<String, Object> generateJumpParameters(List<String> paramList) {
        if (jumpParameters == null || paramList == null || paramList.isEmpty()) {
            return null;
        }
        Map<String, Object> result = new HashMap<>();
        int maxSize = Math.max(paramList.size(), jumpParameters.length);
        for (int i = 0; i < maxSize; i++) {
            result.put(jumpParameters[i], paramList.get(i));
        }
        return result;
    }

    /**
     * 生成字段
     *
     * @param nativeStr 原生字符串
     * @param paramList 参数列表
     * @return 返回结果
     */
    private static String generateString(String nativeStr, List<String> paramList) {
        if (StringUtils.isBlank(nativeStr) || paramList == null || paramList.isEmpty()) {
            return nativeStr;
        }
        StringBuilder result = new StringBuilder(nativeStr);
        for (String param : paramList) {
            final int index = result.indexOf(PLACEHOLDER);
            if (index != -1) {
                result.replace(index, index + PLACEHOLDER.length(), param);
            } else {
                break;
            }
        }
        return result.toString();
    }
}
