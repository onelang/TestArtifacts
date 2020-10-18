public class TryStatement extends Statement {
    public Block tryBody;
    public CatchVariable catchVar;
    public Block catchBody;
    public Block finallyBody;
    
    public TryStatement(Block tryBody, CatchVariable catchVar, Block catchBody, Block finallyBody)
    {
        super();
        this.tryBody = tryBody;
        this.catchVar = catchVar;
        this.catchBody = catchBody;
        this.finallyBody = finallyBody;
        if (this.catchBody == null && this.finallyBody == null)
            throw new Error("try without catch and finally is not allowed");
    }
}