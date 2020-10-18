import java.util.Arrays;

public class ResolveFieldAndPropertyAccess extends InferTypesPlugin {
    public ResolveFieldAndPropertyAccess()
    {
        super("ResolveFieldAndPropertyAccess");
        
    }
    
    protected Reference getStaticRef(Class cls, String memberName)
    {
        var field = Arrays.stream(cls.getFields()).filter(x -> x.getName() == memberName).findFirst().orElse(null);
        if (field != null && field.getIsStatic())
            return new StaticFieldReference(field);
        
        var prop = Arrays.stream(cls.properties).filter(x -> x.getName() == memberName).findFirst().orElse(null);
        if (prop != null && prop.getIsStatic())
            return new StaticPropertyReference(prop);
        
        this.errorMan.throw_("Could not resolve static member access of a class: " + cls.getName() + "::" + memberName);
        return null;
    }
    
    protected Reference getInstanceRef(Class cls, String memberName, Expression obj)
    {
        while (true) {
            var field = Arrays.stream(cls.getFields()).filter(x -> x.getName() == memberName).findFirst().orElse(null);
            if (field != null && !field.getIsStatic())
                return new InstanceFieldReference(obj, field);
            
            var prop = Arrays.stream(cls.properties).filter(x -> x.getName() == memberName).findFirst().orElse(null);
            if (prop != null && !prop.getIsStatic())
                return new InstancePropertyReference(obj, prop);
            
            if (cls.baseClass == null)
                break;
            
            cls = (((ClassType)cls.baseClass)).decl;
        }
        
        this.errorMan.throw_("Could not resolve instance member access of a class: " + cls.getName() + "::" + memberName);
        return null;
    }
    
    protected Reference getInterfaceRef(Interface intf, String memberName, Expression obj)
    {
        var field = Arrays.stream(intf.getFields()).filter(x -> x.getName() == memberName).findFirst().orElse(null);
        if (field != null && !field.getIsStatic())
            return new InstanceFieldReference(obj, field);
        
        for (var baseIntf : intf.getBaseInterfaces()) {
            var res = this.getInterfaceRef((((InterfaceType)baseIntf)).decl, memberName, obj);
            if (res != null)
                return res;
        }
        return null;
    }
    
    protected Expression transformPA(PropertyAccessExpression expr)
    {
        if (expr.object instanceof ClassReference)
            return this.getStaticRef(((ClassReference)expr.object).decl, expr.propertyName);
        
        if (expr.object instanceof StaticThisReference)
            return this.getStaticRef(((StaticThisReference)expr.object).cls, expr.propertyName);
        
        expr.object = this.main.runPluginsOn(expr.object);
        
        if (expr.object instanceof ThisReference)
            return this.getInstanceRef(((ThisReference)expr.object).cls, expr.propertyName, ((ThisReference)expr.object));
        
        var type = expr.object.getType();
        if (type instanceof ClassType)
            return this.getInstanceRef(((ClassType)type).decl, expr.propertyName, expr.object);
        else if (type instanceof InterfaceType) {
            var ref = this.getInterfaceRef(((InterfaceType)type).decl, expr.propertyName, expr.object);
            if (ref == null)
                this.errorMan.throw_("Could not resolve instance member access of a interface: " + ((InterfaceType)type).repr() + "::" + expr.propertyName);
            return ref;
        }
        else if (type == null)
            this.errorMan.throw_("Type was not inferred yet (prop=\"" + expr.propertyName + "\")");
        else if (type instanceof AnyType)
            //this.errorMan.throw(`Object has any type (prop="${expr.propertyName}")`);
            expr.setActualType(AnyType.instance);
        else
            this.errorMan.throw_("Expected class as variable type, but got: " + type.repr() + " (prop=\"" + expr.propertyName + "\")");
        
        return expr;
    }
    
    public Boolean canTransform(Expression expr)
    {
        return expr instanceof PropertyAccessExpression && !(((PropertyAccessExpression)expr).object instanceof EnumReference) && !(((PropertyAccessExpression)expr).parentNode instanceof UnresolvedCallExpression && ((UnresolvedCallExpression)((PropertyAccessExpression)expr).parentNode).func == ((PropertyAccessExpression)expr)) && !(((PropertyAccessExpression)expr).actualType instanceof AnyType);
    }
    
    public Expression transform(Expression expr)
    {
        return this.transformPA(((PropertyAccessExpression)expr));
    }
    
    public Boolean canDetectType(Expression expr)
    {
        return expr instanceof InstanceFieldReference || expr instanceof InstancePropertyReference || expr instanceof StaticFieldReference || expr instanceof StaticPropertyReference;
    }
    
    public Boolean detectType(Expression expr)
    {
        if (expr instanceof InstanceFieldReference) {
            var actualType = GenericsResolver.fromObject(((InstanceFieldReference)expr).object).resolveType(((InstanceFieldReference)expr).field.getType(), true);
            ((InstanceFieldReference)expr).setActualType(actualType, false, TypeHelper.isGeneric(((InstanceFieldReference)expr).object.actualType));
            return true;
        }
        else if (expr instanceof InstancePropertyReference) {
            var actualType = GenericsResolver.fromObject(((InstancePropertyReference)expr).object).resolveType(((InstancePropertyReference)expr).property.getType(), true);
            ((InstancePropertyReference)expr).setActualType(actualType);
            return true;
        }
        else if (expr instanceof StaticPropertyReference) {
            ((StaticPropertyReference)expr).setActualType(((StaticPropertyReference)expr).decl.getType(), false, false);
            return true;
        }
        else if (expr instanceof StaticFieldReference) {
            ((StaticFieldReference)expr).setActualType(((StaticFieldReference)expr).decl.getType(), false, false);
            return true;
        }
        
        return false;
    }
}