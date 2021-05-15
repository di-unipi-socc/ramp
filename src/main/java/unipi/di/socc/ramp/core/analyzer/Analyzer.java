package unipi.di.socc.ramp.core.analyzer;

import java.util.ArrayList;
import java.util.List;

import unipi.di.socc.ramp.core.model.Application;

public class Analyzer {
    
    private static AnalysisReport report;

    public static AnalysisReport getReport(){
        return report;
    }

    //#region utilities
    //#endregion


    //clone a list making another list with the same object refs
    public static <E> List<E> cloneList(List<E> list){
        List<E> cloneList = new ArrayList<>();
        for(E element : list)
            cloneList.add(element);

        return cloneList;
    }



    //########################### OFFERED METHODS ###########################
    public static void planAnalysis(Application app, Plan plan, String property){
    }



    public static void sequenceAnalysis(Application app, Sequence sequence, String property){
        //Sequence backupSeq = new Sequence(cloneList(sequence.getSequence()));

        if(app.isPiDeterministic());
            //deterministicSequenceAnalysis(app, sequence, property);


    }


}
