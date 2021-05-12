package unipi.di.socc.ramp.cli.parser.wrappers;

public class OperationWrapper extends ActionWrapper{
    private final String opName;
    private final String instanceID;

    public OperationWrapper(String instanceID, String opName) {
        this.setAction();
        this.instanceID = instanceID;
        this.opName = opName;
    }

    public String getOpName() {
        return opName;
    }
    public String getInstanceID() {
        return instanceID;
    }

    public void setAction() {
        this.action = "op";
    }


}
