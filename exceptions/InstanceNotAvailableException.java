package exceptions;

//when we start an operation we have to manage the new binding given by 
//the new transient state. If a requirement of the instance in the new state
//can't be mangaged by some instance we have InstanceNotAvailableException
public class InstanceNotAvailableException extends Exception {
    //default 
    private static final long serialVersionUID = 1L;
    
    public InstanceNotAvailableException(){
        super();
    }

    public InstanceNotAvailableException(String msg){
        super(msg);
    }

}