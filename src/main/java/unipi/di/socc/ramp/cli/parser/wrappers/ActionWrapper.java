package unipi.di.socc.ramp.cli.parser.wrappers;

public abstract class ActionWrapper {
    
    protected String action;

    public abstract void setAction();

    public String getActionName(){
        return this.action;
    }

}
