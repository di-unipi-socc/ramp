package unipi.di.socc.ramp.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unipi.di.socc.ramp.core.model.exceptions.*;

public class Application{

    private String name;
    
    //static nodes of the application: node's name -> node
    private Map<String, Node> nodes;
    private GlobalState globalState;

    private Map<NodeReq, NodeCap> bindingFunction; 
    private PiVersion piVersion;
    private boolean deterministicPi;

    /**
     * @param name application's name
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public Application(String name, PiVersion piVersion) 
        throws 
            NullPointerException, 
            IllegalArgumentException 
    {
        this.setName(name);
        this.setNodes(new HashMap<>());
        this.globalState = new GlobalState(this);
        this.setBindingFunction(new HashMap<>());
        this.setPiVersion(piVersion);
        this.setDeterministicPi();
    }

    /**
     * @param name name of the application
     * @param piVersion version of the implementtion of pi
     * @param bf binding function of the static topology
     * @param nodes compontents of the application
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    public Application(String name, PiVersion piVersion, Map<NodeReq, NodeCap> bf, Map<String, Node> nodes)
        throws 
            IllegalArgumentException, 
            NullPointerException
    {
        this.setName(name);
        this.setNodes(nodes);
        this.globalState = new GlobalState(this);
        this.setBindingFunction(bf);
        this.setPiVersion(piVersion);
        this.setDeterministicPi();
    }

    public boolean isPiDeterministic() {
        return deterministicPi;
    }
    public void setDeterministicPi() {
        switch (this.piVersion) {
            case GREEDYPI:
                this.deterministicPi = true;
                break;
            case RANDOMPI:
                this.deterministicPi = false;
            default:
                break;
        }
    }
    public PiVersion getPiVersion() {
        return piVersion;
    }
    public void setPiVersion(PiVersion piVersion) {
        if(piVersion == null)
            throw new NullPointerException();
        this.piVersion = piVersion;
    }
    public Map<NodeReq, NodeCap> getBindingFunction() {
        return bindingFunction;
    }
    public void setBindingFunction(Map<NodeReq, NodeCap> bindingFunction) {
        if(bindingFunction == null)
            throw new NullPointerException();
        this.bindingFunction = bindingFunction;
    }
    public GlobalState getGlobalState() {
        return globalState;
    }
    public void setGlobalState(GlobalState globalState) {
        if(globalState == null)
            throw new NullPointerException();
        this.globalState = globalState;
    }
    public Map<String, Node> getNodes() {
        return nodes;
    }
    public void setNodes(Map<String, Node> nodes) {
        if(nodes == null)
            throw new NullPointerException();
        this.nodes = nodes;
    }
    public String getName() {
        return name;
    }
    public void setName(String name)
        throws
            NullPointerException, 
            IllegalArgumentException
    {
        if(name == null)
            throw new NullPointerException();
        if(name.isBlank())
            throw new IllegalArgumentException();
        
        this.name = name;
    }
    private int randomIndex(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    //############################ FILLING METHODS ###############################

    public void addNode(Node node)
        throws 
            NullPointerException
    {
        if(node == null)
            throw new NullPointerException();
        
        this.nodes.put(node.getName(), node);
    }

    public void addStaticBinding(NodeReq nodeReq, NodeCap nodeCap)
        throws
            NullPointerException, 
            NodeUnknownException
    {
        if(nodeReq == null || nodeCap == null)
            throw new NullPointerException();
        
        if(!this.nodes.containsKey(nodeReq.getNodeName()))
            throw new NodeUnknownException();
        if(!this.nodes.containsKey(nodeCap.getNodeName()))
        throw new NodeUnknownException();

        this.bindingFunction.put(nodeReq, nodeCap);
    }

    //############################## PI IMPLEMENTATIONS ############################

    public NodeInstance pi(String instanceID, Requirement req) 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        switch (this.piVersion) {
            case GREEDYPI:
                return this.greedyPI(instanceID, req);
            case RANDOMPI:
                return this.randomPI(instanceID, req);
            default:
                break;
        }
        return null;
    }

    /**
     * @param instanceID identifier of the instance with the req about we want a server
     * @param req requirement that have to be satisfied
     * @return the first found instance that can satisfy the requirement of the given instance
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    private NodeInstance greedyPI(String instanceID, Requirement req)
        throws
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        NodeInstance instance = this.globalState.getNodeInstanceByID(instanceID);
        if(req == null)
            throw new NullPointerException();

        NodeReq nodeReq = new NodeReq(instance.getNodeType().getName(), req.getName());
        NodeCap nodeCap = this.bindingFunction.get(nodeReq);

        if(nodeCap != null){
            for(NodeInstance activeInstance : this.globalState.getActiveInstances().values()){
                
                boolean instanceRightType = nodeCap.getNodeName().equals(activeInstance.getNodeType().getName());
                boolean instanceOfferingCap = activeInstance.getOfferedCaps().contains(nodeCap.getCap());

                if(instanceOfferingCap && instanceRightType)
                    return activeInstance;
                
            }
        }

        return null;
    }

    /**
     * @param instanceID identifier of the instance with the req about we want a server
     * @param req requirement that have to be satisfied
     * @return a random instance among those that can satisfy the requirement of the given instance
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    private NodeInstance randomPI(String instanceID, Requirement req)
        throws
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        this.globalState.getNodeInstanceByID(instanceID);
        if(req == null)
            throw new NullPointerException();

        NodeInstance server = null;

        List<NodeInstance> capableInstances = this.globalState.getCapableInstances(instanceID, req);
        if(!capableInstances.isEmpty())
            server = capableInstances.get(this.randomIndex(0, capableInstances.size() - 1));

        return server;
    }

    //########################### MODEL RULES IMPLEMENTATION ############################
    public void opStart(String instanceID, String op)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException, 
            OperationNotAvailableException
    {
        NodeInstance instance = this.globalState.getNodeInstanceByID(instanceID);
        Transition targetTransition = instance.getTransitionByOp(op);

        if(targetTransition == null)
            throw new OperationNotAvailableException();

        //instance goes in the new transient state
        instance.setCurrentState(targetTransition.getName());
        //kill old runtime bindings (about previous state)
        this.globalState.removeOldRuntimeBindings(instanceID);
        //add new runtime bindings (about new transient state)
        this.globalState.addNewRuntimeBindings(instanceID);
    }

    public void opEnd(String instanceID, String op)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException, 
            OperationNotAvailableException,
            FailedOperationException
    {
        NodeInstance instance = this.globalState.getNodeInstanceByID(instanceID);
        if(op == null)
            throw new NullPointerException();
        if(op.isBlank())
            throw new IllegalArgumentException();

        if(this.globalState.isBrokenInstance(instanceID))
            throw new FailedOperationException();

        if(!this.globalState.getPendingFaults(instanceID).isEmpty())
            throw new FailedOperationException();
        
        //TODO caso tricky: come e' possibile una cosa del genere? utente definito male applicazione (?)
        //da testare bene in OpEndTest
        Transition targetTransition = 
            instance.getNodeType().getManProtocol().getTransitions().get(instance.getCurrentState());
        if(targetTransition == null)
            throw new FailedOperationException();

        //instance goes in the new final state of the transition
        instance.setCurrentState(targetTransition.getEndState());
        //kill old runtime bindings (about previous state)
        this.globalState.removeOldRuntimeBindings(instanceID);
        //add new runtime bindings (about new state)
        this.globalState.addNewRuntimeBindings(instanceID);
    }

    /**
     * 
     * @param nodeName name of the Node of which we want to create an instance
     * @param newNodeInstanceIDID the id to assign to the new instance
     * @return the new instance of the given node
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws AlreadyUsedIDException
     * @throws RuleNotApplicableException
     */
    public NodeInstance scaleOut1(String nodeName, String newNodeInstanceID)
        throws
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException,
            InstanceUnknownException, 
            NodeUnknownException
    {
        if(nodeName == null || newNodeInstanceID == null)
            throw new NullPointerException();
        if(nodeName.isBlank() || newNodeInstanceID.isBlank())
            throw new IllegalArgumentException();

        if(this.globalState.getActiveInstances().containsKey(newNodeInstanceID))
            throw new AlreadyUsedIDException();

        Node node = this.nodes.get(nodeName);
        //node unknown
        if(node == null)
            throw new NodeUnknownException();
            
        for(Requirement req : node.getReqs()){
            //scaleOut1 do not handle such nodes
            if(req.isContainment())
                throw new RuleNotApplicableException();
        }
        
        //creates new instance
        NodeInstance newNodeInstance = new NodeInstance(node, node.getManProtocol().getInitialState(), newNodeInstanceID);
        //add the new instance to the set of active instances
        this.globalState.getActiveInstances().put(newNodeInstanceID, newNodeInstance);
        //initialize the runtime bindings for the new instance
        this.globalState.getRuntimeBindings().put(newNodeInstanceID, new ArrayList<RuntimeBinding>());
        //add the runtime bindings
        this.globalState.addNewRuntimeBindings(newNodeInstanceID);

        return newNodeInstance;
    }   

