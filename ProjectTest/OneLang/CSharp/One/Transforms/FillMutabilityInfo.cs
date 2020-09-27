using One;
using One.Ast;

namespace One.Transforms
{
    public class FillMutabilityInfo : AstTransformer {
        public FillMutabilityInfo(): base("FillMutabilityInfo")
        {
            
        }
        
        protected IVariable getVar(VariableReference varRef)
        {
            var v = varRef.getVariable();
            v.mutability = v.mutability ?? new MutabilityInfo();
            return v;
        }
        
        protected override VariableReference visitVariableReference(VariableReference varRef)
        {
            this.getVar(varRef).mutability.unused = false;
            return null;
        }
        
        protected override VariableDeclaration visitVariableDeclaration(VariableDeclaration stmt)
        {
            base.visitVariableDeclaration(stmt);
            if (stmt.attributes != null && stmt.attributes.get("mutated") == "true")
                stmt.mutability.mutated = true;
            return null;
        }
        
        protected override Expression visitExpression(Expression expr)
        {
            base.visitExpression(expr);
            
            if (expr is BinaryExpression binExpr && binExpr.left is VariableReference varRef && binExpr.operator_ == "=")
                this.getVar(varRef).mutability.reassigned = true;
            else if (expr is InstanceMethodCallExpression instMethCallExpr && instMethCallExpr.object_ is VariableReference varRef2 && instMethCallExpr.method.attributes.hasKey("mutates"))
                this.getVar(varRef2).mutability.mutated = true;
            return null;
        }
        
        protected override IVariable visitVariable(IVariable variable)
        {
            variable.mutability = variable.mutability ?? new MutabilityInfo();
            return null;
        }
    }
}