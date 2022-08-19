package com.cumulus.mysql.userType;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 通用对 Json 类型的实体在入库前/出库后进行处理的处理器（示例）
 *
 * @author zhaoff
 */
public class UniversalJsonTypeProcessor implements JsonTypeProcessor {

    @Override
    public Object beforeSave(Object data) {
        if (!(data instanceof Map) || !((Map<?, ?>) data).containsKey("services")) {
            return data;
        }
        Map<?, ?> services = (Map<?, ?>) ((Map<?, ?>) data).get("services");
        if (services != null && services.containsKey("vnc")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> vncs = (Map<String, Object>) services.get("vnc");
            if (StringUtils.isNotBlank((String) vncs.get("vnc_passwd"))) {
                // TODO 加密
                byte[] vncpassword = null;
                if (vncpassword != null) {
                    vncs.put("vnc_passwd", vncpassword);
                }
            }
        }
        return data;
    }

    @Override
    public Object afterLoad(Object data) {
        if (!(data instanceof Map) || !((Map<?, ?>) data).containsKey("services")) {
            return data;
        }
        Map<?, ?> services = (Map<?, ?>) ((Map<?, ?>) data).get("services");
        if (services != null && services.containsKey("vnc")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> vncs = (Map<String, Object>) services.get("vnc");
            if (StringUtils.isNotBlank((String) vncs.get("vnc_passwd"))) {
                // TODO 解密
                byte[] vncpassword = null;
                if (vncpassword != null) {
                    vncs.put("vnc_passwd", new String(vncpassword));
                }
            }
        }
        return data;
    }
}
