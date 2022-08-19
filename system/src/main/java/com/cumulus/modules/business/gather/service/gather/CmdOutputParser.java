package com.cumulus.modules.business.gather.service.gather;


import com.cumulus.modules.business.gather.common.utils.CommUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 对命令行输出结果进行解析的解析器
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class CmdOutputParser extends OutputParser {

    @Override
    public String getName() {
        return "command";
    }

    /**
     * 使用解析后的结果，对表达式进行计算，获取 clz 类型的结果
     *
     * @param <E>  返回结果的类型
     * @param el   表达式
     * @param vars 变量
     * @param clz  返回结果的类型
     * @return 解析后的结果
     */
    public <E> E parseEl(String el, Map<String, Object> vars, Class<E> clz) {
        try {
            return evaluate(el, vars, clz);
        } catch (Exception e) {
            if (log.isWarnEnabled()) {
                log.warn("Failed to parse el:" + el + " with vars:" + CommUtils.toJson(vars) + "to result:" + clz);
            }
        }
        return null;
    }

    /**
     * 返回命令的输出
     *
     * @param output 原始输出
     * @return 命令的输出
     */
    public String findCommandOutput(String output) {
        String[] lines = output.split("\\R");
        StringBuilder str = new StringBuilder();
        for (String line : lines) {
            if (line.contains("asset_flag_start") || line.contains("asset_flag_end")) {
                continue;
            }
            str.append(line).append("\n");
        }
        return str.toString();
    }

}
