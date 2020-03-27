using One.Ast;

namespace One.Ast
{
    public interface IReferencable {
        Reference createReference();
    }
    
    public interface IGetMethodBase {
        IMethodBase getMethodBase();
    }
    
    public class Reference : Expression {
        
    }
    
    public class ClassReference : Reference {
        public Class decl;
        
        public ClassReference(Class decl): base() {
            this.decl = decl;
            decl.classReferences.push(this);
        }
        
        public override void setActualType(Type_ type, bool allowVoid, bool allowGeneric) {
            throw new Error("ClassReference cannot have a type!");
        }
    }
    
    public class GlobalFunctionReference : Reference, IGetMethodBase {
        public GlobalFunction decl;
        
        public GlobalFunctionReference(GlobalFunction decl): base() {
            this.decl = decl;
            decl.references.push(this);
        }
        
        public override void setActualType(Type_ type, bool allowVoid, bool allowGeneric) {
            throw new Error("GlobalFunctionReference cannot have a type!");
        }
        
        public IMethodBase getMethodBase() {
            return this.decl;
        }
    }
    
    public class MethodParameterReference : Reference {
        public MethodParameter decl;
        
        public MethodParameterReference(MethodParameter decl): base() {
            this.decl = decl;
            decl.references.push(this);
        }
        
        public override void setActualType(Type_ type, bool allowVoid, bool allowGeneric) {
            base.setActualType(type, false, this.decl.parentMethod is Lambda ? ((Lambda)this.decl.parentMethod).parameters.some((MethodParameter x) => { return Type_.isGeneric(x.type); }) : this.decl.parentMethod is Constructor ? ((Constructor)this.decl.parentMethod).parentClass.typeArguments.length() > 0 : this.decl.parentMethod is Method ? ((Method)this.decl.parentMethod).typeArguments.length() > 0 || ((Method)this.decl.parentMethod).parentInterface.typeArguments.length() > 0 : false);
        }
    }
    
    public class EnumReference : Reference {
        public Enum_ decl;
        
        public EnumReference(Enum_ decl): base() {
            this.decl = decl;
            decl.references.push(this);
        }
        
        public override void setActualType(Type_ type, bool allowVoid, bool allowGeneric) {
            throw new Error("EnumReference cannot have a type!");
        }
    }
    
    public class EnumMemberReference : Expression {
        public EnumMember decl;
        
        public EnumMemberReference(EnumMember decl): base() {
            this.decl = decl;
            decl.references.push(this);
        }
        
        public override void setActualType(Type_ type, bool allowVoid, bool allowGeneric) {
            if (!(type is EnumType))
                throw new Error("Expected EnumType!");
            base.setActualType(type);
        }
    }
    
    public class StaticThisReference : Reference {
        public Class cls;
        
        public StaticThisReference(Class cls): base() {
            this.cls = cls;
            cls.staticThisReferences.push(this);
        }
        
        public override void setActualType(Type_ type, bool allowVoid, bool allowGeneric) {
            throw new Error("StaticThisReference cannot have a type!");
        }
    }
    
    public class ThisReference : Reference {
        public Class cls;
        
        public ThisReference(Class cls): base() {
            this.cls = cls;
            cls.thisReferences.push(this);
        }
        
        public override void setActualType(Type_ type, bool allowVoid, bool allowGeneric) {
            if (!(type is ClassType))
                throw new Error("Expected ClassType!");
            base.setActualType(type, false, this.cls.typeArguments.length() > 0);
        }
    }
    
    public class SuperReference : Reference {
        public Class cls;
        
        public SuperReference(Class cls): base() {
            this.cls = cls;
            cls.superReferences.push(this);
        }
        
        public override void setActualType(Type_ type, bool allowVoid, bool allowGeneric) {
            if (!(type is ClassType))
                throw new Error("Expected ClassType!");
            base.setActualType(type, false, this.cls.typeArguments.length() > 0);
        }
    }
    
    public class VariableDeclarationReference : Reference {
        public VariableDeclaration decl;
        
        public VariableDeclarationReference(VariableDeclaration decl): base() {
            this.decl = decl;
            decl.references.push(this);
        }
    }
    
    public class ForVariableReference : Reference {
        public ForVariable decl;
        
        public ForVariableReference(ForVariable decl): base() {
            this.decl = decl;
            decl.references.push(this);
        }
    }
    
    public class CatchVariableReference : Reference {
        public CatchVariable decl;
        
        public CatchVariableReference(CatchVariable decl): base() {
            this.decl = decl;
            decl.references.push(this);
        }
    }
    
    public class ForeachVariableReference : Reference {
        public ForeachVariable decl;
        
        public ForeachVariableReference(ForeachVariable decl): base() {
            this.decl = decl;
            decl.references.push(this);
        }
    }
    
    public class StaticFieldReference : Reference {
        public Field decl;
        
        public StaticFieldReference(Field decl): base() {
            this.decl = decl;
            decl.staticReferences.push(this);
        }
        
        public override void setActualType(Type_ type, bool allowVoid, bool allowGeneric) {
            if (Type_.isGeneric(type))
                throw new Error("StaticField's type cannot be Generic");
            base.setActualType(type);
        }
    }
    
    public class StaticPropertyReference : Reference {
        public Property decl;
        
        public StaticPropertyReference(Property decl): base() {
            this.decl = decl;
            decl.staticReferences.push(this);
        }
        
        public override void setActualType(Type_ type, bool allowVoid, bool allowGeneric) {
            if (Type_.isGeneric(type))
                throw new Error("StaticProperty's type cannot be Generic");
            base.setActualType(type);
        }
    }
    
    public class InstanceFieldReference : Reference {
        public Expression object_;
        public Field field;
        
        public InstanceFieldReference(Expression object_, Field field): base() {
            this.object_ = object_;
            this.field = field;
            field.instanceReferences.push(this);
        }
    }
    
    public class InstancePropertyReference : Reference {
        public Expression object_;
        public Property property;
        
        public InstancePropertyReference(Expression object_, Property property): base() {
            this.object_ = object_;
            this.property = property;
            property.instanceReferences.push(this);
        }
    }
}