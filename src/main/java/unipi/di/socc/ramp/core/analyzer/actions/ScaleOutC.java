package unipi.di.socc.ramp.core.analyzer.actions;

import java.util.Objects;

public class ScaleOutC extends Action{

    private final String idToAssign;
    private final String nodeName;
    private final String containerID;

    public ScaleOutC(String idToAssign, String nodeName, String containerID) {
        this.setAction();
        this.idToAssign = idToAssign;
        this.nodeName = nodeName;
        this.containerID = containerID;
    }

    public void setAction(){
        this.action = "scaleOutC";
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
    
    @Override
    public boolean equals(Object obj){
        if(obj instanceof ScaleOutC){
            ScaleOutC check = (ScaleOutC) obj;
            return 
                check.getIDToAssign().equals(this.idToAssign) && 
                check.getNodeName().equals(this.nodeName) && 
                check.getContainerID().equals(this.containerID);
        }
        return false;
    }

    @Override 
    public int hashCode(){
        return Objects.hash(
            this.action, 
            this.idToAssign, 
            this.nodeName,
            this.containerID
        );
    }

    @Override
    public ScaleOutC clone(){
        return new ScaleOutC(this.idToAssign, this.nodeName, this.containerID);
    }
}
