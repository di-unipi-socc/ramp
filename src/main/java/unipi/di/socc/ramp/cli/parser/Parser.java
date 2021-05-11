package unipi.di.socc.ramp.cli.parser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import unipi.di.socc.ramp.cli.parser.wrappers.*;
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

    public static PlanOrSequenceWrapper parsePlanOrSequence(String jsonFilePath) 
        throws 
            IOException
    {
        RuntimeTypeAdapterFactory<Action> runtimeTAFactory = 
            RuntimeTypeAdapterFactory.of(Action.class, "action")
                .registerSubtype(ScaleOutWrapper.class, "scaleOut")
                .registerSubtype(OperationWrapper.class, "op")
                .registerSubtype(ScaleInWrapper.class, "scaleIn");

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(runtimeTAFactory).create();
        Reader reader = Files.newBufferedReader(Paths.get(jsonFilePath));

        PlanOrSequenceWrapper planOrSeqWrap = gson.fromJson(reader, PlanOrSequenceWrapper.class);
        for(Action action : planOrSeqWrap.getActions().values())
            action.setAction();

        if(planOrSeqWrap.getPartialOrdering() == null)
            planOrSeqWrap.initializePartialOrdering();

        return planOrSeqWrap;
    }

    public static GlobalState parseGlobalState(String jsonFilePath) throws IOException{
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(Paths.get(jsonFilePath));
        return gson.fromJson(reader, GlobalState.class);
    }

}
