public class InstancePropertyReference extends VariableReference {
    public Expression object;
    public Property property;
    
    public InstancePropertyReference(Expression object, Property property)
    {
        super();
        this.object = object;
        this.property = property;
        property.instanceReferences.add(this);
    }
    
    public IVariable getVariable()
    {
        return this.property;
    }
}