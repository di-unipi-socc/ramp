package mprot.cli.parsing;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import mprot.core.analyzer.Constraint;
import mprot.core.analyzer.executable_element.*;
import mprot.core.model.*;

import mprot.cli.parsing.wrappers.*;

public class Parser {

    /**
     * mind that when an application is parsed a lot of operations done by the constructor are not executed, 
     * so the parser explicitly force some operations
     */


    /**
     * @param appSpecFilePath path to the application specification file .json
     * @param gsFilePath path to the global state configuration file .json
     * @return an application based on the configuration files
     * @throws IOException
     */
    public static Application parseApplication(String appSpecFilePath, String gsFilePath) 
        throws IOException 
    {
        Application app = parseApplication(appSpecFilePath);
        //set the isDeterministic boolean field of app based on the pi function specified in the json

        //the global state is passed but each instance has the ref to the parent node
        //to avoid the revriting in json of the nodes we added a specific field nodeTypeName 
        GlobalState globalState = parseGlobalState(gsFilePath);

        for(NodeInstance instance : globalState.getActiveNodeInstances().values())
            instance.setNodeType(app.getNodes().get(instance.getNodeTypeName()));

        app.setGlobalState(globalState);
        globalState.setApplication(app);

        return app;
    }

    public static Application parseApplication(String appSpecFilePath) throws IOException {
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(Paths.get(appSpecFilePath));

        AppWrapper wrappedApp = gson.fromJson(reader, AppWrapper.class);

        Application parsedApplication = new Application(wrappedApp.getName(), wrappedApp.getPiVersion());
        parsedApplication.piControlSwitch();

        for(NodeWrapper wrappedNode : wrappedApp.getNodes()){
            
            ManProtocolWrapper wrappedNodeMP = wrappedNode.getManagementProtocol();
            Map<String, Requirement> wrappedReqs = wrappedNode.getRequirements();

            ManagementProtocol parsedProtocol = new ManagementProtocol();

            List<String> states = new ArrayList<>();

            for(NodeStateWrapper wrappedState : wrappedNodeMP.getStates()){
                states.add(wrappedState.getName());

                //fill rho of the parsed node's protocol
                List<String> neededReqsNames = wrappedState.getRequires();
                List<Requirement> neededReqs = new ArrayList<>();

                for(String reqName : neededReqsNames)
                    neededReqs.add(wrappedReqs.get(reqName));

                if(neededReqs.size() == 0)
                    parsedProtocol.addRhoEntry(wrappedState.getName(), new ArrayList<>());
                else
                    parsedProtocol.addRhoEntry(wrappedState.getName(), neededReqs);

                //fill gamma of the parsed node's protocol
                if(wrappedState.getOffers() == null)
                    parsedProtocol.addGammaEntry(wrappedState.getName(), new ArrayList<>());
                else
                    parsedProtocol.addGammaEntry(wrappedState.getName(), wrappedState.getOffers());
                
                //fill phi of the parsed node's protocol
                if(wrappedState.getFaultHandlers() == null)
                    parsedProtocol.addPhiEntry(wrappedState.getName(), new ArrayList<>());
                else
                    parsedProtocol.addPhiEntry(wrappedState.getName(), wrappedState.getFaultHandlers());
            }

            for(TransitionWrapper wrappedTransition : wrappedNodeMP.getTransitions()){
                String transientState = 
                    wrappedTransition.getStartingState() + 
                    wrappedTransition.getOperation() + 
                    wrappedTransition.getTargetState()
                ;

                states.add(transientState);
                
                parsedProtocol.addTransition(
                    wrappedTransition.getStartingState(),
                    wrappedTransition.getOperation(),
                    wrappedTransition.getTargetState()
                );

                //fill rho of the parsed node's protocol
                List<String> neededReqsNames = wrappedTransition.getRequires();
                List<Requirement> neededReqs = new ArrayList<>();

                for(String reqName : neededReqsNames)
                    neededReqs.add(wrappedNode.getRequirements().get(reqName));

                if(neededReqs.size() == 0)
                    parsedProtocol.addRhoEntry(transientState, new ArrayList<>());
                else
                    parsedProtocol.addRhoEntry(transientState, neededReqs);

                //fill gamma
                if(wrappedTransition.getOffers() == null)
                    parsedProtocol.addGammaEntry(transientState, new ArrayList<>());
                else
                    parsedProtocol.addGammaEntry(transientState, wrappedTransition.getOffers());

                //fill phi
                if(wrappedTransition.getFaultHandlers() == null)
                    parsedProtocol.addPhiEntry(transientState, new ArrayList<>());
                else
                    parsedProtocol.addPhiEntry(transientState, wrappedTransition.getFaultHandlers());
            }

            Collection<Requirement> reqsCollection =  wrappedNode.getRequirements().values();
            List<Requirement> reqs = new ArrayList<>(reqsCollection);

            Node parsedNode = new Node(
                wrappedNode.getName(), 
                wrappedNodeMP.getInitialState(), 
                parsedProtocol, 
                reqs, 
                wrappedNode.getCapabilities(), 
                states, 
                wrappedNode.getOperations()
            );

            parsedApplication.addNode(parsedNode);
        }

        Map<BindingPair, BindingPair> bindingFunction = new HashMap<>();
        for(StaticBindingWrapper wrappedBinding : wrappedApp.getBindings()){
            BindingPair key = new BindingPair(wrappedBinding.getSourceNode(), wrappedBinding.getSourceRequirement());
            BindingPair value = new BindingPair(wrappedBinding.getTargetNode(), wrappedBinding.getTargetCapability());
            bindingFunction.put(key, value);
        }
        parsedApplication.setBindingFunction(bindingFunction);
        return parsedApplication;

    }

    private static GlobalState parseGlobalState(String gsFilePath) 
        throws 
            IOException 
    {
        Gson gson = new Gson();
        Reader appReader = Files.newBufferedReader(Paths.get(gsFilePath));
        return gson.fromJson(appReader, GlobalState.class);
    }

    public static PlanWrapper parsePlan(String planFilePath) 
        throws 
            IOException 
    {
        //set up the gson parser to parse using ExecutableElement.rule as "differentiator" to
        //parse correctly the implementations of ExecutableElement
        RuntimeTypeAdapterFactory<ExecutableElement> runtimeTAFactory = 
            RuntimeTypeAdapterFactory.of(ExecutableElement.class, "rule")
                .registerSubtype(ScaleOut1.class, "scaleOut1")
                .registerSubtype(ScaleOut2.class, "scaleOut2")
                .registerSubtype(OpStart.class, "opStart")
                .registerSubtype(OpEnd.class, "opEnd")
                .registerSubtype(ScaleIn.class, "scaleIn");


        Gson gson = new GsonBuilder().registerTypeAdapterFactory(runtimeTAFactory).create();
        Reader planReader = Files.newBufferedReader(Paths.get(planFilePath));

        PlanWrapper plan = gson.fromJson(planReader, PlanWrapper.class);
        //since rule is specified in ExecutableElement the parser do not read it,
        //so we call the abstract method setRule that fix that
        for(ExecutableElement element : plan.getPlanExecutableElements().values())
            element.setRule();

        Map<String, ExecutableElement> planElementsMap = plan.getPlanExecutableElements();
        List<Constraint> constraints = new ArrayList<>();
        for(ConstraintStringWrapper label : plan.getConstraintsLables())
            constraints.add(
                new Constraint(
                    planElementsMap.get(label.getBefore()),  
                    planElementsMap.get(label.getAfter()))
            );
        plan.setConstraints(constraints);

        return plan;
    }
    
}
