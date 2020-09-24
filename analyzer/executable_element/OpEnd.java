package analyzer.executable_element;

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
}
