using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;
using One;
using One.Ast;

namespace One.Transforms
{
    public class ResolveUnresolvedTypes : AstTransformer {
        public ResolveUnresolvedTypes(): base("ResolveUnresolvedTypes")
        {
            
        }
        
        protected override Type_ visitType(Type_ type) {
            base.visitType(type);
            if (type is UnresolvedType) {
                var symbol = this.currentFile.availableSymbols.get(((UnresolvedType)type).typeName);
                if (symbol == null) {
                    this.errorMan.throw_($"Unresolved type '{((UnresolvedType)type).typeName}' was not found in available symbols");
                    return null;
                }
                
                if (symbol is Class)
                    return new ClassType(((Class)symbol), ((UnresolvedType)type).typeArguments);
                else if (symbol is Interface)
                    return new InterfaceType(((Interface)symbol), ((UnresolvedType)type).typeArguments);
                else if (symbol is Enum_)
                    return new EnumType(((Enum_)symbol));
                else {
                    this.errorMan.throw_($"Unknown symbol type: {symbol}");
                    return null;
                }
            }
            else
                return null;
        }
        
        protected override Expression visitExpression(Expression expr) {
            if (expr is UnresolvedNewExpression) {
                var classType = this.visitType(((UnresolvedNewExpression)expr).cls);
                if (classType is ClassType) {
                    var newExpr = new NewExpression(((ClassType)classType), ((UnresolvedNewExpression)expr).args);
                    newExpr.parentNode = ((UnresolvedNewExpression)expr).parentNode;
                    base.visitExpression(newExpr);
                    return newExpr;
                }
                else {
                    this.errorMan.throw_($"Excepted ClassType, but got {classType}");
                    return null;
                }
            }
            else
                return base.visitExpression(expr);
        }
    }
}