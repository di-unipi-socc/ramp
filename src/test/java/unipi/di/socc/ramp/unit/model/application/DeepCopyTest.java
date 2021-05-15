package unipi.di.socc.ramp.unit.model.application;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import unipi.di.socc.ramp.cli.parser.Parser;
import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;

public class DeepCopyTest {
    
    @Test
    public void deepCopyTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            IOException, 
            NodeUnknownException
    {
        String thinkingPath = System.getProperty("user.dir").concat("/data/thinking-app/thinking.json");
        String thinkingGSPath = System.getProperty("user.dir").concat("/data/thinking-app/running-globalstate.json");

        Application thinking = Parser.parseApplication(thinkingPath, thinkingGSPath);
        Application thinkingClone = thinking.clone();

        assertTrue(thinking.equals(thinkingClone));

    }


}
