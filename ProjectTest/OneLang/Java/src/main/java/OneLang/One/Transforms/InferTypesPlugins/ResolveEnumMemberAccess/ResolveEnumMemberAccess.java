import java.util.Arrays;

public class ResolveEnumMemberAccess extends InferTypesPlugin {
    public ResolveEnumMemberAccess()
    {
        super("ResolveEnumMemberAccess");
        
    }
    
    public Boolean canTransform(Expression expr) {
        return expr instanceof PropertyAccessExpression && ((PropertyAccessExpression)expr).object instanceof EnumReference;
    }
    
    public Expression transform(Expression expr) {
        var pa = ((PropertyAccessExpression)expr);
        var enumMemberRef = ((EnumReference)pa.object);
        var member = Arrays.stream(enumMemberRef.decl.values).filter(x -> x.name.equals(pa.propertyName)).findFirst().orElse(null);
        if (member == null) {
            this.errorMan.throw_("Enum member was not found: " + enumMemberRef.decl.getName() + "::" + pa.propertyName);
            return null;
        }
        return new EnumMemberReference(member);
    }
    
    public Boolean canDetectType(Expression expr) {
        return expr instanceof EnumMemberReference;
    }
    
    public Boolean detectType(Expression expr) {
        expr.setActualType((((EnumMemberReference)expr)).decl.parentEnum.type);
        return true;
    }
}