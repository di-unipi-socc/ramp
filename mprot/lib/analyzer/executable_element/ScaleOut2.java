package mprot.lib.analyzer.executable_element;

public class ScaleOut2 extends ExecutableElement {
    
    private String nodeName;
    private String idToAssign;
    private String containerID;
    
    public ScaleOut2(String nodeName, String idToAssign, String containerID){
        this.rule = "scaleOut2";
        this.nodeName = nodeName;
        this.idToAssign = idToAssign;
        this.containerID = containerID;
    }

    public String getNodeName(){
        return this.nodeName;
    }

    public String getIDToAssign(){
        return this.idToAssign;
    }

    public String getContainerID(){
        return this.containerID;
    }

    public boolean wellFormattedSequenceElement(){
        boolean ret = true;

        if(this.nodeName == null || this.nodeName.isEmpty() == true)
            ret = false;
        
        if(this.idToAssign == null || this.idToAssign.isEmpty() == true)
            ret = false;

        if(this.containerID == null || this.containerID.isEmpty() == true)
            ret = false;
            
        return ret;
    }

    @Override
    public boolean equals(Object f){

        if(f instanceof ScaleOut2 == false)
            return false;

        ScaleOut2 toCheck = (ScaleOut2) f;
        boolean ret = false;

        if(toCheck.getIDToAssign().equals(this.idToAssign) && toCheck.getContainerID().equals(this.containerID) && toCheck.getNodeName().equals(this.nodeName) && toCheck.getRule().equals(this.rule))
            ret = true;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.idToAssign.hashCode();
        result = 31 * result + this.rule.hashCode();
        result = 31 * result + this.nodeName.hashCode();
        result = 31 * result + this.containerID.hashCode();


        return result;
    }

}
