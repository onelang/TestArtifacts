public interface IMethodCallExpression extends IExpression {
    Method getMethod();
    void setMethod(Method value);
    
    IType[] getTypeArgs();
    void setTypeArgs(IType[] value);
    
    Expression[] getArgs();
    void setArgs(Expression[] value);
}