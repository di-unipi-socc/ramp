package mprot.lib.model;

import mprot.lib.model.exceptions.*;
import mprot.lib.analyzer.executable_element.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



//represents the whole application
public class Application {

    // name of the application
    private final String name;
    // set T: all the application's component: node's name -> node
    private Map<String, Node> nodes;
    private GlobalState globalState;
    private final PiVersion piVersion;
    private boolean deterministicPi;

    // b in the cameriero's thesis. this represent a static binding such as
    // <name of static node n, name of the requirement r of n> -> <name of static
    // node n1 that satify r, capability that satisfy r>
    private Map<StaticBinding, StaticBinding> bindingFunction;

    private int randomIndex(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }

    public boolean isPiDeterministic() {
        return deterministicPi;
    }

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
        if(name == null)
            throw new NullPointerException("name null");

        if(name.isEmpty() == true)
            throw new IllegalArgumentException("name empty");

        this.name = name;
        this.nodes = new HashMap<>();
        this.globalState = new GlobalState(this);
        this.bindingFunction = new HashMap<>();
        this.piVersion = piVersion;
        this.piControlSwitch();

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
    public Application(String name, PiVersion piVersion, Map<String, Node> nodes, Map<StaticBinding, StaticBinding> bf)
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
        this.piVersion = piVersion; 
        this.piControlSwitch();
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
     * @param instanceID id of the instance that has a requirement
     * @param req requirement that needs to be handled
     * @return a random instances among those who can take care of <askingInstance, req>
     * @throws NullPointerException
     */
    public NodeInstance randomPI(String instanceID, Requirement req)
        throws
            NullPointerException,
            IllegalArgumentException,
            InstanceUnknownException
    {
        if(req == null)
            throw new NullPointerException("req null");
        
        try {
            this.globalState.checkNodeInstanceExistance(instanceID);
        } catch (IllegalArgumentException | NullPointerException | InstanceUnknownException e) {
            throw e; 
        }
        
        ArrayList<NodeInstance> capableInstances = (ArrayList<NodeInstance>) this.globalState.getCapableInstances(instanceID, req);
        NodeInstance servingInstance = capableInstances.get(this.randomIndex(0, capableInstances.size() - 1));
        
        return servingInstance;
    }

    /**
     * @param instanceID id of the instance that has a requirement
     * @param req requirement that needs to be handled
     * @return the first node instance that can take care of <askingInstance, req>
     * @throws NullPointerException
     */
    public NodeInstance greedyPI(String instanceID, Requirement req) 
        throws 
            NullPointerException,
            InstanceUnknownException
    {    
        if(req == null)
            throw new NullPointerException("req null");
        
        NodeInstance instance = this.globalState.getNodeInstanceByID(instanceID);
        NodeInstance servingInstance = null;

        Collection<NodeInstance> activeInstancesCollection =  this.globalState.activeNodeInstances.values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);
        
        //<nodeAsking, req> -> <nodeServing, cap> at a static level, between nodes
        StaticBinding reqStaticBinding = new StaticBinding(instance.getNodeType().getName(), req.getName());
        StaticBinding capStaticBinding = this.bindingFunction.get(reqStaticBinding); 

        //if capStaticBinding is null means that nodeAsking's req can't be handled by any node
        if(capStaticBinding != null){
            for(NodeInstance activeIns : activeInstances){

                //instance is the right type of Node?
                boolean instanceRightType = activeIns.getNodeType().getName().equals(capStaticBinding.getNodeName());
                
                //instance is currently offering the right cap of instance?
                boolean instanceOfferingRightCap = activeIns.getOfferedCaps().contains(capStaticBinding.getCapOrReq());

                if(instanceRightType == true && instanceOfferingRightCap == true){
                    servingInstance = activeIns;
                    break;
                }
            }
        }

