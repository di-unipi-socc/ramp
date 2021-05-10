package unipi.di.socc.ramp.core.model;

import java.util.Objects;

/**
 * models a pair <static node n, requirement of n>
    * this pair is used by the application static binding function as a key
    * to retrieve the <node, cap> to handle the requirement in the static topology 
 */

public class NodeReq {
    private final String nodeName;
    private final String reqName;

    /**
     * @param nodeName name of the node
     * @param reqName requirement's name about the binding
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public NodeReq(String nodeName, String reqName) 
        throws
            NullPointerException,
            IllegalArgumentException
    {
        if(nodeName == null || reqName == null)
            throw new NullPointerException();

        if(nodeName.isBlank() || reqName.isBlank())
            throw new IllegalArgumentException();

        this.nodeName = nodeName;
        this.reqName = reqName;
    }

    public String getNodeName() {
        return this.nodeName;
    }
    public String getReqName() {
        return this.reqName;
    }

    @Override
    public int hashCode(){
        return Objects.hash(nodeName, reqName);
    }
    @Override
    public boolean equals(Object obj){
        if(obj == null)
            throw new NullPointerException();
            
        NodeReq nr = (NodeReq) obj;
        return this.nodeName.equals(nr.getNodeName()) && this.reqName.equals(nr.getReqName());
    }

}
