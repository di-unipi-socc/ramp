package unipi.di.socc.ramp.cli.parser.wrappers;


public class ScaleOutWrapper extends ActionWrapper{
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
