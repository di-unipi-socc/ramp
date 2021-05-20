package unipi.di.socc.ramp.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unipi.di.socc.ramp.core.model.exceptions.*;

public class GlobalState {
    
    private Application app;
    private final Map<String, NodeInstance> activeInstances;
    private final Map<String, List<RuntimeBinding>> runtimeBindings;

    public GlobalState(Application app) throws NullPointerException{
        if(app == null)
            throw new NullPointerException();

        this.app = app;
        this.activeInstances = new HashMap<String, NodeInstance>();
        this.runtimeBindings = new HashMap<String, List<RuntimeBinding>>();
    }

    public Application getApp() {
        return app;
    }
    public Map<String, List<RuntimeBinding>> getRuntimeBindings() {
        return runtimeBindings;
    }   
    public Map<String, NodeInstance> getActiveInstances() {
        return activeInstances;
    }
    public NodeInstance getNodeInstanceByID(String instanceID)
        throws
            NullPointerException,
            IllegalArgumentException, 
            InstanceUnknownException
    {
        if(instanceID == null)
            throw new NullPointerException();
        if(instanceID.isBlank())
            throw new IllegalArgumentException();

        NodeInstance instance = this.activeInstances.get(instanceID);
        if(instance == null)
            throw new InstanceUnknownException();

        return instance;
    }

    // needed just for parsing with GSON
    public void setApplication(Application app){
        this.app = app;
    }

    /**
     * @param instanceID id of the instance of which we want the satisfied requirement
     * @return list of requirements that are currently satisfied
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    public List<Requirement> getSatisfiedReqs(String instanceID)
        throws
            NullPointerException,
            IllegalArgumentException, 
            InstanceUnknownException
    {
        Node instanceType = this.getNodeInstanceByID(instanceID).getNodeType();
        List<RuntimeBinding> instanceRuntimeBindings = this.runtimeBindings.get(instanceID);

        List<Requirement> satisfiedReqs = new ArrayList<>();

        for(RuntimeBinding rb : instanceRuntimeBindings){
            NodeReq nodeReq = new NodeReq(instanceType.getName(), rb.getReq().getName());
            NodeCap nodeCap = this.app.getBindingFunction().get(nodeReq);

            //the binding is defined in the static topology
            if(nodeCap != null){ 
                NodeInstance server = this.getActiveInstances().get(rb.getNodeInstanceID());
                //the server is actually active and wasnt killed
                if(server != null){
                    /**
                     * the global state (the runtime state of the app) could not be correct
                        * it is not enough to have a runtime binding to have a satisfied requirement
                    */
                    boolean serverRightType = nodeCap.getNodeName().equals(server.getNodeType().getName());
                    boolean serverOfferingCap = server.getOfferedCaps().contains(nodeCap.getCap());

