package unipi.di.socc.ramp.cli.parser.wrappers;

import unipi.di.socc.ramp.core.analyzer.actions.Action;

public class OperationWrapper extends Action{
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

    @Override
    public void setAction() {
        this.action = "op";
    }


}
