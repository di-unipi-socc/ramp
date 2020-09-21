package exceptions;

public class AlreadyUsedIDException extends Exception {

    private static final long serialVersionUID = 1L;

    public AlreadyUsedIDException(){
        super();
    }

    public AlreadyUsedIDException(String s){
        super(s);
    }
    
}
