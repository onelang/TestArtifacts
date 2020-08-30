using One.Ast;

namespace One.Ast
{
    public interface IHasTypeArguments {
        Type_[] typeArguments { get; set; }
    }
    
    public interface IInterfaceType : IType {
        Type_[] typeArguments { get; set; }
        
        IInterface getDecl();
    }
    
    public class Type_ : IType {
        public static bool isGeneric(Type_ type) {
            if (type is GenericsType)
                return true;
            else if (type is ClassType classType)
                return classType.typeArguments.some(x => Type_.isGeneric(x));
            else if (type is InterfaceType intType)
                return intType.typeArguments.some(x => Type_.isGeneric(x));
            else if (type is LambdaType lambdType)
                return lambdType.parameters.some(x => Type_.isGeneric(x.type)) || Type_.isGeneric(lambdType.returnType);
            else
                return false;
        }
        
        public static bool equals(Type_ type1, Type_ type2) {
            if (type1 == null || type2 == null)
                throw new Error("Type is missing!");
            if (type1 is VoidType && type2 is VoidType)
                return true;
            if (type1 is AnyType && type2 is AnyType)
                return true;
            if (type1 is GenericsType genType && type2 is GenericsType genType2)
                return genType.typeVarName == genType2.typeVarName;
            if (type1 is EnumType enumType && type2 is EnumType enumType2)
                return enumType.decl == enumType2.decl;
            if (type1 is LambdaType lambdType2 && type2 is LambdaType lambdType3)
                return Type_.equals(lambdType2.returnType, lambdType3.returnType) && lambdType2.parameters.length() == lambdType3.parameters.length() && lambdType2.parameters.every((t, i) => Type_.equals(t.type, lambdType3.parameters.get(i).type));
            if (type1 is ClassType classType2 && type2 is ClassType classType3)
                return classType2.decl == classType3.decl && classType2.typeArguments.length() == classType3.typeArguments.length() && classType2.typeArguments.every((t, i) => Type_.equals(t, classType3.typeArguments.get(i)));
            if (type1 is InterfaceType intType2 && type2 is InterfaceType intType3)
                return intType2.decl == intType3.decl && intType2.typeArguments.length() == intType3.typeArguments.length() && intType2.typeArguments.every((t, i) => Type_.equals(t, intType3.typeArguments.get(i)));
            return false;
        }
        
        public static bool isAssignableTo(Type_ toBeAssigned, Type_ whereTo) {
            // AnyType can assigned to any type except to void
            if (toBeAssigned is AnyType && !(whereTo is VoidType))
                return true;
            // any type can assigned to AnyType except void
            if (whereTo is AnyType && !(toBeAssigned is VoidType))
                return true;
            // any type can assigned to GenericsType except void
            if (whereTo is GenericsType && !(toBeAssigned is VoidType))
                return true;
            // null can be assigned anywhere
            // TODO: filter out number and boolean types...
            if (toBeAssigned is NullType && !(whereTo is VoidType))
                return true;
            
            if (Type_.equals(toBeAssigned, whereTo))
                return true;
            
            if (toBeAssigned is ClassType classType4 && whereTo is ClassType classType5)
                return (classType4.decl.baseClass != null && Type_.isAssignableTo(classType4.decl.baseClass, classType5)) || classType4.decl == classType5.decl && classType4.typeArguments.every((x, i) => Type_.isAssignableTo(x, classType5.typeArguments.get(i)));
            if (toBeAssigned is ClassType classType6 && whereTo is InterfaceType intType4)
                return (classType6.decl.baseClass != null && Type_.isAssignableTo(classType6.decl.baseClass, intType4)) || classType6.decl.baseInterfaces.some(x => Type_.isAssignableTo(x, intType4));
            if (toBeAssigned is InterfaceType intType5 && whereTo is InterfaceType intType6)
                return intType5.decl.baseInterfaces.some(x => Type_.isAssignableTo(x, intType6)) || intType5.decl == intType6.decl && intType5.typeArguments.every((x, i) => Type_.isAssignableTo(x, intType6.typeArguments.get(i)));
            if (toBeAssigned is LambdaType lambdType4 && whereTo is LambdaType lambdType5)
                return lambdType4.parameters.length() == lambdType5.parameters.length() && lambdType4.parameters.every((p, i) => Type_.isAssignableTo(p.type, lambdType5.parameters.get(i).type)) && (Type_.isAssignableTo(lambdType4.returnType, lambdType5.returnType) || lambdType5.returnType is GenericsType);
            
            return false;
        }
        
