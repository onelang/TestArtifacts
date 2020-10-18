import java.util.List;

public class Lambda extends Expression implements IMethodBase {
    public IType returns;
    public List<IVariable> captures;
    
    MethodParameter[] parameters;
    public MethodParameter[] getParameters() { return this.parameters; }
    public void setParameters(MethodParameter[] value) { this.parameters = value; }
    
    Block body;
    public Block getBody() { return this.body; }
    public void setBody(Block value) { this.body = value; }
    
    Boolean throws_;
    public Boolean getThrows() { return this.throws_; }
    public void setThrows(Boolean value) { this.throws_ = value; }
    
    public Lambda(MethodParameter[] parameters, Block body)
    {
        super();
        this.setParameters(parameters);
        this.setBody(body);
        this.returns = null;
        this.captures = null;
    }
}