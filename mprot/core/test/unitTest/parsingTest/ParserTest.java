package mprot.core.test.unitTest.parsingTest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import mprot.core.analyzer.Analyzer;
import mprot.core.analyzer.execptions.IllegalSequenceElementException;
import mprot.core.analyzer.executable_element.ExecutableElement;
import mprot.core.model.*;
import mprot.core.model.exceptions.*;
import mprot.core.test.utilities.ThesisAppFactory;

import mprot.cli.parsing.*;
import mprot.cli.parsing.wrappers.PlanWrapper;

public class ParserTest {

    @Test
    public void appParsingTest() throws IOException, NullPointerException, IllegalSequenceElementException,
            InstanceUnknownException, IllegalArgumentException, FailedOperationException, RuleNotApplicableException,
            OperationNotAvailableException, AlreadyUsedIDException 
    {

        Application factoryApp = ThesisAppFactory.createApplication(PiVersion.GREEDYPI);
        Application parsedApp = Parser.parseApplication("C:\\Users\\Giulio\\UniPi\\Tesi_Triennale\\thesis\\data\\examples\\web-app\\app.json");
       
        assertTrue(factoryApp.clone().equals(parsedApp));

        //the global state conf file is based on (all scaleout) of interaction test
        Application parsedAppWithGS = Parser.parseApplication(
            "C:\\Users\\Giulio\\UniPi\\Tesi_Triennale\\thesis\\data\\examples\\web-app\\app.json", 
            "C:\\Users\\Giulio\\UniPi\\Tesi_Triennale\\thesis\\data\\examples\\web-app\\globalState.json"
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

    @Test
    public void planParsingTest()
            throws NullPointerException, IllegalSequenceElementException, InstanceUnknownException, IOException {
        Application app = Parser.parseApplication("C:\\Users\\Giulio\\UniPi\\Tesi_Triennale\\thesis\\data\\examples\\web-app\\app.json");
        PlanWrapper validPlan = Parser.parsePlan("C:\\Users\\Giulio\\UniPi\\Tesi_Triennale\\thesis\\data\\examples\\web-app\\validPlan.json");

        PlanWrapper weaklyValidPlan = Parser.parsePlan("C:\\Users\\Giulio\\UniPi\\Tesi_Triennale\\thesis\\data\\examples\\web-app\\weaklyValidPlan.json");

        //PrintingUtilities.printExecutablePlan(plan);

        Collection<ExecutableElement> planEECollection = validPlan.getPlanExecutableElements().values();
        List<ExecutableElement> planEElist = new ArrayList<>(planEECollection);


        Collection<ExecutableElement> weaklyValidPlanEECollection = weaklyValidPlan.getPlanExecutableElements().values();
        List<ExecutableElement> weaklyValidPlanEElist = new ArrayList<>(weaklyValidPlanEECollection);

        Analyzer analyzer = new Analyzer();
        //a valid plan is always immediatly weakly valid
        assertTrue(analyzer.isWeaklyValidPlan(app, planEElist, validPlan.getConstraints()));

        //a weakly valid plan is always (usally pretty fast) a not totally valid plan
        assertFalse(analyzer.isValidPlan(app, weaklyValidPlanEElist, weaklyValidPlan.getConstraints()));

    }
}
