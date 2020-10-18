public class UnresolvedMethodCallExpression extends Expression {
    public Expression object;
    public String methodName;
    public IType[] typeArgs;
    public Expression[] args;
    
    public UnresolvedMethodCallExpression(Expression object, String methodName, IType[] typeArgs, Expression[] args)
    {
        super();
        this.object = object;
        this.methodName = methodName;
        this.typeArgs = typeArgs;
        this.args = args;
    }
}