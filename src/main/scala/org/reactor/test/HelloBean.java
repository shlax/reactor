package org.reactor.test;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import java.time.LocalDateTime;

@Named
@RequestScoped
public class HelloBean {

    LocalDateTime time;
    public HelloBean(){
        time = LocalDateTime.now();
        System.out.println("created:HelloBean "+time);
    }
    
    public String hello(){
        return "hello["+time+"]";
    }

}
