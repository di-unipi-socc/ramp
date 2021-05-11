package unipi.di.socc.ramp.cli.parser.wrappers;

import unipi.di.socc.ramp.core.analyzer.actions.Action;

public class ScaleInWrapper extends Action{
    private final String instanceID;

    public ScaleInWrapper(String instanceID){
        this.setAction();
        this.instanceID = instanceID;
    }

    public void setAction(){
        this.action = "scaleIn";
    }

    public String getInstanceID() {
        return instanceID;
    }
}
