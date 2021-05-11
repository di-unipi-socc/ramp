package unipi.di.socc.ramp.cli.parser.wrappers;

public class ConstraintWrapper {
    private final String before;
    private final String after;

    public ConstraintWrapper(String before, String after) {
        this.before = before;
        this.after = after;
    }

    public String getBefore() {
        return before;
    }
    public String getAfter() {
        return after;
    }
}
