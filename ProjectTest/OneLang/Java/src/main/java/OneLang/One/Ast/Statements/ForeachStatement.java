public class ForeachStatement extends Statement {
    public ForeachVariable itemVar;
    public Expression items;
    public Block body;
    
    public ForeachStatement(ForeachVariable itemVar, Expression items, Block body)
    {
        super();
        this.itemVar = itemVar;
        this.items = items;
        this.body = body;
    }
}