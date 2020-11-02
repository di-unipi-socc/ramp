package mprot.cli;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mprot.lib.model.*;
import mprot.lib.analyzer.Constraint;
import mprot.lib.analyzer.Plan;
import mprot.lib.analyzer.executable_element.*;



public class Parser {

    public static Application parseApplication(String appSpecFilePath, String gsFilePath) 
        throws IOException 
    {
        Gson gson = new Gson();
        Reader appReader = Files.newBufferedReader(Paths.get(appSpecFilePath));

        Application app = gson.fromJson(appReader, Application.class);
        app.piControlSwitch();

        Map<StaticBinding, StaticBinding> bindingFunction = new HashMap<>();
        for(int i = 0; i < app.getAllStaticBindings().size(); i = i + 2)
            bindingFunction.put(app.getAllStaticBindings().get(i), app.getAllStaticBindings().get(i+1));
        app.setBindingFunction(bindingFunction);

        GlobalState globalState = parseGlobalState(gsFilePath);
        for(NodeInstance instance : globalState.getActiveNodeInstances().values())
            instance.setNodeType(app.getNodes().get(instance.getNodeTypeName()));

        app.setGlobalState(globalState);

        return app;
    }

    public static Application parseApplication(String appSpecFilePath) 
        throws 
            IOException 
    {
        Gson gson = new Gson();
        Reader appReader = Files.newBufferedReader(Paths.get(appSpecFilePath));
        Application app = gson.fromJson(appReader, Application.class);
        app.piControlSwitch();

        Map<StaticBinding, StaticBinding> bindingFunction = new HashMap<>();
        for(int i = 0; i < app.getAllStaticBindings().size(); i = i + 2)
            bindingFunction.put(app.getAllStaticBindings().get(i), app.getAllStaticBindings().get(i+1));
        app.setBindingFunction(bindingFunction);

        app.setGlobalState(new GlobalState(app));

        for(Node n : app.getNodes().values()){
            ManagementProtocol mp = n.getManagementProtocol();
            for(String state : n.getStates()){

                if(mp.getGamma().get(state) == null)
                    mp.addGammaEntry(state, new ArrayList<>());
                
                if(mp.getRho().get(state) == null)
                    mp.addGammaEntry(state, new ArrayList<>());

                if(mp.getPhi().get(state) == null)
                    mp.addGammaEntry(state, new ArrayList<>());

            }
        }

        return app;
    }

    private static GlobalState parseGlobalState(String gsFilePath) 
        throws 
            IOException 
    {
        Gson gson = new Gson();
        Reader appReader = Files.newBufferedReader(Paths.get(gsFilePath));
        return gson.fromJson(appReader, GlobalState.class);
    }

    public static Plan parsePlan(String planFilePath) 
        throws 
            IOException 
    {
        RuntimeTypeAdapterFactory<ExecutableElement> runtimeTAFactory = 
            RuntimeTypeAdapterFactory.of(ExecutableElement.class, "rule")
                .registerSubtype(ScaleOut1.class, "scaleOut1")
                .registerSubtype(ScaleOut2.class, "scaleOut2")
                .registerSubtype(ScaleIn.class, "scaleIn")
                .registerSubtype(OpStart.class, "opStart")
                .registerSubtype(OpEnd.class, "opEnd");

        Gson gson = new GsonBuilder().registerTypeAdapterFactory(runtimeTAFactory).create();

        Reader planReader = Files.newBufferedReader(Paths.get(planFilePath));
        Plan plan = gson.fromJson(planReader, Plan.class);

        for(ExecutableElement element : plan.getPlanExecutableElements())
            element.setRule();

        for(Constraint c : plan.getConstraints()){
            c.getBefore().setRule();
            c.getAfter().setRule();
        }
    

        return plan;
    }
    
}
