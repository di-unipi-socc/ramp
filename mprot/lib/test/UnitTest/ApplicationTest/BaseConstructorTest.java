package mprot.lib.test.unitTest.applicationTest;

import mprot.lib.model.*;

import static org.junit.Assert.*;

import org.junit.Test;

public class BaseConstructorTest{

    public Application testApp = null;

    @Test(expected = NullPointerException.class)
    public void appConstructorNullNameTest(){
        this.testApp = new Application(null, PiVersion.GREEDYPI);
    }

    @Test(expected = IllegalArgumentException.class)
    public void appConstructorEmptyNameTest(){
        this.testApp = new Application("", PiVersion.GREEDYPI);
    }
    
    @Test
    public void appConstructorTest(){
        this.testApp = new Application("test", PiVersion.GREEDYPI);
        assertNotNull("testApp null", this.testApp);;
        assertNotNull("nodes null", this.testApp.getNodes());
        assertNotNull("globalState null", this.testApp.getGlobalState());
        assertNotNull("bindingFunction null", this.testApp.getBindingFunction());
    }

}