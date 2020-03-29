using System.Collections.Generic;
using One.Ast;

namespace One.Ast
{
    public interface IType {
        string repr();
    }
    
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
            else if (type is ClassType)
                return ((ClassType)type).typeArguments.some((Type_ x) => { return Type_.isGeneric(x); });
            else if (type is InterfaceType)
                return ((InterfaceType)type).typeArguments.some((Type_ x) => { return Type_.isGeneric(x); });
            else if (type is LambdaType)
                return ((LambdaType)type).parameters.some((MethodParameter x) => { return Type_.isGeneric(x.type); }) || Type_.isGeneric(((LambdaType)type).returnType);
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
            if (type1 is GenericsType && type2 is GenericsType)
                return ((GenericsType)type1).typeVarName == ((GenericsType)type2).typeVarName;
            if (type1 is EnumType && type2 is EnumType)
                return ((EnumType)type1).decl == ((EnumType)type2).decl;
            if (type1 is LambdaType && type2 is LambdaType)
                return Type_.equals(((LambdaType)type1).returnType, ((LambdaType)type2).returnType) && ((LambdaType)type1).parameters.length() == ((LambdaType)type2).parameters.length() && ((LambdaType)type1).parameters.every((MethodParameter t, int i) => { return Type_.equals(t.type, ((LambdaType)type2).parameters.get(i).type); });
            if (type1 is ClassType && type2 is ClassType)
                return ((ClassType)type1).decl == ((ClassType)type2).decl && ((ClassType)type1).typeArguments.length() == ((ClassType)type2).typeArguments.length() && ((ClassType)type1).typeArguments.every((Type_ t, int i) => { return Type_.equals(t, ((ClassType)type2).typeArguments.get(i)); });
            if (type1 is InterfaceType && type2 is InterfaceType)
                return ((InterfaceType)type1).decl == ((InterfaceType)type2).decl && ((InterfaceType)type1).typeArguments.length() == ((InterfaceType)type2).typeArguments.length() && ((InterfaceType)type1).typeArguments.every((Type_ t, int i) => { return Type_.equals(t, ((InterfaceType)type2).typeArguments.get(i)); });
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
            
            if (toBeAssigned is ClassType && whereTo is ClassType)
                return (((ClassType)toBeAssigned).decl.baseClass != null && Type_.isAssignableTo(((ClassType)toBeAssigned).decl.baseClass, ((ClassType)whereTo))) || ((ClassType)toBeAssigned).decl == ((ClassType)whereTo).decl && ((ClassType)toBeAssigned).typeArguments.every((Type_ x, int i) => { return Type_.isAssignableTo(x, ((ClassType)whereTo).typeArguments.get(i)); });
            if (toBeAssigned is ClassType && whereTo is InterfaceType)
                return (((ClassType)toBeAssigned).decl.baseClass != null && Type_.isAssignableTo(((ClassType)toBeAssigned).decl.baseClass, ((InterfaceType)whereTo))) || ((ClassType)toBeAssigned).decl.baseInterfaces.some((Type_ x) => { return Type_.isAssignableTo(x, ((InterfaceType)whereTo)); });
            if (toBeAssigned is InterfaceType && whereTo is InterfaceType)
                return ((InterfaceType)toBeAssigned).decl.baseInterfaces.some((Type_ x) => { return Type_.isAssignableTo(x, ((InterfaceType)whereTo)); }) || ((InterfaceType)toBeAssigned).decl == ((InterfaceType)whereTo).decl && ((InterfaceType)toBeAssigned).typeArguments.every((Type_ x, int i) => { return Type_.isAssignableTo(x, ((InterfaceType)whereTo).typeArguments.get(i)); });
            if (toBeAssigned is LambdaType && whereTo is LambdaType)
                return ((LambdaType)toBeAssigned).parameters.length() == ((LambdaType)whereTo).parameters.length() && ((LambdaType)toBeAssigned).parameters.every((MethodParameter p, int i) => { return Type_.isAssignableTo(p.type, ((LambdaType)whereTo).parameters.get(i).type); }) && (Type_.isAssignableTo(((LambdaType)toBeAssigned).returnType, ((LambdaType)whereTo).returnType) || ((LambdaType)whereTo).returnType is GenericsType);
            
            return false;
        }
        
        public virtual string repr() {
            return "U:UNKNOWN";
        }
    }
    
    public class TypeHelper {
        public static string argsRepr(Type_[] args) {
            return args.length() == 0 ? "" : $"<{args.map((Type_ x) => { return x.repr(); }).join(", ")}>";
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
            return $"L:({this.parameters.map((MethodParameter x) => { return x.type.repr(); }).join(", ")})=>{this.returnType.repr()}";
        }
    }
}