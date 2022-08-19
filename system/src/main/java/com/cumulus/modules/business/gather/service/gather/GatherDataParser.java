package com.cumulus.modules.business.gather.service.gather;

import com.alibaba.fastjson.JSONObject;
import com.cumulus.modules.business.gather.common.constant.GatherConstants;
import com.cumulus.modules.business.gather.model.GatherDetail;
import com.cumulus.modules.business.gather.model.GatherDetailOutput;
import com.cumulus.modules.business.gather.model.GatherDetailVariable;
import com.cumulus.modules.business.gather.request.GatherAssetRequest;
import com.cumulus.modules.business.gather.request.GatherCmdRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 采集引擎返回的数据的解析
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class GatherDataParser {

    /**
     * groovy 引擎名称
     */
    public final static String GROOVY_ENGINE_NAME = "groovy";

    /**
     * 解析原始输出的工具
     */
    @Autowired
    private CmdOutputParser cmdOutputParser;

    /**
     * 采集指标维护中心
     */
    @Autowired
    private GatherCenter gatherCenter;

    /**
     * groovy脚本引擎
     */
    private ScriptEngine groovyEngine;

    /**
     * 脚本缓存，key为脚本的md5值
     */
    private Map<String, CompiledScript> scriptCache;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        ScriptEngineManager factory = new ScriptEngineManager();
        groovyEngine = factory.getEngineByName(GROOVY_ENGINE_NAME);
        scriptCache = new ConcurrentHashMap<>();
    }

    /**
     * 解析采集结果
     *
     * @param request    资产采集请求对象
     * @param key        采集项key
     * @param outputVars commands的输出
     * @return 解析采集结果是否成功
     */
    public boolean parseResult(GatherAssetRequest request, String key, Map<String, Object> outputVars) {
        GatherCmdRequest cmdRequest = request.getCmdRequest(key);
        if (null == cmdRequest && key.startsWith(GatherConstants.ITEM_KEY_PRE_REQUISITE)) {
            // 登录测试命令
            cmdRequest = new GatherCmdRequest(key);
        } else if (null == cmdRequest) {
            return false;
        }
        if (null == outputVars) {
            outputVars = new HashMap<>();
        }
        cmdRequest.setOutputs(outputVars);
        GatherDetail detail = gatherCenter.getGatherDetail(key);
        String preKey = gatherCenter.getPreKey(request.getMainCategory(), request.getCategory(), key);
        if (null == preKey && null != detail.getRequire()) {
            preKey = GatherConstants.ITEM_KEY_PRE_REQUISITE + "." + detail.getRequire();
        }
        Map<String, Object> preVars = null;
        Map<String, Object> ansVars = new HashMap<>();
        try {
            GatherAssetRequest preAssetRequest = request.getPreRequest();
            if (null != preAssetRequest) {
                GatherCmdRequest preCmdRequest = preAssetRequest.getCmdRequest(preKey);
                if (null != preCmdRequest) {
                    preVars = preCmdRequest.getVars();
                }
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Make pre gather data error.", e);
            }
        }
        GatherDetailOutput indicatorDetailOutput = null;
        for (GatherDetailOutput itemOutput : detail.getOutput()) {
            String ifParam = itemOutput.getIfParam();
            if (null == ifParam) {
                indicatorDetailOutput = itemOutput;
                break;
            }
            if (null != preVars) {
                try {
                    Boolean ok = cmdOutputParser.evaluate(ifParam, preVars, Boolean.class);
                    if (ok) {
                        indicatorDetailOutput = itemOutput;
                        break;
                    }
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("CmdOutputParser evaluate failed", e);
                    }
                }
            }
        }
        if (null == indicatorDetailOutput) {
            if (log.isWarnEnabled()) {
                log.warn("IndicatorDetailOutput not found");
            }
            return false;
        }

        for (GatherDetailVariable v : indicatorDetailOutput.getVariable()) {
            String el = v.getEvalEl();
            String script = v.getScript();
            Class<?> clz = getType(v.getValueType());
            if (null == clz) {
                return false;
            }
            if (null != el) {
                Object tmp = cmdOutputParser.parseEl(el, cmdRequest.getOutputs(), clz);
                ansVars.put(v.getName(), tmp);
            } else if (null != script) {
                if (null == groovyEngine) {
                    if (log.isErrorEnabled()) {
                        log.error("Groovy not found");
                    }
                    return false;
                }

                CompiledScript compileScript = null;
                String md5 = getMd5(script);
                try {
                    if (null != md5) {
                        compileScript = scriptCache.get(md5);
                    }
                    if (null == compileScript) {
                        compileScript = ((Compilable) groovyEngine).compile(script);
                        if (null != md5) {
                            scriptCache.put(md5, compileScript);
                        }
                    }
                } catch (ScriptException e) {
                    if (log.isErrorEnabled()) {
                        log.error("Groovy script compile fail for itemKey:" + key, e);
                    }
                    return false;
                }

                Bindings bindings = groovyEngine.createBindings();
                bindings.putAll(cmdRequest.getOutputs());
                try {
                    Object scriptRet = compileScript.eval(bindings);
                    ansVars.put(v.getName(), scriptRet);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("ItemKey=" + key, e);
                    }
                }
            }
        }
        cmdRequest.setVars(ansVars);
        if (log.isInfoEnabled()) {
            log.info("parseResult result :{}", JSONObject.toJSONString(ansVars));
        }
        return true;
    }

    /**
     * 根据类型的字符串返回Class实例
     *
     * @param type 字符串指定的类型
     * @return Class实例
     */
    public Class<?> getType(String type) {
        if ("int".equalsIgnoreCase(type)) {
            return Integer.class;
        } else if ("long".equalsIgnoreCase(type)) {
            return Long.class;
        } else if ("double".equalsIgnoreCase(type)) {
            return Double.class;
        } else if ("boolean".equalsIgnoreCase(type)) {
            return Boolean.class;
        } else if ("string".equalsIgnoreCase(type)) {
            return String.class;
        } else if ("list".equalsIgnoreCase(type)) {
            return List.class;
        } else if ("set".equalsIgnoreCase(type)) {
            return Set.class;
        } else if ("map".equalsIgnoreCase(type)) {
            return Map.class;
        } else {
            if (log.isWarnEnabled()) {
                log.warn("invalid variable type:" + type);
            }
            return null;
        }
    }

    /**
     * 获取script脚本的Md5值
     *
     * @param script 脚本
     * @return 脚本的Md5值
     */
    private String getMd5(String script) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("SHA1");
            md5.update(script.getBytes(StandardCharsets.UTF_8));
            byte[] md5Array = md5.digest();
            BigInteger bigInt = new BigInteger(1, md5Array);
            return bigInt.toString(16);
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("failed to get md5 for script:" + script);
            }
            return null;
        }
    }
}
