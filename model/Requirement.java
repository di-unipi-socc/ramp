package model;


//represent the requirement that a node need to work properly
public class Requirement {

    private final String name;
    private final RequirementSort reqSort;

    /**
     * @param name of the requirement
     * @param reqSort sort of the requirement 
     * @throws InvalidArgumentException
     * @throws NullPointerException
     */
    public Requirement(String name, RequirementSort reqSort) {
        assert name.length() > 0;
        assert reqSort != null;

        this.name = name;
        this.reqSort = reqSort;
    }

    /**
     * @return name of the requirement
     */
    public String getName() {
        return name;
    }

    /**
     * @return true if this is a replica aware requirement
     */
    public boolean isReplicaAware(){
    return this.reqSort == RequirementSort.REPLICA_AWARE;
    }

    /**
     * @return true if this is a replica unaware requirement 
     */
    public boolean isReplicaUnaware(){
       return this.reqSort == RequirementSort.REPLICA_UNAWARE;
    }

    /**
     * @return true if this is a containemnt requirement
     */
    public boolean isContainment(){
       return this.reqSort == RequirementSort.CONTAINMENT;
    }
}