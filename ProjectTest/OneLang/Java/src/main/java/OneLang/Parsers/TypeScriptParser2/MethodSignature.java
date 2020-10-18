public class MethodSignature {
    public MethodParameter[] params;
    public Field[] fields;
    public Block body;
    public IType returns;
    public Expression[] superCallArgs;
    
    public MethodSignature(MethodParameter[] params, Field[] fields, Block body, IType returns, Expression[] superCallArgs)
    {
        this.params = params;
        this.fields = fields;
        this.body = body;
        this.returns = returns;
        this.superCallArgs = superCallArgs;
    }
}