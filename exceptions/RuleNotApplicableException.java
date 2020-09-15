package exceptions;

public class RuleNotApplicableException extends Exception {
    
    private static final long serialVersionUID = 1L;

    public RuleNotApplicableException() {
        super();
    }

    public RuleNotApplicableException(String msg){
        super(msg);
    }
}
