public interface IVariable {
    String getName();
    void setName(String value);
    
    IType getType();
    void setType(IType value);
    
    MutabilityInfo getMutability();
    void setMutability(MutabilityInfo value);
}