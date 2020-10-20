public class ForVariableReference extends VariableReference {
    public ForVariable decl;
    
    public ForVariableReference(ForVariable decl)
    {
        super();
        this.decl = decl;
        decl.references.add(this);
    }
    
    public IVariable getVariable() {
        return this.decl;
    }
}