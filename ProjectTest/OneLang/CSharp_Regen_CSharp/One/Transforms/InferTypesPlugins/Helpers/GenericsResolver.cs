using One.Ast;

namespace One.Transforms.InferTypesPlugins.Helpers
{
    public class GenericsResolver {
        public Map<string, Type_> resolutionMap;
        
        public GenericsResolver()
        {
            this.resolutionMap = new Map<string, Type_>();
        }
        
        public static GenericsResolver fromObject(Expression object_) {
            var resolver = new GenericsResolver();
            resolver.collectClassGenericsFromObject(object_);
            return resolver;
        }
        
        public void addResolution(string typeVarName, Type_ actualType) {
            var prevRes = this.resolutionMap.get(typeVarName);
            if (prevRes != null && !Type_.equals(prevRes, actualType))
                throw new Error($"Resolving '{typeVarName}' is ambiguous, {prevRes.repr()} <> {actualType.repr()}");
            this.resolutionMap.set(typeVarName, actualType);
        }
        
        public void collectFromMethodCall(IMethodCallExpression methodCall) {
            if (methodCall.typeArgs.length() == 0)
                return;
            if (methodCall.typeArgs.length() != methodCall.method.typeArguments.length())
                throw new Error($"Expected {methodCall.method.typeArguments.length()} type argument(s) for method call, but got {methodCall.typeArgs.length()}");
            for (int i = 0; i < methodCall.typeArgs.length(); i++)
                this.addResolution(methodCall.method.typeArguments.get(i), methodCall.typeArgs.get(i));
        }
        
        public void collectClassGenericsFromObject(Expression actualObject) {
            var actualType = actualObject.getType();
            if (actualType is ClassType) {
                if (!this.collectResolutionsFromActualType(((ClassType)actualType).decl.type, ((ClassType)actualType))) { }
            }
            else if (actualType is InterfaceType) {
                if (!this.collectResolutionsFromActualType(((InterfaceType)actualType).decl.type, ((InterfaceType)actualType))) { }
            }
            else
                throw new Error($"Expected ClassType or InterfaceType, got {(actualType != null ? actualType.repr() : "<null>")}");
        }
        
        public bool collectResolutionsFromActualType(Type_ genericType, Type_ actualType) {
            if (!Type_.isGeneric(genericType))
                return true;
            if (genericType is GenericsType) {
                this.addResolution(((GenericsType)genericType).typeVarName, actualType);
                return true;
            }
            else if (genericType is ClassType && actualType is ClassType && ((ClassType)genericType).decl == ((ClassType)actualType).decl) {
                if (((ClassType)genericType).typeArguments.length() != ((ClassType)actualType).typeArguments.length())
                    throw new Error($"Same class ({((ClassType)genericType).repr()}) used with different number of type arguments ({((ClassType)genericType).typeArguments.length()} <> {((ClassType)actualType).typeArguments.length()})");
                return ((ClassType)genericType).typeArguments.every((Type_ x, int i) => { return this.collectResolutionsFromActualType(x, ((ClassType)actualType).typeArguments.get(i)); });
            }
            else if (genericType is InterfaceType && actualType is InterfaceType && ((InterfaceType)genericType).decl == ((InterfaceType)actualType).decl) {
                if (((InterfaceType)genericType).typeArguments.length() != ((InterfaceType)actualType).typeArguments.length())
                    throw new Error($"Same class ({((InterfaceType)genericType).repr()}) used with different number of type arguments ({((InterfaceType)genericType).typeArguments.length()} <> {((InterfaceType)actualType).typeArguments.length()})");
                return ((InterfaceType)genericType).typeArguments.every((Type_ x, int i) => { return this.collectResolutionsFromActualType(x, ((InterfaceType)actualType).typeArguments.get(i)); });
            }
            else if (genericType is LambdaType && actualType is LambdaType) {
                if (((LambdaType)genericType).parameters.length() != ((LambdaType)actualType).parameters.length())
                    throw new Error($"Generic lambda type has {((LambdaType)genericType).parameters.length()} parameters while the actual type has {((LambdaType)actualType).parameters.length()}");
                var paramsOk = ((LambdaType)genericType).parameters.every((MethodParameter x, int i) => { return this.collectResolutionsFromActualType(x.type, ((LambdaType)actualType).parameters.get(i).type); });
                var resultOk = this.collectResolutionsFromActualType(((LambdaType)genericType).returnType, ((LambdaType)actualType).returnType);
                return paramsOk && resultOk;
            }
            else if (genericType is EnumType && actualType is EnumType && ((EnumType)genericType).decl == ((EnumType)actualType).decl) { }
            else if (genericType is AnyType || actualType is AnyType) { }
            else
                throw new Error($"Generic type {genericType.repr()} is not compatible with actual type {actualType.repr()}");
            return false;
        }
        
        public Type_ resolveType(Type_ type, bool mustResolveAllGenerics) {
            if (type is GenericsType) {
                var resolvedType = this.resolutionMap.get(((GenericsType)type).typeVarName);
                if (resolvedType == null && mustResolveAllGenerics)
                    throw new Error($"Could not resolve generics type: {((GenericsType)type).repr()}");
                return resolvedType != null ? resolvedType : ((GenericsType)type);
            }
            else if (type is ClassType)
                return new ClassType(((ClassType)type).decl, ((ClassType)type).typeArguments.map((Type_ x) => { return this.resolveType(x, mustResolveAllGenerics); }));
            else if (type is InterfaceType)
                return new InterfaceType(((InterfaceType)type).decl, ((InterfaceType)type).typeArguments.map((Type_ x) => { return this.resolveType(x, mustResolveAllGenerics); }));
            else if (type is LambdaType)
                return new LambdaType(((LambdaType)type).parameters.map((MethodParameter x) => { return new MethodParameter(x.name, this.resolveType(x.type, mustResolveAllGenerics), x.initializer); }), this.resolveType(((LambdaType)type).returnType, mustResolveAllGenerics));
            else
                return type;
        }
    }
}