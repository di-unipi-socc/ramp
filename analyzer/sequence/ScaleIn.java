package analyzer.sequence;

public class ScaleIn extends SequenceElement {
    
    private final String instanceID;

    public ScaleIn(String rule, String id){
        this.rule = rule;
        this.instanceID = id;
    }   

    public String getInstanceID(){
        return this.instanceID;
    }
    
    public boolean wellFormattedSequenceElement(){
        boolean ret = true;

        if(rule.equals("scaleIn") == false)
            ret = false;
        
        if(this.instanceID == null || this.instanceID.isEmpty() == true)
            ret = false;

        return ret;
    }

}
