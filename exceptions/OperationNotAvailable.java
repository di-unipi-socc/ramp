package exceptions;

public class OperationNotAvailable extends Exception {
    //default 
    private static final long serialVersionUID = 1L;
    
    public OperationNotAvailable(){
        super();
    }

    public OperationNotAvailable(String msg){
        super(msg);
    }

}