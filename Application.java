import java.util.ArrayList;
import java.util.List;

import exceptions.OperationNotAvailable;

//represents the whole application
public class Application {

    //name of the application
    private final String name;
    //set T: list of application's component
    private List<Node> nodes;
    private GlobalState gState;
    // pi
    // b

    /**
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @param name application's name
     */
    public Application(String name) {
        //this assert is just to check that name is not null nor empty
        //used many more times in this project, it is just a reminder for 
        //a real exception handling
        assert name.length() > 0;
        
        this.name = name;
        this.nodes = new ArrayList<Node>();
        this.gState = new GlobalState(this);
    }

     /**
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @param name application's name
     * @param nodes list of applicaton's Node
     */
    public Application(String name, List<Node> nodes){
        assert name.length() > 0;
        this.name = name; 
        this.setNodes(nodes);
        this.gState = new GlobalState(this);
    }

    /**
     * @return current GlobalState
     */
    public GlobalState getGState() {
        return gState;
    }

    /**
     * @param gs given GlobalState 
     */
    public void setGState(GlobalState gs) {
        this.gState = gs;
    }

    /**
     * @return list of the application's Node
     */
    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * @param nodes list of Node to bet set to the applicaton
     * @throws NullPointerException
     */
    public void setNodes(List<Node> nodes) {
        assert nodes != null;
        this.nodes = nodes;
    }

    /**
     * @return name of the applicaton
     */
    public String getName() {
        return name;
    }

    /**
     * @param n node instance on which it's required to do the managment operation op
     * @param op management operation to execute
     * @throws NullPointerException
     * @throws OperationNotAvailable
     */
    public void opStart(NodeInstance n, String op) throws OperationNotAvailable {
        assert n != null;
        assert op.length() != 0;

        Transition transitionToHappen = null;

        ArrayList<Transition> possibleTransitions = (ArrayList<Transition>) n.getPossibleTransitions();
        for (Transition t : possibleTransitions){
            if(t.getOp().equals(op) == true && t.getEndingState().equals("damaged") == false)
               transitionToHappen = t;   
        }

        //among the possible transitions there is not a transition with this op, 
        //hence the op is not available
        if(transitionToHappen == null)
            throw new OperationNotAvailable();
        
        //n goes in a transient state
        n.setCurrenState(transitionToHappen.getName());

        //TODO: aggiornare i binding
            //1) rimuovi i vecchi binding legati al vecchio stato
            //2) provi a creare in maniera automatica i nuovi binding 
            //   (relativi ai nuovi reqs dello stato transiente di n)
            //   TODO: devi quindi definire finalmente pi, la funzione che decide quale istanza usare
            //   fra quelle che offrono le corrette capability

    }

    public void opEnd(NodeInstance n, String op){

    }


}