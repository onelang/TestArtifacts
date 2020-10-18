import java.util.Arrays;

public class MethodParameterReference extends VariableReference {
    public MethodParameter decl;
    
    public MethodParameterReference(MethodParameter decl)
    {
        super();
        this.decl = decl;
        decl.references.add(this);
    }
    
    public void setActualType(IType type, Boolean allowVoid, Boolean allowGeneric)
    {
        super.setActualType(type, false, this.decl.parentMethod instanceof Lambda ? Arrays.stream(((Lambda)this.decl.parentMethod).getParameters()).anyMatch(x -> TypeHelper.isGeneric(x.getType())) : this.decl.parentMethod instanceof Constructor ? ((Constructor)this.decl.parentMethod).parentClass.getTypeArguments().length > 0 : this.decl.parentMethod instanceof Method ? ((Method)this.decl.parentMethod).typeArguments.length > 0 || ((Method)this.decl.parentMethod).parentInterface.getTypeArguments().length > 0 : false);
    }
    
    public void setActualType(IType type, Boolean allowVoid) {
        this.setActualType(type, allowVoid, false);
    }
    
    public void setActualType(IType type) {
        this.setActualType(type, false, false);
    }
    
    public IVariable getVariable()
    {
        return this.decl;
    }
}