    public NodeInstance scaleOut2(String nodeName, String newNodeInstanceID, String containerID)
        throws 
            RuleNotApplicableException,
            NullPointerException, 
            AlreadyUsedIDException, 
            InstanceUnknownException,
            NodeUnknownException
    {
        NodeInstance container = this.globalState.getNodeInstanceByID(containerID);

        if(nodeName == null || newNodeInstanceID == null)
            throw new NullPointerException();
        if(nodeName.isBlank() || newNodeInstanceID.isBlank())
            throw new IllegalArgumentException();

        if(this.globalState.getActiveInstances().containsKey(newNodeInstanceID))
            throw new AlreadyUsedIDException();

        Node node = this.nodes.get(nodeName);
        //node unknown
        if(node == null)
            throw new NodeUnknownException();

        Requirement containmentReq = null;
        for(Requirement req : node.getReqs()){
            if(req.isContainment()){
                containmentReq = req;
                break;
            }
        }

        if(containmentReq == null)
            throw new RuleNotApplicableException();

        NodeReq nodeReq = new NodeReq(nodeName, containmentReq.getName());
        NodeCap nodeCap = this.bindingFunction.get(nodeReq);

        if(!nodeCap.getNodeName().equals(container.getNodeType().getName()))
            throw new RuleNotApplicableException();

        NodeInstance newNodeInstance = new NodeInstance(node, node.getManProtocol().getInitialState(), newNodeInstanceID);
        //add the new instance to the set of active instances
        this.globalState.getActiveInstances().put(newNodeInstanceID, newNodeInstance);
        //initialize the runtime bindings for the new instance
        this.globalState.getRuntimeBindings().put(newNodeInstanceID, new ArrayList<RuntimeBinding>());
        //adding the containment runtime binding
        this.globalState.addRuntimeBinding(newNodeInstanceID, containmentReq, containerID);
        //add the non containment runtime bidnigns
        this.globalState.addNewRuntimeBindings(newNodeInstanceID);

        return newNodeInstance;
    }

