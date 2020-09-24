package analyzer.executable_element;

public abstract class ExecutableElement {

    protected String rule;

    public String getRule(){
        return this.rule;
    }

    public abstract boolean wellFormattedSequenceElement();

}
