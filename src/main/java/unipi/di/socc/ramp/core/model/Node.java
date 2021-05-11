package unipi.di.socc.ramp.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Node {
    
    private final String name;
    private final ManagementProtocol manProtocol;
    private final List<Requirement> reqs;
    private final List<String> caps;
    private final List<String> ops;

    public Node(
        String name, 
        ManagementProtocol manProtocol, 
        List<Requirement> reqs, 
        List<String> caps, 
        List<String> ops
    )
        throws
            NullPointerException,
            IllegalArgumentException
    {
        if(name == null || manProtocol == null || reqs == null || caps == null || ops == null)
                throw new NullPointerException();
        
        if(name.isBlank())
            throw new IllegalArgumentException();

        this.name = name;
        this.manProtocol = manProtocol;
        this.reqs = reqs;
        this.caps = caps;
        this.ops = ops;
    }

    public Node(String name, ManagementProtocol manProtocol)
        throws
            NullPointerException,
            IllegalArgumentException
    {
        if(name == null || manProtocol == null)
            throw new NullPointerException();

        if(name.isBlank())
            throw new IllegalArgumentException();

        this.name = name;
        this.manProtocol = manProtocol;

        this.caps = new ArrayList<String>();
        this.ops = new ArrayList<String>();
        this.reqs = new ArrayList<Requirement>();
    }

    public void addOperation(String op){
        if(op == null)
            throw new NullPointerException();
        if(op.isBlank())
            throw new IllegalArgumentException();

        this.ops.add(op);
    }

    public void addRequirement(Requirement req){
        if(req == null)
            throw new NullPointerException();
        
        this.reqs.add(req);
    }

    public void addCapability(String cap){
        if(cap == null)
            throw new NullPointerException();
        if(cap.isBlank())
            throw new IllegalArgumentException();
        
        this.caps.add(cap);
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null)
            throw new NullPointerException();

        Node n = (Node) obj;
        return
            this.caps.equals(n.getCaps()) && 
            this.ops.equals(n.getOps()) && 
            this.manProtocol.equals(n.getManProtocol()) &&
            this.reqs.equals(n.getReqs()) && 
            this.name.equals(n.getName());
    }

    @Override
    public int hashCode(){
        return Objects.hash(
            this.name, 
            this.caps, 
            this.ops, 
            this.reqs, 
            this.manProtocol);
    }
    
    public String getName() {
        return name;
    }
    public List<String> getOps() {
        return ops;
    }
    public List<String> getCaps() {
        return caps;
    }
    public List<Requirement> getReqs() {
        return reqs;
    }
    public ManagementProtocol getManProtocol() {
        return manProtocol;
    }

}
