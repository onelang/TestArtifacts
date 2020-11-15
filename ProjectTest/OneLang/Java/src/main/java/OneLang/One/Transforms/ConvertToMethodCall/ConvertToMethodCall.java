package OneLang.One.Transforms.ConvertToMethodCall;

import OneLang.One.Ast.Expressions.Expression;
import OneLang.One.Ast.Expressions.ElementAccessExpression;
import OneLang.One.Ast.Expressions.UnresolvedCallExpression;
import OneLang.One.Ast.Expressions.PropertyAccessExpression;
import OneLang.One.Ast.Expressions.BinaryExpression;
import OneLang.One.Ast.Expressions.StringLiteral;
import OneLang.One.AstTransformer.AstTransformer;

import OneLang.One.AstTransformer.AstTransformer;
import OneLang.One.Ast.Expressions.Expression;
import OneLang.One.Ast.Expressions.BinaryExpression;
import OneStd.Objects;
import OneLang.One.Ast.Expressions.UnresolvedCallExpression;
import OneLang.One.Ast.Expressions.PropertyAccessExpression;
import OneLang.One.Ast.Interfaces.IType;

public class ConvertToMethodCall extends AstTransformer {
    public ConvertToMethodCall()
    {
        super("ConvertToMethodCall");
        
    }
    
    protected Expression visitExpression(Expression expr) {
        var origExpr = expr;
        var visitExpressionResult = super.visitExpression(expr);
        
        expr = visitExpressionResult != null ? visitExpressionResult : expr;
        
        if (expr instanceof BinaryExpression && Objects.equals(((BinaryExpression)expr).operator, "in"))
            expr = new UnresolvedCallExpression(new PropertyAccessExpression(((BinaryExpression)expr).right, "hasKey"), new IType[0], new Expression[] { ((BinaryExpression)expr).left });
        
        expr.parentNode = origExpr.parentNode;
        return expr;
    }
}