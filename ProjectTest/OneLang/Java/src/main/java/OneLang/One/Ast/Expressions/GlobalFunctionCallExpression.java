public class GlobalFunctionCallExpression extends Expression {
    public GlobalFunction func;
    public Expression[] args;
    
    public GlobalFunctionCallExpression(GlobalFunction func, Expression[] args)
    {
        super();
        this.func = func;
        this.args = args;
    }
}