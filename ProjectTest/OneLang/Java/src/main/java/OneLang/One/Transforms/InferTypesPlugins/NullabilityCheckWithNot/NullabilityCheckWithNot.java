public class NullabilityCheckWithNot extends InferTypesPlugin {
    public NullabilityCheckWithNot()
    {
        super("NullabilityCheckWithNot");
        
    }
    
    public Boolean canTransform(Expression expr) {
        return expr instanceof UnaryExpression ? ((UnaryExpression)expr).operator.equals("!") : false;
    }
    
    public Expression transform(Expression expr) {
        var unaryExpr = ((UnaryExpression)expr);
        if (unaryExpr.operator.equals("!")) {
            this.main.processExpression(expr);
            var type = unaryExpr.operand.actualType;
            var litTypes = this.main.currentFile.literalTypes;
            if (type instanceof ClassType && ((ClassType)type).decl != litTypes.boolean_.decl && ((ClassType)type).decl != litTypes.numeric.decl)
                return new BinaryExpression(unaryExpr.operand, "==", new NullLiteral());
        }
        
        return null;
    }
}