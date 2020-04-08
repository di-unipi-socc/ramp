package model;

//this is a couple (tuple). we do not use Map.Entry for readibility.
//this is a fault such as <instanceID, failed requirement>
public class Fault {
    private final String instanceID;
    private final Requirement req;

    public Fault(String instanceID, Requirement req) {
        assert instanceID != null;
        assert req != null;

        this.instanceID = instanceID;
        this.req = req;
    }

    public String getInstanceID() {
        return this.instanceID;
    }

    public Requirement getReq() {
        return this.req;
    }

}