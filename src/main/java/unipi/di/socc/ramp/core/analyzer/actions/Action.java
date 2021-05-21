package unipi.di.socc.ramp.core.analyzer.actions;

import java.util.Objects;

public abstract class Action {

    protected String action;

    public String getActionName(){
        return this.action;
    }

    @Override
    public boolean equals(Object obj){
        if(obj instanceof OpStart && this instanceof OpStart){
            OpStart toCheck = (OpStart) obj;
            return toCheck.equals(this);
        }

        if(obj instanceof OpEnd && this instanceof OpEnd){
            OpEnd toCheck = (OpEnd) obj;
            return toCheck.equals(this);
        }

        if(obj instanceof ScaleIn && this instanceof ScaleIn){
            ScaleIn toCheck = (ScaleIn) obj;
            return toCheck.equals(this);
        }

        if(obj instanceof ScaleOut && this instanceof ScaleOut){
            ScaleOut toCheck = (ScaleOut) obj;
            return toCheck.equals(this);
        }

        if(obj instanceof ScaleOutC && this instanceof ScaleOutC){
            ScaleOutC toCheck = (ScaleOutC) obj;
            return toCheck.equals(this);
        }

        return false;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.action);
    }

    @Override
    public Action clone(){
        if(this instanceof OpStart){
            OpStart castedThis = (OpStart) this;
            return castedThis.clone();
        }
        if(this instanceof OpEnd){
            OpEnd castedThis = (OpEnd) this;
            return castedThis.clone();
        }
        if(this instanceof ScaleIn){
            ScaleIn castedThis = (ScaleIn) this;
            return castedThis.clone();
        }
        if(this instanceof ScaleOut){
            ScaleOut castedThis = (ScaleOut) this;
            return castedThis.clone();
        }
        if(this instanceof ScaleOutC){
            ScaleOutC castedThis = (ScaleOutC) this;
            return castedThis.clone();
        }

        return null;
        
    }
}