                    if(serverOfferingCap && serverRightType)
                        satisfiedReqs.add(rb.getReq());
                }
            }
        }
        return satisfiedReqs;
    }

    /**
     * @param instanceID identifier of the instance
     * @param req requirement of the instance
     * @return the list of all the activeInstances that can satisfy the requirement (might be empty)
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
        List<NodeInstance> capableactiveInstances = new ArrayList<>();

        NodeReq nodeReq = new NodeReq(this.getNodeInstanceByID(instanceID).getNodeType().getName(), req.getName());
        NodeCap nodeCap = this.app.getBindingFunction().get(nodeReq);

        if(nodeCap != null){            
            /**
             * among all the currently active activeInstances we check if the active instance is the right kind of node 
             * and if it is currently offering the needed capability. If so the instance is a "capable instance", 
             * whereas it could satisfy the requirement of the given instance
             */
            for(NodeInstance activeInstance : this.activeInstances.values()){
                boolean instanceRightType = nodeCap.getNodeName().equals(activeInstance.getNodeType().getName());
                boolean instanceOfferingCap = activeInstance.getOfferedCaps().contains(nodeCap.getCap());

                if(instanceOfferingCap && instanceRightType)
                    capableactiveInstances.add(activeInstance);
            }
        }
        return capableactiveInstances;
    }

    //######################### RUNTIME BINDING MANIPULATION ############################
    /**
     * @param instanceID identifier of the instance
     * @param req requirement which we want to remove the runtime binding
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    public void removeRuntimeBinding(String instanceID, Requirement req)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        //to check instance existance
        this.getNodeInstanceByID(instanceID);

        if(req == null)
            throw new NullPointerException();

        RuntimeBinding badBinding = null;
        //this is the list of all <server instance, requirement handled by server instance>
        List<RuntimeBinding> instanceRuntimeBindings = this.runtimeBindings.get(instanceID);

        for(RuntimeBinding runBinding : instanceRuntimeBindings){
            //this binding is about the target requirement
            if(runBinding.getReq().equals(req))
                badBinding = runBinding;
        }

        //cant make a remove inside an iterator
        if(badBinding != null)
            instanceRuntimeBindings.remove(badBinding);
    }

    /**
     * @param instanceID identifier of the instance of which we want to remove the old bindings, 
     *        where "old" stands for "not needed anymore"
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    public void removeOldRuntimeBindings(String instanceID)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        NodeInstance instance = this.getNodeInstanceByID(instanceID);
        List<Requirement> neededReqs = instance.getNeededReqs();
        for(Requirement satisfiedReq : this.getSatisfiedReqs(instanceID)){
            if(!satisfiedReq.isContainment() && !neededReqs.contains(satisfiedReq))
                this.removeRuntimeBinding(instanceID, satisfiedReq);
        }
    }

    /**
     * @param instanceID identifier of the instance of which we want to delete all the runtime bindings
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    public void removeAllRuntimeBindings(String instanceID)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        this.getNodeInstanceByID(instanceID);
        this.runtimeBindings.get(instanceID).clear();
    }

    /**
     * @param instanceID identifier of the to-be-dead instance about we want to remove all the runtim binding about
     *        it, both where it is has a served requirement or where it is currently offering a capability for another
     *        instance
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    public void removeAllRuntimeBindingsBothWays(String instanceID)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        this.getNodeInstanceByID(instanceID);

        //direct way: remove all the runtime bindings about the requirement of instance(ID)
        this.runtimeBindings.get(instanceID).clear();

        /**
         * reverse way: we remove all the runtkme bindings that have instance(ID) as a server of a capability
         * for each active instance i we check all of its runtime binding, when
         * we find a runtime binding that has instanceID as a server we remove the binding
         */
        List<RuntimeBinding> badBindings = new ArrayList<>();
        for(NodeInstance activeInstance : this.activeInstances.values()){
            List<RuntimeBinding> activeInstanceRunBindings = this.runtimeBindings.get(activeInstance.getID());

            for(RuntimeBinding activeInsanceRunBinding : activeInstanceRunBindings){
                //instance(ID) is a server for another instance
                if(activeInsanceRunBinding.getNodeInstanceID().equals(instanceID))
                    badBindings.add(activeInsanceRunBinding);
            }

            for(RuntimeBinding badBinding : badBindings)
                activeInstanceRunBindings.remove(badBinding);
                
            badBindings.clear();
        }
    }

    /**
     * @param instanceID the identifier of the instance to which add a runtime binding
     * @param req the requirement of which the binding is about
     * @param serverID the identifier of the instance that satisfy the requirement
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    public void addRuntimeBinding(String instanceID, Requirement req, String serverID)
        throws
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        this.getNodeInstanceByID(instanceID);
        this.getNodeInstanceByID(serverID);
        if(req == null)
            throw new NullPointerException();
            
        this.runtimeBindings.get(instanceID).add(new RuntimeBinding(req, serverID));
    }

    /**
     * @param instanceID identifier of the instance of which we try to satisfy all the needed requirement
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    public void addNewRuntimeBindings(String instanceID)
        throws
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        NodeInstance instance = this.getNodeInstanceByID(instanceID);
        List<Requirement> satisfiedReq = this.getSatisfiedReqs(instanceID);

        for(Requirement neededReq : instance.getNeededReqs()){
            //the needed requirement is not containmenet and it is not satisfied
            if(!neededReq.isContainment() && !satisfiedReq.contains(neededReq)){
                NodeInstance server = this.app.pi(instanceID, neededReq);
                //we satisfy what we can, but it is possible to have some not satisfied reqs
                if(server != null)
                    this.addRuntimeBinding(instanceID, neededReq, server.getID());
            }
        }
    }

    //############################ FAULT HANDLING ##############################
    /**
     * @param instanceID identifier of the instance of which we want the pending faults
     * @return list of pending faults
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    public List<Fault> getPendingFaults(String instanceID)
        throws
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        NodeInstance instance = this.getNodeInstanceByID(instanceID);
        List<Fault> pendingFaults = new ArrayList<>();

        //for each needed requirement (non containment), if it is not satisfied we have a fault
        for(Requirement neededReq : instance.getNeededReqs()){
            // if(!neededReq.isContainment() && !this.getSatisfiedReqs(instanceID).contains(neededReq))
            //     pendingFaults.add(new Fault(instanceID, neededReq));

            if(!neededReq.isContainment()){
                boolean satisfied = false;
                for(Requirement r : this.getSatisfiedReqs(instanceID)){
                    if(r.getName().equals(neededReq.getName()))
                        satisfied = true;
                }

                if(!satisfied)
                    pendingFaults.add(new Fault(instanceID, neededReq));

            }

        }

        return pendingFaults;
    }

    /**
     * @return list of all the pending faults in the global state
     */
    public List<Fault> getPendingFaults()
        throws
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        List<Fault> pendingFaults = new ArrayList<>();

        for(NodeInstance activeInstance : this.activeInstances.values())
            pendingFaults.addAll(this.getPendingFaults(activeInstance.getID()));

        return pendingFaults;
    }

    /**
     * @param instanceID identifier of the instance of which we want to know if it is a broken instance
     * @return true if the container of the instance is not active (hence instance is broken)
     * @throws InstanceUnknownException
     * @throws IllegalArgumentException
     * @throws NullPointerException
     * @throws MissingContainmentRuntimeBindingException
     */
    public boolean isBrokenInstance(String instanceID) 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        NodeInstance instance = this.getNodeInstanceByID(instanceID);
        Requirement containmentReq = null;

        for(Requirement req : instance.getNodeType().getReqs()){
            if(req.isContainment())
                containmentReq = req;
        }

        //instance has not a containment req, so it cant be a broken instance
        if(containmentReq == null)
            return false;

        List<RuntimeBinding> instanceRuntimeBindings = this.runtimeBindings.get(instanceID);
        for(RuntimeBinding runtimeBinding : instanceRuntimeBindings){
            if(runtimeBinding.getReq().equals(containmentReq)){
                //destroyed container
                if(this.activeInstances.get(runtimeBinding.getNodeInstanceID()) == null)
                    return true;
                else
                    return false;
            }
        }
        //if here, no vertical binding was involving instance, which is hence broken
        return true;
    }

    /**
     * 
     * @return list of all the broken instances in the global state
     * @throws MissingContainmentRuntimeBindingException
     */
    public List<NodeInstance> getBrokenInstances() 
        throws
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        List<NodeInstance> brokenInstances = new ArrayList<>();

        for(NodeInstance instance : this.activeInstances.values())
            if(this.isBrokenInstance(instance.getID()))
                brokenInstances.add(instance);

        return brokenInstances;
    }

    /**
     * @param fault the fault that we want to know if it is resolvable
     * @return true if the fault is resolvableß
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    public boolean isResolvableFault(Fault fault)
        throws
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        if(fault == null)
            throw new NullPointerException();

        NodeInstance instance = this.getNodeInstanceByID(fault.getNodeInstanceID());        
        
        //only replica unaware reqs can be resolvable 
        if(!fault.getReq().isReplicaUnaware())
            return false;

        NodeReq nodeReq = new NodeReq(instance.getNodeType().getName(), fault.getReq().getName());
        NodeCap nodeCap = this.app.getBindingFunction().get(nodeReq);

        if(nodeCap != null){
            for(NodeInstance activeInstance : this.activeInstances.values()){
                
                boolean instanceRightType = nodeCap.getNodeName().equals(activeInstance.getNodeType().getName());
                boolean instanceOfferingCap = activeInstance.getOfferedCaps().contains(nodeCap.getCap());

                //found at least one node instance that can resolve the fault
                if(instanceOfferingCap && instanceRightType)
                    return true;
                
            }
        }
        return false;
    }

    /**
     * @param instanceID identifier of the instance of which we want the resolvable faults
     * @return list of the resolvable faults of instance(ID)
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @throws InstanceUnknownException
     */
    public List<Fault> getResolvableFaults(String instanceID)
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException
    {
        this.getNodeInstanceByID(instanceID);

        List<Fault> resolvableFaults = new ArrayList<>();

        for(Fault fault : this.getPendingFaults(instanceID)){
            if(this.isResolvableFault(fault))
                resolvableFaults.add(fault);
        }

        return resolvableFaults;
    }

    /**
     * @return the list fo all the resolvable faults in the global state
     */
    public List<Fault> getResolvableFaults()
        throws
            NullPointerException, 
            IllegalArgumentException, 
            InstanceUnknownException    
    {
        List<Fault> resolvableFaults = new ArrayList<>();

        for(NodeInstance activeInstance : this.activeInstances.values())
            resolvableFaults.addAll(this.getResolvableFaults(activeInstance.getID()));

        return resolvableFaults;
    }

    @Override
    public boolean equals(Object obj){
        GlobalState check = (GlobalState) obj;

        //dont need to check if the application is the same
        //it could be usefull to see if two different app have equals gs

        return 
            this.activeInstances.equals(check.activeInstances) &&
            this.runtimeBindings.equals(check.runtimeBindings)
        ;

    }

}
