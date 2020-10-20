import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class ResolveElementAccess extends InferTypesPlugin {
    public ResolveElementAccess()
    {
        super("ResolveElementAccess");
        
    }
    
    public Boolean canTransform(Expression expr) {
        var isSet = expr instanceof BinaryExpression && ((BinaryExpression)expr).left instanceof ElementAccessExpression && new ArrayList<>(List.of("=", "+=", "-=")).stream().anyMatch(((BinaryExpression)expr).operator::equals);
        return expr instanceof ElementAccessExpression || isSet;
    }
    
    public Boolean isMapOrArrayType(IType type) {
        return TypeHelper.isAssignableTo(type, this.main.currentFile.literalTypes.map) || Arrays.stream(this.main.currentFile.arrayTypes).anyMatch(x -> TypeHelper.isAssignableTo(type, x));
    }
    
    public Expression transform(Expression expr) {
        if (expr instanceof BinaryExpression && ((BinaryExpression)expr).left instanceof ElementAccessExpression) {
            ((ElementAccessExpression)((BinaryExpression)expr).left).object = this.main.runPluginsOn(((ElementAccessExpression)((BinaryExpression)expr).left).object);
            if (this.isMapOrArrayType(((ElementAccessExpression)((BinaryExpression)expr).left).object.getType())) {
                var right = ((BinaryExpression)expr).operator.equals("=") ? ((BinaryExpression)expr).right : new BinaryExpression(((Expression)((ElementAccessExpression)((BinaryExpression)expr).left).copy()), ((BinaryExpression)expr).operator.equals("+=") ? "+" : "-", ((BinaryExpression)expr).right);
                return new UnresolvedMethodCallExpression(((ElementAccessExpression)((BinaryExpression)expr).left).object, "set", new IType[0], new Expression[] { ((ElementAccessExpression)((BinaryExpression)expr).left).elementExpr, right });
            }
        }
        else if (expr instanceof ElementAccessExpression) {
            ((ElementAccessExpression)expr).object = this.main.runPluginsOn(((ElementAccessExpression)expr).object);
            if (this.isMapOrArrayType(((ElementAccessExpression)expr).object.getType()))
                return new UnresolvedMethodCallExpression(((ElementAccessExpression)expr).object, "get", new IType[0], new Expression[] { ((ElementAccessExpression)expr).elementExpr });
            else if (((ElementAccessExpression)expr).elementExpr instanceof StringLiteral)
                return new PropertyAccessExpression(((ElementAccessExpression)expr).object, ((StringLiteral)((ElementAccessExpression)expr).elementExpr).stringValue);
        }
        return expr;
    }
}