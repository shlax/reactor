package org.reactor.test;

import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDateTime;

@Named
@ViewScoped
public class WebsocketHelloBean implements Serializable {

    @Inject @Push
    private PushContext helloChannel;

    public void sendMessage(){
        helloChannel.send(""+ LocalDateTime.now());
    }

}
