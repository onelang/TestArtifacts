using One;
using One.Ast;

namespace One.Transforms
{
    public class ResolveGenericTypeIdentifiers : AstTransformer {
        public ResolveGenericTypeIdentifiers(): base("ResolveGenericTypeIdentifiers")
        {
            
        }
        
        protected override Type_ visitType(Type_ type) {
            base.visitType(type);
            
            //console.log(type && type.constructor.name, JSON.stringify(type));
            if (type is UnresolvedType && ((this.currentInterface is Class && ((Class)this.currentInterface).typeArguments.includes(((UnresolvedType)type).typeName)) || (this.currentMethod is Method && ((Method)this.currentMethod).typeArguments.includes(((UnresolvedType)type).typeName))))
                return new GenericsType(((UnresolvedType)type).typeName);
            
            return null;
        }
    }
}