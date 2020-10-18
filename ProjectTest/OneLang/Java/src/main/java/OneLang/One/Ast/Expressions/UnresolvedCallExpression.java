public class UnresolvedCallExpression extends Expression {
    public Expression func;
    public IType[] typeArgs;
    public Expression[] args;
    
    public UnresolvedCallExpression(Expression func, IType[] typeArgs, Expression[] args)
    {
        super();
        this.func = func;
        this.typeArgs = typeArgs;
        this.args = args;
    }
}