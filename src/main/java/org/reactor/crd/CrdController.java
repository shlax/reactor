package org.reactor.crd;

import io.fabric8.kubernetes.client.dsl.base.ResourceDefinitionContext;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.reactor.kubernetes.KubernetesContext;
import java.util.Objects;

@Named
@SessionScoped
public class CrdController implements Serializable{

    @Inject    
    private KubernetesContext context;
    
    private final Object definitionsLock = new Object();
    private List<MenuItem> definitions = null;
    
    public List<MenuItem> getCrdDefinitions(){
        //System.out.println("getCrdDefinitions()");
        synchronized (definitionsLock) {
            if(definitions != null) return definitions;

            var crdList = context.getClient().apiextensions().v1().customResourceDefinitions().list().getItems();

            definitions = new ArrayList<>();
            for(var crd : crdList){
                definitions.add(new MenuItem( crd.getMetadata().getName(), crd.getSpec().getNames().getKind()));
            }

            return definitions;
        }
    }
    
    private final Object objectsLock = new Object();
    private List<MenuItem> objects = null;
    private String crdId;
    
    public void list(String crdId){
        //System.out.println("setCrdId("+crdId+")");
        synchronized (objectsLock) {
            if(!Objects.equals( this.crdId, crdId)){
                this.crdId = crdId;
                objects = null;                                
            }
        }
    }    
    
    // https://developers.redhat.com/articles/2023/01/05/how-use-fabric8-kubernetes-client#basic_create__read__update__and_delete_operations
    public List<MenuItem> getCrdObjects(){
        //System.out.println("getCrdObjects("+crdId+")");
         
        synchronized (objectsLock) {
            if(this.crdId == null) return Collections.emptyList();
        
            if(objects != null) return objects;

            var crd = context.getClient().apiextensions().v1().customResourceDefinitions().withName(this.crdId).get();

            var kind = crd.getSpec().getNames().getKind();
            var group = crd.getSpec().getGroup();

            objects = new ArrayList<>();
            for(var v : crd.getSpec().getVersions()){
                var ver = v.getName();

                var ctx = new ResourceDefinitionContext.Builder()
                        .withKind(kind)
                        .withGroup(group)                    
                        .withVersion(ver)
                        .build();

                var objs = context.getClient().genericKubernetesResources(ctx).list().getItems();

                for(var obj : objs){
                    var nm = obj.getMetadata().getName();
                    objects.add(new MenuItem(nm, nm));
                }

            }

            return objects;
        }
    }
        
    private volatile String objId;
    
    public void open(String objId){
        //System.out.println("setObjId("+objId+")");
        this.objId = objId;        
        
    } 
    
    public String getCrdName(){
        return crdId+" "+objId;
    }
    
}
