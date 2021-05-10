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

//TODO qui uso scaleOut, il test di greedy conta

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

        this.testApp = new Application("testApp", PiVersion.GREEDYPI);
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
        this.testApp.scaleOut1("needy", "needyInstance");

        //null requirement
        assertThrows(NullPointerException.class, () -> this.testApp.pi("needyInstance", null));

        //create 1000 instances of server 
        for(int i = 0; i < 1000; i++)
            this.testApp.scaleOut1("server", "server_" + i);;

        //there are now 101 active instances
        assertTrue(this.testApp.getGlobalState().getActiveInstances().size() == 1001);

        //list of instances that can take care of needyReq (serverA, serverB) (unordered)
        List<NodeInstance> capableInstances = this.testApp.getGlobalState().getCapableInstances("needyInstance", this.needyReq);
        assertTrue(capableInstances.size() == 1000);

        int indexing[] = new int[1000];
        for(int i = 0; i < 1000; i++){
            NodeInstance randomInstance = this.testApp.pi("needyInstance", this.needyReq);
            assertNotNull(randomInstance);
            assertTrue(capableInstances.contains(randomInstance));

            indexing[capableInstances.indexOf(randomInstance)]++;
        }

        //TODO
            //1) poor random performance
            //2) good random treshhold?

        // //since is (pseudo) random, indexing shoult have all the elements at 1
        // int test = 0;
        // for(int i = 0; i < 1000; i++){
        //     if(indexing[i] == 1)
        //         test++;
        // }

        // assertTrue(false ,test+"");

    }

}
