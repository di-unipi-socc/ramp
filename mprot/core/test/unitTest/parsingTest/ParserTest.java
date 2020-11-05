package mprot.core.test.unitTest.parsingTest;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import mprot.core.analyzer.execptions.IllegalSequenceElementException;
import mprot.core.model.*;
import mprot.core.model.exceptions.*;
import mprot.core.test.utilities.ThesisAppFactory;

import mprot.cli.parsing.*;

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

    //TODO: test del plan parsing

}
