package unipi.di.socc.ramp.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unipi.di.socc.ramp.core.analyzer.actions.*;
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

        Transition targetTransition = 
            instance.getNodeType().getManProtocol().getTransitions().get(instance.getCurrentState());
        
        if(!this.globalState.getPendingFaults(instanceID).isEmpty())
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
    public NodeInstance scaleOut(String nodeName, String newNodeInstanceID)
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
            //scaleOut do not handle such nodes
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

    public NodeInstance scaleOutC(String nodeName, String newNodeInstanceID, String containerID)
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
        this.destroy();
    }

    private void destroy()
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

    public void handleFault(Fault fault)
        throws  
        FailedFaultHandlingExecption, 
        NullPointerException, 
        RuleNotApplicableException, 
        InstanceUnknownException
    {           
        if(fault == null)
            throw new NullPointerException();

        // not a pending fault
        if(!this.globalState.getPendingFaults().contains(fault) || this.globalState.isResolvableFault(fault))
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
    public void resolveFault(Fault fault)
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

    //#region UTILITIES

    @Override
    public Application clone(){ 
        Application clonedApp = new Application(this.name, this.piVersion);

        //CLONE APPLICATION TOPOLOGY

        //cloning nodes
        for(String nodeName : this.nodes.keySet()){
            Node node = this.nodes.get(nodeName);
            ManagementProtocol nodeMP = node.getManProtocol();

            Node clonedNode = new Node(node.getName(), new ManagementProtocol(new String(nodeMP.getInitialState())));
            ManagementProtocol clonedNodeMP = clonedNode.getManProtocol();

            //cloning ops
            for(String nO : node.getOps())
                clonedNode.addOperation(nO);
            
            //cloning reqs
            for(Requirement nR : node.getReqs())
                clonedNode.addRequirement(nR.clone());
            
            //cloning caps
            for(String nC : node.getCaps())
                clonedNode.addCapability(nC);

            //cloning states
            for(String nS : nodeMP.getStates())
                clonedNodeMP.addState(nS);

            //cloning transitons
            for(Transition nT : nodeMP.getTransitions().values())
                clonedNodeMP.addTransition(nT.getStartState(), nT.getOp(), nT.getEndState());
            
            //cloning rho
            for(String nS : nodeMP.getRho().keySet()){
                for(Requirement req : nodeMP.getRho().get(nS))
                    clonedNodeMP.getRho().get(nS).add(req.clone());
            }
            //cloning gamma
            for(String nS : nodeMP.getGamma().keySet()){
                for(String cap : nodeMP.getGamma().get(nS))
                    clonedNodeMP.getGamma().get(nS).add(cap);
            }
            //cloning phi
            for(String nS : nodeMP.getPhi().keySet()){
                for(String faultHandleState : nodeMP.getPhi().get(nS))
                    clonedNodeMP.getPhi().get(nS).add(faultHandleState);
            }

            clonedApp.addNode(clonedNode);
        }

        //cloning static binding
        for(NodeReq nodeReq : this.bindingFunction.keySet()){
            NodeCap nodeCap = this.bindingFunction.get(nodeReq);

            NodeReq clonedNodeReq = new NodeReq(nodeReq.getNodeName(), nodeReq.getReqName());
            NodeCap clonedNodeCap = new NodeCap(nodeCap.getNodeName(), nodeCap.getCap());

            clonedApp.bindingFunction.put(clonedNodeReq, clonedNodeCap);
        }

        //CLONE THE GLOBAL STATE

        //clone the active instances
        for(NodeInstance activeInstance : this.globalState.getActiveInstances().values()){
            //retrieve the node type (CLONED) of the cloned instance we're about to make
            Node clonedNode = clonedApp.getNodes().get(activeInstance.getNodeType().getName());

            //it is important that the cloned instance has as a node type referement the cloned node
            NodeInstance clonedInstance = new NodeInstance(
                clonedNode, 
                activeInstance.getCurrentState(), 
                activeInstance.getID()
            );

            clonedApp.getGlobalState().getActiveInstances().put(clonedInstance.getID(), clonedInstance);
        }

        //clone the runtime bindings
        for(String instanceID : this.globalState.getRuntimeBindings().keySet()){
            List<RuntimeBinding> clonedRuntimeBindings = new ArrayList<>();

            for(RuntimeBinding instanceRB : this.globalState.getRuntimeBindings().get(instanceID)){

                RuntimeBinding clonedRuntimeBinding = new RuntimeBinding(
                    instanceRB.getReq().clone(), 
                    instanceRB.getNodeInstanceID()
                );
                
                clonedRuntimeBindings.add(clonedRuntimeBinding);
            }
            clonedApp.globalState.getRuntimeBindings().put(instanceID, clonedRuntimeBindings);
        }

        return clonedApp;
    }

    @Override
    public boolean equals(Object obj){

        Application check = (Application) obj;

        return 
            this.bindingFunction.equals(check.bindingFunction) &&
            this.deterministicPi == check.deterministicPi &&
            this.globalState.equals(check.globalState) &&
            this.nodes.equals(check.nodes) && 
            this.name.equals(check.name) &&
            this.piVersion == check.piVersion
        ;
    }

    //#endregion


    public void execute(Action action) 
        throws 
            NullPointerException, 
            RuleNotApplicableException, 
            AlreadyUsedIDException, 
            InstanceUnknownException, 
            NodeUnknownException, 
            IllegalArgumentException, 
            OperationNotAvailableException, 
            FailedOperationException
    {
        switch (action.getActionName()) {
            case "opStart":
                OpStart opStart = (OpStart) action;
                this.opStart(opStart.getInstanceID(), opStart.getOpName());
                break;
            case "opEnd":
                OpEnd opEnd = (OpEnd) action;
                this.opEnd(opEnd.getInstanceID(), opEnd.getOpName());
                break;
            case "scaleIn":
                ScaleIn scaleIn = (ScaleIn) action;
                this.scaleIn(scaleIn.getInstanceID());
                break;
            case "scaleOut":
                ScaleOut scaleOut = (ScaleOut) action;
                this.scaleOut(scaleOut.getNodeName(), scaleOut.getIDToAssign());
                break;
            case "scaleOutC": 
                ScaleOutC scaleOutC = (ScaleOutC) action;
                this.scaleOutC(scaleOutC.getNodeName(), scaleOutC.getIDToAssign(), scaleOutC.getContainerID());
                break;

            default:
                break;
        }
    }

}