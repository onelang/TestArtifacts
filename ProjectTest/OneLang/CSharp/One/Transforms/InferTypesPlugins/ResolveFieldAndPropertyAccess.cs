using System.Collections.Generic;
using System.Threading.Tasks;
using System;
using One.Transforms.InferTypesPlugins.Helpers;
using One.Ast;

namespace One.Transforms.InferTypesPlugins
{
    public class ResolveFieldAndPropertyAccess : InferTypesPlugin {
        public ResolveFieldAndPropertyAccess(): base("ResolveFieldAndPropertyAccess") {
            
        }
        
        protected Reference getStaticRef(Class cls, string memberName) {
            var field = cls.fields.find((Field x) => { return x.name == memberName; });
            if (field != null && field.isStatic)
                return new StaticFieldReference(field);
            
            var prop = cls.properties.find((Property x) => { return x.name == memberName; });
            if (prop != null && prop.isStatic)
                return new StaticPropertyReference(prop);
            
            this.errorMan.throw_($"Could not resolve static member access of a class: {cls.name}::{memberName}");
            return null;
        }
        
        protected Reference getInstanceRef(Class cls, string memberName, Expression obj) {
            while (true) {
                var field = cls.fields.find((Field x) => { return x.name == memberName; });
                if (field != null && !field.isStatic)
                    return new InstanceFieldReference(obj, field);
                
                var prop = cls.properties.find((Property x) => { return x.name == memberName; });
                if (prop != null && !prop.isStatic)
                    return new InstancePropertyReference(obj, prop);
                
                if (cls.baseClass == null)
                    break;
                
                cls = (((ClassType)cls.baseClass)).decl;
            }
            
            this.errorMan.throw_($"Could not resolve instance member access of a class: {cls.name}::{memberName}");
            return null;
        }
        
        protected Reference getInterfaceRef(Interface intf, string memberName, Expression obj) {
            var field = intf.fields.find((Field x) => { return x.name == memberName; });
            if (field != null && !field.isStatic)
                return new InstanceFieldReference(obj, field);
            
            foreach (var baseIntf in intf.baseInterfaces) {
                var res = this.getInterfaceRef((((InterfaceType)baseIntf)).decl, memberName, obj);
                if (res != null)
                    return res;
            }
            return null;
        }
        
        protected Expression transformPA(PropertyAccessExpression expr) {
            if (expr.object_ is ClassReference)
                return this.getStaticRef(((ClassReference)expr.object_).decl, expr.propertyName);
            
            if (expr.object_ is StaticThisReference)
                return this.getStaticRef(((StaticThisReference)expr.object_).cls, expr.propertyName);
            
            expr.object_ = this.main.runPluginsOn(expr.object_);
            
            if (expr.object_ is ThisReference)
                return this.getInstanceRef(((ThisReference)expr.object_).cls, expr.propertyName, ((ThisReference)expr.object_));
            
            var type = expr.object_.getType();
            if (type is ClassType)
                return this.getInstanceRef(((ClassType)type).decl, expr.propertyName, expr.object_);
            else if (type is InterfaceType) {
                var ref_ = this.getInterfaceRef(((InterfaceType)type).decl, expr.propertyName, expr.object_);
                if (ref_ == null)
                    this.errorMan.throw_($"Could not resolve instance member access of a interface: {((InterfaceType)type).repr()}::{expr.propertyName}");
                return ref_;
            }
            else if (type == null)
                this.errorMan.throw_($"Type was not inferred yet (prop=\"{expr.propertyName}\")");
            else if (type is AnyType)
                //this.errorMan.throw(`Object has any type (prop="${expr.propertyName}")`);
                expr.setActualType(AnyType.instance);
            else
                this.errorMan.throw_($"Expected class as variable type, but got: {type.repr()} (prop=\"{expr.propertyName}\")");
            
            return expr;
        }
        
        public override bool canTransform(Expression expr) {
            return expr is PropertyAccessExpression && !(((PropertyAccessExpression)expr).object_ is EnumReference) && !(((PropertyAccessExpression)expr).parentNode is UnresolvedCallExpression && ((UnresolvedCallExpression)((PropertyAccessExpression)expr).parentNode).func == ((PropertyAccessExpression)expr)) && !(((PropertyAccessExpression)expr).actualType is AnyType);
        }
        
        public override Expression transform(Expression expr) {
            return this.transformPA(((PropertyAccessExpression)expr));
        }
        
        public override bool canDetectType(Expression expr) {
            return expr is InstanceFieldReference || expr is InstancePropertyReference || expr is StaticFieldReference || expr is StaticPropertyReference;
        }
        
        public override bool detectType(Expression expr) {
            if (expr is InstanceFieldReference) {
                var actualType = GenericsResolver.fromObject(((InstanceFieldReference)expr).object_).resolveType(((InstanceFieldReference)expr).field.type, true);
                ((InstanceFieldReference)expr).setActualType(actualType, false, Type_.isGeneric(((InstanceFieldReference)expr).object_.actualType));
                return true;
            }
            else if (expr is InstancePropertyReference) {
                var actualType = GenericsResolver.fromObject(((InstancePropertyReference)expr).object_).resolveType(((InstancePropertyReference)expr).property.type, true);
                ((InstancePropertyReference)expr).setActualType(actualType);
                return true;
            }
            else if (expr is StaticPropertyReference) {
                ((StaticPropertyReference)expr).setActualType(((StaticPropertyReference)expr).decl.type, false, false);
                return true;
            }
            else if (expr is StaticFieldReference) {
                ((StaticFieldReference)expr).setActualType(((StaticFieldReference)expr).decl.type, false, false);
                return true;
            }
            
            return false;
        }
    }
}