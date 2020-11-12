package mprot.core.analyzer.executable_element;

public class ScaleIn extends ExecutableElement {
    
    private final String instanceID;

    public ScaleIn(String instanceID){

        if(instanceID == null)
            throw new NullPointerException("instanceID null");
        if(instanceID.isEmpty() == true)
            throw new IllegalArgumentException("instanceID empty");

        this.rule = "scaleIn";
        this.instanceID = instanceID;
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

    @Override
    /**
     */
    public boolean equals(Object f){

        if(f instanceof ScaleIn == false)
            return false;

        ScaleIn toCheck = (ScaleIn) f;
        boolean ret = false;

        if(toCheck.instanceID.equals(this.instanceID) && toCheck.getRule().equals(this.rule))
            ret = true;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.instanceID.hashCode();
        result = 31 * result + this.rule.hashCode();

        return result;
    }

    @Override
    public void setRule() {
        this.rule = "scaleIn";
    }

}
