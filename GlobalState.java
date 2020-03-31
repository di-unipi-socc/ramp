import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//represents the current runtime state of the applicaton 
public class GlobalState {
    Application app;

    //active node instances: <NodeInstance unique id, NodeInstance>
    Map<String, NodeInstance> activeNodes;

    //set of runtime binding such as: 
    //id of node instance n-> list of Binding <Requirement r of n, unique id of n1 that satisfies r>
    Map<String, List<Binding>> binding;

    /**
     * @param app application of which this is the global state
     * @throws NullPointerException
     */
    public GlobalState(Application app){
        assert app != null;
        this.app = app;
        
        this.activeNodes = new HashMap<>();
        this.binding = new HashMap<>();
    }

    /**
     * @return map of the node instances currently active
     */
    public Map<String, NodeInstance> getActiveNodes(){
        return this.activeNodes;
    }

    /**
     * @return map of the runtime bindings between node instances 
     */
    public Map<String, List<Binding>> getBinding(){
        return this.binding;
    }

    /**
     * @param n node instance of which it's asked the list of satisfied reqs
     * @return list of the satisfied requirement of n (for which exist a binding)
     * @throws NullPointerException
     */
    public List<Requirement> getSatisfiedReqs(NodeInstance n){
        assert n != null;
        List<Requirement> satisfiedReqs = new ArrayList<>();
        
        //list of <Requirement of n, NodeInstance that satisfy r>
        List<Binding> nBindings = this.binding.get(n.getId());

        //for each pair this extract just the satisfied requirement
        for(Binding e: nBindings){
            satisfiedReqs.add(e.getReq());
        }

        return satisfiedReqs;
    }

    /**
     * add a runtime binding such as <n, r, n1>
     * @param n node instance asking for the requirement r
     * @param r requirement that is being asked by n
     * @param n1 node istanc that satisfy r with the correct capability
     * @throws NullPointerException
     */
    public void addBinding(NodeInstance n, Requirement r, NodeInstance n1){
        assert n != null;
        assert r != null;
        assert n1 != null;

        //get the value of the binding using the key n
        List<Binding> tmp = this.binding.get(n.getId());

        if(tmp == null){ 
            //this is the first binding of n
            tmp = new ArrayList<>();
            tmp.add(new Binding(r, n1.getId()));
        }else
            //this is another binding of n
            tmp.add(new Binding(r, n1.getId()));
    
    }

    /**
     * remove a runtime binding such as <n, r, n1>
     * @param n node instance that was asking for the requirement r
     * @param r requirement that was required
     * @param n1 NodeInstance that was satisfying r with the correct capability
     * @throws NullPointerException
     */
    public void removeBinding(NodeInstance n, Requirement r, NodeInstance n1){
        assert n != null;
        assert n1 != null;
        assert r != null;   
        this.binding.remove(n.getId());
    }

    /**
     * pending fault: a node instance require a requirement and there isn't a node
     * instance offering that capability
     * @param n node instance of which it's asked the pending faults
     * @return list of requirement of n that are not met 
     * @throws NullPointerException
     */
    public List<Fault> getPendingFaults(NodeInstance n){
        assert n != null;
        //list of fault that will return
        List<Fault> faults = new ArrayList<>();
    
        //list of requirement needed by n
        List<Requirement> neededReqs = this.getNeededReqs(n);

        //list of assumed requirement of n
        List<Requirement> satisfiedReqs = this.getSatisfiedReqs(n);

        //for each needed requirement r we check if exists a binding that 
        //resolve it, if not there is a fault
        for(Requirement r : neededReqs){
            if(r.isContainment() == false){
                if(satisfiedReqs.contains(r) == false)
                    faults.add(new Fault(n.toString(), r));
            }
        }
        return faults;
    }

