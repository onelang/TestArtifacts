using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;
using One.Transforms.InferTypesPlugins.Helpers;
using One.Ast;

namespace One.Transforms.InferTypesPlugins
{
    public class ResolveMethodCalls : InferTypesPlugin {
        public ResolveMethodCalls(): base("ResolveMethodCalls")
        {
            
        }
        
        protected Method findMethod(IInterface cls, string methodName, bool isStatic, Expression[] args) {
            var allBases = cls is Class ? ((Class)cls).getAllBaseInterfaces().filter((IInterface x) => { return x is Class; }) : cls.getAllBaseInterfaces();
            
            var allMethods = new List<Method>();
            foreach (var base_ in allBases)
                foreach (var method in base_.methods)
                    allMethods.push(method);
            
            var methods = allMethods.filter((Method m) => { var minLen = m.parameters.filter((MethodParameter p) => { return p.initializer == null; }).length();
            var maxLen = m.parameters.length();
            var match = m.name == methodName && m.isStatic == isStatic && minLen <= args.length() && args.length() <= maxLen;
            return match; });
            
            if (methods.length() == 0)
                throw new Error($"Method '{methodName}' was not found on type '{cls.name}' with {args.length()} arguments");
            else if (methods.length() > 1) {
                // TODO: actually we should implement proper method shadowing here...
                var thisMethods = methods.filter((Method x) => { return x.parentInterface == cls; });
                if (thisMethods.length() == 1)
                    return thisMethods.get(0);
                throw new Error($"Multiple methods found with name '{methodName}' and {args.length()} arguments on type '{cls.name}'");
            }
            return methods.get(0);
        }
        
        protected void resolveReturnType(IMethodCallExpression expr, GenericsResolver genericsResolver) {
            genericsResolver.collectFromMethodCall(expr);
            
            for (int i = 0; i < expr.args.length(); i++) {
                // actually doesn't have to resolve, but must check if generic type confirm the previous argument with the same generic type
                var paramType = genericsResolver.resolveType(expr.method.parameters.get(i).type, false);
                if (paramType != null)
                    expr.args.get(i).setExpectedType(paramType);
                expr.args.set(i, this.main.runPluginsOn(expr.args.get(i)));
                genericsResolver.collectResolutionsFromActualType(paramType, expr.args.get(i).actualType);
            }
            
            if (expr.method.returns == null) {
                this.errorMan.throw_($"Method ({expr.method.parentInterface.name}::{expr.method.name}) return type was not specified or infered before the call.");
                return;
            }
            
            expr.setActualType(genericsResolver.resolveType(expr.method.returns, true), true, expr is InstanceMethodCallExpression && Type_.isGeneric(((InstanceMethodCallExpression)expr).object_.getType()));
        }
        
        protected Expression transformMethodCall(UnresolvedMethodCallExpression expr) {
            if (expr.object_ is ClassReference || expr.object_ is StaticThisReference) {
                var cls = expr.object_ is ClassReference ? ((ClassReference)expr.object_).decl : expr.object_ is StaticThisReference ? ((StaticThisReference)expr.object_).cls : null;
                var method = this.findMethod(cls, expr.methodName, true, expr.args);
                var result = new StaticMethodCallExpression(method, expr.typeArgs, expr.args, expr.object_ is StaticThisReference);
                this.resolveReturnType(result, new GenericsResolver());
                return result;
            }
            else {
                var resolvedObject = expr.object_.actualType != null ? expr.object_ : this.main.runPluginsOn(expr.object_) ?? expr.object_;
                var objectType = resolvedObject.getType();
                var intfType = objectType is ClassType ? ((IInterface)((ClassType)objectType).decl) : objectType is InterfaceType ? ((InterfaceType)objectType).decl : null;
                
                if (intfType != null) {
                    var method = this.findMethod(intfType, expr.methodName, false, expr.args);
                    var result = new InstanceMethodCallExpression(resolvedObject, method, expr.typeArgs, expr.args);
                    this.resolveReturnType(result, GenericsResolver.fromObject(resolvedObject));
                    return result;
                }
                else if (objectType is AnyType) {
                    expr.setActualType(AnyType.instance);
                    return expr;
                }
                else { }
                return resolvedObject;
            }
        }
        
        public override bool canTransform(Expression expr) {
            return expr is UnresolvedMethodCallExpression && !(((UnresolvedMethodCallExpression)expr).actualType is AnyType);
        }
        
        public override Expression transform(Expression expr) {
            return this.transformMethodCall(((UnresolvedMethodCallExpression)expr));
        }
    }
}