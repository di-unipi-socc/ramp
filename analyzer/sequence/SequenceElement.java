package analyzer.sequence;

public abstract class SequenceElement {

    protected String rule;

    public String getRule(){
        return this.rule;
    }

    public abstract boolean wellFormattedSequenceElement();

}
