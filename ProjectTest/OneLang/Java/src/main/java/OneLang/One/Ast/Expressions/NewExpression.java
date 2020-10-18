public class NewExpression extends Expression {
    public ClassType cls;
    public Expression[] args;
    
    public NewExpression(ClassType cls, Expression[] args)
    {
        super();
        this.cls = cls;
        this.args = args;
    }
}