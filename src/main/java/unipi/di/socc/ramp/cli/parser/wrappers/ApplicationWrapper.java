package unipi.di.socc.ramp.cli.parser.wrappers;

import java.util.List;
import unipi.di.socc.ramp.core.model.PiVersion;

public class ApplicationWrapper {
    private final String name;
    private final PiVersion piVersion;
    private final List<NodeWrapper> nodes;
    private final List<StaticBindingWrapper> bindings;

    public ApplicationWrapper(
        List<NodeWrapper> nodes, 
        List<StaticBindingWrapper> bindings, 
        String name, 
        PiVersion piVersion
    ){
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
