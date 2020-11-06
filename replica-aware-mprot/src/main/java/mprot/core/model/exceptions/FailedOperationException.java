package mprot.core.model.exceptions;

//when we invoke opEnd, if the application cant be completed it's raised a FailedOperationException
public class FailedOperationException extends Exception {
    //default 
    private static final long serialVersionUID = 1L;
    
    public FailedOperationException(){
        super();
    }

    public FailedOperationException(String msg){
        super(msg);
    }

}