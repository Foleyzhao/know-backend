package com.cumulus.modules.business.gather.common.utils;


import com.cumulus.modules.business.entity.Asset;
import com.cumulus.modules.business.gather.common.service.CmdReceiveListener;
import com.cumulus.modules.business.gather.common.service.impl.CmdReceiveBean;
import com.cumulus.modules.business.gather.request.TaskResponse;
import com.cumulus.modules.business.gather.request.TaskResponseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 采集业务通用服务
 *
 * @author zhaoff
 */
@Component
public class BusinessCommon {

    /**
     * 采集指令专用消息接收器
     */
    @Autowired
    private CmdReceiveBean cmdReceiveBean;

    /**
     * 给采集任务添加监听采集指令执行结果响应的监听器
     *
     * @param taskResponseWrapper 采集引擎响应的封装
     * @param taskId              执行采集指令任务id
     * @return 等待对象，收到消息之后变为有信号的状态
     */
    public Object addTaskListener(TaskResponseWrapper taskResponseWrapper, UUID taskId) {
        Object monitor = new Object();
        cmdReceiveBean.addListener(new CmdReceiveListener() {
            @Override
            public boolean receiveAndHandle(TaskResponse response) {
                taskResponseWrapper.setTaskResponse(response);
                synchronized (monitor) {
                    monitor.notify();
                }
                cmdReceiveBean.removeListener(this);
                return true;
            }

            @Override
            public boolean interest(String responseId) {
                return responseId.equals(taskId.toString());
            }
        });
        return monitor;
    }

    /**
     * 超时等待监听采集指令执行结果响应的监听器的结果
     *
     * @param taskResponseWrapper 采集引擎响应的封装
     * @param monitor             监听采集指令执行结果响应的监听器的等待对象
     * @param tto                 等待超时（单位：秒）
     * @throws Exception 异常
     */
    public void waitForResponse(TaskResponseWrapper taskResponseWrapper, Object monitor, int tto) throws Exception {
        try {
            synchronized (monitor) {
                if (null == taskResponseWrapper.getTaskResponse()) {
                    monitor.wait(tto * 1000L);
                }
            }
        } catch (InterruptedException e) {
            throw new InterruptedException("Wait interrupt");
        }
        if (null == taskResponseWrapper.getTaskResponse()) {
            throw new Exception("Wait gather engine timeout");
        }
    }

    /**
     * 合并两个相同实体
     *
     * @param target       实体M
     * @param destinnation 实体M
     * @param <M>          实体
     * @throws Exception 抛出异常
     */
    public static <M> void merge(M target, M destinnation) throws Exception {
        BeanInfo beanInfo = Introspector.getBeanInfo(target.getClass());

        for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {

            if (descriptor.getWriteMethod() != null) {
                Object originalValue = descriptor.getReadMethod().invoke(target);

                if (originalValue == null) {
                    Object defaultValue = descriptor.getReadMethod().invoke(destinnation);
                    descriptor.getWriteMethod().invoke(target, defaultValue);
                }
            }
        }
    }

    /**
     * 去除待系统前缀的采集key，例如lapp-xxx.xxx.xxx -> app-xxx.xxx.xxx
     *
     * @param itemKey 采集项
     * @return 去除待系统前缀后采集key
     */
    public static String eraseSysFromItemkey(String itemKey) {
        if (null == itemKey) {
            return null;
        }
        if (itemKey.contains("db-")) {
            int index = itemKey.indexOf("db-");
            itemKey = itemKey.substring(index);
        } else if (itemKey.contains("app-")) {
            int index = itemKey.indexOf("app-");
            itemKey = itemKey.substring(index);
        } else if (itemKey.contains("mw-")) {
            int index = itemKey.indexOf("mw-");
            itemKey = itemKey.substring(index);
        } else {
            int index = itemKey.indexOf("-");
            itemKey = itemKey.substring(index + 1);
        }
        return itemKey;
    }

    /**
     * 从itemkey中获取采集项的名称
     * eg:l-osversion.osversion.osversion -> osversion
     *
     * @param itemKey 采集项key
     * @return 采集项名称
     */
    public static String getNameFromItemKey(String itemKey) {
        if (null == itemKey) {
            return null;
        }
        int index = itemKey.lastIndexOf(".");
        return itemKey.substring(index + 1);
    }


}
