public class InstanceMethodCallExpression extends Expression implements IMethodCallExpression {
    public Expression object;
    
    Method method;
    public Method getMethod() { return this.method; }
    public void setMethod(Method value) { this.method = value; }
    
    IType[] typeArgs;
    public IType[] getTypeArgs() { return this.typeArgs; }
    public void setTypeArgs(IType[] value) { this.typeArgs = value; }
    
    Expression[] args;
    public Expression[] getArgs() { return this.args; }
    public void setArgs(Expression[] value) { this.args = value; }
    
    public InstanceMethodCallExpression(Expression object, Method method, IType[] typeArgs, Expression[] args)
    {
        super();
        this.object = object;
        this.setMethod(method);
        this.setTypeArgs(typeArgs);
        this.setArgs(args);
    }
}