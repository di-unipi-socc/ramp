package unipi.di.socc.ramp.cli.parser.wrappers;

public class StaticBindingWrapper {
    private final String sourceNode;
    private final String sourceRequirement;
    private final String targetNode;
    private final String targetCapability;

    public StaticBindingWrapper(
        String sourceNode, 
        String sourceRequirement, 
        String targetNode,
        String targetCapability
    ){
        this.sourceNode = sourceNode;
        this.sourceRequirement = sourceRequirement;
        this.targetNode = targetNode;
        this.targetCapability = targetCapability;
    }

    public String getSourceNode() {
        return sourceNode;
    }
    public String getTargetCapability() {
        return targetCapability;
    }
    public String getTargetNode() {
        return targetNode;
    }
    public String getSourceRequirement() {
        return sourceRequirement;
    }
}