        return servingInstance;
    }

    /**
     * @param instanceID id of the instance on which it's required to do the managment operation op
     * @param op management operation to execute
     * @throws NullPointerException
     * @throws OperationNotAvailableException
     */
    public void opStart(String instanceID, String op)
        throws  
            OperationNotAvailableException, 
            IllegalArgumentException,
            NullPointerException, 
            InstanceUnknownException
    {
        if(op == null)
            throw new NullPointerException("op null");
        if(op.isEmpty() == true)
            throw new IllegalArgumentException("op empty");

        NodeInstance instance = this.globalState.getNodeInstanceByID(instanceID);

        Transition transitionToHappen = instance.getTransitionByOp(op);

        //if op it's not bound to any transition it means that op is not available
        if(transitionToHappen == null)
            throw new OperationNotAvailableException();
        
        //instance goes in a new transient state
        instance.setCurrentState(transitionToHappen.getName());

        //kill old bindings (the ones that were about the old state)
        this.globalState.removeOldBindings(instanceID);

        //add the new bindings (the ones that are about the new transient state)
        this.globalState.addNewBindings(instanceID);
    }

    /**
     * @param instanceID id of the instance on which it's being executed op
     * @param op management op of n that has to end
     * @throws FailedOperationException
     * @throws NullPonterException
     */
    public void opEnd(String instanceID, String op) 
        throws 
            FailedOperationException, 
            IllegalArgumentException,
            NullPointerException,
            RuleNotApplicableException,
            InstanceUnknownException
    {

        if(op == null)
            throw new NullPointerException("op null");
        if(op.isEmpty() == true)
            throw new IllegalArgumentException("op empty");

        NodeInstance instance = this.globalState.getNodeInstanceByID(instanceID);

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) this.globalState.getPendingFaults(instanceID);

        if(pendingFaults.isEmpty() == false)
            throw new FailedOperationException("pending faults to be handled");

        if(this.globalState.isBrokenInstance(instanceID) == true)
            throw new FailedOperationException("broken instance");

        //get the transition by its name (which is stored in the current state, since instance is in a transient state)
        Transition transitionToComplete = instance.getNodeType().getManagementProtocol().getTransition().get(instance.getCurrentState());
        
        if(transitionToComplete == null)
            throw new FailedOperationException("no op");

        //instance goes in the new (final) state
        instance.setCurrentState(transitionToComplete.getEndingState());

        //kill old bindings (the ones that were about the old state)
        this.globalState.removeOldBindings(instanceID);

        //add the new bindings (the ones that are about the new (final) state
        this.globalState.addNewBindings(instanceID);
    }

    /**
     * 
     * @param instanceID the id of the instance that has a fault
     * @param req the faulted requirement
     * @throws FailedFaultHandlingExecption
     * @throws RuleNotAplicableException
     */
    public void fault(String instanceID, Requirement req) 
        throws  
            FailedFaultHandlingExecption, 
            NullPointerException, 
            RuleNotApplicableException, 
            InstanceUnknownException
    {
        if(req == null)
            throw new NullPointerException("req null");

        NodeInstance instance = this.globalState.getNodeInstanceByID(instanceID);

        Fault fault = new Fault(instance.getID(), req);

        ArrayList<String> faultHandlinGlobalStates = new ArrayList<>();

        //check if the pair <instance, req> does raise a fault
        if(this.globalState.getPendingFaults().contains(fault) == false)
            throw new RuleNotApplicableException("not a pending fault");
        
        //check if the fault is not a resolvable fault
        if(this.globalState.isResolvableFault(fault) == true)
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
        this.globalState.removeOldBindings(instanceID);
        this.globalState.addNewBindings(instanceID);
    }

    /**
     * @param instanceID id of the instance that have a fault to be resolved
     * @param req the (resolvable) faulted requirement
     * @throws RuleNotApplicableException
     * @throws NullPointerException
     * @throws InstanceUnknownException
     */
    public void autoreconnect(String instanceID, Requirement req)
        throws 
            RuleNotApplicableException, 
            NullPointerException, 
            InstanceUnknownException
    {
        if(req == null)
            throw new NullPointerException("req null");

        this.globalState.checkNodeInstanceExistance(instanceID);

        Fault fault = new Fault(instanceID, req);

        if(this.globalState.isResolvableFault(fault) == false)
            throw new RuleNotApplicableException("not a resolvable fault");
        
        //delete the old binding (that has failed)
        this.globalState.removeRuntimeBinding(instanceID, req);

        //find a new capable instance that can take care of req
        NodeInstance servingInstance = null;
        if(this.piVersion == PiVersion.GREEDYPI)
            servingInstance = this.greedyPI(instanceID, req);
        if(this.piVersion == PiVersion.RANDOMPI)
            servingInstance = this.randomPI(instanceID, req);

        //servingInstance cant be null, otherwise req wouldn't be resolvable
        this.globalState.addBinding(instanceID, req, servingInstance.getID());
    }

    /**
     * @param nodeName the name of the node of which it's going to be created an instance
     * @param instanceID the id to be assigned at the instance
     * @throws RuleNotApplicableException
     * @throws NullPointerException
     * @throws NodeUnkownExcception
     * @return the newly created instance
     */
    public NodeInstance scaleOut1(String nodeName, String instanceID) 
        throws 
            RuleNotApplicableException,
            NullPointerException,
            InstanceUnknownException, 
            AlreadyUsedIDException, 
            IllegalArgumentException
    {
        if(nodeName == null)
            throw new NullPointerException("nodeName null");
        if(nodeName.isEmpty() == true)
            throw new IllegalArgumentException("nodeName null");
        
        Node node = this.getNodes().get(nodeName);

        if(node == null)
            throw new RuleNotApplicableException("node unknown");
        
        ArrayList<Requirement> nodeRequirements = (ArrayList<Requirement>) node.getReqs();

        //scaleOut1 not handle the containement requirements 
        for(Requirement req : nodeRequirements){
            if(req.isContainment() == true)
                throw new RuleNotApplicableException();
        }

        NodeInstance newNodeInstance = this.createNewNodeInstance(node, instanceID);

        //add the new instance in the G set
        this.globalState.activeNodeInstances.put(instanceID, newNodeInstance);

        //set up the runtime binding for the new instance
        this.globalState.runtimeBindings.put(instanceID, new ArrayList<RuntimeBinding>());
        
        //add the bindings needed for the initial state of the instance
        this.globalState.addNewBindings(instanceID);

        return newNodeInstance;
    }

    /**
     * @param nodeName the name of the node of which it's going to be created an instance
     * @param instanceID the id to be assigned at the instance
     * @param containerID the node instance ID that take care of the containement req of the new instance
     * @return the newly created node instance
     * @throws RuleNotApplicableException
     * @throws NullPointerException
     * @throws InstanceUnknownException
     * @throws AlreadyUsedIDException
     */
    public NodeInstance scaleOut2(String nodeName, String instanceID, String containerID) 
        throws 
            RuleNotApplicableException,
            NullPointerException, 
            AlreadyUsedIDException, 
            InstanceUnknownException
    {
        if(nodeName == null)
            throw new NullPointerException("nodeName null");
        if(nodeName.isEmpty() == true)
            throw new IllegalArgumentException("nodeName empty");

        Node node = this.getNodes().get(nodeName);
        if(node == null)
            throw new RuleNotApplicableException("node unknown");

        NodeInstance container = this.globalState.getNodeInstanceByID(containerID);
        
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
        newNodeInstance = this.createNewNodeInstance(node, instanceID);

        //add the new instance in the G set
        this.globalState.activeNodeInstances.put(instanceID, newNodeInstance);

        //set up the runtime binding for the new instance
        this.globalState.runtimeBindings.put(instanceID, new ArrayList<RuntimeBinding>());

        //explicitly add the containment binding
        this.globalState.addBinding(instanceID, containmentRequirement, containerID);

        //add the non-containemnt bindings needed for the initial state of the new instance
        this.globalState.addNewBindings(instanceID);
        
        return newNodeInstance;
    }

    /**
     * @param instanceID the id of the instance that has to be killed
     * @throws NullPonterException
     * @throws RuleNotApplicableException
     * @throws InstanceUnknownException
     */
    public void scaleIn(String instanceID)
        throws 
            RuleNotApplicableException, 
            NullPointerException, 
            InstanceUnknownException 
    {
        this.globalState.checkNodeInstanceExistance(instanceID);
    
        this.globalState.activeNodeInstances.remove(instanceID);

        //remove the bindings that took care of instance's req and the ones where instance was the server
        this.globalState.removeAllBindingsBothWays(instanceID);

        //if instance was a container the contained instance must be destroyed too
        this.autodestroy();
    }
    
    /**
     * @throws RuleNotApplicableException
     * @throws InstanceUnknownException
     * @throws NullPointerException
     */
    private void autodestroy() 
        throws 
            RuleNotApplicableException, 
            NullPointerException, 
            InstanceUnknownException 
    {
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) this.globalState.getBrokeninstances();
        
        if(brokenInstances.isEmpty() == false)
            this.scaleIn(brokenInstances.get(0).getID());     
    }

    private NodeInstance createNewNodeInstance(Node node, String id) 
        throws 
            NullPointerException,
            AlreadyUsedIDException
    {    
        if(node == null)
            throw new NullPointerException("node null");
        if(id == null)
            throw new NullPointerException("id null");
        if(id.isEmpty() == true)
            throw new IllegalArgumentException("id empty"); 

        //node instance's id must be unique among all node instances
        // String newNodeInstanceID = RandomID.generateRandomString(3);
        // while(this.globalState.activeNodeInstances.keySet().contains(newNodeInstanceID) == true)
        //     newNodeInstanceID = RandomID.generateRandomString(8);
        
        if(this.globalState.getActiveNodeInstances().containsKey(id) == true)
            throw new AlreadyUsedIDException("id already in use");

        return new NodeInstance(node, node.getInitialState(), id);
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

    /**
     * clones the application (recursivly, hence clone the global state and so on)
     */
    public Application clone(){
        Application clone = new Application(this.name, this.piVersion);

        Collection<Node> appNodesCollection =  this.nodes.values();
        ArrayList<Node> appNodes = new ArrayList<>(appNodesCollection);

        //cloning all nodes
        for (Node n : appNodes) {
            ManagementProtocol nMP = n.getManagementProtocol();

            HashMap<String, Transition> nTransitions = (HashMap<String, Transition>) nMP.getTransition();
            HashMap<String, List<Requirement>> nRho = (HashMap<String, List<Requirement>>) nMP.getRho();
            HashMap<String, List<String>> nGamma = (HashMap<String, List<String>>) nMP.getGamma();
            HashMap<String, List<String>> nPhi = (HashMap<String, List<String>>) nMP.getPhi();

            ManagementProtocol cloneMp = new ManagementProtocol();

            //cloning transitions
            for (Transition t : nTransitions.values())
                cloneMp.addTransition(new String(t.getStartingState()), new String(t.getOp()), new String(t.getEndingState()));
            
            //cloning rho
            for (String s : nRho.keySet()) {
                List<Requirement> clonedNodeReqs = new ArrayList<>();
                for (Requirement r : nRho.get(s)) 
                    clonedNodeReqs.add(new Requirement(r.getName(), r.getRequirementSort()));
                
                cloneMp.addRhoEntry(new String(s), clonedNodeReqs);
            }

            //cloning gamma
            for(String key : nGamma.keySet()){
                List<String> clonedNodeCaps = new ArrayList<>();
                for(String cap : nGamma.get(key))
                    clonedNodeCaps.add(new String(cap));

                cloneMp.addGammaEntry(new String(key), clonedNodeCaps);
            }

            //cloning phi
            for(String key : nPhi.keySet()){
                List<String> clonedNodeFStates = new ArrayList<>();
                for(String state : nPhi.get(key))
                    clonedNodeFStates.add(new String(state));
                
                cloneMp.addPhiEntry(new String(key), clonedNodeFStates);
            }

            //cloning ops
            List<String> clonedNodeOps = new ArrayList<>();
            for(String op : n.getOps())
                clonedNodeOps.add(new String(op));
            
            //cloning states
            List<String> clonedNodeStates = new ArrayList<>();
            for(String state : n.getStates())
                clonedNodeStates.add(new String(state));

            //cloning reqs
            List<Requirement> clonedNodeReqs = new ArrayList<>();
            for(Requirement r : n.getReqs())
                clonedNodeReqs.add(new Requirement(new String(r.getName()), r.getRequirementSort()));
            
            //cloning caps
            List<String> clonedNodeCaps = new ArrayList<>();
            for(String nodeCap : n.getCaps())
                clonedNodeCaps.add(new String(nodeCap));

            clone.addNode(
                new Node(n.getName(), 
                new String(n.getInitialState()), cloneMp, clonedNodeReqs, clonedNodeCaps, clonedNodeStates, clonedNodeOps)
            );
        }

        //cloning activeInstancses
        HashMap<String, NodeInstance> appActiveInstances = (HashMap<String, NodeInstance>) this.globalState.getActiveNodeInstances();
        HashMap<String, NodeInstance> cloneActiveInstances = (HashMap<String, NodeInstance>) clone.getGlobalState().getActiveNodeInstances();
        for(NodeInstance instance : appActiveInstances.values()){
            cloneActiveInstances.put(
                new String(instance.getID()), 
                new NodeInstance(clone.getNodes().get(instance.getNodeType().getName()), new String(instance.getCurrentState()), new String(instance.getID()))
            );
        }

        //cloning runtime bindings
        HashMap<String, List<RuntimeBinding>> appRuntimeBindings = (HashMap<String, List<RuntimeBinding>>) this.globalState.getRuntimeBindings();
        HashMap<String, List<RuntimeBinding>> cloneRuntimeBindings = (HashMap<String, List<RuntimeBinding>>) clone.getGlobalState().getRuntimeBindings();

        for(String key : appRuntimeBindings.keySet()){
            ArrayList<RuntimeBinding> appBindings = (ArrayList<RuntimeBinding>) appRuntimeBindings.get(key);
            
            List<RuntimeBinding> clonedBindings = new ArrayList<>();

            for(RuntimeBinding appBinding : appBindings)
                clonedBindings.add(new RuntimeBinding(new Requirement(new String(appBinding.getReq().getName()), appBinding.getReq().getRequirementSort()), new String(appBinding.getNodeInstanceID())));
            
            cloneRuntimeBindings.put(new String(key), clonedBindings);
        }

        //cloning static binding
        HashMap<StaticBinding, StaticBinding> appBindingFunction = (HashMap<StaticBinding, StaticBinding>) this.bindingFunction;
        HashMap<StaticBinding, StaticBinding> cloneBindingFunction = (HashMap<StaticBinding, StaticBinding>) clone.getBindingFunction();

        for(StaticBinding firstHalf : appBindingFunction.keySet()){
            StaticBinding secondHalf = appBindingFunction.get(firstHalf);

            StaticBinding firstHalfCopy = new StaticBinding(new String(firstHalf.getNodeName()), new String(firstHalf.getCapOrReq()));
            StaticBinding secondHalfCopy = new StaticBinding(new String(secondHalf.getNodeName()), new String(secondHalf.getCapOrReq()));
            
            cloneBindingFunction.put(firstHalfCopy, secondHalfCopy);
        }

        return clone;
    }

    public void execute(ExecutableElement element) 
        throws 
            IllegalArgumentException, 
            NullPointerException,
            FailedOperationException, 
            RuleNotApplicableException, 
            InstanceUnknownException,
            OperationNotAvailableException, 
            AlreadyUsedIDException 
    {
        switch (element.getRule()) {
            case "opStart":
                OpStart el = (OpStart) element;
                this.opStart(el.getInstnaceID(), el.getOp());                    
                break;
            case "opEnd":
                OpEnd el1 = (OpEnd) element;
                this.opEnd(el1.getInstanceID(), el1.getOp());
                break;
            case "scaleIn":
                ScaleIn el2 = (ScaleIn) element;
                this.scaleIn(el2.getInstanceID());
                break;
            case "scaleOut1":
                ScaleOut1 el3 = (ScaleOut1) element;
                this.scaleOut1(el3.getNodeName(), el3.getIDToAssign());
                break;
            case "scaleOut2": 
                ScaleOut2 el4 = (ScaleOut2) element;
                this.scaleOut2(el4.getNodeName(), el4.getIDToAssign(), el4.getContainerID());
            default:
                break;
        }
    }

    private void piControlSwitch(){
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
}