package org.reactor.crd;

import java.io.Serializable;
import lombok.Getter;

public class MenuItem implements Serializable{
    
    @Getter
    private final String id;
    
    @Getter
    private final String label;

    public MenuItem(String id, String label) {
        this.id = id;
        this.label = label;
    }
       
    
}
