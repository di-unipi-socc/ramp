package test.ApplicationTest;

import static org.junit.Assert.*;

import org.junit.Test;
import model.*;

public class BaseConstructorTest{

    public Application testApp = null;

    @Test(expected = NullPointerException.class)
    public void appConstructorNullNameTest(){
        this.testApp = new Application(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void appConstructorEmptyNameTest(){
        this.testApp = new Application("");
    }
    
    @Test
    public void appConstructorTest(){
        this.testApp = new Application("test");
        assertNotNull("testApp null", this.testApp);;
        assertNotNull("nodes null", this.testApp.getNodes());
        assertNotNull("globalState null", this.testApp.getGlobalState());
        assertNotNull("bindingFunction null", this.testApp.getBindingFunction());
    }

}