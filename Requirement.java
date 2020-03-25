enum Sort {
    REPLICA_AWARE, REPLICA_UNAWARE, CONTAINMENT
}

//represent the requirement that a Node need to work properly
public class Requirement {

    private final String name;
    private final Sort reqSort;

    /**
     * @param name of Requirement
     * @param reqSort sort of the Requirement 
     * @throws InvalidArgumentException
     * @throws NullPointerException
     */
    public Requirement(String name, Sort reqSort) {
        assert name.length() > 4;
        assert reqSort != null;

        this.name = name;
        this.reqSort = reqSort;
    }

    /**
     * @return name of the Requirement
     */
    public String getName() {
        return name;
    }

    /**
     * @return true if this is a replica aware requirement
     */
    public boolean isReplicaAware(){
    return this.reqSort == Sort.REPLICA_AWARE;
    }

    /**
     * @return true if this is a replica unaware requirement 
     */
    public boolean isReplicaUnaware(){
       return this.reqSort == Sort.REPLICA_UNAWARE;
    }

    /**
     * @return true if this is a containemnt requirement
     */
    public boolean isContainment(){
       return this.reqSort == Sort.CONTAINMENT;
    }
}