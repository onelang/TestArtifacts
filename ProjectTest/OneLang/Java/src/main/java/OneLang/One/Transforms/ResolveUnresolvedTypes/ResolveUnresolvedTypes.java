import java.util.Arrays;

public class ResolveUnresolvedTypes extends AstTransformer {
    public ResolveUnresolvedTypes()
    {
        super("ResolveUnresolvedTypes");
        
    }
    
    protected IType visitType(IType type) {
        super.visitType(type);
        if (type instanceof UnresolvedType) {
            if (this.currentInterface != null && Arrays.stream(this.currentInterface.getTypeArguments()).anyMatch(((UnresolvedType)type).typeName::equals))
                return new GenericsType(((UnresolvedType)type).typeName);
            
            var symbol = this.currentFile.availableSymbols.get(((UnresolvedType)type).typeName);
            if (symbol == null) {
                this.errorMan.throw_("Unresolved type '" + ((UnresolvedType)type).typeName + "' was not found in available symbols");
                return null;
            }
            
            if (symbol instanceof Class)
                return new ClassType(((Class)symbol), ((UnresolvedType)type).getTypeArguments());
            else if (symbol instanceof Interface)
                return new InterfaceType(((Interface)symbol), ((UnresolvedType)type).getTypeArguments());
            else if (symbol instanceof Enum)
                return new EnumType(((Enum)symbol));
            else {
                this.errorMan.throw_("Unknown symbol type: " + symbol);
                return null;
            }
        }
        else
            return null;
    }
    
    protected Expression visitExpression(Expression expr) {
        if (expr instanceof UnresolvedNewExpression) {
            var clsType = this.visitType(((UnresolvedNewExpression)expr).cls);
            if (clsType instanceof ClassType) {
                var newExpr = new NewExpression(((ClassType)clsType), ((UnresolvedNewExpression)expr).args);
                newExpr.parentNode = ((UnresolvedNewExpression)expr).parentNode;
                super.visitExpression(newExpr);
                return newExpr;
            }
            else {
                this.errorMan.throw_("Excepted ClassType, but got " + clsType);
                return null;
            }
        }
        else
            return super.visitExpression(expr);
    }
}