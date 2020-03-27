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
    //id of node istance n-> list of bond <Requirement r of n, unique id of n1 that satisfies r>
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
            tmp.add(new Bond(r, n1.getId()));
        }else
            //this is another binding of n
            tmp.add(new Bond(r, n1.getId()));
    
    }

    /**
     * @param n node istance of which we want a list of all its requirement that are not met
     * @return list of requirement of n that are not met
     */
    public List<Requirement> getRequirementsNotMet(NodeIstance n){
        List<Requirement> reqsNotMet = new ArrayList<>();

        ArrayList<Requirement> neededReqs = (ArrayList<Requirement>) this.getNeededReqs(n);
        ArrayList<Requirement> satisfiedReqs = (ArrayList<Requirement>) this.getSatisfiedReqs(n);

        for(Requirement r : neededReqs){
            if(satisfiedReqs.contains(r) == false)
                reqsNotMet.add(r);
            
        }
        return reqsNotMet;
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
     * pending fault: a node istance require a requirement and there isn't a node
     * istance offering that capability
     * @param n node istance of which it's asked the pending faults
     * @return list of requirement of n that are not met 
     * @throws NullPointerException
     */
    public List<Requirement> getPendingFaults(NodeIstance n){
        assert n != null;

        ArrayList<Requirement> reqsNotMet = (ArrayList<Requirement>) this.getRequirementsNotMet(n);
        List<Requirement> faults = new ArrayList<>();

        //all the currently active node istances
        ArrayList<NodeIstance> istances = (ArrayList<NodeIstance>) this.activeNodes.values();
        boolean found = false;

        for(Requirement r : reqsNotMet){  
                //the requirement r it's not satisfied anymore, hence we check if there is another
                //node istance that can satisfy it, if there isn't we have a pending fault
                for(NodeIstance ist : istances){
                    if(this.getOfferedCaps(ist).contains(r.getName()) == true)
                        //we have found a node istance that can take care of r
                        found = true;

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
    public Map<String, PendingFault> getPendingFaults(){

        //node istance id -> fault of that node istance
        //if needed the node istance is stored inside Fault
        Map<String, PendingFault> faults = new HashMap<>();

        //all the currently active node istances
        ArrayList<NodeIstance> istances = (ArrayList<NodeIstance>) this.activeNodes.values();
        
        for(NodeIstance ist : istances){
            faults.put(ist.getId(), new PendingFault(ist, this.getPendingFaults(ist)));
        }
        
        return faults;

    }

    /**
     * @param n node istance of which we want to know if it he has a broken istance
     * @return true if n has a broken istance, false otherwise
     * @throws NullPointerException
     */
    public boolean haveBrokenIstance(NodeIstance n){
        assert n != null;
        boolean res = false;

        //all bonds of n
        List<Bond> bondsOfN = this.binding.get(n.getId());

        //for each bond we check if this is a containment relation
        for(Bond b : bondsOfN){
            if(b.getReq().getName().equals("containment") == true){

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
     * broken istance: a node istance have a "contain" relation with a 
     * node istance that is no longer alive
     * @return list of node istance id(s) that have a broken istance
     */
    public List<String> getBrokenIstances(){
        List<String> brokenIstances = new ArrayList<>();
        List<NodeIstance> activeNodes = (ArrayList<NodeIstance>) this.activeNodes.values();

        for(NodeIstance n : activeNodes){
            if(this.haveBrokenIstance(n) == true)
                brokenIstances.add(n.getId());
        }

        return brokenIstances;    
    }

    //per ogni requirement fallito di n associa la lista di nodi che lo risolvono
    public List<ResolvableFault> getResolvableFaults(NodeIstance n){
        assert n != null;
        List<ResolvableFault> resolvableFaults = new ArrayList<>();

        //requisiti di n non soddisfatti
        ArrayList<Requirement> reqsNotMet = (ArrayList<Requirement>) this.getRequirementsNotMet(n);
        
        //tutti i nodi attivi
        ArrayList<NodeIstance> activeNodes = (ArrayList<NodeIstance>) this.activeNodes.values();


        for(Requirement r : reqsNotMet){
            if(r.isReplicaUnaware() == true){
                List<String> capableNodeIstance = new ArrayList<>();
                for(NodeIstance n1 : activeNodes){
                    if(this.getOfferedCaps(n1).contains(r.getName()) == true)
                        capableNodeIstance.add(n1.getId());
                }
                if(capableNodeIstance.isEmpty() == false)
                    resolvableFaults.add(new ResolvableFault(r, capableNodeIstance));
                
            }
        }
        return resolvableFaults;
    }

    public Map<String, List<ResolvableFault>> getResolvableFaults(){
        Map<String, List<ResolvableFault>> resolvableFault = new HashMap<>();

        ArrayList<NodeIstance> activeNodes = (ArrayList<NodeIstance>) this.activeNodes.values();
        for(NodeIstance n : activeNodes){
            resolvableFault.put(n.getId(), this.getResolvableFaults(n));
        }
        return resolvableFault;
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