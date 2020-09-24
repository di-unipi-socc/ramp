package analyzer.executable_element;

public class ScaleIn extends ExecutableElement {
    
    private final String instanceID;

    public ScaleIn(String id){
        this.rule = "scaleIn";
        this.instanceID = id;
    }   

    public String getInstanceID(){
        return this.instanceID;
    }
    
    public boolean wellFormattedSequenceElement(){
        boolean ret = true;

        if(this.instanceID == null || this.instanceID.isEmpty() == true)
            ret = false;

        return ret;
    }

}
