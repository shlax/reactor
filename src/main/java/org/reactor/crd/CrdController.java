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
import org.yaml.snakeyaml.Yaml;

import java.util.Objects;

@Named
@SessionScoped
public class CrdController implements Serializable{

    @Inject    
    private KubernetesContext context;
    
    private final Object definitionsLock = new Object();
    private List<MenuItem> definitions = null;
    
    public List<MenuItem> getCrdDefinitions(){
        // if(true) return Collections.singletonList(new MenuItem("a", "a"));

        //System.out.println("getCrdDefinitions()");
        synchronized (definitionsLock) {
            if(definitions != null) return definitions;

            var ext = context.getClient().apiextensions();
            var crdList = ext.v1().customResourceDefinitions().list().getItems();

            definitions = new ArrayList<>();
            for (var crd : crdList) {
                definitions.add(new MenuItem(crd.getMetadata().getName(), crd.getSpec().getNames().getKind()));
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
        // if(true) return Collections.singletonList(new MenuItem("a", "a"));
        //System.out.println("getCrdObjects("+crdId+")");
         
        synchronized (objectsLock) {
            if(this.crdId == null) return Collections.emptyList();
        
            if(objects != null) return objects;

            var ext = context.getClient().apiextensions();
            var crd = ext.v1().customResourceDefinitions().withName(this.crdId).get();

            var kind = crd.getSpec().getNames().getKind();
            var group = crd.getSpec().getGroup();

            objects = new ArrayList<>();
            for (var v : crd.getSpec().getVersions()) {
                var ver = v.getName();

                var ctx = new ResourceDefinitionContext.Builder()
                        .withNamespaced(true)
                        .withKind(kind)
                        .withGroup(group)
                        .withVersion(ver)
                        .build();

                var objs = context.getClient().genericKubernetesResources(ctx).list().getItems();

                for (var obj : objs) {
                    var nm = obj.getMetadata().getName();
                    objects.add(new MenuItem(nm+"/"+ver, nm));
                }
            }

            return objects;
        }
    }

    private final Object specLock = new Object();
    private String spec;
    private String objId;
    
    public void open(String objId){
        //System.out.println("setObjId("+objId+")");
        synchronized (specLock) {
            if(!Objects.equals(this.objId, objId)) {
                this.objId = objId;
                spec = null;
            }
        }
        
    } 

    // https://developers.redhat.com/articles/2023/01/05/how-use-fabric8-kubernetes-client#
    public String getSpec(){
        //if(true) return "'"+getTitle()+"'";

        synchronized (objectsLock){
            if(crdId == null) return "''";

            synchronized (specLock){
                if(objId == null) return "''";
                if(spec != null) return spec;

                var ext = context.getClient().apiextensions();
                var crd = ext.v1().customResourceDefinitions().withName(this.crdId).get();

                var kind = crd.getSpec().getNames().getKind();
                var group = crd.getSpec().getGroup();

                var nmVer = objId.split("/");

                var ctx = new ResourceDefinitionContext.Builder()
                        .withNamespaced(true)
                        .withKind(kind)
                        .withGroup(group)
                        .withVersion(nmVer[1])
                        .build();

                var i = context.getClient().genericKubernetesResources(ctx)
                        .withName(nmVer[0])
                        .get();

                var s = i.getAdditionalProperties().get("spec");
                var str = new Yaml().dump(s);

                var first = true;
                var sb = new StringBuilder("[");
                for(var l : str.split("\n")){
                    if(first) first = false; else sb.append(",");
                    sb.append("'");
                    for(char c : l.toCharArray()){
                        switch (c) {
                            case '"', '\'', '\\' -> sb.append("\\").append(c);
                            case '\t' -> sb.append("\\t");
                            case '\r' -> sb.append("\\r");
                            case '\b' -> sb.append("\\b");
                            case '\f' -> sb.append("\\f");
                            default -> sb.append(c);
                        }
                    }
                    sb.append("'");
                }
                sb.append("].join('\\n')");
                spec = sb.toString();
                return spec;
            }
        }
    }

    public String getTitle(){
        synchronized (objectsLock){
            if(crdId == null) return "";
            synchronized (specLock){
                if(objId == null) return crdId;
                return crdId+":"+objId;
            }
        }
    }
    
}
