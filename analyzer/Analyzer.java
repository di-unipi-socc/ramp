package analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import exceptions.FailedOperationException;
import exceptions.IllegalSequenceElementException;
import exceptions.NodeUnknownException;
import exceptions.OperationNotAvailableException;
import exceptions.RuleNotApplicableException;
import model.Application;
import model.Fault;
import model.GlobalState;
import model.ManagementProtocol;
import model.Node;
import model.NodeInstance;
import model.Requirement;
import model.RuntimeBinding;
import model.StaticBinding;
import model.Transition;

/**
 * idea del metodo analisi della sequenza con /pi deterministica algoritmo
 * ricorsivo che prende una lista di coppie <app, op> cioe' <app su cui
 * applicare op, op>
 * 
 * -> per la biforcazione dei fault ci serve una deep copy
 */

public class Analyzer {

    public Analyzer() throws NullPointerException {
    }

    public boolean isValidSequence(Application app, List<SequenceElement> sequence)
        throws 
            IllegalSequenceElementException, 
            NullPointerException 
    {
        if (app == null)
            throw new NullPointerException();
        if (this.wellFormattedSequence(sequence) == false)
            throw new IllegalSequenceElementException();

        if (sequence.isEmpty() == true)
            return true;

        SequenceElement seqElement = sequence.remove(0);
        try {
            this.executeSequenceElement(app, seqElement);
        } catch (Exception e) {
            return false;
        }

        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        if(pendingFaults.isEmpty() == true && brokenInstances.isEmpty() == true)
            //there is no biforcation with a deterministic pi, proceeding
            return this.isValidSequence(app, sequence);
        else{
            //there are some faults. there is a biforcation and the sequence must be tested for both branches
            //the not-handled-fault branch goes frist (might fail faster)
            Application cloneApp = this.cloneApp(app);

            if(this.isValidSequence(this.cloneApp(app), sequence) == false)
                return false;
            
            //now we explore the fault handled branch
            //first goes scaleIn that implies the no-broken-instances
            cloneApp = this.cloneApp(app);
            try{
                cloneApp.scaleIn(brokenInstances.get(0));
            }catch(Exception E){
                return false;
            }

            if(this.isValidSequence(cloneApp, sequence) == false)
                return false;

            //for each fault check both the handled and not handled branch
            for (Fault f : pendingFaults) {
                cloneApp = this.cloneApp(app);

                if(app.getGlobalState().isResolvableFault(f) == true){
                    try {
                        cloneApp.autoreconnect(cloneApp.getGlobalState().getActiveNodeInstances().get(f.getInstanceID()), f.getReq());
                    } catch (Exception e) {
                        //TODO: handle exception
                    }
                    
                    if(this.isValidSequence(cloneApp, sequence) == false)
                        return false;  

                }else{
                    try {
                        cloneApp.fault(cloneApp.getGlobalState().getActiveNodeInstances().get(f.getInstanceID()), f.getReq()); 
                    } catch (Exception e) {
                        //TODO: handle exception
                    }
                    
                    if(this.isValidSequence(cloneApp, sequence) == false)
                        return false;  
                }
            }
        }
        return true;
    }

