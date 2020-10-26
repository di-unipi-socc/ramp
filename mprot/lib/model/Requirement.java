package mprot.lib.model;


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
    public Requirement(String name, RequirementSort reqSort) 
        throws 
            NullPointerException, 
            IllegalArgumentException
    {
        if(name == null)
            throw new NullPointerException("name null");
        if(name.isEmpty() == true)
            throw new IllegalArgumentException("name empty");

        if(reqSort == null)
            throw new NullPointerException("reqSort null");

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

    public RequirementSort getRequirementSort(){
        return this.reqSort;
    }

    @Override
    /**
     */
    public boolean equals(Object f){
        Requirement toCheck = (Requirement) f;
        boolean ret = false;

        if(toCheck.getName().equals(this.name) && toCheck.getRequirementSort().equals(this.reqSort))
            ret = true;
        
        return ret;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.name.hashCode();
        result = 31 * result + this.reqSort.hashCode();
        return result;
    }
}