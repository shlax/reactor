package org.reactor.test;

import jakarta.inject.Named;

import java.time.LocalDateTime;

@Named
public class AppHelloBean {

    private final LocalDateTime time;

    public AppHelloBean(){
        time = LocalDateTime.now();
        System.out.println("created:AppHelloBean "+time);
    }

    public String hello(){
        return "hello-app["+time+"]";
    }

}
