package com.cumulus.utils;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * CDATA适配器
 *
 * @author zhaoff
 */
public class AdapterCDATA extends XmlAdapter<String, String> {

    @Override
    public String marshal(String str) {
        return "<![CDATA[" + str + "]]>";
    }

    @Override
    public String unmarshal(String str) {
        return str;
    }
}
