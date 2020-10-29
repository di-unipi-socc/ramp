package mprot.lib.model;

import mprot.lib.model.exceptions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


//represents the current runtime state of the applicaton 
public class GlobalState {
    Application app;

    // active node instances: <instance unique id, NodeInstance>
    Map<String, NodeInstance> activeNodeInstances;

    // <node instance "n" id -> list of runtime binding <req r of n, server instance of r>
    Map<String, List<RuntimeBinding>> runtimeBindings;

    /**
     * @param app application of which this is the global state
     * @throws NullPointerException
     */
    public GlobalState(Application app) {
        if (app == null)
            throw new NullPointerException("app null");

        this.app = app;

        this.activeNodeInstances = new HashMap<>();
        this.runtimeBindings = new HashMap<>();
    }

    /**
     * @return map of the node instances currently active
     */
    public Map<String, NodeInstance> getActiveNodeInstances() {
        return this.activeNodeInstances;
    }

    /**
     * @return map of the runtime bindings between node instances
     */
    public Map<String, List<RuntimeBinding>> getRuntimeBindings() {
        return this.runtimeBindings;
    }

    /**
     * @param instanceID node instance id of which it's asked the list of satisfied reqs
     * @return list of the satisfied requirement of instance
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnkownException
     */
    public List<Requirement> getSatisfiedReqs(String instanceID) 
        throws 
            NullPointerException,
            IllegalArgumentException, 
            InstanceUnknownException
    {
        
        NodeInstance instance = this.getNodeInstanceByID(instanceID);

        Node instanceType = instance.getNodeType();
        List<Requirement> satisfiedReqs = new ArrayList<>();

        // runtime bindings of instance
        ArrayList<RuntimeBinding> instanceRunBindings = (ArrayList<RuntimeBinding>) this.runtimeBindings.get(instanceID);

        for (RuntimeBinding runBinding : instanceRunBindings) {

            StaticBinding reqStaticBinding = new StaticBinding(instanceType.getName(), runBinding.getReq().getName());
            StaticBinding capStaticBinding = this.app.getBindingFunction().get(reqStaticBinding);

            if (capStaticBinding != null) {
                // the serving instance that is currently helding the runtime binding with instance
                NodeInstance servingInstance = this.activeNodeInstances.get(runBinding.getNodeInstanceID());

                // the serving instance is the right kind of Node?
                boolean servingInsRightNodeType = servingInstance.getNodeType().getName().equals(capStaticBinding.getNodeName());

                // the serving instance is currently offering the right cap of instance?
                boolean servingInsOfferingRightCap = servingInstance.getOfferedCaps().contains(capStaticBinding.getCapOrReq());

                if (servingInsOfferingRightCap == true && servingInsRightNodeType == true)
                    // the requirement is trurly satisfied
                    satisfiedReqs.add(runBinding.getReq());
            }
        }

        return satisfiedReqs;
    }

    /**
     * @param instanceID node instance ID of which we want to remove the old bindings
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownExecption
     */
    public void removeOldBindings(String instanceID) 
        throws 
            NullPointerException,
            IllegalArgumentException,
            InstanceUnknownException
    {

        NodeInstance instance = this.getNodeInstanceByID(instanceID);
        
        // if a req is satisfied but not needed it has to be removed
        for (Requirement satisfiedReq : this.getSatisfiedReqs(instanceID)) {
            if (satisfiedReq.isContainment() == false && instance.getNeededReqs().contains(satisfiedReq) == false)
                this.removeRuntimeBinding(instanceID, satisfiedReq);
        }
    }

