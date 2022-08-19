package com.cumulus.modules.mnt.websocket;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket服务
 */
@ServerEndpoint("/webSocket/{sid}")
@Slf4j
@Component
public class WebSocketServer {

    /**
     * 存放每个客户端对应的MyWebSocket对象
     */
    private static final CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 客户端连接会话
     */
    private Session session;

    /**
     * 会话ID
     */
    private String sid = "";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        // 如果存在就先删除一个，防止重复推送消息
        webSocketSet.removeIf(webSocket -> webSocket.sid.equals(sid));
        webSocketSet.add(this);
        this.sid = sid;
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送的消息
     * @param session 客户端连接会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        if (log.isInfoEnabled()) {
            log.info("Receive information: " + " + sid: " + sid + ", message: " + message);
        }
        // 群发消息
        for (WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 发生异常后调用的方法
     *
     * @param session 客户端连接会话
     * @param error   异常
     */
    @OnError
    public void onError(Session session, Throwable error) {
        if (log.isErrorEnabled()) {
            log.error("An error occurred");
        }
        error.printStackTrace();
    }

    /**
     * 服务器主动推送信息
     *
     * @param message 信息
     * @throws IOException 异常
     */
    private void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 群发自定义消息
     *
     * @param socketMsg 自定义Websocket消息
     * @param sid       会话ID
     * @throws IOException 异常
     */
    public static void sendInfo(SocketMsg socketMsg, @PathParam("sid") String sid) throws IOException {
        String message = JSONObject.toJSONString(socketMsg);
        if (log.isInfoEnabled()) {
            log.info("Push message to " + sid + ", push content: " + message);
        }
        for (WebSocketServer item : webSocketSet) {
            try {
                // 这里可以设定只推送给这个sid的，为null则全部推送
                if (null == sid) {
                    item.sendMessage(message);
                } else if (item.sid.equals(sid)) {
                    item.sendMessage(message);
                }
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        WebSocketServer that = (WebSocketServer) o;
        return Objects.equals(session, that.session) && Objects.equals(sid, that.sid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(session, sid);
    }

}
