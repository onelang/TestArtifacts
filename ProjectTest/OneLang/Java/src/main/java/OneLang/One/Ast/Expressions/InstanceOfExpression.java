import java.util.List;

public class InstanceOfExpression extends Expression {
    public Expression expr;
    public IType checkType;
    public List<CastExpression> implicitCasts;
    public String alias;
    
    public InstanceOfExpression(Expression expr, IType checkType)
    {
        super();
        this.expr = expr;
        this.checkType = checkType;
        this.implicitCasts = null;
        this.alias = null;
    }
}