        public virtual string repr() {
            return "U:UNKNOWN";
        }
    }
    
    public class TypeHelper {
        public static string argsRepr(Type_[] args) {
            return args.length() == 0 ? "" : $"<{args.map(x => x.repr()).join(", ")}>";
        }
    }
    
    public class PrimitiveType : Type_ {
        
    }
    
    public class VoidType : PrimitiveType {
        public static VoidType instance;
        
        static VoidType()
        {
            VoidType.instance = new VoidType();
        }
        
        public override string repr() {
            return "Void";
        }
    }
    
    public class AnyType : PrimitiveType {
        public static AnyType instance;
        
        static AnyType()
        {
            AnyType.instance = new AnyType();
        }
        
        public override string repr() {
            return "Any";
        }
    }
    
    public class NullType : PrimitiveType {
        public static NullType instance;
        
        static NullType()
        {
            NullType.instance = new NullType();
        }
        
        public override string repr() {
            return "Null";
        }
    }
    
    public class GenericsType : Type_ {
        public string typeVarName;
        
        public GenericsType(string typeVarName): base()
        {
            this.typeVarName = typeVarName;
        }
        
        public override string repr() {
            return $"G:{this.typeVarName}";
        }
    }
    
    public class EnumType : Type_ {
        public Enum_ decl;
        
        public EnumType(Enum_ decl): base()
        {
            this.decl = decl;
        }
        
        public override string repr() {
            return $"E:{this.decl.name}";
        }
    }
    
    public class InterfaceType : Type_, IHasTypeArguments, IInterfaceType {
        public Interface decl;
        public Type_[] typeArguments { get; set; }
        
        public InterfaceType(Interface decl, Type_[] typeArguments): base()
        {
            this.decl = decl;
            this.typeArguments = typeArguments;
        }
        
        public IInterface getDecl() {
            return this.decl;
        }
        
        public override string repr() {
            return $"I:{this.decl.name}{TypeHelper.argsRepr(this.typeArguments)}";
        }
    }
    
    public class ClassType : Type_, IHasTypeArguments, IInterfaceType {
        public Class decl;
        public Type_[] typeArguments { get; set; }
        
        public ClassType(Class decl, Type_[] typeArguments): base()
        {
            this.decl = decl;
            this.typeArguments = typeArguments;
        }
        
        public IInterface getDecl() {
            return this.decl;
        }
        
        public override string repr() {
            return $"C:{this.decl.name}{TypeHelper.argsRepr(this.typeArguments)}";
        }
    }
    
    public class UnresolvedType : Type_, IHasTypeArguments {
        public string typeName;
        public Type_[] typeArguments { get; set; }
        
        public UnresolvedType(string typeName, Type_[] typeArguments): base()
        {
            this.typeName = typeName;
            this.typeArguments = typeArguments;
        }
        
        public override string repr() {
            return $"X:{this.typeName}{TypeHelper.argsRepr(this.typeArguments)}";
        }
    }
    
    public class LambdaType : Type_ {
        public MethodParameter[] parameters;
        public Type_ returnType;
        
        public LambdaType(MethodParameter[] parameters, Type_ returnType): base()
        {
            this.parameters = parameters;
            this.returnType = returnType;
            if (returnType == null) { }
        }
        
        public override string repr() {
            return $"L:({this.parameters.map(x => x.type.repr()).join(", ")})=>{this.returnType.repr()}";
        }
    }
}