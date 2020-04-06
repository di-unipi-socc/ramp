package application;
//represent a static binding, both ways
//the name is temporary
public class Tmp {
    private final String node;
    private final String need;

    public Tmp(String node, String need) {
        this.need = need;
        this.node = node;
    }

    public String getNeed() {
        return need;
    }

    public String getNodeName() {
        return node;
    }
}