    public void scaleIn(String instanceID)
        throws 
            RuleNotApplicableException, 
            NullPointerException, 
            InstanceUnknownException, 
            IllegalArgumentException 
    {
        this.globalState.getNodeInstanceByID(instanceID);

        //remove all runtime bindings
        this.globalState.removeAllRuntimeBindingsBothWays(instanceID);
        
        //remove the instance from the active instances and runtime bindings
        this.globalState.getActiveInstances().remove(instanceID);
        this.globalState.getRuntimeBindings().remove(instanceID);

        //if instance was a container this cause the death of the instances it was containing
        this.autodestory();
    }

    //destorys broken instances
    private void autodestory()
        throws 
            RuleNotApplicableException, 
            NullPointerException, 
            InstanceUnknownException, 
            IllegalArgumentException 
    {
        List<NodeInstance> brokenInstances = this.globalState.getBrokenInstances();
        if(!brokenInstances.isEmpty())
            this.scaleIn(brokenInstances.get(0).getID());
    }

    //this was fault()
    public void faultHandler(Fault fault)
        throws  
        FailedFaultHandlingExecption, 
        NullPointerException, 
        RuleNotApplicableException, 
        InstanceUnknownException
    {   
        //TODO: possibile?
        //not a pending fault
        // if(!this.globalState.getPendingFaults().contains(fault))
        //     throw new RuleNotApplicableException();
        
        if(fault == null)
            throw new NullPointerException();
       
        //not a pending fault
        if(this.globalState.isResolvableFault(fault))
            throw new RuleNotApplicableException();

        NodeInstance instance = this.globalState.getNodeInstanceByID(fault.getNodeInstanceID());
        List<String> faultStates = new ArrayList<>();

        //phi: state s -> fault handling states if fault in s
        //rho: state s -> requirements needed in s
        List<String> phiStates = instance.getNodeType().getManProtocol().getPhi().get(instance.getCurrentState());
        
        for(String phiState : phiStates){
            //if phiState has as a requirement the faulted requirement then it is not usable
            if(!instance.getNodeType().getManProtocol().getRho().get(phiState).contains(fault.getReq()))
                faultStates.add(phiState);
        }

        if(faultStates.isEmpty())
            throw new FailedFaultHandlingExecption();
        
        //to maintain detrminism behaviour we always go to the state with most needed requirements
        String targetFaultState = null;
        int max = -1;
        for(String state : faultStates){
            int tmp = instance.getNodeType().getManProtocol().getRho().get(state).size();
            if(tmp > max){
                max = tmp; 
                targetFaultState = state;
            }
        }

        //put the instance in the new fault handling state
        instance.setCurrentState(targetFaultState);
        //remove old bindings
        this.globalState.removeOldRuntimeBindings(instance.getID());
        //add new bindings
        this.globalState.addNewRuntimeBindings(instance.getID());
    }

    /***
     * @param fault the fault to solve by finding a new server instance for the failed requirement
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     * @throws RuleNotApplicableException
     */
    public void autoreconnect(Fault fault)
        throws 
            NullPointerException,
            IllegalArgumentException, 
            InstanceUnknownException, 
            RuleNotApplicableException
    {
        if(fault == null)
            throw new NullPointerException();
        if(!this.globalState.isResolvableFault(fault))
            throw new RuleNotApplicableException();

        String instanceID = fault.getNodeInstanceID();
        Requirement failedReq = fault.getReq();
        this.globalState.getNodeInstanceByID(instanceID);
        
        //delete the runtime binding about the fault
        this.globalState.removeRuntimeBinding(instanceID, failedReq);
        //add new runtime binding with the new instance (retireved by pi)
        //pi cant return null because the fault is resolvable
        this.globalState.addRuntimeBinding(instanceID, failedReq, this.pi(instanceID, failedReq).getID());
    }
}