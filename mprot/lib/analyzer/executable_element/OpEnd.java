package mprot.lib.analyzer.executable_element;

public class OpEnd extends ExecutableElement {
    private final String instanceID;
    private final String op;

    public OpEnd(String id, String op){
        this.rule = "opEnd";
        this.instanceID = id;
        this.op = op;
    }

    public String getInstanceID(){
        return this.instanceID;
    }

    public String getOp(){
        return this.op;
    }

    public boolean wellFormattedSequenceElement(){
        boolean ret = true;

        if(this.instanceID == null || this.instanceID.isEmpty() == true)
            ret = false;
        
        if(this.op == null || this.op.isEmpty() == true)
            ret = false;

        return ret;
    }

    @Override
    /**
     */
    public boolean equals(Object f){

        if(f instanceof OpEnd == false)
            return false;

        OpEnd toCheck = (OpEnd) f;
        boolean ret = false;

        if(toCheck.instanceID.equals(this.instanceID) && toCheck.getOp().equals(this.op) && toCheck.getRule().equals(this.rule))
            ret = true;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.instanceID.hashCode();
        result = 31 * result + this.rule.hashCode();
        result = 31 * result + this.op.hashCode();

        return result;
    }

    @Override
    public void setRule() {
        this.rule = "opEnd";
    }
}
