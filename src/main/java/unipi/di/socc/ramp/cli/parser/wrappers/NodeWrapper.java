package unipi.di.socc.ramp.cli.parser.wrappers;

import java.util.List;
import java.util.Map;
import unipi.di.socc.ramp.core.model.Requirement;

public class NodeWrapper {
    
    private final String name;
    //requirements are not wrapped and inserted
    //in the json as  object themselves
    private final Map<String, Requirement> requirements;
    private final List<String> capabilities;
    private final List<String> operations;
    private final ManProtocolWrapper managementProtocol;

    public NodeWrapper(
        String name,
        Map<String, Requirement> requirements, 
        List<String> capabilities, 
        List<String> operations, 
        ManProtocolWrapper managementProtocol
    ){
        this.name = name;
        this.requirements = requirements;
        this.operations = operations;
        this.capabilities = capabilities;
        this.managementProtocol = managementProtocol;
    }

    //TODO: rinomina getMP in getWrappedMP

    public List<String> getCapabilities() {
        return capabilities;
    }
    public ManProtocolWrapper getManagementProtocol() {
        return managementProtocol;
    }
    public List<String> getOperations() {
        return operations;
    }
    public Map<String, Requirement> getRequirements() {
        return requirements;
    }
    public String getName() {
        return name;
    }
}
