using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;
using One.Transforms.InferTypesPlugins.Helpers;
using One.Ast;

namespace One.Transforms.InferTypesPlugins
{
    public class LambdaResolver : InferTypesPlugin {
        public LambdaResolver(): base("LambdaResolver")
        {
            
        }
        
        protected void setupLambdaParameterTypes(Lambda lambda) {
            if (lambda.expectedType == null)
                return;
            
            if (lambda.expectedType is LambdaType) {
                var declParams = ((LambdaType)lambda.expectedType).parameters;
                if (declParams.length() != lambda.parameters.length())
                    this.errorMan.throw_($"Expected {lambda.parameters.length()} parameters for lambda, but got {declParams.length()}!");
                else
                    for (int i = 0; i < declParams.length(); i++) {
                        if (lambda.parameters.get(i).type == null)
                            lambda.parameters.get(i).type = declParams.get(i).type;
                        else if (!Type_.isAssignableTo(lambda.parameters.get(i).type, declParams.get(i).type))
                            this.errorMan.throw_($"Parameter type {lambda.parameters.get(i).type.repr()} cannot be assigned to {declParams.get(i).type.repr()}.");
                    }
            }
            else
                this.errorMan.throw_("Expected LambdaType as Lambda's type!");
        }
        
        protected void processReturnExpr(Expression returnExpr) {
            
        }
        
        protected void visitLambda(Lambda lambda) {
            this.setupLambdaParameterTypes(lambda);
        }
        
        public override bool canTransform(Expression expr) {
            return expr is Lambda;
        }
        
        public override Expression transform(Expression expr) {
            this.visitLambda(((Lambda)expr));
            // does not transform actually
            return null;
        }
    }
}