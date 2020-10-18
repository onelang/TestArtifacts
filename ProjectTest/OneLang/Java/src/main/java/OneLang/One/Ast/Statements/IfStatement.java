public class IfStatement extends Statement {
    public Expression condition;
    public Block then;
    public Block else_;
    
    public IfStatement(Expression condition, Block then, Block else_)
    {
        super();
        this.condition = condition;
        this.then = then;
        this.else_ = else_;
    }
}