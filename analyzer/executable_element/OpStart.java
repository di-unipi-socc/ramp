package analyzer.executable_element;

public class OpStart extends ExecutableElement{
    
    private final String instanceID;
    private final String op;

    public OpStart(String instanceID, String op){

        if(instanceID == null)
            throw new NullPointerException("instanceID null");
        if(instanceID.isEmpty() == true)
            throw new IllegalArgumentException("instanceID empty");
        if(op == null)
            throw new NullPointerException("op null");
        if(op.isEmpty() == true)
            throw new IllegalArgumentException("op empty");


        this.rule = "opStart";
        this.instanceID = instanceID;
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
