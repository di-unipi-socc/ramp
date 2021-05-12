package unipi.di.socc.ramp.cli.parser.wrappers;


public class ScaleInWrapper extends ActionWrapper{
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
