package model;

import exceptions.*;
import utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

//represents the whole application
public class Application {

    // name of the application
    private final String name;
    // set T: all the application's component: node's name -> node
    private Map<String, Node> nodes;
    private GlobalState globalState;

    // b in the cameriero's thesis. this represent a static binding such as
    // <name of static node n, name of the requirement r of n> -> <name of static
    // node n1 that satify r, capability that satisfy r>
    private Map<StaticBinding, StaticBinding> bindingFunction;

    private int randomIndex(int min, int max){
        return (int) ((Math.random() * (max - min)) + min); 
    }

    /**
     * @param name application's name
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public Application(String name) 
        throws 
            NullPointerException, 
            IllegalArgumentException 
    {
        if(name == null)
            throw new NullPointerException("name null");

        if(name.isEmpty() == true)
            throw new IllegalArgumentException("name empty");

        this.name = name;
        this.nodes = new HashMap<>();
        this.globalState = new GlobalState(this);
        this.bindingFunction = new HashMap<>();

        // if pi is null we will use greedyPI
    }

    public Map<StaticBinding, StaticBinding> getBindingFunction() {
        return this.bindingFunction;
    }

    public void setBindingFunction(Map<StaticBinding, StaticBinding> bf) 
        throws 
            NullPointerException
    {
        if(bf == null)
            throw new NullPointerException("bf null");
        this.bindingFunction = bf;
    }

    /**
     * @param name application's name
     * @param nodes map of applicaton's Node, by name
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public Application(String name, Map<String, Node> nodes, Map<StaticBinding, StaticBinding> bf)
        throws 
            NullPointerException, 
            IllegalArgumentException
    {    
        if(name == null)
            throw new NullPointerException("name null");

        if(name.isEmpty() == true)
            throw new IllegalArgumentException("name empty");
        
        if(nodes == null)
            throw new NullPointerException("nodes null");

        if(bf == null)
            throw new NullPointerException("bf null");

        this.name = name; 
        this.nodes = nodes;
        this.globalState = new GlobalState(this);
        this.bindingFunction = bf;
    }

    /**
     * @return current GlobalState
     */
    public GlobalState getGlobalState() {
        return globalState;
    }

    /**
     * @param gs given GlobalState 
     */
    public void setGlobalState(GlobalState gs) 
        throws  
            NullPointerException
    {
        if(gs == null)
            throw new NullPointerException("gs null");
        this.globalState = gs;
    }

    /**
     * @return list of the application's Node
     */
    public Map<String, Node> getNodes() {
        return nodes;
    }

    /**
     * @param nodes list of Node to bet set to the applicaton
     * @throws NullPointerException
     */
    public void setNodes(Map<String, Node> nodes) 
        throws 
            NullPointerException 
    {
        if(nodes == null)
            throw new NullPointerException("nodes null");
        this.nodes = (HashMap<String, Node>) nodes;
    }

    /**
     * @return name of the applicaton
     */
    public String getName() {
        return name;
    }

    /**
     * @param askingInstance
     * @param req
     * @return a random instances among those who can take care of <askingInstance, req>
     * @throws NullPointerException
     */
    public NodeInstance randomPI(NodeInstance askingInstance, Requirement req)
        throws
            NullPointerException
    {
        if(askingInstance == null)
            throw new NullPointerException("askingInstance null");
        if(req == null)
            throw new NullPointerException("req null");
        
        ArrayList<NodeInstance> capableInstances = (ArrayList<NodeInstance>) this.globalState.getCapableInstances(askingInstance, req);
        NodeInstance servingInstance = capableInstances.get(this.randomIndex(0, capableInstances.size() - 1));
        
        return servingInstance;
    }



