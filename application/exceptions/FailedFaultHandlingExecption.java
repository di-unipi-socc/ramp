package application.exceptions;

public class FailedFaultHandlingExecption extends Exception {
    private static final long serialVersionUID = 1L;

    public FailedFaultHandlingExecption() {
        super();
    }

    public FailedFaultHandlingExecption(String s){
        super(s);
    }

}