    public boolean isWeaklyValidSequence(Application app, List<SequenceElement> sequence)
        throws 
            IllegalSequenceElementException, 
            NullPointerException 
    {
        if (app == null)
            throw new NullPointerException();
        if (this.wellFormattedSequence(sequence) == false)
            throw new IllegalSequenceElementException();

        if (sequence.isEmpty() == true)
            return true;

        SequenceElement seqElement = sequence.remove(0);
        try {
            this.executeSequenceElement(app, seqElement);
        } catch (Exception e) {
            return false;
        }
       
        ArrayList<Fault> pendingFaults = (ArrayList<Fault>) app.getGlobalState().getPendingFaults();
        ArrayList<NodeInstance> brokenInstances = (ArrayList<NodeInstance>) app.getGlobalState().getBrokeninstances();

        if(pendingFaults.isEmpty() == true && brokenInstances.isEmpty() == true)
            //there is no biforcation with a deterministic pi, proceeding
            return this.isValidSequence(app, sequence);
        else{
            //there are some faults. there is a biforcation and the sequence must be tested for both branches
            //the not-handled-fault branch goes frist (might fail faster)
            Application cloneApp = this.cloneApp(app);

            if(this.isValidSequence(cloneApp, sequence) == true)
                return true;
            
            //now we explore the fault handled branch
            //first goes scaleIn that implies the no-broken-instances
            cloneApp = this.cloneApp(app);
            try {
                cloneApp.scaleIn(brokenInstances.get(0));
            } catch (Exception e) {
                //TODO: handle exception
            }

            if(this.isValidSequence(cloneApp, sequence) == true)
                return true;

            //for each fault check both the handled and not handled branch
            for (Fault f : pendingFaults) {
                cloneApp = this.cloneApp(app);

                if(app.getGlobalState().isResolvableFault(f) == true){
                    try {
                        cloneApp.autoreconnect(cloneApp.getGlobalState().getActiveNodeInstances().get(f.getInstanceID()), f.getReq()); 
                    } catch (Exception e) {
                        //TODO: handle exception
                    }
                    
                    if(this.isValidSequence(cloneApp, sequence) == true)
                        return true;  

                }else{
                    try {
                        cloneApp.fault(cloneApp.getGlobalState().getActiveNodeInstances().get(f.getInstanceID()), f.getReq());   
                    } catch (Exception e) {
                        //TODO: handle exception
                    }
                    
                    if(this.isValidSequence(cloneApp, sequence) == true)
                        return true;  
                }
            }
        }
        return false;
    }

    public boolean isNotValidSequence(Application app, List<SequenceElement> sequence)
        throws 
            NullPointerException,
            IllegalSequenceElementException
    {
        if (app == null)
            throw new NullPointerException();
        if (this.wellFormattedSequence(sequence) == false)
            throw new IllegalSequenceElementException();

        return !this.isWeaklyValidSequence(app, sequence);
    }

