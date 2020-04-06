package application;

//this is a couple (tuple). we do not use Map.Entry for readibility.
//this is a fault such as <node istance unique id, failed requirement>
public class Fault {
    private final String ist;
    private final Requirement req;

    public Fault(String ist, Requirement req) {
        assert ist != null;
        assert req != null;

        this.ist = ist;
        this.req = req;
    }

    public String getIst() {
        return this.ist;
    }

    public Requirement getReq() {
        return this.req;
    }

}