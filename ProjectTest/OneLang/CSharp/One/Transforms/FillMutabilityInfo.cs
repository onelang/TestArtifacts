using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using One;
using One.Ast;

namespace One.Transforms
{
    public class FillMutabilityInfo : AstTransformer {
        public FillMutabilityInfo(): base("FillMutabilityInfo")
        {
            
        }
        
        protected IVariable getVar(VariableReference varRef) {
            var v = varRef.getVariable();
            v.mutability = v.mutability ?? new MutabilityInfo();
            return v;
        }
        
        protected override VariableReference visitVariableReference(VariableReference varRef) {
            this.getVar(varRef).mutability.unused = false;
            return null;
        }
        
        protected override Expression visitExpression(Expression expr) {
            base.visitExpression(expr);
            
            if (expr is BinaryExpression && ((BinaryExpression)expr).left is VariableReference && ((BinaryExpression)expr).operator_ == "=")
                this.getVar(((VariableReference)((BinaryExpression)expr).left)).mutability.reassigned = true;
            else if (expr is InstanceMethodCallExpression && ((InstanceMethodCallExpression)expr).object_ is VariableReference && ((InstanceMethodCallExpression)expr).method.attributes.hasKey("mutates"))
                this.getVar(((VariableReference)((InstanceMethodCallExpression)expr).object_)).mutability.mutated = true;
            return null;
        }
        
        protected override IVariable visitVariable(IVariable variable) {
            variable.mutability = variable.mutability ?? new MutabilityInfo();
            return null;
        }
    }
}