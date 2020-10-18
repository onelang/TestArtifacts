import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

public class LambdaCaptureCollector extends AstTransformer {
    public List<Set<IVariable>> scopeVarStack;
    public Set<IVariable> scopeVars;
    public Set<IVariable> capturedVars;
    
    public LambdaCaptureCollector()
    {
        super("LambdaCaptureCollector");
        this.scopeVarStack = new ArrayList<Set<IVariable>>();
        this.scopeVars = null;
        this.capturedVars = null;
    }
    
    protected Lambda visitLambda(Lambda lambda)
    {
        if (this.scopeVars != null)
            this.scopeVarStack.add(this.scopeVars);
        
        this.scopeVars = new HashSet<IVariable>();
        this.capturedVars = new HashSet<IVariable>();
        
        super.visitLambda(lambda);
        lambda.captures = new ArrayList<IVariable>();
        for (var capture : this.capturedVars.toArray(IVariable[]::new))
            lambda.captures.add(capture);
        
        this.scopeVars = this.scopeVarStack.size() > 0 ? this.scopeVarStack.remove(this.scopeVarStack.size() - 1) : null;
        return null;
    }
    
    protected IVariable visitVariable(IVariable variable)
    {
        if (this.scopeVars == null)
            return null;
        this.scopeVars.add(variable);
        return null;
    }
    
    protected VariableReference visitVariableReference(VariableReference varRef)
    {
        if (varRef instanceof StaticFieldReference || varRef instanceof InstanceFieldReference || varRef instanceof StaticPropertyReference || varRef instanceof InstancePropertyReference)
            return null;
        if (this.scopeVars == null)
            return null;
        var vari = varRef.getVariable();
        if (!this.scopeVars.contains(vari))
            this.capturedVars.add(vari);
        return null;
    }
}