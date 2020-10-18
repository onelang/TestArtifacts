public class LambdaCallExpression extends Expression {
    public Expression method;
    public Expression[] args;
    
    public LambdaCallExpression(Expression method, Expression[] args)
    {
        super();
        this.method = method;
        this.args = args;
    }
}