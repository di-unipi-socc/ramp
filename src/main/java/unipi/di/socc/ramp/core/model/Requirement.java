package unipi.di.socc.ramp.core.model;

import java.util.Objects;

public class Requirement {
    
    private final String name; 
    private final RequirementSort sort;
    
    /**
     * @param name requirement's name
     * @param sort kind of requirement
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public Requirement(String name, RequirementSort sort) 
        throws
            NullPointerException,
            IllegalArgumentException
    {
        if(name == null)
            throw new NullPointerException();
        if(name.isBlank())
            throw new IllegalArgumentException();

        this.name = name;
        this.sort = sort;
    }

    public String getName() {
        return this.name;
    }
    public RequirementSort getSort() {
        return this.sort;
    }
    public boolean isContainment(){
        return this.sort == RequirementSort.CONTAINMENT;
    }
    public boolean isReplicaAware(){
        return this.sort == RequirementSort.REPLICA_AWARE;
    }
    public boolean isReplicaUnaware(){
        return this.sort == RequirementSort.REPLICA_UNAWARE;
    }

    @Override
    public boolean equals(Object obj){
        if(obj == null)
            throw new NullPointerException();
            
        Requirement req = (Requirement) obj;
        return this.name.equals(req.getName()) && this.sort == req.getSort();
    }
    @Override
    public int hashCode(){
        return Objects.hash(this.name, this.sort);
    }

}
