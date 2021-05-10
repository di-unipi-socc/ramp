package unipi.di.socc.ramp.core.model.exceptions;

public class AlreadyUsedIDException extends Exception{
    public AlreadyUsedIDException() {
        super();
    }
    public AlreadyUsedIDException(String s){
        super(s);
    }
}

