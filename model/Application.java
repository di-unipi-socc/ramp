package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.exceptions.*;
import model.utils.*;

//represents the whole application
public class Application {

    // name of the application
    private final String name;
    // set T: all the application's component
    private Map<String, Node> nodes;
    private GlobalState globalState;

    // b in the cameriero's thesis. this represent a static binding such as
    // <name of static node n, name of the requirement r of n> -> <name of static
    // node n1 that satify r, capability that satisfy r>
    private Map<StaticBinding, StaticBinding> bindingFunction;

    /**
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @param name application's name
     */
    public Application(String name) {
        // this assert is just to check that name is not null nor empty
        // used many more times in this project, it is just a reminder for
        // a real exception handling
        assert name.length() > 0;
        this.name = name;
        this.nodes = new HashMap<>();
        this.globalState = new GlobalState(this);
        this.bindingFunction = new HashMap<>();

        // if p is null we will use this.defaultPi
    }

    public Map<StaticBinding, StaticBinding> getBindingFunction() {
        return this.bindingFunction;
    }

    public void setBindingFunction(Map<StaticBinding, StaticBinding> bf){
        this.bindingFunction = bf;
    }

    /**
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @param name  application's name
     * @param nodes map of applicaton's Node, by name
     */
    public Application(String name, Map<String, Node> nodes, Map<StaticBinding, StaticBinding> bf){
        assert name.length() > 0;
        assert nodes != null;
        assert bf != null;
        this.name = name; 
        this.nodes = nodes;
        this.globalState = new GlobalState(this);
        this.bindingFunction = bf;
    }

    /**
     * @return current GlobalState
     */
    public GlobalState getglobalState() {
        return globalState;
    }

