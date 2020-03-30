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
            if (type is UnresolvedType unrType) {
                var symbol = this.currentFile.availableSymbols.get(unrType.typeName);
                if (symbol == null) {
                    this.errorMan.throw_($"Unresolved type '{unrType.typeName}' was not found in available symbols");
                    return null;
                }
                
                if (symbol is Class class_)
                    return new ClassType(class_, unrType.typeArguments);
                else if (symbol is Interface int_)
                    return new InterfaceType(int_, unrType.typeArguments);
                else if (symbol is Enum_ enum_)
                    return new EnumType(enum_);
                else {
                    this.errorMan.throw_($"Unknown symbol type: {symbol}");
                    return null;
                }
            }
            else
                return null;
        }
        
        protected override Expression visitExpression(Expression expr) {
            if (expr is UnresolvedNewExpression unrNewExpr) {
                var clsType = this.visitType(unrNewExpr.cls);
                if (clsType is ClassType classType) {
                    var newExpr = new NewExpression(classType, unrNewExpr.args);
                    newExpr.parentNode = unrNewExpr.parentNode;
                    base.visitExpression(newExpr);
                    return newExpr;
                }
                else {
                    this.errorMan.throw_($"Excepted ClassType, but got {clsType}");
                    return null;
                }
            }
            else
                return base.visitExpression(expr);
        }
    }
}