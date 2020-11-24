package mprot.cli.parsing.wrappers;

public class ConstraintStringWrapper {
    private final String before;
    private final String after;

    public ConstraintStringWrapper(String before, String after) {
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
