package mprot.core.analyzer;

import mprot.core.analyzer.executableElement.*;
public class Constraint {

    private final ExecutableElement before;
    private final ExecutableElement after;

    public Constraint(ExecutableElement before, ExecutableElement after) {
        this.before = before;
        this.after = after;
    }

    public ExecutableElement getAfter() {
        return after;
    }

    public ExecutableElement getBefore() {
        return before;
    }

    @Override
    /**
     */
    public boolean equals(Object f){
        Constraint toCheck = (Constraint) f;
        boolean ret = false;

        if(this.before.equals(toCheck.getBefore()) && this.after.equals(toCheck.getAfter()))
            ret = true;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.before.hashCode();
        result = 31 * result + this.after.hashCode();
        return result;
    }


}
