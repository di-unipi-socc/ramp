import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//represents the current runtime state of the applicaton 
public class GlobalState {
    Application app;

    //active node istances: <NodeIstance unique id, NodeIstance>
    Map<String, NodeIstance> activeNodes;

    //set of runtime binding such as: 
    //id of node istance n-> list of bond <Requirement r of n, n1 that satisfies r>
    Map<String, List<Bond>> binding;

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
     * @return map of the node istances currently active
     */
    public Map<String, NodeIstance> getActiveNodes(){
        return this.activeNodes;
    }

    /**
     * @return map of the runtime bindings between node istances 
     */
    public Map<String, List<Bond>> getBinding(){
        return this.binding;
    }

    /**
     * @param n node istance of which it's asked the list of satisfied reqs
     * @return list of the satisfied requirement of n
     * @throws NullPointerException
     */
    public List<Requirement> getSatisfiedReqs(NodeIstance n){
        assert n != null;
        List<Requirement> satisfiedReqs = new ArrayList<>();
        
        //list of <Requirement of n, NodeIstance that satisfy r>
        List<Bond> nBindings = this.binding.get(n.getId());

        //for each pair this extract just the satisfied requirement
        for(Bond e: nBindings){
            satisfiedReqs.add(e.getReq());
        }

        return satisfiedReqs;
    }

    /**
     * add a runtime binding such as <n, r, n1>
     * @param n node istance asking for the requirement r
     * @param r requirement that is being asked by n
     * @param n1 node istanc that satisfy r with the correct capability
     * @throws NullPointerException
     */
    public void addBinding(NodeIstance n, Requirement r, NodeIstance n1){
        assert n != null;
        assert r != null;
        assert n1 != null;

        //get the value of the binding using the key n
        List<Bond> tmp = this.binding.get(n.getId());

        if(tmp == null){ 
            //this is the first binding of n
            tmp = new ArrayList<>();
            tmp.add(new Bond(r, n1));
        }else
            //this is another binding of n
            tmp.add(new Bond(r, n1));
    
    }

    /**
     * remove a runtime binding such as <n, r, n1>
     * @param n node istance that was asking for the requirement r
     * @param r requirement that was required
     * @param n1 nodeIstance that was satisfying r with the correct capability
     * @throws NullPointerException
     */
    public void removeBinding(NodeIstance n, Requirement r, NodeIstance n1){
        assert n != null;
        assert n1 != null;
        assert r != null;
        this.binding.remove(n.getId());
    }

    /**
     * pending fault: requirement that is not currently satified by any capability
     * @param n node istance of which it's asked the pending faults
     * @return list of requirement of n that are not met 
     * @throws NullPointerException
     */
    public List<Requirement> getPendingFaults(NodeIstance n){
        assert n != null;

        ArrayList<Requirement> neededReqs = (ArrayList<Requirement>) this.getNeededReqs(n);
        ArrayList<Requirement> satisfiedReqs = (ArrayList<Requirement>) this.getSatisfiedReqs(n);

        List<Requirement> faults = new ArrayList<>();

        //all the currently active node istances
        ArrayList<NodeIstance> istances = (ArrayList<NodeIstance>) this.activeNodes.values();
        boolean found = false;

        for(Requirement r : neededReqs){
            //if a requirmeent r is required but not satified there is a fault
            if(satisfiedReqs.contains(r) == false){
                
                //the requirement r it's not satisfied anymore, hence we check if there is another
                //node istance that can satisfy it, if there isn't we have a pending fault
                for(NodeIstance ist : istances){
                    if(this.getOfferedCaps(ist).contains(r.getName()) == true)
                        //we have found a node istance that can take care of r
                        found = true;
                }

                //a node istance offering the right capability was not found, hence pending fault
                if(found == false)
                    faults.add(r);
            }
            found = false;      
        }
        return faults;
    }

    /**
     * @return all the pending faults of the whole application, node istance by node istance
     */
    public Map<String, Fault> getPendingFaults(){

        //node istance id -> fault of that node istance
        //if needed the node istance is stored inside Fault
        Map<String, Fault> faults = new HashMap<>();

        //all the currently active node istances
        ArrayList<NodeIstance> istances = (ArrayList<NodeIstance>) this.activeNodes.values();
        
        for(NodeIstance ist : istances){
            faults.put(ist.getId(), new Fault(ist, this.getPendingFaults(ist)));
        }
        
        return faults;

    }

     /**
     * @param n node istance of 
     * @return list of requirements that the node istance is currently asking
     * @throws NullPointerException
     */
    public List<Requirement> getNeededReqs(NodeIstance n){
        assert n != null;
        return n.getNodeType().getMp().getRho().get(n.getCurrenState());
    }

    /**
     * @param n node istance of which it's asked the list of offered caps
     * @return list of capabilities offred by n
     * @throws NullPointerException
     */
    public List<String> getOfferedCaps(NodeIstance n){
        assert n != null;
        return n.getNodeType().getMp().getGamma().get(n.getCurrenState());
    }

}