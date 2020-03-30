using One.Transforms.InferTypesPlugins.Helpers;
using One.Ast;

namespace One.Transforms.InferTypesPlugins
{
    public class ResolveFuncCalls : InferTypesPlugin {
        public ResolveFuncCalls(): base("ResolveFuncCalls")
        {
            
        }
        
        public override bool canTransform(Expression expr) {
            return expr is UnresolvedCallExpression;
        }
        
        public override Expression transform(Expression expr) {
            var callExpr = ((UnresolvedCallExpression)expr);
            if (callExpr.func is GlobalFunctionReference globFunctRef) {
                var newExpr = new GlobalFunctionCallExpression(globFunctRef.decl, callExpr.args);
                callExpr.args = callExpr.args.map((Expression arg) => { return this.main.runPluginsOn(arg) ?? arg; });
                newExpr.setActualType(globFunctRef.decl.returns);
                return newExpr;
            }
            else {
                this.main.processExpression(expr);
                if (callExpr.func.actualType is LambdaType lambdType) {
                    var newExpr = new LambdaCallExpression(callExpr.func, callExpr.args);
                    newExpr.setActualType(lambdType.returnType);
                    return newExpr;
                }
                else
                    return expr;
            }
        }
    }
}