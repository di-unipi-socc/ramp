package unipi.di.socc.ramp.cli.parser.wrappers;

import java.util.List;

public class NodeStateWrapper {
    
    private final String name; 
    private final List<String> requires;
    private final List<String> offers; 
    private final List<String> faultHandlers;
    
    public NodeStateWrapper(
        String name, 
        List<String> requires, 
        List<String> offers, 
        List<String> faultHandlers
    ){
        this.name = name;
        this.requires = requires;
        this.offers = offers;
        this.faultHandlers = faultHandlers;
    }

    public String getName() {
        return name;
    }
    public List<String> getRequires() {
        return requires;
    }
    public List<String> getOffers() {
        return offers;
    }
    public List<String> getFaultHandlers() {
        return faultHandlers;
    }

}
