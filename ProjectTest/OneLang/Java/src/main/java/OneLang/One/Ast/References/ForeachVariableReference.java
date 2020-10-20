public class ForeachVariableReference extends VariableReference {
    public ForeachVariable decl;
    
    public ForeachVariableReference(ForeachVariable decl)
    {
        super();
        this.decl = decl;
        decl.references.add(this);
    }
    
    public IVariable getVariable() {
        return this.decl;
    }
}