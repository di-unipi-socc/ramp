package test.UnitTest.ApplicationTest;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import model.*;

public class FullConstructorTest {

    public String name = null;
    public Application testApp = null;
    public Map<String, Node> nodes = null;
    public Map<StaticBinding, StaticBinding> bf = null;

    
    @Before
    public void createParams(){
        name = "testApp";
        this.nodes = new HashMap<>();
        this.bf = new HashMap<>();
    }

    @Test(expected = NullPointerException.class)
    public void appConstructorNullNameTest(){
        this.testApp = new Application(null, PiVersion.GREEDYPI, nodes, bf);
    }

    @Test(expected = NullPointerException.class)
    public void appConstructorNullNodesTest(){
        this.testApp = new Application(name, PiVersion.GREEDYPI, null, bf);
    }

    @Test(expected = NullPointerException.class)
    public void appConstructorNullBindingFunctionTest(){
        this.testApp = new Application(name, PiVersion.GREEDYPI, nodes, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void appConstructorEmptyNameTest(){
        this.testApp = new Application("", PiVersion.GREEDYPI, nodes, bf);
    }

    @Test
    public void appConstructorTest(){
        this.testApp = new Application(name, PiVersion.GREEDYPI, nodes, bf);
        assertNotNull("testApp null", this.testApp);
        assertNotNull("nodes null", this.testApp.getNodes());
        assertNotNull("globalState null", this.testApp.getGlobalState());
        assertNotNull("bindingFunction null", this.testApp.getBindingFunction());
    }
}