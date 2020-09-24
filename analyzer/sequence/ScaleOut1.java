package analyzer.sequence;

public class ScaleOut1 extends SequenceElement {
    
    private String nodeName;
    private String idToAssign;
    
    public ScaleOut1(String rule, String nodeName, String idToAssign){
        this.rule = rule;
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

        if(rule.equals("scaleOut1") == false)
            ret = false;
        
        if(this.nodeName == null || this.nodeName.isEmpty() == true)
            ret = false;
        
        if(this.idToAssign == null || this.idToAssign.isEmpty() == true)
            ret = false;
        
        return ret;
    }

}
