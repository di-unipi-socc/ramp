package mprot.lib.model.exceptions;

//if it is called an operation on a node that have not that operation
//we have OperationNotAvailableException
public class OperationNotAvailableException extends Exception {
    //default 
    private static final long serialVersionUID = 1L;
    
    public OperationNotAvailableException(){
        super();
    }

    public OperationNotAvailableException(String msg){
        super(msg);
    }

}