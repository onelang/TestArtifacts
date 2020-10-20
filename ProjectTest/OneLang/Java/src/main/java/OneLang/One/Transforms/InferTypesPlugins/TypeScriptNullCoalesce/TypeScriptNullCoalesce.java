public class TypeScriptNullCoalesce extends InferTypesPlugin {
    public TypeScriptNullCoalesce()
    {
        super("TypeScriptNullCoalesce");
        
    }
    
    public Boolean canTransform(Expression expr) {
        return expr instanceof BinaryExpression && ((BinaryExpression)expr).operator.equals("||");
    }
    
    public Expression transform(Expression expr) {
        if (expr instanceof BinaryExpression && ((BinaryExpression)expr).operator.equals("||")) {
            var litTypes = this.main.currentFile.literalTypes;
            
            ((BinaryExpression)expr).left = this.main.runPluginsOn(((BinaryExpression)expr).left) != null ? this.main.runPluginsOn(((BinaryExpression)expr).left) : ((BinaryExpression)expr).left;
            var leftType = ((BinaryExpression)expr).left.getType();
            
            if (((BinaryExpression)expr).right instanceof ArrayLiteral && ((ArrayLiteral)((BinaryExpression)expr).right).items.length == 0) {
                if (leftType instanceof ClassType && ((ClassType)leftType).decl == litTypes.array.decl) {
                    ((ArrayLiteral)((BinaryExpression)expr).right).setActualType(((ClassType)leftType));
                    return new NullCoalesceExpression(((BinaryExpression)expr).left, ((ArrayLiteral)((BinaryExpression)expr).right));
                }
            }
            
            if (((BinaryExpression)expr).right instanceof MapLiteral && ((MapLiteral)((BinaryExpression)expr).right).items.length == 0) {
                if (leftType instanceof ClassType && ((ClassType)leftType).decl == litTypes.map.decl) {
                    ((MapLiteral)((BinaryExpression)expr).right).setActualType(((ClassType)leftType));
                    return new NullCoalesceExpression(((BinaryExpression)expr).left, ((MapLiteral)((BinaryExpression)expr).right));
                }
            }
            
            ((BinaryExpression)expr).right = this.main.runPluginsOn(((BinaryExpression)expr).right) != null ? this.main.runPluginsOn(((BinaryExpression)expr).right) : ((BinaryExpression)expr).right;
            var rightType = ((BinaryExpression)expr).right.getType();
            
            if (((BinaryExpression)expr).right instanceof NullLiteral)
                // something-which-can-be-undefined || null
                return ((BinaryExpression)expr).left;
            else if (TypeHelper.isAssignableTo(rightType, leftType) && !TypeHelper.equals(rightType, this.main.currentFile.literalTypes.boolean_))
                return new NullCoalesceExpression(((BinaryExpression)expr).left, ((BinaryExpression)expr).right);
        }
        return null;
    }
}