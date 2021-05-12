package unipi.di.socc.ramp.core.analyzer;

import java.util.List;

import unipi.di.socc.ramp.core.analyzer.actions.Action;

//kinda wrapping class for clarity
public class Sequence {

    private final List<Action> sequence;

    public Sequence(List<Action> sequence){
        if(sequence == null)
            throw new NullPointerException();
        this.sequence = sequence;
    }

    public List<Action> getSequence() {
        return sequence;
    }
}
