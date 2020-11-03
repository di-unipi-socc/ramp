package mprot.lib.test.unitTest.parsingTest;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import mprot.lib.analyzer.*;
import mprot.lib.analyzer.execptions.IllegalSequenceElementException;
import mprot.lib.analyzer.executable_element.*;
import mprot.lib.model.*;
import mprot.lib.model.exceptions.*;

import mprot.lib.test.utilities.ThesisAppFactory;
import mprot.cli.*;

public class ParserTest {

    @Test
    public void test() throws IOException, NullPointerException, IllegalSequenceElementException,
            InstanceUnknownException, IllegalArgumentException, FailedOperationException, RuleNotApplicableException,
            OperationNotAvailableException, AlreadyUsedIDException 
    {
        Analyzer analyzer = new Analyzer();

        Application factoryApp = ThesisAppFactory.createApplication(PiVersion.GREEDYPI);
        Application parsedApp = Parser.parseApplication("C:\\Users\\Giulio\\UniPi\\Tesi_Triennale\\thesis\\configFiles\\appSpec.json");
        PlanWrapper parsedPlan = Parser.parsePlan("C:\\Users\\Giulio\\UniPi\\Tesi_Triennale\\thesis\\configFiles\\plan.json");

        //creates a valid plan
        List<ExecutableElement> planExecutableElements = new ArrayList<>();
        List<Constraint> constraints = new ArrayList<>();

        ExecutableElement e1 = new ScaleOut1("node", "n1");
        ExecutableElement e2 = new ScaleOut1("mongo", "m1");
        ExecutableElement e3 = new ScaleOut2("backend", "b1", "n1");
        ExecutableElement e4 = new ScaleOut2("frontend", "f1", "n1");
        
        planExecutableElements.add(e1);
        planExecutableElements.add(e2);
        planExecutableElements.add(e3);
        planExecutableElements.add(e4);

        //backend and frontend are in a containment relation with node
        Constraint c1 = new Constraint(e1, e3);
        Constraint c2 = new Constraint(e1, e4);
        constraints.add(c1);
        constraints.add(c2);

        assertTrue(planExecutableElements.equals(parsedPlan.getPlanExecutableElements()));
        assertTrue(constraints.equals(parsedPlan.getConstraints()));
        
        assertTrue(factoryApp.equals(parsedApp));

        //analyze on factory app and locally created plan
        assertTrue(analyzer.isValidPlan(
            factoryApp, 
            this.cloneList(planExecutableElements), 
            constraints
        ));

        //analyze on factory app and parsed plan
        assertTrue(analyzer.isValidPlan(
            factoryApp, 
            this.cloneList(parsedPlan.getPlanExecutableElements()), 
            parsedPlan.getConstraints()
        ));
        
        //analyze on parsed app and locally created plan
        assertTrue(analyzer.isValidPlan(
            parsedApp, 
            this.cloneList(planExecutableElements), 
            constraints
        ));
        
        //analyze on parsed app and parsed plan
        assertTrue(analyzer.isValidPlan(
            parsedApp, 
            this.cloneList(parsedPlan.getPlanExecutableElements()), 
            parsedPlan.getConstraints()
        ));

        //the global state conf file is based on (all scaleout) of interaction test
        Application parsedAppWithGS = Parser.parseApplication(
            "C:\\Users\\Giulio\\UniPi\\Tesi_Triennale\\thesis\\configFiles\\appSpec.json", 
            "C:\\Users\\Giulio\\UniPi\\Tesi_Triennale\\thesis\\configFiles\\globalState.json"
        );

        factoryApp.scaleOut1("mongo", "mongoM1");
        factoryApp.scaleOut1("node", "nodeN1");
        factoryApp.scaleOut1("node", "nodeN2");
        factoryApp.scaleOut1("node", "nodeN3");
        factoryApp.scaleOut2("frontend", "frontendF1", "nodeN3");
        factoryApp.scaleOut2("backend", "backendB1", "nodeN1");
        factoryApp.scaleOut2("backend", "backendB2", "nodeN2");

        assertTrue(parsedAppWithGS.equals(factoryApp));
    }

    /**
     * @param list
     * @return new list with the same object refs as list
     */
    private <E> List<E> cloneList(List<E> list){
        List<E> clonedList = new ArrayList<>();

        for(E element : list)
            clonedList.add(element);

        return clonedList;
    }   
}
