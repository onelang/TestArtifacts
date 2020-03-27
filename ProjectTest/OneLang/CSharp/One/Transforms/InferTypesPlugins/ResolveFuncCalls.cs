using System.Collections.Generic;
using System.Threading.Tasks;
using System;
using One.Transforms.InferTypesPlugins.Helpers;
using One.Ast;

namespace One.Transforms.InferTypesPlugins
{
    public class ResolveFuncCalls : InferTypesPlugin {
        public ResolveFuncCalls(): base("ResolveFuncCalls") {
            
        }
        
        public override bool canTransform(Expression expr) {
            return expr is UnresolvedCallExpression;
        }
        
        public override Expression transform(Expression expr) {
            var callExpr = ((UnresolvedCallExpression)expr);
            if (callExpr.func is GlobalFunctionReference) {
                var newExpr = new GlobalFunctionCallExpression(((GlobalFunctionReference)callExpr.func).decl, callExpr.args);
                callExpr.args = callExpr.args.map((Expression arg) => { return this.main.runPluginsOn(arg) ?? arg; });
                newExpr.setActualType(((GlobalFunctionReference)callExpr.func).decl.returns);
                return newExpr;
            }
            else {
                this.main.processExpression(expr);
                if (callExpr.func.actualType is LambdaType) {
                    var newExpr = new LambdaCallExpression(callExpr.func, callExpr.args);
                    newExpr.setActualType(((LambdaType)callExpr.func.actualType).returnType);
                    return newExpr;
                }
                else
                    return expr;
            }
        }
    }
}