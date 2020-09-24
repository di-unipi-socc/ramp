package analyzer.executable_element;

public class ScaleOut1 extends ExecutableElement {
    
    private String nodeName;
    private String idToAssign;
    
    

    public ScaleOut1(String nodeName, String idToAssign){
        this.rule = "scaleOut1";
        this.nodeName = nodeName;
        this.idToAssign = idToAssign;
    }

    public String getNodeName(){
        return this.nodeName;
    }

    public String getIDToAssign(){
        return this.idToAssign;
    }

    public boolean wellFormattedSequenceElement(){
        boolean ret = true;

        if(this.nodeName == null || this.nodeName.isEmpty() == true)
            ret = false;
        
        if(this.idToAssign == null || this.idToAssign.isEmpty() == true)
            ret = false;
        
        return ret;
    }

}