    /**
     * @param instance node instance that requires req
     * @param req requirement that needs to be handled
     * @return the first node instance that can take care of <askingInstance, req>
     * @throws NullPointerException
     */
    public NodeInstance greedyPI(NodeInstance askingInstance, Requirement req) 
        throws 
            NullPointerException
    {    
        if(askingInstance == null)
            throw new NullPointerException("askingInstance null");
        if(req == null)
            throw new NullPointerException("req null");
        
        NodeInstance servingInstance = null;

        Collection<NodeInstance> activeInstancesCollection =  this.globalState.activeNodeInstances.values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);
        
        //<nodeAsking, req> -> <nodeServing, cap> at a static level, between nodes
        StaticBinding reqStaticBinding = new StaticBinding(askingInstance.getNodeType().getName(), req.getName());
        StaticBinding capStaticBinding = this.bindingFunction.get(reqStaticBinding); 

        //if capStaticBinding is null means that nodeAsking's req can't be handled by any node
        if(capStaticBinding != null){
            for(NodeInstance instance : activeInstances){

                //instance is the right type of Node?
                boolean instanceRightType = instance.getNodeType().getName().equals(capStaticBinding.getNodeName());
                
                //instance is currently offering the right cap of instance?
                boolean instanceOfferingRightCap = instance.getOfferedCaps().contains(capStaticBinding.getCapOrReq());

                if(instanceRightType == true && instanceOfferingRightCap == true){
                    servingInstance = instance;
                    break;
                }
            }
        }

