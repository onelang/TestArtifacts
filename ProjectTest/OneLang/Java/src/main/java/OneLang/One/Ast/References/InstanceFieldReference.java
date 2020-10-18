public class InstanceFieldReference extends VariableReference {
    public Expression object;
    public Field field;
    
    public InstanceFieldReference(Expression object, Field field)
    {
        super();
        this.object = object;
        this.field = field;
        field.instanceReferences.add(this);
    }
    
    public IVariable getVariable()
    {
        return this.field;
    }
}