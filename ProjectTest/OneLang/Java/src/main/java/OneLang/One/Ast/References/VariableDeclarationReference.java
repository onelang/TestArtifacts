public class VariableDeclarationReference extends VariableReference {
    public VariableDeclaration decl;
    
    public VariableDeclarationReference(VariableDeclaration decl)
    {
        super();
        this.decl = decl;
        decl.references.add(this);
    }
    
    public IVariable getVariable()
    {
        return this.decl;
    }
}