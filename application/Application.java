package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import application.utils.RandomID;
import application.exceptions.FailedFaultHandlingExecption;
import application.exceptions.FailedOperationException;
import application.exceptions.OperationNotAvailableException;
import application.exceptions.OperationNotStartableException;

//represents the whole application
public class Application {

    // name of the application
    private final String name;
    // set T: all the application's component
    private Map<String, Node> nodes;
    private GlobalState gState;

    // b in the cameriero's thesis. this represent a static binding such as
    // <name of static node n, name of the requirement r of n> -> <name of static
    // node n1 that satify r, capability that satisfy r>
    private Map<Tmp, Tmp> bindingFunction;

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
        this.gState = new GlobalState(this);
        this.bindingFunction = new HashMap<>();

        // if p is null we will use this.defaultPi
    }

    public Map<Tmp, Tmp> getBindingFunction() {
        return this.bindingFunction;
    }

    public void setBindingFunction(Map<Tmp, Tmp> bf){
        this.bindingFunction = bf;
    }

    /**
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @param name  application's name
     * @param nodes map of applicaton's Node, by name
     */
    public Application(String name, Map<String, Node> nodes, Map<Tmp, Tmp> bf){
        assert name.length() > 0;
        assert nodes != null;
        assert bf != null;
        this.name = name; 
        this.nodes = nodes;
        this.gState = new GlobalState(this);
        this.bindingFunction = bf;
    }

    /**
     * @return current GlobalState
     */
    public GlobalState getGState() {
        return gState;
    }

    /**
     * @param gs given GlobalState 
     */
    public void setGState(GlobalState gs) {
        this.gState = gs;
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
     * @param n node isntance that requires r
     * @param r requirement that needs to be handled
     * @return the first node instance that can take care of r
     */
    public NodeInstance defaultPi(NodeInstance n, Requirement r){
        NodeInstance ret = null;
        ArrayList<NodeInstance> activeNodes = (ArrayList<NodeInstance>) this.gState.activeNodes.values();
        
        /**
         * detto facile. prendo il binding statico di <n, req>, cioe' quella coppia <n1, cap> che mi 
         * soddisfa il req di n.
         * Per ogni istanza attiva controllo quindi che l' istanza sia di tipo giusto, cioe' sia istanza 
         * del giusto nodo statico dato dalla funzione b.
         * A questo punto controllo che la cap richiesta nello static binding sia contenuta nelle cap che
         * offre l'istanza.
         * Se si ho finito.
         */

        Tmp staticBinding = this.bindingFunction.get(new Tmp(n.getNodeType().getName(), r.getName())); 
        if(staticBinding != null){
            for(NodeInstance n1 : activeNodes){
                if(n1.getNodeType().getName().equals(staticBinding.getNodeName()) == true){
                    //n1 is an isntance of the right node of the right topological binding
                    if(n1.getOfferedCaps().contains(staticBinding.getNeed()) == true){
                        //this means that 
                        ret = n1;
                        break;
                    }
                }
            }
        }
        return ret;
    }

    /**
     * @param n  node instance on which it's required to do the managment operation op
     * @param op management operation to execute
     * @throws NullPointerException
     * @throws OperationNotAvailableException
     * @throws InstanceNotAvailableException
     */
    public void opStart(NodeInstance n, String op)
        throws OperationNotAvailableException, 
        OperationNotStartableException 
    {
        assert n != null;
        assert op.length() != 0;

        Transition transitionToHappen = n.getTransitionByOp(op);
        if(transitionToHappen == null)
            //if op it's not bound to any transition it means that op is not available
            throw new OperationNotAvailableException();
        
        //n goes in a new transient state
        n.setCurrenState(transitionToHappen.getName());
        //we kill old bindings (the ones that were about the old state)
        this.gState.removeOldBindings(n);
        //we add the new bindings (the ones that are about the new transient state)
        this.gState.addNewBindings(n);
    }

    /**
     * @param n node instance on which it's being executed op
     * @param op management op of n that has to end
     * @throws FailedOperationException
     */
    public void opEnd(NodeInstance n, String op) throws FailedOperationException  {
        ArrayList<Fault> faults = (ArrayList<Fault>) this.gState.getPendingFaults(n);
        if(faults.isEmpty() == false)
            throw new FailedOperationException();

        //we get the transition by it's name (which is stored in the current state, since it is transient)
        Transition transitionToComplete = n.getNodeType().getMp().getTransition().get(n.getCurrenState());
        //n goes in a new final state
        n.setCurrenState(transitionToComplete.getEndingState());
        //we kill old bindings (the ones that were about the old state)
        this.gState.removeOldBindings(n);
        //we add the new bindings (the ones that are about the new transient state)
        this.gState.addNewBindings(n);
    }

    public void fault(NodeInstance n, Requirement r) throws FailedFaultHandlingExecption {
        Fault f = new Fault(n.getId(), r);
        ArrayList<String> faultHandlingStates = new ArrayList<>();

        if(this.gState.getPendingFaults().contains(f) == false)
            return; //not a fault, we do nothing
        else{
            //required by the thesis
            if(this.gState.isResolvableFault(f) == false){

                //phi: failed state -> states to go
                ArrayList<String> phiStates = 
                            (ArrayList<String>) n.getNodeType().getMp().getPhi().get(n.getCurrenState());
                
                //for each state in phiStates we check if r it's needed in that state, if not the
                //state is usable for the fault handling (stated by the thesis)
                for(String s : phiStates){
                    //rho: state s -> list of requirement needed in s
                    if(n.getNodeType().getMp().getRho().get(s).contains(r) == false)
                        //since r it's not required when n is in s we can use s
                        faultHandlingStates.add(s);
                }

                //we have to choose to go to the state that have the most reqs needed (to mantein the deterministic of mp)
                //required by the thesis
                String rightState = null;
                int max = -1;
                for(String s : faultHandlingStates){
                    int tmp = n.getNodeType().getMp().getRho().get(s).size();
                    if(tmp > max){
                        max = tmp;
                        rightState = s;
                    }
                }

                //giusto? TODO
                if(rightState == null)
                    throw new FailedFaultHandlingExecption();

                //we apply the rule
                n.setCurrenState(rightState);
                this.gState.removeOldBindings(n);
                this.gState.addNewBindings(n);
            }
        }
    }

    /**
     * @param n node instance that have a fault to be resolved
     * @param r requirement that has failed
     */
    public void autoreconnect(NodeInstance n, Requirement r){
        Fault f = new Fault(n.getId(), r);

        //TODO: qui la regola non rimuove il vecchio binding, secondo me pero' sarebbe utile. 
        if(this.gState.getPendingFaults(n).contains(f) == false)
            return; //nothing to do
        else{
            //we find a capable instance that can take care of r
            NodeInstance n1 = this.defaultPi(n, r);
            //n1 cant be null, otherwise r wouldn't be resolvable
            this.gState.addBinding(n, r, n1);
        }
    }

    /**
     * @param n node of which we want create a new instance
     * @throws OperationNotStartableException
     * @return the newly created instance
     */
    public NodeInstance scaleOut1(Node n) throws OperationNotStartableException {
        //scaleOut1 can't handle nodes with contaimenent requirements
        //for that see scaleOut2
        ArrayList<Requirement> reqsOfN = (ArrayList<Requirement>) n.getReqs();
        for(Requirement r : reqsOfN){
            if(r.isContainment() == true)
                throw new OperationNotStartableException();
        }

        String id = RandomID.generateRandomString(12);
        //node instance's id must be unique among all instances
        while(this.gState.activeNodes.keySet().contains(id) == true)
            id = RandomID.generateRandomString(8);
        
        NodeInstance newNodeInstanceInstance = new NodeInstance(n, n.getInitialState(), id);
        //add the new instance in the G set
        this.gState.activeNodes.put(id, newNodeInstanceInstance);
        //add the bindings needed for the initial state of the instance
        this.gState.addNewBindings(newNodeInstanceInstance);

        return newNodeInstanceInstance;
    }

    public NodeInstance scaleOut2(Node n, NodeInstance container) throws OperationNotStartableException {
        ArrayList<Requirement> reqsOfN = (ArrayList<Requirement>) n.getReqs();
        int i = 0;
        Requirement containmentRequirement = null;
        //the containement req must be defined and it mus be unique (stated by the thesis)
        for(Requirement r : reqsOfN){
            if(r.isContainment() == true){
                i++;
                containmentRequirement = r;
            }
        }
        if(i != 1)
            throw new OperationNotStartableException();

        //bindingFunction: (node, req) -> (node, cap) 
        Tmp staticBinding = this.bindingFunction.get(new Tmp(n.getName(), containmentRequirement.getName()));
        NodeInstance newNodeInstance = null;

        //here we check if the container is the right type of node
        if(container.getNodeType().getName().equals(staticBinding.getNodeName()) == false)
            throw new OperationNotStartableException();
        else{
            //here we check if the container is currently offering the right cap
            if(container.getOfferedCaps().contains(staticBinding.getNeed()) == false)
                throw new OperationNotStartableException();
            else{
                //the container has the right type and it is currently offering the right cap

                //this take care of the non-containment reqs
                newNodeInstance = this.scaleOut1(n);
                //then we add the containement req binding
                this.gState.addBinding(newNodeInstance, containmentRequirement, container);
            }
        }
        return newNodeInstance;
    }

    /**
     * @param i node instance we have to kill
     * @throws OperationNotStartableException
     */
    public void scaleIn(NodeInstance i) throws OperationNotStartableException {
        if(this.gState.activeNodes.containsValue(i) == false)
            throw new OperationNotStartableException();
        else{
            //we remove i from the G set
            this.gState.activeNodes.remove(i.getId());
            this.gState.removeAllBindingsBothWays(i);
            this.autodestroy();
        }
    }
    
    /**
     * @throws OperationNotStartableException
     */
    private void autodestroy() throws OperationNotStartableException {
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) this.gState.getBrokeninstances();
        for(NodeInstance i : brokenInstances) 
            this.scaleIn(i);     
    }
}