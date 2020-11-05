package mprot.cli.parsing.wrappers;

import java.util.List;

import mprot.core.model.PiVersion;

public class AppWrapper {

    private final String name;
    private final PiVersion piVersion;
    private final List<NodeWrapper> nodes;
    private final List<StaticBindingWrapper> bindings;

    public AppWrapper(List<NodeWrapper> nodes, List<StaticBindingWrapper> bindings, String name, PiVersion piVersion) {
        this.piVersion = piVersion;
        this.name = name;
        this.nodes = nodes;
        this.bindings = bindings;
    }

    public PiVersion getPiVersion() {
        return piVersion;
    }

    public String getName() {
        return name;
    }

    public List<StaticBindingWrapper> getBindings() {
        return bindings;
    }

    public List<NodeWrapper> getNodes() {
        return nodes;
    }



}
