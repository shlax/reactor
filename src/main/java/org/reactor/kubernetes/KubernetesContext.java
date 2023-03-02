package org.reactor.kubernetes;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;

@ApplicationScoped
public class KubernetesContext {
    
    @Getter
    private KubernetesClient client;
    
    @PostConstruct
    public void init(){
        client = new KubernetesClientBuilder().build();
    }
    
    
}