    /**
     * @param gs given GlobalState 
     */
    public void setglobalState(GlobalState gs) {
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
    public void setNodes(Map<String, Node> nodes) {
        assert nodes != null;
        this.nodes = (HashMap<String, Node>) nodes;
    }

    /**
     * @return name of the applicaton
     */
    public String getName() {
        return name;
    }

    /**
     * @param instance node isntance that requires req
     * @param req requirement that needs to be handled
     * @return the first node instance that can take care of <askingInstance, req>
     * @throws NullPointerException
     */
    public NodeInstance defaultPi(NodeInstance askingInstance, Requirement req) throws NullPointerException{
        assert askingInstance != null;
        assert req != null;
        NodeInstance ret = null;
        ArrayList<NodeInstance> activeInstances = (ArrayList<NodeInstance>) this.globalState.activeNodeInstances.values();
        
        StaticBinding reqStaticBinding = new StaticBinding(askingInstance.getNodeType().getName(), req.getName());
        StaticBinding capStaticBinding = this.bindingFunction.get(reqStaticBinding); 

        if(capStaticBinding != null){
            //for each instance among the active instances we check if it can take care of <askingInstance, req>
            for(NodeInstance instance : activeInstances){
                //instance is the right kind of Node?
                boolean instanceRightType = instance.getNodeType().getName().equals(capStaticBinding.getNodeName());
                //instance is currently offering the right cap of instance?
                boolean instanceOfferingRightCap = instance.getOfferedCaps().contains(capStaticBinding.getCapOrReq());

                if(instanceRightType == instanceOfferingRightCap == true){
                    ret = instance;
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * @param instance node instance on which it's required to do the managment operation op
     * @param op management operation to execute
     * @throws NullPointerException
     * @throws OperationNotAvailableException
     * @throws NullPointerExcption
     */
    public void opStart(NodeInstance instance, String op)
        throws  OperationNotAvailableException, 
                NullPointerException 
    {
        assert instance != null;
        assert op.length() != 0;

        Transition transitionToHappen = instance.getTransitionByOp(op, instance.getCurrenState());
        if(transitionToHappen == null)
            //if op it's not bound to any transition it means that op is not available
            throw new OperationNotAvailableException();
        
        //instance goes in a new transient state
        instance.setCurrenState(transitionToHappen.getName());
        //we kill old bindings (the ones that were about the old state)
        this.globalState.removeOldBindings(instance);
        //we add the new bindings (the ones that are about the new transient state)
        this.globalState.addNewBindings(instance);
    }

    /**
     * @param instance node instance on which it's being executed op
     * @param op management op of n that has to end
     * @throws FailedOperationException
     * @throws NullPonterException
     */
    public void opEnd(NodeInstance instance, String op) 
        throws FailedOperationException, 
               NullPointerException 
    {
        assert instance != null;
        assert op.length() != 0;

        ArrayList<Fault> faults = (ArrayList<Fault>) this.globalState.getPendingFaults(instance);
        if(faults.isEmpty() == false)
            throw new FailedOperationException();

        //we get the transition by it's name (which is stored in the current state, since it is transient)
        Transition transitionToComplete = instance.getNodeType().getMp().getTransition().get(instance.getCurrenState());
        //instance goes in a new final state
        
        instance.setCurrenState(transitionToComplete.getEndingState());
        //we kill old bindings (the ones that were about the old state)
        this.globalState.removeOldBindings(instance);
        //we add the new bindings (the ones that are about the new transient state)
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
        throws FailedFaultHandlingExecption, 
               RuleNotApplicableException 
    {
        
        Fault fault = new Fault(instance.getId(), req);
        ArrayList<String> faultHandlinglobalStates = new ArrayList<>();

        if(this.globalState.getPendingFaults().contains(fault) == false || this.globalState.isResolvableFault(fault) == false)
            throw new RuleNotApplicableException("not a pending fault");
        else{
            //phi: failed state -> states to go
            ArrayList<String> phiStates = 
                        (ArrayList<String>) instance.getNodeType().getMp().getPhi().get(instance.getCurrenState());
            
            //for each state in phiStates we check if req it's needed in that state, if it is usable for the fault handling 
            for(String state : phiStates){
                //rho: state s -> list of requirement needed in s
                if(instance.getNodeType().getMp().getRho().get(state).contains(req) == false)
                    //since req it's not required when instance is in state we can use state
                    faultHandlinglobalStates.add(state);
            }

            //we have to choose to go to the state that have the most reqs needed (to mantein the deterministic of mp)
            String rightState = null;
            int max = -1;
            for(String s : faultHandlinglobalStates){
                //might not be right. to test
                int tmp = instance.getNodeType().getMp().getRho().get(s).size();
                if(tmp > max){
                    max = tmp;
                    rightState = s;
                }
            }

            if(rightState == null)
                throw new FailedFaultHandlingExecption("no state to go for fault handling");

            //we apply the rule
            instance.setCurrenState(rightState);
            this.globalState.removeOldBindings(instance);
            this.globalState.addNewBindings(instance);
        }
    }

    /**
     * @param instance node instance that have a fault to be resolved
     * @param req requirement that has failed
     * @throws RuleNotApplicableException
     * @throws NullPointerException
     */
    public void autoreconnect(NodeInstance askingInstance, Requirement req) 
        throws RuleNotApplicableException,
               NullPointerException
    {
        assert askingInstance != null;
        assert req != null;
        Fault fault = new Fault(askingInstance.getId(), req);

        if(this.globalState.isResolvableFault(fault) == false)
            throw new RuleNotApplicableException("fault not resolvable");
        else{
            //we delete the old binding (that has failed)
            this.globalState.removeRuntimeBinding(askingInstance, req);

            //we find a new capable instance that can take care of req
            NodeInstance servingInstance = this.defaultPi(askingInstance, req);
            //servingInstance cant be null, otherwise req wouldn't be resolvable
            this.globalState.addBinding(askingInstance, req, servingInstance);
        }
    }

    /**
     * @param node node of which we want create a new instance
     * @throws RuleNotApplicableException
     * @throws NullPointerException
     * @return the newly created instance
     */
    public NodeInstance scaleOut1(Node node) 
        throws RuleNotApplicableException,
               NullPointerException
    {
        assert node != null;

        ArrayList<Requirement> nodeRequirements = (ArrayList<Requirement>) node.getReqs();
        //scaleOut not handle the containement requirements 
        for(Requirement req : nodeRequirements){
            if(req.isContainment() == true)
                throw new RuleNotApplicableException();
        }

        NodeInstance newNodeInstance = this.createNewNodeInstance(node);
        //add the new instance in the G set
        this.globalState.activeNodeInstances.put(newNodeInstance.getId(), newNodeInstance);
        //add the bindings needed for the initial state of the instance
        this.globalState.runtimeBindings.put(newNodeInstance.getId(), new ArrayList<RuntimeBinding>());
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
        assert node != null;
        assert container != null;

        ArrayList<Requirement> nodeRequirements = (ArrayList<Requirement>) node.getReqs();
        Requirement containmentRequirement = null;
        for(Requirement req : nodeRequirements){
            if(req.isContainment() == true)
                containmentRequirement = req;
        }

        if(containmentRequirement == null)
            throw new RuleNotApplicableException("no containement requirement");

        //bindingFunction: (node, req) -> (node, cap) 
        StaticBinding reqStaticBinding = new StaticBinding(node.getName(), containmentRequirement.getName());
        StaticBinding capStaticBinding = this.bindingFunction.get(reqStaticBinding);
        
        NodeInstance newNodeInstance = null;
        
        //here we check if the container is the right type of node
        if(container.getNodeType().getName().equals(capStaticBinding.getNodeName()) == false)
            throw new RuleNotApplicableException("wrong kind of node");
        else{
            //create the new instance
            newNodeInstance = this.createNewNodeInstance(node);

            //add the new instance in the G set
            this.globalState.activeNodeInstances.put(newNodeInstance.getId(), newNodeInstance);

            //add the non-containemnt bindings needed for the initial state of the new instance
            this.globalState.runtimeBindings.put(newNodeInstance.getId(), new ArrayList<RuntimeBinding>());
            this.globalState.addNewBindings(newNodeInstance);

            //explicitly add the containment binding
            this.globalState.addBinding(newNodeInstance, containmentRequirement, container);
        }

        return newNodeInstance;
    }

    /**
     * @param instance node instance we have to kill
     * @throws NullPonterException
     * @throws RuleNotApplicableException
     */
    public void scaleIn(NodeInstance instance) 
        throws RuleNotApplicableException, 
               NullPointerException 
    {
        assert instance != null;
        if(this.globalState.activeNodeInstances.containsValue(instance) == false)
            throw new RuleNotApplicableException();
        else{
            //we remove i from the G set
            this.globalState.activeNodeInstances.remove(instance.getId());
            this.globalState.removeAllBindingsBothWays(instance);
            this.autodestroy();
        }
    }
    
    /**
     * @throws RuleNotApplicableException
     */
    private void autodestroy() throws RuleNotApplicableException {
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) this.globalState.getBrokeninstances();
        if(brokenInstances.isEmpty() == false)
            this.scaleIn(brokenInstances.get(0));     
    }

    private NodeInstance createNewNodeInstance(Node node){
        String newNodeInstanceID = RandomID.generateRandomString(12);
        //node instance's id must be unique among all node instances
        while(this.globalState.activeNodeInstances.keySet().contains(newNodeInstanceID) == true)
            newNodeInstanceID = RandomID.generateRandomString(12);
        
        return new NodeInstance(node, node.getInitialState(), newNodeInstanceID);
    }
}