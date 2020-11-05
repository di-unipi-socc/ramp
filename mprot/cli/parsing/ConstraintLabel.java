package mprot.cli.parsing;

public class ConstraintLabel {
    private final String before;
    private final String after;

    public ConstraintLabel(String before, String after) {
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
