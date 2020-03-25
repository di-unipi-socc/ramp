import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//represents the current runtime state of the applicaton 
public class GlobalState {
    Application app;

    //active NodeIstances: <NodeIstance unique id, NodeIstance>
    Map<String, NodeIstance> activeNodes;

    //set of runtime binding such as: 
    //n-> list of pair <Requirement r of n, n1 that satisfies r >
    Map<NodeIstance, List<Map.Entry<Requirement, NodeIstance>>> binding;

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
     * @return map of the NodeIstances currently active
     */
    public Map<String, NodeIstance> getActiveNodes(){
        return this.activeNodes;
    }

    /**
     * @return map of the runtime bindings between NodeIstances
     */
    public Map<NodeIstance, List<Map.Entry<Requirement, NodeIstance>>> getBinding(){
        return this.binding;
    }

    /**
     * @param n NodeIstance of which it's asked the list of satisfied reqs
     * @return list of the satisfied Requirement of n
     * @throws NullPointerException
     */
    public List<Requirement> getSatisfiedReqs(NodeIstance n){
        assert n != null;
        List<Requirement> satisfiedReqs = new ArrayList<>();
        
        //list of <Requirement of n, NodeIstance that satisfy r>
        List<Map.Entry<Requirement, NodeIstance>> nBindings = this.binding.get(n);

        //for each pair this extract just the satisfied requirement
        for(Map.Entry<Requirement, NodeIstance> e: nBindings){
            satisfiedReqs.add(e.getKey());
        }

        return satisfiedReqs;
    }

    /**
     * add a runtime binding such as <n, r, n1>
     * @param n NodeIstance asking for the Requirement r
     * @param r Requirement that is being asked by n
     * @param n1 NodeIstance that satisfy r with the correct capability
     * @throws NullPointerException
     */
    public void addBinding(NodeIstance n, Requirement r, NodeIstance n1){
        assert n != null;
        assert r != null;
        assert n1 != null;

        //get the value of the binding using the key n
        List<Map.Entry<Requirement, NodeIstance>> tmp = this.binding.get(n);

        if(tmp == null){ 
            //this is the first binding of n
            tmp = new ArrayList<>();
            tmp.add(new AbstractMap.SimpleEntry<Requirement, NodeIstance>(r, n1));
        }else
            //this is another binding of n
            tmp.add(new AbstractMap.SimpleEntry<Requirement, NodeIstance>(r, n1));
    
    }

    /**
     * remove a runtime binding such as <n, r, n1>
     * @param n NodeIstance that was asking for the Requirement r
     * @param r Requirement that was required
     * @param n1 NodeIstance that was satisfying r with the correct capability
     * @throws NullPointerException
     */
    public void removeBinding(NodeIstance n, Requirement r, NodeIstance n1){
        assert n != null;
        assert n1 != null;
        assert r != null;
        this.binding.remove(n);
    }

    /**
     * @param n NodeIstance of which it's asked the pending faults
     * @return list of Requirement of n that are not met 
     * @throws NullPointerException
     */
    public List<Requirement> getPendingFaults(NodeIstance n){
        assert n != null;
        ArrayList<Requirement> neededReqs = (ArrayList<Requirement>) this.getNeededReqs(n);
        ArrayList<Requirement> satisfiedReqs = (ArrayList<Requirement>) this.getSatisfiedReqs(n);

        List<Requirement> faults = new ArrayList<>();

        //if a requirmeent r is required but not satified there is a fault
        for(Requirement r : neededReqs){
            if(satisfiedReqs.contains(r) == false)
                faults.add(r);
        }
      
        return faults;
    }

     /**
     * @param n node istance of 
     * @return list of requirements that the NodeIstance is currently asking
     * @throws NullPointerException
     */
    public List<Requirement> getNeededReqs(NodeIstance n){
        assert n != null;
        return n.getMyNode().getMp().getRho().get(n.getCurrenState());
    }

    /**
     * @param n NodeIstance of which it's asked the list of offered caps
     * @return list of capabilities offred by n
     * @throws NullPointerException
     */
    public List<String> getOfferedCaps(NodeIstance n){
        assert n != null;
        return n.getMyNode().getMp().getGamma().get(n.getCurrenState());
    }

}