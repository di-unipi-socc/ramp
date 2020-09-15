package analyzer;

import model.Fault;
import model.NodeInstance;

//wrapper for fault and broken instance
public class Error {
    
    private Fault fault;
    private NodeInstance brokenInstance;
    private String type;

    public Error(NodeInstance brokenInstance){
        this.fault = null;
        this.type = new String("brokenInstance");
        this.brokenInstance = brokenInstance;
    }

    public Error(Fault fault){
        this.type = new String("fault");
        this.fault = fault;
        this.brokenInstance = null;
    }

    public String getErrorType(){
        return this.type;
    }

    public Fault getFault(){
        return this.fault;
    }

    public NodeInstance getBrokeInstance(){
        return this.brokenInstance;
    }

}
