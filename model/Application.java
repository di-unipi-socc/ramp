package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.utils.RandomID;
import model.exceptions.FailedFaultHandlingExecption;
import model.exceptions.FailedOperationException;
import model.exceptions.OperationNotAvailableException;
import model.exceptions.OperationNotStartableException;

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
     * @param n node isntance that requires r
     * @param r requirement that needs to be handled
     * @return the first node instance that can take care of r
     */
    public NodeInstance defaultPi(NodeInstance n, Requirement r){
        NodeInstance ret = null;
        ArrayList<NodeInstance> actactiveNodeInstances = (ArrayList<NodeInstance>) this.globalState.activeNodeInstances.values();
        
        /**
         * detto facile. prendo il binding statico di <n, req>, cioe' quella coppia <n1, cap> che mi 
         * soddisfa il req di n.
         * Per ogni istanza attiva controllo quindi che l' istanza sia di tipo giusto, cioe' sia istanza 
         * del giusto nodo statico dato dalla funzione b.
         * A questo punto controllo che la cap richiesta nello static binding sia contenuta nelle cap che
         * offre l'istanza.
         * Se si ho finito.
         */

        StaticBinding staticBinding = this.bindingFunction.get(new StaticBinding(n.getNodeType().getName(), r.getName())); 
        if(staticBinding != null){
            for(NodeInstance n1 : actactiveNodeInstances){
                if(n1.getNodeType().getName().equals(staticBinding.getNodeName()) == true){
                    //n1 is an isntance of the right node of the right topological binding
                    if(n1.getOfferedCaps().contains(staticBinding.getCapOrReq()) == true){
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
        this.globalState.removeOldBindings(n);
        //we add the new bindings (the ones that are about the new transient state)
        this.globalState.addNewBindings(n);
    }

    /**
     * @param n node instance on which it's being executed op
     * @param op management op of n that has to end
     * @throws FailedOperationException
     */
    public void opEnd(NodeInstance n, String op) throws FailedOperationException  {
        ArrayList<Fault> faults = (ArrayList<Fault>) this.globalState.getPendingFaults(n);
        if(faults.isEmpty() == false)
            throw new FailedOperationException();

        //we get the transition by it's name (which is stored in the current state, since it is transient)
        Transition transitionToComplete = n.getNodeType().getMp().getTransition().get(n.getCurrenState());
        //n goes in a new final state
        n.setCurrenState(transitionToComplete.getEndingState());
        //we kill old bindings (the ones that were about the old state)
        this.globalState.removeOldBindings(n);
        //we add the new bindings (the ones that are about the new transient state)
        this.globalState.addNewBindings(n);
    }

    public void fault(NodeInstance n, Requirement r) throws FailedFaultHandlingExecption {
        Fault f = new Fault(n.getId(), r);
        ArrayList<String> faultHandlinglobalStates = new ArrayList<>();

        //TODO o implementi equals in fault o controlli esplicitamente che n e r siano gli stessi
        //altrimenti usa object.equals e torna sempre false
        if(this.globalState.getPendingFaults().contains(f) == false)
            //qui lanci eccezione TODO rulenotapplicable
            return; //not a fault, we do nothing
        else{
            //required by the thesis
            if(this.globalState.isResolvableFault(f) == false){

                //phi: failed state -> states to go
                ArrayList<String> phiStates = 
                            (ArrayList<String>) n.getNodeType().getMp().getPhi().get(n.getCurrenState());
                
                //for each state in phiStates we check if r it's needed in that state, if not the
                //state is usable for the fault handling (stated by the thesis)
                for(String s : phiStates){
                    //rho: state s -> list of requirement needed in s
                    if(n.getNodeType().getMp().getRho().get(s).contains(r) == false)
                        //since r it's not required when n is in s we can use s
                        faultHandlinglobalStates.add(s);
                }

                //we have to choose to go to the state that have the most reqs needed (to mantein the deterministic of mp)
                //required by the thesis
                String rightState = null;
                int max = -1;
                //TODO: non e' l'esatta condizione, forse dara' problemi.
                for(String s : faultHandlinglobalStates){
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
                this.globalState.removeOldBindings(n);
                this.globalState.addNewBindings(n);
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
            //implementa il controllo
        //todo contains poblematico
        if(this.globalState.isResolvableFault(f) == false)
            return; //nothing to do, notaplicableruleex
        else{
            //we find a capable instance that can take care of r
            NodeInstance n1 = this.defaultPi(n, r);
            //n1 cant be null, otherwise r wouldn't be resolvable
            this.globalState.addBinding(n, r, n1);
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
        while(this.globalState.activeNodeInstances.keySet().contains(id) == true)
            id = RandomID.generateRandomString(8);
        
        NodeInstance newNodeInstance = new NodeInstance(n, n.getInitialState(), id);
        //add the new instance in the G set
        this.globalState.activeNodeInstances.put(id, newNodeInstance);
        //add the bindings needed for the initial state of the instance
        this.globalState.addNewBindings(newNodeInstance);

        return newNodeInstance;
    }

    public NodeInstance scaleOut2(Node n, NodeInstance container) throws OperationNotStartableException {
        ArrayList<Requirement> reqsOfN = (ArrayList<Requirement>) n.getReqs();
        Requirement containmentRequirement = null;
        //the containement req must be defined and it mus be unique (stated by the thesis)
        for(Requirement r : reqsOfN){
            if(r.isContainment() == true){
                containmentRequirement = r;
            }
        }
        if(containmentRequirement == null)
            throw new OperationNotStartableException();

        //bindingFunction: (node, req) -> (node, cap) 
        StaticBinding staticBinding = this.bindingFunction.get(new StaticBinding(n.getName(), containmentRequirement.getName()));
        NodeInstance newNodeInstance = null;

        //here we check if the container is the right type of node
        if(container.getNodeType().getName().equals(staticBinding.getNodeName()) == false)
            throw new OperationNotStartableException();
        else{
            //here we check if the container is currently offering the right cap
            //todo: non serve che container stia offrendo ORA la giusta cap, basta che la possa offrire in generale
            //quindi basta il controllo sopra
            if(container.getOfferedCaps().contains(staticBinding.getCapOrReq()) == false)
                throw new OperationNotStartableException();
            else{
                //the container has the right type and it is currently offering the right cap

                //this take care of the non-containment reqs
                //TODO in pratica errore, scaleOut1 controlla i reqs e tu ne hai uno di containement, ti fallisce
                newNodeInstance = this.scaleOut1(n);
                //then we add the containement req binding
                this.globalState.addBinding(newNodeInstance, containmentRequirement, container);
            }
        }
        return newNodeInstance;
    }

    /**
     * @param i node instance we have to kill
     * @throws OperationNotStartableException
     */
    public void scaleIn(NodeInstance i) throws OperationNotStartableException {
        if(this.globalState.activeNodeInstances.containsValue(i) == false)
            throw new OperationNotStartableException();
        else{
            //we remove i from the G set
            this.globalState.activeNodeInstances.remove(i.getId());
            this.globalState.removeAllBindingsBothWays(i);
            this.autodestroy();
        }
    }
    
    /**
     * @throws OperationNotStartableException
     */
    private void autodestroy() throws OperationNotStartableException {
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) this.globalState.getBrokeninstances();
        if(brokenInstances.isEmpty() == false)
            this.scaleIn(brokenInstances.get(0));     
    }
}