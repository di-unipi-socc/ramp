package unipi.di.socc.ramp.unit.model.application;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import unipi.di.socc.ramp.core.model.Application;
import unipi.di.socc.ramp.core.model.ManagementProtocol;
import unipi.di.socc.ramp.core.model.Node;
import unipi.di.socc.ramp.core.model.NodeCap;
import unipi.di.socc.ramp.core.model.NodeInstance;
import unipi.di.socc.ramp.core.model.NodeReq;
import unipi.di.socc.ramp.core.model.PiVersion;
import unipi.di.socc.ramp.core.model.Requirement;
import unipi.di.socc.ramp.core.model.RequirementSort;
import unipi.di.socc.ramp.core.model.exceptions.AlreadyUsedIDException;
import unipi.di.socc.ramp.core.model.exceptions.InstanceUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.NodeUnknownException;
import unipi.di.socc.ramp.core.model.exceptions.RuleNotApplicableException;

//the scaleOut method indirectly calls the PI in use. The "clean" test (where scaleOut is not
//used to spawn new instances is in GreedyPITest)
//We later on fully test scaleOut on its own, so here we assume that scaleOut is correct.

public class RandomPiTest {
    public Application testApp;
    public Requirement needyReq = new Requirement("needyReq", RequirementSort.REPLICA_UNAWARE);


    @BeforeEach
    public void before() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            NodeUnknownException
    {
        /**
         * creates a simple custom application with two nodes: needy and server, both with 
         * just one state
         * - needy requires the requirement needyReq
         * - server offer the right capability serverCap for needyReq
         */

        this.testApp = new Application("testApp", PiVersion.RANDOMPI);
        this.testApp.addNode(this.createNeedy());
        this.testApp.addNode(this.createServer());

        testApp.addStaticBinding(new NodeReq("needy", "needyReq"), new NodeCap("server", "serverCap"));
    }
    
    public Node createNeedy(){
        Node needy = new Node("needy", new ManagementProtocol("state"));
        needy.addRequirement(this.needyReq);
        ManagementProtocol needyMP = needy.getManProtocol();
        
        //rho: state -> needed req in that state
        needyMP.getRho().get("state").add(this.needyReq);

        return needy;
    }
    public Node createServer(){
        Node server = new Node("server", new ManagementProtocol("state"));
        server.addCapability("serverCap");
        ManagementProtocol serverMP = server.getManProtocol();
        
        //gamma: state -> cap offered in that state
        serverMP.getGamma().get("state").add("serverCap");

        return server;
    }

    @Test
    public void greedyPiTest() 
        throws 
            NullPointerException, 
            IllegalArgumentException, 
            AlreadyUsedIDException, 
            RuleNotApplicableException, 
            InstanceUnknownException, 
            NodeUnknownException
    {
        //null instanceID
        assertThrows(NullPointerException.class, () -> this.testApp.pi(null, this.needyReq));
        //empty instanceID
        assertThrows(IllegalArgumentException.class, () -> this.testApp.pi("", this.needyReq));
        //unknown instanceID
        assertThrows(InstanceUnknownException.class, () -> this.testApp.pi("unknown", this.needyReq));

        //create first needy (that have an unsatisfied requiremnet needyReq)
        this.testApp.scaleOut("needy", "needyInstance");

        //null requirement
        assertThrows(NullPointerException.class, () -> this.testApp.pi("needyInstance", null));

        //create 1000 instances of server 
        for(int i = 0; i < 10; i++)
            this.testApp.scaleOut("server", "server_" + i);;

        //there are now 101 active instances
        assertTrue(this.testApp.getGlobalState().getActiveInstances().size() == 11);

        //list of instances that can take care of needyReq (serverA, serverB) (unordered)
        List<NodeInstance> capableInstances = this.testApp.getGlobalState().getCapableInstances("needyInstance", this.needyReq);
        assertTrue(capableInstances.size() == 10);

        int indexing[] = new int[10];
        for(int i = 0; i < 1000; i++){
            NodeInstance randomInstance = this.testApp.pi("needyInstance", this.needyReq);
            assertNotNull(randomInstance);
            assertTrue(capableInstances.contains(randomInstance));

            indexing[capableInstances.indexOf(randomInstance)]++;
        }

        int check = 0;
        for(int i = 0; i < 10; i++){
            if(indexing[i] > 0)
                check++;
        }

        assertTrue(check >= 2);
       

    }

}
