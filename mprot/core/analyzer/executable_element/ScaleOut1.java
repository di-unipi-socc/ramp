package mprot.core.analyzer.executable_element;

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

    @Override
    /**
     */
    public boolean equals(Object f){

        if(f instanceof ScaleOut1 == false)
            return false;

        ScaleOut1 toCheck = (ScaleOut1) f;
        boolean ret = false;

        if(toCheck.getIDToAssign().equals(this.idToAssign) && toCheck.getNodeName().equals(this.nodeName) && toCheck.getRule().equals(this.rule))
            ret = true;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.idToAssign.hashCode();
        result = 31 * result + this.rule.hashCode();
        result = 31 * result + this.nodeName.hashCode();

        return result;
    }

    @Override
    public void setRule() {
        this.rule = "scaleOut1";
    }

}
