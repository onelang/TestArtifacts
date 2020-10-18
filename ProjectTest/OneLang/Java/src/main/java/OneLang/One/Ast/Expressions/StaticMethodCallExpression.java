public class StaticMethodCallExpression extends Expression implements IMethodCallExpression {
    public Boolean isThisCall;
    
    Method method;
    public Method getMethod() { return this.method; }
    public void setMethod(Method value) { this.method = value; }
    
    IType[] typeArgs;
    public IType[] getTypeArgs() { return this.typeArgs; }
    public void setTypeArgs(IType[] value) { this.typeArgs = value; }
    
    Expression[] args;
    public Expression[] getArgs() { return this.args; }
    public void setArgs(Expression[] value) { this.args = value; }
    
    public StaticMethodCallExpression(Method method, IType[] typeArgs, Expression[] args, Boolean isThisCall)
    {
        super();
        this.setMethod(method);
        this.setTypeArgs(typeArgs);
        this.setArgs(args);
        this.isThisCall = isThisCall;
    }
}