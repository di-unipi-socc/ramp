package unipi.di.socc.ramp.core.model;

import java.util.Objects;

/**
 * models a pair <static node n, capability of n>
    * this pair is used by the application static binding function as a value
    * when to the static binding function is passed a <node, req>, if there is the 
    * link in the static topology of the application, an object NodeCap is returned, which is
    * <node that offer the right capability, capability> 
 */

public class NodeCap {
    private final String nodeName;
    private final String capName;

    /**
     * @param nodeName name of the node
     * @param capName capability's name about the binding
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public NodeCap(String nodeName, String capName) 
        throws
            NullPointerException,
            IllegalArgumentException
    {
        if(nodeName == null || capName == null)
            throw new NullPointerException();

        if(nodeName.isBlank() || capName.isBlank())
            throw new IllegalArgumentException();

        this.nodeName = nodeName;
        this.capName = capName;
    }

    public String getNodeName() {
        return this.nodeName;
    }
    public String getCap() {
        return this.capName;
    }

    @Override
    public int hashCode(){
        return Objects.hash(nodeName, capName);
    }
    @Override
    public boolean equals(Object obj){
        if(obj == null)
            throw new NullPointerException();
            
        NodeCap nc = (NodeCap) obj;
        return this.nodeName.equals(nc.getNodeName()) && this.capName.equals(nc.getCap());
    }

}
