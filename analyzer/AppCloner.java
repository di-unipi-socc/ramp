package analyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import model.*;
//TODO: ridefinisci clone in Application
public class AppCloner {
    
    public static Application cloneApp(Application app)
        throws 
            NullPointerException    
    {
        if(app == null)
            throw new NullPointerException();

        Application clone = new Application(app.getName());

        Collection<Node> appNodesCollection =  app.getNodes().values();
        ArrayList<Node> appNodes = new ArrayList<>(appNodesCollection);

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
                cloneMp.addTransition(new String(t.getStartingState()), new String(t.getOp()), new String(t.getEndingState()));
            
            //cloning rho
            for (String s : nRho.keySet()) {
                List<Requirement> clonedNodeReqs = new ArrayList<>();
                for (Requirement r : nRho.get(s)) 
                    clonedNodeReqs.add(new Requirement(r.getName(), r.getRequirementSort()));
                
                cloneMp.addRhoEntry(new String(s), clonedNodeReqs);
            }

            //cloning gamma
            for(String key : nGamma.keySet()){
                List<String> clonedNodeCaps = new ArrayList<>();
                for(String cap : nGamma.get(key))
                    clonedNodeCaps.add(new String(cap));

                cloneMp.addGammaEntry(new String(key), clonedNodeCaps);
            }

            //cloning phi
            for(String key : nPhi.keySet()){
                List<String> clonedNodeFStates = new ArrayList<>();
                for(String state : nPhi.get(key))
                    clonedNodeFStates.add(new String(state));
                
                cloneMp.addPhiEntry(new String(key), clonedNodeFStates);
            }

            //cloning ops
            List<String> clonedNodeOps = new ArrayList<>();
            for(String op : n.getOps())
                clonedNodeOps.add(new String(op));
            
            //cloning states
            List<String> clonedNodeStates = new ArrayList<>();
            for(String state : n.getStates())
                clonedNodeStates.add(new String(state));

            //cloning reqs
            List<Requirement> clonedNodeReqs = new ArrayList<>();
            for(Requirement r : n.getReqs())
                clonedNodeReqs.add(new Requirement(new String(r.getName()), r.getRequirementSort()));
            
            //cloning caps
            List<String> clonedNodeCaps = new ArrayList<>();
            for(String nodeCap : n.getCaps())
                clonedNodeCaps.add(new String(nodeCap));

            clone.addNode(
                new Node(n.getName(), 
                new String(n.getInitialState()), cloneMp, clonedNodeReqs, clonedNodeCaps, clonedNodeStates, clonedNodeOps)
            );
        }

        //cloning activeInstancses
        HashMap<String, NodeInstance> appActiveInstances = (HashMap<String, NodeInstance>) app.getGlobalState().getActiveNodeInstances();
        HashMap<String, NodeInstance> cloneActiveInstances = (HashMap<String, NodeInstance>) clone.getGlobalState().getActiveNodeInstances();
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
            ArrayList<RuntimeBinding> appBindings = (ArrayList<RuntimeBinding>) appRuntimeBindings.get(key);
            
            List<RuntimeBinding> clonedBindings = new ArrayList<>();

            for(RuntimeBinding appBinding : appBindings)
                clonedBindings.add(new RuntimeBinding(new Requirement(new String(appBinding.getReq().getName()), appBinding.getReq().getRequirementSort()), new String(appBinding.getNodeInstanceID())));
            
            cloneRuntimeBindings.put(new String(key), clonedBindings);
        }

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

}
