package unipi.di.socc.ramp.cli.parser.wrappers;

import unipi.di.socc.ramp.core.analyzer.actions.Action;

public class ScaleOutWrapper extends Action{
    private final String idToAssign;
    private final String nodeName;
    private final String containerID;

    public ScaleOutWrapper(String idToAssign, String nodeName, String containerID) {
        this.setAction();
        this.idToAssign = idToAssign;
        this.nodeName = nodeName;
        this.containerID = containerID;
    }

    public void setAction(){
        this.action = "scaleOut";
    }
    public String getContainerID() {
        return containerID;
    }
    public String getIDToAssign() {
        return idToAssign;
    }
    public String getNodeName() {
        return nodeName;
    }
    
}
