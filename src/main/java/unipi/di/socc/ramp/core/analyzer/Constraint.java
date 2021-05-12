package unipi.di.socc.ramp.core.analyzer;

import java.util.Objects;

import unipi.di.socc.ramp.core.analyzer.actions.Action;

public class Constraint {
    
    private final Action before;
    private final Action after;


    public Constraint(Action before, Action after)
        throws
            NullPointerException
    {
        if(before == null || after == null)
            throw new NullPointerException();

        this.before = before;
        this.after = after;
    }

    public Action getBefore() {
        return before;
    }
    public Action getAfter() {
        return after;
    }
    
    @Override
    public boolean equals(Object obj){
        if(obj == null)
            throw new NullPointerException();

        Constraint check = (Constraint) obj;

        return check.getAfter().equals(this.after) && 
                check.getBefore().equals(this.before);
    }

    @Override 
    public int hashCode(){
        return Objects.hash(this.before, this.after);
    }

}
