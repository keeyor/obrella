/* 
     Author: Michael Gatzonis - 3/20/2019 
     OpenDelosDAC
*/
package org.opendelos.model.resources;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceTags implements Serializable {

    protected String ResApp;
    protected String ResPub;
    protected String ResFin;
    protected String MetEdt;
    protected String MultUp;
    protected String MultEdt;
    protected String MultRed;
    protected String PreSyn;
    protected String PreUp;
    protected String Sub;

    public ResourceTags() {
    }

    public ResourceTags(String value) {
        this.setResApp(value);
        this.setMetEdt(value);
        this.setMultUp(value);
        this.setMultEdt(value);
        this.setMultRed(value);
        this.setPreSyn(value);
        this.setPreUp(value);
        this.setResApp(value);
        this.setResFin(value);
        this.setResPub(value);
        this.setSub(value);
    }
}