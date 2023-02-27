package org.reactor.test;

import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;

@ServerEndpoint("/time")
public class HelloSocket {

    @Inject
    private AppHelloBean appHelloBean;

    @OnOpen
    public void onOpen(Session session){
        System.out.println(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException {
        System.out.println(session+":"+message);
        session.getBasicRemote().sendText(appHelloBean.hello());
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
