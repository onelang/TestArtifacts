public class CatchVariableReference extends VariableReference {
    public CatchVariable decl;
    
    public CatchVariableReference(CatchVariable decl)
    {
        super();
        this.decl = decl;
        decl.references.add(this);
    }
    
    public IVariable getVariable() {
        return this.decl;
    }
}