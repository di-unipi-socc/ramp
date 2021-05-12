package unipi.di.socc.ramp.cli.parser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import unipi.di.socc.ramp.cli.parser.wrappers.*;
import unipi.di.socc.ramp.core.analyzer.Constraint;
import unipi.di.socc.ramp.core.analyzer.Plan;
import unipi.di.socc.ramp.core.analyzer.Sequence;
import unipi.di.socc.ramp.core.analyzer.actions.*;

import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.GlobalState;
import unipi.di.socc.ramp.core.model.ManagementProtocol;
import unipi.di.socc.ramp.core.model.Node;
import unipi.di.socc.ramp.core.model.NodeCap;
import unipi.di.socc.ramp.core.model.NodeInstance;
import unipi.di.socc.ramp.core.model.NodeReq;
import unipi.di.socc.ramp.core.model.Requirement;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;

public class Parser {
    
    public static Application parseApplication(String jsonAppPath, String jsonGSPath) throws IOException, NullPointerException, IllegalArgumentException, NodeUnknownException{
        Application app = parseApplication(jsonAppPath);
        GlobalState gs = parseGlobalState(jsonGSPath);

        for(NodeInstance instance : gs.getActiveInstances().values())
            instance.setNode(app.getNodes().get(instance.getNodeType().getName()));

        return app;
    }

    public static Application parseApplication(String jsonFilePath) 
        throws 
            IOException, 
            NullPointerException, 
            IllegalArgumentException, 
            NodeUnknownException
    {
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(Paths.get(jsonFilePath));
        ApplicationWrapper appWrap = gson.fromJson(reader, ApplicationWrapper.class);

        Application app = new Application(appWrap.getName(), appWrap.getPiVersion());

        //now we inspect the wrapped application and rebuild the real application

        //for each wrapped node
        for(NodeWrapper nodeWrap : appWrap.getNodes()){
            //all requirements of the node: requirement name -> requirement
            Map<String, Requirement> reqsMap = nodeWrap.getRequirements();

            ManProtocolWrapper mpWrap = nodeWrap.getManagementProtocol();
            
            //we start building the management protocol of the node from its wrap
            ManagementProtocol mp = new ManagementProtocol(mpWrap.getInitialState());

            //for each wrapped node state
            for(NodeStateWrapper nodeStateWrap : mpWrap.getStates()){
                //add the state to the protocol
                mp.addState(nodeStateWrap.getName());

                //add the needed requirements for the state
                for(String reqName : nodeStateWrap.getRequires())
                    //rho: state s -> requirements needed in s
                    mp.getRho().get(nodeStateWrap.getName()).add(reqsMap.get(reqName));
        
                //add the offered capabilities
                mp.getGamma().get(nodeStateWrap.getName()).addAll(nodeStateWrap.getOffers());

                //add the fault handlers
                mp.getPhi().get(nodeStateWrap.getName()).addAll(nodeStateWrap.getFaultHandlers());
            }

            //for each wrapped transition
            for(TransitionWrapper transitionWrap : mpWrap.getTransitions()){
                //add the transition to the protocol
                mp.addTransition(
                    transitionWrap.getStartState(), 
                    transitionWrap.getOperation(),
                    transitionWrap.getTargetState()    
                );

                String transitionName = 
                    transitionWrap.getStartState() + 
                    transitionWrap.getOperation() + 
                    transitionWrap.getTargetState();

                //add the needed req for the transition
                for(String reqName : transitionWrap.getRequires())
                    mp.getRho().get(transitionName).add(reqsMap.get(reqName));
                
                //add the offered capabilities for the transition
                mp.getGamma().get(transitionName).addAll(transitionWrap.getOffers());
                
                //add the fault handlers for the transition
                mp.getPhi().get(transitionName).addAll(transitionWrap.getFaultHandlers());
            }

            //now the management protocol is ready
            //we build the real node 
            Node node = new Node(
                nodeWrap.getName(), 
                mp, 
                new ArrayList<Requirement>(nodeWrap.getRequirements().values()), 
                nodeWrap.getCapabilities(), 
                nodeWrap.getOperations()
            );

            //add the node to the application
            app.addNode(node);
        }

        //now we retrieve the static topology
        for(StaticBindingWrapper sbWrap : appWrap.getBindings())
            app.addStaticBinding(
                new NodeReq(sbWrap.getSourceNode(), sbWrap.getSourceRequirement()), 
                new NodeCap(sbWrap.getTargetNode(), sbWrap.getTargetCapability()));

        return app;
    }

    public static Sequence parseSequence(String jsonFilePath) throws IOException{
        PlanOrSequenceWrapper sequenceWrapper = parsePlanOrSequence(jsonFilePath);
        List<Action> sequence = new ArrayList<>();

        for(ActionWrapper actionWrap : sequenceWrapper.getActions().values())
            sequence.addAll(parseAction(actionWrap));
        
        return new Sequence(sequence);
    }

