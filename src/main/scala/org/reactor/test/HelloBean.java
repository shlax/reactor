package org.reactor.test;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class HelloBean {

    public HelloBean(){
        System.out.println("created:HelloBean");
    }
    
    public String hello(){
        return "hello";
    }

}
