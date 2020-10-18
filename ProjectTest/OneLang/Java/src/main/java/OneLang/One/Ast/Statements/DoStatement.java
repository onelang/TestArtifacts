public class DoStatement extends Statement {
    public Expression condition;
    public Block body;
    
    public DoStatement(Expression condition, Block body)
    {
        super();
        this.condition = condition;
        this.body = body;
    }
}