    /**
     * @return list of all the pending faults of the whole application
     */
    public List<Fault> getPendingFaults(){
       List<Fault> faults = new ArrayList<>();

        //all the currently active node instances
        ArrayList<NodeInstance> instances = (ArrayList<NodeInstance>) this.activeNodes.values();
        
        //for each instance we get the list of failed requirement and then we add <ist, failed req> 
        //to the list of all faults
        for(NodeInstance ist : instances){
            ArrayList<Fault> istPendingFaults = (ArrayList<Fault>) this.getPendingFaults(ist);
            faults.addAll(istPendingFaults);
        }
        return faults;
    }

    /**
     * @param n node instance of which we want to know if it he has a broken instance
     * @return true if n has a broken instance, false otherwise
     * @throws NullPointerException
     */
    public boolean isBrokeninstance(NodeInstance n){
        assert n != null;
        boolean res = false;

        //all Bindings of n
        List<Binding> BindingsOfN = this.binding.get(n.getId());

        //for each Binding we check if this is a containment relation
        for(Binding b : BindingsOfN){
            if(b.getReq().isContainment() == true){

                //this is a containment relation, hence we check if the container
                //is still active
                if(activeNodes.get(b.getIst()) == null){
                    res = true;
                    break;
                }
            }
        }
        return res;
    }

    /**
     * broken instance: a node instance have a "contain" relation with a 
     * node instance that is no longer alive
     * @return list of node instance id(s) that have a broken instance
     */
    public List<String> getBrokeninstances(){
        List<String> brokeninstances = new ArrayList<>();
        List<NodeInstance> activeNodes = (ArrayList<NodeInstance>) this.activeNodes.values();

        for(NodeInstance n : activeNodes){
            if(this.isBrokeninstance(n) == true)
                brokeninstances.add(n.getId());
        }

        return brokeninstances;    
    }

    //TODO commenta
    public boolean isResolvableFault(Fault f){
        assert f != null;
        boolean res = false;

        //all active node instances
        ArrayList<NodeInstance> instances = (ArrayList<NodeInstance>) this.activeNodes.values();
        
        //a fault can be resolvable only if it is replica unware
        if(f.getReq().isReplicaUnaware()){
            //for each active node instance we check if it offer the right capability 
            //to resolve the fault
            for(NodeInstance n : instances){
                if(this.getOfferedCaps(n).contains(f.getReq().getName()) == true){
                    res = true;
                    break;
                }
            }
        }
        
        return res;
    }

    /**
     * @param n node instance of which we want the resolvable faults
     * @return list of resolvable faults of n
     * @throws NullPointerException
     */
    public List<Fault> getResolvableFaults(NodeInstance n){
        assert n != null;
        List<Fault> resolvableFaults = new ArrayList<>();

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) this.getPendingFaults(n);

        for(Fault f : pendingFaults){
            if(this.isResolvableFault(f) == true)
                resolvableFaults.add(f);
        }
        return resolvableFaults;
    }

    /**
     * @return list of all the resolvable faults of the whole applicaton
     */
    public List<Fault> getResolvableFaults(){
        List<Fault> resolvableFault = new ArrayList<>();
        ArrayList<NodeInstance> activeNodes = (ArrayList<NodeInstance>) this.activeNodes.values();
        
        for(NodeInstance n : activeNodes)
            resolvableFault.addAll(this.getResolvableFaults(n));
        
        return resolvableFault;
    }

     /**
     * @param n node instance of 
     * @return list of requirements that the node instance is currently asking
     * @throws NullPointerException
     */
    public List<Requirement> getNeededReqs(NodeInstance n){
        assert n != null;
        return n.getNodeType().getMp().getRho().get(n.getCurrenState());
    }

    /**
     * @param n node instance of which it's asked the list of offered caps
     * @return list of capabilities offred by n
     * @throws NullPointerException
     */
    public List<String> getOfferedCaps(NodeInstance n){
        assert n != null;
        return n.getNodeType().getMp().getGamma().get(n.getCurrenState());
    }
}