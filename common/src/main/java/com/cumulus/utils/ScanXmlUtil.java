package com.cumulus.utils;

import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;

/**
 * 实体转化成漏扫配置文件工具类
 *
 * @author zhaoff
 */
public class ScanXmlUtil {

    /**
     * 实体转化成漏扫配置文件
     *
     * @param obj      实体
     * @param filename 文件名字
     * @return 文件路径
     */
    public static String toXmlString(Object obj, String filename) {
        String result;
        try {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(CharacterEscapeHandler.class.getName(),
                    (CharacterEscapeHandler) (ch, start, length, isAttVal, writer) -> writer.write(ch, start, length));
            File file = FileUtils.createTempFile("scan_" + filename, ".xml");
            marshaller.marshal(obj, file);
            result = file.getPath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
