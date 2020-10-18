public class WhileStatement extends Statement {
    public Expression condition;
    public Block body;
    
    public WhileStatement(Expression condition, Block body)
    {
        super();
        this.condition = condition;
        this.body = body;
    }
}