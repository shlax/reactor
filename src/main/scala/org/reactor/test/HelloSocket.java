package org.reactor.test;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/time")
public class HelloSocket {

    @OnOpen
    public void onOpen(Session session){
        System.out.println(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println(session+":"+message);
    }

    @OnClose
    public void onClose(Session session){
        System.out.println(session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println(session);
        throwable.printStackTrace();
    }

}