        return servingInstance;
    }

    /**
     * @param instance node instance on which it's required to do the managment operation op
     * @param op management operation to execute
     * @throws NullPointerException
     * @throws OperationNotAvailableException
     */
    public void opStart(NodeInstance instance, String op)
        throws  
            OperationNotAvailableException, 
            IllegalArgumentException,
            NullPointerException 
    {

        if(instance == null)
            throw new NullPointerException("instance null");
        if(op == null)
            throw new NullPointerException("op null");
        if(op.isEmpty() == true)
            throw new IllegalArgumentException("op empty");

        Transition transitionToHappen = instance.getTransitionByOp(op);

        //if op it's not bound to any transition it means that op is not available
        if(transitionToHappen == null)
            throw new OperationNotAvailableException();
        
        //instance goes in a new transient state
        instance.setCurrentState(transitionToHappen.getName());

        //kill old bindings (the ones that were about the old state)
        this.globalState.removeOldBindings(instance);

        //add the new bindings (the ones that are about the new transient state)
        this.globalState.addNewBindings(instance);
    }

    /**
     * @param instance node instance on which it's being executed op
     * @param op management op of n that has to end
     * @throws FailedOperationException
     * @throws NullPonterException
     */
    public void opEnd(NodeInstance instance, String op) 
        throws 
            FailedOperationException, 
            IllegalArgumentException,
            NullPointerException 
    {
        if(instance == null)
            throw new NullPointerException("instance null");
        if(op == null)
            throw new NullPointerException("op null");
        if(op.isEmpty() == true)
            throw new IllegalArgumentException("op empty");

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) this.globalState.getPendingFaults(instance);

        if(pendingFaults.isEmpty() == false)
            throw new FailedOperationException("pending faults to be handled");

        if(this.globalState.isBrokenInstance(instance) == true)
            throw new FailedOperationException("broken instance");

        //get the transition by its name (which is stored in the current state, since instance is in a transient state)
        Transition transitionToComplete = instance.getNodeType().getManagementProtocol().getTransition().get(instance.getCurrentState());
        
        //instance goes in the new (final) state
        instance.setCurrentState(transitionToComplete.getEndingState());

        //kill old bindings (the ones that were about the old state)
        this.globalState.removeOldBindings(instance);

        //add the new bindings (the ones that are about the new (final) state
        this.globalState.addNewBindings(instance);
    }

    /**
     * 
     * @param instance
     * @param req
     * @throws FailedFaultHandlingExecption
     * @throws RuleNotAplicableException
     */
    public void fault(NodeInstance instance, Requirement req) 
        throws  
            FailedFaultHandlingExecption, 
            NullPointerException, 
            RuleNotApplicableException 
    {
        if(instance == null)
            throw new NullPointerException("instance null");
        if(req == null)
            throw new NullPointerException("req null");

        Fault fault = new Fault(instance.getID(), req);

        ArrayList<String> faultHandlinGlobalStates = new ArrayList<>();

        //check if the pair <instance, req> does raise a fault
        if(this.globalState.getPendingFaults().contains(fault) == false)
            throw new RuleNotApplicableException("not a pending fault");
        
        //check if the fault is not a resolvable fault
        if( this.globalState.isResolvableFault(fault) == true)
            throw new RuleNotApplicableException("the fault is resolvable");
    
        //phi: failed state -> states to go
        ArrayList<String> phiStates = 
            (ArrayList<String>) instance.getNodeType().getManagementProtocol().getPhi().get(instance.getCurrentState());
        
        //for each state in phiStates check if req is needed in that state
        for(String state : phiStates){
            //rho: state s -> list of requirement needed in s
            if(instance.getNodeType().getManagementProtocol().getRho().get(state).contains(req) == false)
                //req it's not required when instance is in state, hence it is usable for fault handlig
                faultHandlinGlobalStates.add(state);
        }

        //go to the fault handling state that have the most reqs needed (to mantein the deterministic of mp)
        String rightState = null;
        int max = -1;
        for(String s : faultHandlinGlobalStates){
            int tmp = instance.getNodeType().getManagementProtocol().getRho().get(s).size();
            if(tmp > max){
                max = tmp;
                rightState = s;
            }
        }

        if(rightState == null)
            throw new FailedFaultHandlingExecption("no state to go for fault handling");

        //we apply the rule
        instance.setCurrentState(rightState);
        this.globalState.removeOldBindings(instance);
        this.globalState.addNewBindings(instance);
    
    }

    /**
     * @param instance node instance that have a fault to be resolved
     * @param req requirement that has failed
     * @throws RuleNotApplicableException
     * @throws NullPointerException
     */
    public void autoreconnect(NodeInstance askingInstance, Requirement req) 
        throws 
            RuleNotApplicableException,
            NullPointerException
    {
        if(askingInstance == null)
            throw new NullPointerException("instance null");
        if(req == null)
            throw new NullPointerException("req null");
        
        Fault fault = new Fault(askingInstance.getID(), req);

        if(this.globalState.isResolvableFault(fault) == false)
            throw new RuleNotApplicableException("not a resolvable fault");
        
        //delete the old binding (that has failed)
        this.globalState.removeRuntimeBinding(askingInstance, req);

        //find a new capable instance that can take care of req
        NodeInstance servingInstance = this.greedyPI(askingInstance, req);

        //servingInstance cant be null, otherwise req wouldn't be resolvable
        this.globalState.addBinding(askingInstance, req, servingInstance);
        
    }

    /**
     * @param node node of which we want create a new instance
     * @throws RuleNotApplicableException
     * @throws NullPointerException
     * @throws NodeUnkownExcception
     * @return the newly created instance
     */
    public NodeInstance scaleOut1(Node node) 
        throws 
            RuleNotApplicableException,
            NullPointerException,
            NodeUnknownException
    {
        if(node == null)
            throw new NullPointerException("node null");
        if(this.nodes.containsKey(node.getName()) == false)
            throw new NodeUnknownException("node unknown");
        
        ArrayList<Requirement> nodeRequirements = (ArrayList<Requirement>) node.getReqs();

        //scaleOut1 not handle the containement requirements 
        for(Requirement req : nodeRequirements){
            if(req.isContainment() == true)
                throw new RuleNotApplicableException();
        }

        NodeInstance newNodeInstance = this.createNewNodeInstance(node);

        //add the new instance in the G set
        this.globalState.activeNodeInstances.put(newNodeInstance.getID(), newNodeInstance);

        //set up the runtime binding for the new instance
        this.globalState.runtimeBindings.put(newNodeInstance.getID(), new ArrayList<RuntimeBinding>());
        
        //add the bindings needed for the initial state of the instance
        this.globalState.addNewBindings(newNodeInstance);

        return newNodeInstance;
    }

    /**
     * @param node the component of which we want a new instance
     * @param container the node instance that take care of the containement req of the new instance of node
     * @return the newly created node instance
     * @throws RuleNotApplicableException
     * @throws NullPointerException
     */
    public NodeInstance scaleOut2(Node node, NodeInstance container) 
        throws RuleNotApplicableException,
               NullPointerException 
    {
        if(container == null)
            throw new NullPointerException("container null");
        if(node == null)
            throw new NullPointerException("node null");

        ArrayList<Requirement> nodeRequirements = (ArrayList<Requirement>) node.getReqs();

        Requirement containmentRequirement = null;
        for(Requirement req : nodeRequirements){
            if(req.isContainment() == true)
                containmentRequirement = req;
        }

        if(containmentRequirement == null)
            throw new RuleNotApplicableException("no containement requirement");

        //<nodeAsking, req> -> <nodeServing, cap> at a static level, between nodes
        StaticBinding reqStaticBinding = new StaticBinding(node.getName(), containmentRequirement.getName());
        StaticBinding capStaticBinding = this.bindingFunction.get(reqStaticBinding);
        
        NodeInstance newNodeInstance = null;

        //here we check if the container is the right type of node
        if(container.getNodeType().getName().equals(capStaticBinding.getNodeName()) == false)
            throw new RuleNotApplicableException("wrong kind of node");
        
        //create the new instance
        newNodeInstance = this.createNewNodeInstance(node);

        //add the new instance in the G set
        this.globalState.activeNodeInstances.put(newNodeInstance.getID(), newNodeInstance);

        //set up the runtime binding for the new instance
        this.globalState.runtimeBindings.put(newNodeInstance.getID(), new ArrayList<RuntimeBinding>());

        //explicitly add the containment binding
        this.globalState.addBinding(newNodeInstance, containmentRequirement, container);

        //add the non-containemnt bindings needed for the initial state of the new instance
        this.globalState.addNewBindings(newNodeInstance);
        
        return newNodeInstance;
    }

    /**
     * @param instance node instance we have to kill
     * @throws NullPonterException
     * @throws RuleNotApplicableException
     */
    public void scaleIn(NodeInstance instance) 
        throws 
            RuleNotApplicableException, 
            NullPointerException 
    {
        if(instance == null)
            throw new NullPointerException("instance null");
        
        if(this.globalState.activeNodeInstances.containsValue(instance) == false)
            throw new RuleNotApplicableException();
    
        this.globalState.activeNodeInstances.remove(instance.getID());

        //remove the bindings that took care of instance's req and the ones where instance was the server
        this.globalState.removeAllBindingsBothWays(instance);

        //if instance was a container the contained instance must be destroyed too
        this.autodestroy();
    }
    
    /**
     * @throws RuleNotApplicableException
     */
    private void autodestroy() throws RuleNotApplicableException {
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) this.globalState.getBrokeninstances();
        
        if(brokenInstances.isEmpty() == false)
            this.scaleIn(brokenInstances.get(0));     
    }

    private NodeInstance createNewNodeInstance(Node node) throws NullPointerException{
        if(node == null)
            throw new NullPointerException("node null");

        //node instance's id must be unique among all node instances
        String newNodeInstanceID = RandomID.generateRandomString(3);
        while(this.globalState.activeNodeInstances.keySet().contains(newNodeInstanceID) == true)
            newNodeInstanceID = RandomID.generateRandomString(8);
        
        return new NodeInstance(node, node.getInitialState(), newNodeInstanceID);
    }

    public void addNode(Node node) throws NullPointerException{
        if(node == null)
            throw new NullPointerException("node null");
        this.nodes.put(node.getName(), node);
    }

    public void addStaticBinding(StaticBinding source, StaticBinding target){
        if(source == null)
            throw new NullPointerException("source null");

        if(target == null)
            throw new NullPointerException("target null");
        
        this.bindingFunction.put(source, target);
    }
}