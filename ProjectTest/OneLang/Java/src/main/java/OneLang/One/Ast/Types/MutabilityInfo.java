public class MutabilityInfo {
    public Boolean unused;
    public Boolean reassigned;
    public Boolean mutated;
    
    public MutabilityInfo(Boolean unused, Boolean reassigned, Boolean mutated)
    {
        this.unused = unused;
        this.reassigned = reassigned;
        this.mutated = mutated;
    }
}