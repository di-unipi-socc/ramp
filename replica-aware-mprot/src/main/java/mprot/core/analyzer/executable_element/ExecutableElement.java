package mprot.core.analyzer.executable_element;

public abstract class ExecutableElement {

    protected String rule;

    public String getRule(){
        return this.rule;
    }

    public abstract boolean wellFormattedSequenceElement();

    public abstract void setRule();

    @Override
    /**
     */
    public boolean equals(Object f){

        if(f instanceof OpStart && this instanceof OpStart){
            OpStart toCheck = (OpStart) f;
            return toCheck.equals(this);
        }

        if(f instanceof OpEnd && this instanceof OpEnd){
            OpEnd toCheck = (OpEnd) f;
            return toCheck.equals(this);
        }

        if(f instanceof ScaleIn && this instanceof ScaleIn){
            ScaleIn toCheck = (ScaleIn) f;
            return toCheck.equals(this);
        }

        if(f instanceof ScaleOut1 && this instanceof ScaleOut1){
            ScaleOut1 toCheck = (ScaleOut1) f;
            return toCheck.equals(this);
        }

        if(f instanceof ScaleOut2 && this instanceof ScaleOut2){
            ScaleOut2 toCheck = (ScaleOut2) f;
            return toCheck.equals(this);
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.rule.hashCode();
        
        return result;
    }


}