    public static List<Action> parseAction(ActionWrapper actionWrap){
        List<Action> action = new ArrayList<>();

        if(actionWrap instanceof OperationWrapper){
            OperationWrapper opWrap = (OperationWrapper) actionWrap;
            action.add(new OpStart(opWrap.getInstanceID(), opWrap.getOpName()));
            action.add(new OpEnd(opWrap.getInstanceID(), opWrap.getOpName()));
        }
        if(actionWrap instanceof ScaleOutWrapper){
            ScaleOutWrapper scaleOutWrap = (ScaleOutWrapper) actionWrap;
            //scaleOut1
            if(scaleOutWrap.getContainerID() == null)
            action.add(
                    new ScaleOut1(
                        scaleOutWrap.getIDToAssign(), 
                        scaleOutWrap.getNodeName()
                    )
                );
            else
                //scaleOut2
                action.add(
                    new ScaleOut2(
                        scaleOutWrap.getIDToAssign(), 
                        scaleOutWrap.getNodeName(), 
                        scaleOutWrap.getContainerID()
                    )
                );
        }
        if(actionWrap instanceof ScaleInWrapper){
            ScaleInWrapper scaleInWrap = (ScaleInWrapper) actionWrap;
            action.add(new ScaleIn(scaleInWrap.getInstanceID()));
        }

        return action;
    }
    
    public static List<Constraint> getConstraints(PlanOrSequenceWrapper planWrapper){
        //json action name -> actual action (as a list)
        //if op -> [opStart, opEnd], otherwise a list with only 1 element
        Map<String, List<Action>> actionNameToAction = new HashMap<>();

        for(String actionName : planWrapper.getActions().keySet())
            actionNameToAction.put(actionName, parseAction(planWrapper.getActions().get(actionName)));
        
        List<Constraint> constraintsList = new ArrayList<>();

        //we now add the constraints about op: opStart -> opEnd
        for(List<Action> action : actionNameToAction.values()){
            //this is an op
            if(action.size() == 2)
                constraintsList.add(new Constraint(action.get(0), action.get(1)));
        }

        //constraints adding
        for(ConstraintWrapper constraintWrap : planWrapper.getPartialOrderWrap()){
            List<Action> before = actionNameToAction.get(constraintWrap.getBefore());
            List<Action> after = actionNameToAction.get(constraintWrap.getAfter());

            //op -> action x, this become opEnd -> action x
            if(before.size() == 2)
                constraintsList.add(new Constraint(before.get(1), after.get(0)));
            else
                constraintsList.add(new Constraint(before.get(0), after.get(0)));
        }

        return constraintsList;
    }


    public static Plan parsePlan(String jsonFilePath) throws IOException{
        //EXTRACT AND ORGANIZE DATA FROM THE JSON
        
        PlanOrSequenceWrapper planWrapper = parsePlanOrSequence(jsonFilePath);
        //list of parsed constraints
        List<Constraint> constraintsList = getConstraints(planWrapper);

        //NOW WE BUILD THE PLAN

        //set of actions that compose the plan
        List<Action> planActions = parseSequence(jsonFilePath).getSequence();

        //partialOrder: action x -> list of actions that have to executed after x
        Map<Action, List<Action>> partialOrder = new HashMap<>();

        //initialize partial order
        for(Action action : planActions)
            partialOrder.put(action, new ArrayList<>());
        
        //for each action we add the explicit order expressed by the constraints
        for(Constraint constraint : constraintsList)
            partialOrder.get(constraint.getBefore()).add(constraint.getAfter());

        //now we express the implicit ordering: a -> b, b -> c => a -> c
        boolean changed = true;
        while(changed){
            changed = false;
            //for each action that have "afters"
            //eg: before = a;
            for(Action before : partialOrder.keySet()){

                //for each "after" of the current before
                //eg: after = b
                List<Action> toAdd = new ArrayList<>();
                for(Action after : partialOrder.get(before)){
                    
                    //for the "afters" of after
                    //eg: afterOfAfter = afters of b = c
                    for(Action afterOfAfter : partialOrder.get(after)){

                        //if afterOfAfter is not contained in the afters of before we add it
                        //c is not contained in the afters of before (a)
                        //we add c to the afters of a (since c is done after b which done after a)
                        if(!partialOrder.get(before).contains(afterOfAfter)){
                            if(!toAdd.contains(afterOfAfter))
                                toAdd.add(afterOfAfter);
                            changed = true;
                        }
                    }
                }
                //cant do it inside because the iterator
                if(changed)
                    partialOrder.get(before).addAll(toAdd);
                
            }
        }

        //we finally return the plan
        return new Plan(planActions, partialOrder);
    }


    public static PlanOrSequenceWrapper parsePlanOrSequence(String jsonFilePath) 
        throws 
            IOException
    {
        RuntimeTypeAdapterFactory<ActionWrapper> runtimeTAFactory = 
            RuntimeTypeAdapterFactory.of(ActionWrapper.class, "action")
                .registerSubtype(ScaleOutWrapper.class, "scaleOut")
                .registerSubtype(OperationWrapper.class, "op")
                .registerSubtype(ScaleInWrapper.class, "scaleIn");

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(runtimeTAFactory).create();
        Reader reader = Files.newBufferedReader(Paths.get(jsonFilePath));

        PlanOrSequenceWrapper planOrSeqWrap = gson.fromJson(reader, PlanOrSequenceWrapper.class);
        for(ActionWrapper actionWrap : planOrSeqWrap.getActions().values())
            actionWrap.setAction();

        if(planOrSeqWrap.getPartialOrderWrap() == null)
            planOrSeqWrap.initializePartialOrdering();

        return planOrSeqWrap;
    }

    public static GlobalState parseGlobalState(String jsonFilePath) throws IOException{
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(Paths.get(jsonFilePath));
        return gson.fromJson(reader, GlobalState.class);
    }

}