    /**
     * @param askingInstanceID node instance id of which we want all the instance that can satisfy req
     * @param req requirement of which we want the capable instance
     * @return list of all instances that can take care of <askingInstance, req>
     * @throws NullPointerException
     * @throws IllegalArgumentException 
     * @throws InstanceUnknownException
     */
    public List<NodeInstance> getCapableInstances(String instanceID, Requirement req)
        throws 
            NullPointerException,
            IllegalArgumentException,
            InstanceUnknownException 
    {
        NodeInstance askingInstance = this.getNodeInstanceByID(instanceID);

        List<NodeInstance> capableInstances = new ArrayList<>();

        StaticBinding reqStaticBinding = new StaticBinding(askingInstance.getNodeType().getName(), req.getName());
        StaticBinding capStaticBinding = this.app.getBindingFunction().get(reqStaticBinding);

        if (capStaticBinding != null) {
            Collection<NodeInstance> activeInstancesCollection = this.activeNodeInstances.values();
            ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);

            // for each instance check if it is the right kind of node and if it currently
            // offering the right capability for <askingInstance, req>
            for (NodeInstance instance : activeInstances) {
                boolean instanceRightType = instance.getNodeType().getName().equals(capStaticBinding.getNodeName());
                boolean instanceOfferingRightCap = instance.getOfferedCaps().contains(capStaticBinding.getCapOrReq());

                if (instanceRightType == true && instanceOfferingRightCap == true)
                    capableInstances.add(instance);
            }
        }
        return capableInstances;
    }

    /**
     * @param instanceID node instance (id) that needs new bindings since it had a change of state
     * @throws NullPointerException
     * @throws InstanceUnknownException
     * @throws IllegalArgumentException
     */
    public void addNewBindings(String instanceID) 
        throws 
            NullPointerException, 
            IllegalArgumentException,
            InstanceUnknownException
    {
        NodeInstance instance = this.getNodeInstanceByID(instanceID);
        
        //for each (non containment) needed requirement check it is satisfied, if not create the right binding
        for (Requirement neededReq : instance.getNeededReqs()) {

            if (neededReq.isContainment() == false && this.getSatisfiedReqs(instanceID).contains(neededReq) == false) {
                NodeInstance capableInstance = this.app.greedyPI(instanceID, neededReq);
                if(capableInstance != null)
                    this.addBinding(instanceID, neededReq, capableInstance.getID());  
            }          
        }
    }

    /**
     * add a runtime binding such as <askingInstance, req, servingInstance>
     * @param askingInstanceID node instance (id) asking for the requirement req
     * @param req requirement that is being asked by askingInstance
     * @param servingInstance node istance (id) that satisfy req with the correct capability
     * @throws NullPointerException
     */
    public void addBinding(String askingInstanceID, Requirement req, String servingInstanceID) 
        throws 
            NullPointerException,
            IllegalArgumentException,
            InstanceUnknownException
    {
        //TODO: questo e' pubblico ma non viene controllato che 
        //serving instance offra la giusta cap, che askingInstance richieda req
        //controlli saltati? Questo crea binding anche sbagliati se gli si passa
        //roba a caso
        //OPPURE questi controlli li faccio da altre parti?

        NodeInstance askingInstance = this.getNodeInstanceByID(askingInstanceID);
        NodeInstance servingInstance = this.getNodeInstanceByID(servingInstanceID);

        this.runtimeBindings.get(askingInstance.getID()).add(new RuntimeBinding(req, servingInstance.getID()));
    }

    /**
     * remove a runtime binding such as <n, r, *>
     * @param instanceID node instance (id) that was asking for the requirement req
     * @param req requirement that was required
     * @throws NullPointerException
     * @throws IllegalArgumentExeption
     * @throws InstanceUnknownException
     */
    public void removeRuntimeBinding(String instanceID, Requirement req) 
        throws 
            NullPointerException,
            IllegalArgumentException,
            InstanceUnknownException
    {
        this.checkNodeInstanceExistance(instanceID);
        
        if(req == null)
            throw new NullPointerException("req null");

        RuntimeBinding badBinding = null;

        ArrayList<RuntimeBinding> instanceRunBindings = (ArrayList<RuntimeBinding>) this.runtimeBindings.get(instanceID);
        
        for (RuntimeBinding runBinding : instanceRunBindings) {
            if(runBinding.getReq().equals(req) == true)
                //we have <instance, req, *> so we remove it
                badBinding = runBinding;
        }

        if(badBinding != null)
            instanceRunBindings.remove(badBinding);  

    }

    /**
     * given a node instance n this remove all the binding such as <n, *, *> and <*, *, n>
     * @param targetID node instance (id) whose bindings are to be deleted. delete all bindings of the target
     * @throws InstanceUnknownException
     * @throws IllegalArgumentException
     * @throws NullPoninterException
     */
    public void removeAllBindingsBothWays(String targetInstanceID)
        throws 
            NullPointerException, 
            IllegalArgumentException
    {

        if(targetInstanceID == null)
            throw new NullPointerException("targetInstanceID null");
        if(targetInstanceID.isEmpty() == true)
            throw new IllegalArgumentException("targetInstanceID empty");

        //direct way: targetInstance -> list of runtime bindings with other instances
        if(this.activeNodeInstances.containsKey(targetInstanceID) == false)
            //targetInstance was destroyed with a scaleIn
            this.runtimeBindings.remove(targetInstanceID);
        else
            //for some reason all bindings of targetInstance must be eliminated  
            this.runtimeBindings.replace(targetInstanceID, new ArrayList<>());
        
        //other way; delete those runtime bindings that have targetInstance as the server of a requirement
        ArrayList<RuntimeBinding> otherWayBadBindings = new ArrayList<>();

        Collection<NodeInstance> activeInstancesCollection =  this.activeNodeInstances.values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);

        //for each active instance check if among its bindings the target instance is the server 
        for (NodeInstance activeInstance : activeInstances) {
            ArrayList<RuntimeBinding> activeInstanceRunBindings = (ArrayList<RuntimeBinding>) this.runtimeBindings.get(activeInstance.getID());
            
            for(RuntimeBinding runBinding : activeInstanceRunBindings){ 
                if(runBinding.getNodeInstanceID().equals(targetInstanceID))
                    otherWayBadBindings.add(runBinding);   
            }
            
            for(RuntimeBinding badBinding : otherWayBadBindings)
                activeInstanceRunBindings.remove(badBinding);
        }   
        
    }

    /**
     * pending fault: a node instance require a requirement and there isn't a node
     * instance offering that capability
     * @param instance node instanceID of which it's asked the pending faults
     * @return list of requirement of instance that are not met
     * @throws NullPointerException
     * @throws InstanceUnknownException
     * @throws IllegalArgumentException
     */
    public List<Fault> getPendingFaults(String instanceID)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {

        NodeInstance instance = this.getNodeInstanceByID(instanceID);
        List<Fault> faults = new ArrayList<>();

        //for each neededReq check that it is also satisfied,
        //if a needed requirement is not satisfied we have a fault
        for(Requirement neededReq : instance.getNeededReqs()){
            if(this.getSatisfiedReqs(instanceID).contains(neededReq) == false)
                faults.add(new Fault(instanceID, neededReq));
        }

        return faults;
    }

    /**
     * @return list of all the pending faults of the whole application
     * @throws InstanceUnknownException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    public List<Fault> getPendingFaults()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
       List<Fault> faults = new ArrayList<>();

        Collection<NodeInstance> activeInstancesCollection =  this.activeNodeInstances.values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);
        
        //for each instance get the list of failed requirement, then we add <instance, failed req> to the list of all faults
        for(NodeInstance instance : activeInstances){
            ArrayList<Fault> instancePendingFaults = (ArrayList<Fault>) this.getPendingFaults(instance.getID());
            faults.addAll(instancePendingFaults);
        }
        return faults;
    }

    /**
     * @param instance node instance (id) of which we want to know if he is a broken instance
     * @return true if instance is a broken instance, false otherwise
     * @throws NullPointerException
     * @throws InstanceUnknownException
     * @throws IllegalArgumentException
     */
    public boolean isBrokenInstance(String instanceID)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        
        NodeInstance instance = this.getNodeInstanceByID(instanceID);
        boolean res = false;
        
        /**
         * we check if instance has a containment requirement. 
         * If so we get all the bindings of instance. 
         *  - if there is not a binding (among the existing bindings) that involves the containment requirement we have a broken instance
         *  - if the binding of the containment requirement involves a nodeInstance container that is no longer active 
         *    we have a broken instance
         *  - if there are no binding we have a broken instance
         */

        ArrayList<RuntimeBinding> instanceRuntimeBindings = (ArrayList<RuntimeBinding>) this.runtimeBindings.get(instanceID);
        int check = 0;
        
        ArrayList<Requirement> instanceReqs = (ArrayList<Requirement>) instance.getNeededReqs();

        for (Requirement req : instanceReqs) {
            if(req.isContainment() == true){
                
                //there is no binding, hence there is not a containment binding, hence broken instance
                if(instanceRuntimeBindings.size() == 0)
                    return true;

                for(RuntimeBinding binding : instanceRuntimeBindings){
                    if(binding.getReq().equals(req)){
                        //this is the binding to check
                        check ++;
                        NodeInstance container = null;
                        try {
                            container = this.getNodeInstanceByID(binding.getNodeInstanceID());
                        } catch (InstanceUnknownException e) {
                            container = null;
                        }

                        //container is null, broken instance
                        if(container == null)
                            return true;
                    } 
                }

                //among bindings there is not a binding that involves the containment req
                if(check == 0)
                    return true;

                break;
            }
        }
        return res;
    }

    /**
     * broken instance: a node instance have a "contain" relation with a node
     * instance that is no longer alive
     * @return list of node instances that have a broken instance
     * @throws InstanceUnknownException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    public List<NodeInstance> getBrokeninstances()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        List<NodeInstance> brokeninstances = new ArrayList<>();

        Collection<NodeInstance> activeInstancesCollection =  this.activeNodeInstances.values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);

        for(NodeInstance instance : activeInstances){
            if(this.isBrokenInstance(instance.getID()) == true)
                brokeninstances.add(instance);
        }
        return brokeninstances;    
    }

    /**
     * @param fault fault of which we want to know if it is resolvable
     * @return true if fault is resolvable
     * @throws NullPointerException
     * @throws InstanceUnknownException
     * @throws IllegalArgumentException
     */
    public boolean isResolvableFault(Fault fault)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        if(fault == null)
            throw new NullPointerException("fault null");

        boolean res = false;

        //all active node instances
        Collection<NodeInstance> activeInstancesCollection =  this.activeNodeInstances.values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);
        
        //data extracted from fault
        NodeInstance faultedInstance = this.getNodeInstanceByID(fault.getInstanceID());
        Node faultedNodeInstanceType = faultedInstance.getNodeType();
        Requirement faultedReq = fault.getReq();

        StaticBinding reqStaticBinding = new StaticBinding(faultedNodeInstanceType.getName(), faultedReq.getName());
        StaticBinding capStaticBinding =  this.app.getBindingFunction().get(reqStaticBinding);

        //a fault can be resolvable only if it is replica unware
        if(faultedReq.isReplicaUnaware() == true){

            //for each active instance check if it offer the right capability to resolve the fault
            for(NodeInstance instance : activeInstances){

                //a static binding might not be defined
                if(capStaticBinding != null){
                    //instance is the right kind of Node?
                    boolean instanceRightType = instance.getNodeType().getName().equals(capStaticBinding.getNodeName());
                    //instance is currently offering the right cap of instance?
                    boolean instanceOfferingRightCap = instance.getOfferedCaps().contains(capStaticBinding.getCapOrReq()); 

                    if(instanceOfferingRightCap == true && instanceRightType == true){
                        res = true;
                        break;
                    }
                }
            }
        }   
        return res;
    }

    /**
     * @param instanceID node instance (id) of which we want the resolvable faults
     * @return list of resolvable faults of n
     * @throws NullPointerException
     * @throws InstanceUnknownException
     * @throws IllegalArgumentException
     */
    public List<Fault> getResolvableFaults(String instanceID)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        this.checkNodeInstanceExistance(instanceID);
    
        List<Fault> resolvableFaults = new ArrayList<>();
        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) this.getPendingFaults(instanceID);

        for(Fault f : pendingFaults){
            if(this.isResolvableFault(f) == true)
                resolvableFaults.add(f);
        }

        return resolvableFaults;
    }

    /**
     * @return list of all the resolvable faults of the whole applicaton
     * @throws InstanceUnknownException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     */
    public List<Fault> getResolvableFaults()
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException 
    {
        List<Fault> resolvableFault = new ArrayList<>();

        Collection<NodeInstance> activeInstancesCollection =  this.activeNodeInstances.values();
        ArrayList<NodeInstance> activeInstances = new ArrayList<>(activeInstancesCollection);

        for(NodeInstance n : activeInstances)
            resolvableFault.addAll(this.getResolvableFaults(n.getID()));
        
        return resolvableFault;
    }

    public NodeInstance getNodeInstanceByID(String instanceID)
        throws
            NullPointerException,
            IllegalArgumentException,
            InstanceUnknownException
    {

        if(instanceID == null)
            throw new NullPointerException("instanceID null");
        if(instanceID.isEmpty() == true)
            throw new IllegalArgumentException("instanceID empty");


        NodeInstance ret = this.activeNodeInstances.get(instanceID);

        if(ret == null){
            //fail("OOOOOO");
            throw new InstanceUnknownException("instanceID: " + instanceID + " is not matched with an instance" );
        }
        
        return ret;
    }

    public void checkNodeInstanceExistance(String instanceID) 
        throws 
            InstanceUnknownException,
            NullPointerException, 
            InstanceUnknownException 
    {
        if(instanceID == null)
            throw new NullPointerException("instanceID null");
        if(instanceID.isEmpty() == true)
            throw new IllegalArgumentException("instanceID empty");
        if(this.activeNodeInstances.get(instanceID) == null)
            throw new InstanceUnknownException("instanceID not matched with an instance");

    }
}