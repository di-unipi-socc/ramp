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
        //devi aggiungere il controllo in cui si verifica che il nodo che soddisfa il r offra ancora la giusta cap
        List<Requirement> satisfiedReqs = new ArrayList<>();
        
        //list of <Requirement of n, NodeInstance that satisfy r> (theorically, in runtime)
        List<Binding> nBindings = this.binding.get(n.getId());

        /**
         * detto potabile.
         * prendo tutti i runtime binding n (cioe' una lista di <req di n, n1_i che soddisfa r>)
         * prendo i binding statici, cioe' <nodo n, req r di n> -> <nodo n1_s, cap c di n1 che soddisfa r>
         * 
         * per ogni n1_i controllo che 
         *  - sia del tipo corretto (cioe' n1_s)
         *  - fra le caps offerte da n1_i ci sia quella richiesta dalla topologia statica
         * 
         * se si il req e' soddisfatto, questo perche il binding runtime <n, r, n'> e' giusto
         * (ovvero n1 e' del tipo giusto, offre la giusta cap e la offre ora)
         */

        //for each pair this extract just the satisfied requirement
        for(Binding b: nBindings){
            //get the topoligical binding such as <node, req> -> <node, cap> (function b)
            Tmp staticBinding = this.app.getBindingFunction().get(new Tmp(b.getIst(), b.getReq().getName()));
            if(staticBinding != null){

                //this is the node instance that is currently helding the runtime binding and
                //theorically satisfying the requirement of n
                NodeInstance n1 = this.activeNodes.get(b.getIst());

                //here we check if n1 is the right "type" of node (if it is the right "instance")
                if(n1.getNodeType().getName().equals(staticBinding.getNodeName()) == true){
                    //here we check if n1 is offering the right cap
                    if(this.getOfferedCaps(n1).contains(staticBinding.getNeed())){
                        //this means that the node instance n1 is offering right now the requirement that
                        //it is needed topologically speaking, hence the requirement is satisfied
                        satisfiedReqs.add(b.getReq());
                    }
                }
            }
        }
        return satisfiedReqs;
    }

    /**
     * @param n node instance of which we want to remove the old bindings
     */
    public void removeOldBindings(NodeInstance n){
        assert n != null;
        //list of the satisfied reqs of n
        ArrayList<Requirement> reqs = (ArrayList<Requirement>) this.getSatisfiedReqs(n);

        //we remove the needed reqs for the satisfied reqs, so what remains are the unneeded reqs of n
        if(reqs.removeAll(this.getNeededReqs(n)) == true){
            for(Requirement r : reqs){
                if(r.isContainment() == false)
                    this.removeBinding(n, r);
            }
        }
    }

    public void addNewBindings(NodeInstance n){
        //list of requirement that n needs
        ArrayList<Requirement> reqs = (ArrayList<Requirement>) this.getNeededReqs(n);

        //we remove the satisfied reqs from the needed reqs, so what remains are the unsatisfied reqs of n
        if(reqs.removeAll(this.getSatisfiedReqs(n)) == true){
            for(Requirement r : reqs){
                if(r.isContainment() == false){
                    NodeInstance capableInstance = this.app.defaultPi(n, r);
                    this.addBinding(n, r, capableInstance);
                }
            }
        }
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
     * @throws NullPointerException
     */
    public void removeBinding(NodeInstance n, Requirement r){
        assert n != null;
        assert r != null;
        
        //ref to the binding of n
        ArrayList<Binding> nBindings = (ArrayList<Binding>) this.binding.get(n.getId());

        //we are already in a situation such as <n, ., .>
        for (Binding b : nBindings) {
            if(b.getReq().equals(r) == true)
                //we have <n, r, x> so we remove it
                nBindings.remove(b);   
        }
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
                    faults.add(new Fault(n.getId(), r));
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

    /**
     * @param f fault of witch we want to know if it is resolvable
     * @return true if f is resolvable
     */
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