public class ForStatement extends Statement {
    public ForVariable itemVar;
    public Expression condition;
    public Expression incrementor;
    public Block body;
    
    public ForStatement(ForVariable itemVar, Expression condition, Expression incrementor, Block body)
    {
        super();
        this.itemVar = itemVar;
        this.condition = condition;
        this.incrementor = incrementor;
        this.body = body;
    }
}