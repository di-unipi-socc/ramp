package unipi.di.socc.ramp.core.analyzer.actions;

import java.util.Objects;

public class ScaleOut extends Action{

    private final String idToAssign;
    private final String nodeName;

    public ScaleOut(String idToAssign, String nodeName) {
        this.setAction();
        this.idToAssign = idToAssign;
        this.nodeName = nodeName;
    }

    public void setAction(){
        this.action = "scaleOut";
    }

    public String getIDToAssign() {
        return idToAssign;
    }

    public String getNodeName() {
        return nodeName;
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj instanceof ScaleOut){
            ScaleOut check = (ScaleOut) obj;
            return 
                check.getIDToAssign().equals(this.idToAssign) && 
                check.getNodeName().equals(this.nodeName);
        }
        return false;
    }

    @Override 
    public int hashCode(){
        return Objects.hash(this.action, this.idToAssign, this.nodeName);
    }

    @Override
    public ScaleOut clone(){
        return new ScaleOut(this.idToAssign, this.nodeName);
    }
}