    public Application cloneApp(Application app)
        throws 
            NullPointerException
    {

        if(app == null)
            throw new NullPointerException();

        Application clone = new Application(app.getName());

        ArrayList<Node> appNodes = (ArrayList<Node>) app.getNodes().values();

        //cloning all nodes
        for (Node n : appNodes) {
            ManagementProtocol nMP = n.getManagementProtocol();

            HashMap<String, Transition> nTransitions = (HashMap<String, Transition>) nMP.getTransition();
            HashMap<String, List<Requirement>> nRho = (HashMap<String, List<Requirement>>) nMP.getRho();
            HashMap<String, List<String>> nGamma = (HashMap<String, List<String>>) nMP.getGamma();
            HashMap<String, List<String>> nPhi = (HashMap<String, List<String>>) nMP.getPhi();

            ManagementProtocol cloneMp = new ManagementProtocol();

            //cloning transitions
            for (Transition t : nTransitions.values())
                //TODO: new String sono inutili?
                cloneMp.addTransition(new String(t.getStartingState()), new String(t.getOp()), new String(t.getEndingState()));
            
            //cloning rho
            List<Requirement> clonedNodeReqs = new ArrayList<>();
            for (String s : nRho.keySet()) {
                for (Requirement r : nRho.get(s)) 
                    clonedNodeReqs.add(new Requirement(r.getName(), r.getRequirementSort()));
                
                cloneMp.addRhoEntry(new String(s), clonedNodeReqs);
            }

            //cloning gamma
            List<String> clonedNodeCaps = new ArrayList<>();
            for(String key : nGamma.keySet()){
                for(String cap : nGamma.get(key))
                    clonedNodeCaps.add(new String(cap));

                cloneMp.addGammaEntry(new String(key), clonedNodeCaps);
            }

            //cloning phi
            List<String> clonedNodeFStates = new ArrayList<>();
            for(String key : nPhi.keySet()){
                for(String state : nPhi.get(key))
                    clonedNodeFStates.add(new String(state));
                
                cloneMp.addPhiEntry(new String(key), clonedNodeFStates);
            }

            List<String> clonedNodeOps = new ArrayList<>();
            for(String op : n.getOps())
                clonedNodeOps.add(new String(op));
            
            List<String> clonedNodeStates = new ArrayList<>();
            for(String state : n.getStates())
                clonedNodeOps.add(new String(state));

            clone.addNode(new Node(n.getName(), new String(n.getInitialState()), cloneMp, clonedNodeReqs, clonedNodeCaps, clonedNodeStates, clonedNodeOps));
        }

        GlobalState gsClone = new GlobalState(clone);

        //cloning activeInstancses
        HashMap<String, NodeInstance> appActiveInstances = (HashMap<String, NodeInstance>) app.getGlobalState().getActiveNodeInstances();
        HashMap<String, NodeInstance> cloneActiveInstances = (HashMap<String, NodeInstance>) gsClone.getActiveNodeInstances();
        for(NodeInstance instance : appActiveInstances.values()){
            cloneActiveInstances.put(
                new String(instance.getID()), 
                new NodeInstance(clone.getNodes().get(instance.getNodeType().getName()), new String(instance.getCurrentState()), new String(instance.getID()))
            );
        }

        //cloning runtime bindings
        HashMap<String, List<RuntimeBinding>> appRuntimeBindings = (HashMap<String, List<RuntimeBinding>>) app.getGlobalState().getRuntimeBindings();
        HashMap<String, List<RuntimeBinding>> cloneRuntimeBindings = (HashMap<String, List<RuntimeBinding>>) clone.getGlobalState().getRuntimeBindings();

        for(String key : appRuntimeBindings.keySet()){
            List<RuntimeBinding> clonedBindings = new ArrayList<>();
            ArrayList<RuntimeBinding> appBindings = (ArrayList<RuntimeBinding>) appRuntimeBindings.get(key);
            for(RuntimeBinding appBinding : appBindings)
                clonedBindings.add(new RuntimeBinding(new Requirement(new String(appBinding.getReq().getName()), appBinding.getReq().getRequirementSort()), new String(appBinding.getNodeInstanceID())));
            
            cloneRuntimeBindings.put(new String(key), clonedBindings);
        }

        clone.setGlobalState(gsClone);

        //cloning static binding
        HashMap<StaticBinding, StaticBinding> appBindingFunction = (HashMap<StaticBinding, StaticBinding>) app.getBindingFunction();
        HashMap<StaticBinding, StaticBinding> cloneBindingFunction = (HashMap<StaticBinding, StaticBinding>) clone.getBindingFunction();

        for(StaticBinding firstHalf : appBindingFunction.keySet()){
            StaticBinding secondHalf = appBindingFunction.get(firstHalf);

            StaticBinding firstHalfCopy = new StaticBinding(new String(firstHalf.getNodeName()), new String(firstHalf.getCapOrReq()));
            StaticBinding secondHalfCopy = new StaticBinding(new String(secondHalf.getNodeName()), new String(secondHalf.getCapOrReq()));
            cloneBindingFunction.put(firstHalfCopy, secondHalfCopy);
        }

        return clone;
    }

    public void executeSequenceElement(Application app, SequenceElement seqElement)
        throws 
            IllegalArgumentException, 
            NullPointerException, 
            OperationNotAvailableException,
            FailedOperationException, 
            RuleNotApplicableException, 
            NodeUnknownException 
    {
        switch (seqElement.getRule()) {
            case "opStart":
                app.opStart(seqElement.getTargetInstance(), seqElement.getOp());
                break;
            case "opEnd":
                app.opEnd(seqElement.getTargetInstance(), seqElement.getOp());
                break;
            case "scaleIn":
                app.scaleIn(seqElement.getTargetInstance());
                break;
            case "scaleOut1":
                app.scaleOut1(seqElement.getTargetNode());
                break;
            case "scaleOut2": 
                app.scaleOut2(seqElement.getTargetNode(), seqElement.getServingInstance());
            default:
                break;
        }
    }


    //metodo privato che controlla che gli elementi della sequenza siano ben formattati (abbiano i giusti campi)
    private boolean wellFormattedSequence(List<SequenceElement> sequence){
        boolean res = true;
        
        for (SequenceElement element : sequence) { 
            if(element.wellFormattedOpSequence() == false)
                res = false;
        }

        return res;
    }   
}