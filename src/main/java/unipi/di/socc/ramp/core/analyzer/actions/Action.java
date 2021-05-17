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

        if(obj instanceof ScaleOut1 && this instanceof ScaleOut1){
            ScaleOut1 toCheck = (ScaleOut1) obj;
            return toCheck.equals(this);
        }

        if(obj instanceof ScaleOut2 && this instanceof ScaleOut2){
            ScaleOut2 toCheck = (ScaleOut2) obj;
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
        if(this instanceof ScaleOut1){
            ScaleOut1 castedThis = (ScaleOut1) this;
            return castedThis.clone();
        }
        if(this instanceof ScaleOut2){
            ScaleOut2 castedThis = (ScaleOut2) this;
            return castedThis.clone();
        }

        return null;
        
    }
}
