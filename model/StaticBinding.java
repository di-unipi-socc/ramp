package model;
//represent a static binding, both ways
//such as <node, requirement> and <node, capability>
public class StaticBinding {
    private final String node;
    private final String capOrReq;

    public StaticBinding(String node, String capOrReq) {
        this.capOrReq = capOrReq;
        this.node = node;
    }

    public String getCapOrReq() {
        return capOrReq;
    }

    public String getNodeName() {
        return node;
    }
}