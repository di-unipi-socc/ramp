package exceptions;

public class NodeUnknownException extends Exception {
    private static final long serialVersionUID = 1L;

    public NodeUnknownException() {
        super();
    }

    public NodeUnknownException(String s){
        super(s);
    }

}
