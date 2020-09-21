package exceptions;

public class InstanceUnknownException extends Exception {
    private static final long serialVersionUID = 1L;

    public InstanceUnknownException() {
        super();
    }

    public InstanceUnknownException(String s){
        super(s);
    }
}
