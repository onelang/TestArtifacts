public class UnresolvedNewExpression extends Expression {
    public UnresolvedType cls;
    public Expression[] args;
    
    public UnresolvedNewExpression(UnresolvedType cls, Expression[] args)
    {
        super();
        this.cls = cls;
        this.args = args;
    }
}