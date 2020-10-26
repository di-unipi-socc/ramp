package mprot.lib.analyzer;

import mprot.lib.analyzer.executable_element.*;
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

}
