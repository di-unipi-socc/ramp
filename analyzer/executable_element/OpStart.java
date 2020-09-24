package analyzer.executable_element;

public class OpStart extends ExecutableElement{
    
    private final String instanceID;
    private final String op;

    public OpStart(String id, String op){
        this.rule = "opStart";
        this.instanceID = id;
        this.op = op;
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

    public String getInstnaceID() {
        return this.instanceID;
    }


}
