using System.Collections.Generic;
using One;
using One.Ast;

namespace One.Transforms
{
    public class InstanceOfImplicitCast : AstTransformer {
        public List<InstanceOfExpression> casts;
        public List<int> castCounts;
        
        public InstanceOfImplicitCast(): base("InstanceOfImplicitCast")
        {
            this.casts = new List<InstanceOfExpression>();
            this.castCounts = new List<int>();
        }
        
        protected void addCast(InstanceOfExpression cast) {
            if (this.castCounts.length() > 0) {
                this.casts.push(cast);
                var last = this.castCounts.length() - 1;
                this.castCounts.set(last, this.castCounts.get(last) + 1);
            }
        }
        
        protected void pushContext() {
            this.castCounts.push(0);
        }
        
        protected void popContext() {
            var castCount = this.castCounts.pop();
            if (castCount != 0)
                this.casts.splice(this.casts.length() - castCount, castCount);
        }
        
        protected bool equals(Expression expr1, Expression expr2) {
            // implicit casts don't matter when checking equality...
            while (expr1 is CastExpression && ((CastExpression)expr1).implicit_)
                expr1 = ((CastExpression)expr1).expression;
            while (expr2 is CastExpression && ((CastExpression)expr2).implicit_)
                expr2 = ((CastExpression)expr2).expression;
            
            // MetP, V, MethP.PA, V.PA, MethP/V [ {FEVR} ], FEVR
            if (expr1 is PropertyAccessExpression)
                return expr2 is PropertyAccessExpression && ((PropertyAccessExpression)expr1).propertyName == ((PropertyAccessExpression)expr2).propertyName && this.equals(((PropertyAccessExpression)expr1).object_, ((PropertyAccessExpression)expr2).object_);
            else if (expr1 is VariableDeclarationReference)
                return expr2 is VariableDeclarationReference && ((VariableDeclarationReference)expr1).decl == ((VariableDeclarationReference)expr2).decl;
            else if (expr1 is MethodParameterReference)
                return expr2 is MethodParameterReference && ((MethodParameterReference)expr1).decl == ((MethodParameterReference)expr2).decl;
            else if (expr1 is ForeachVariableReference)
                return expr2 is ForeachVariableReference && ((ForeachVariableReference)expr1).decl == ((ForeachVariableReference)expr2).decl;
            else if (expr1 is InstanceFieldReference)
                return expr2 is InstanceFieldReference && ((InstanceFieldReference)expr1).field == ((InstanceFieldReference)expr2).field;
            else if (expr1 is ThisReference)
                return expr2 is ThisReference;
            else if (expr1 is StaticThisReference)
                return expr2 is StaticThisReference;
            return false;
        }
        
        protected override Expression visitExpression(Expression expr) {
            Expression result = null;
            if (expr is InstanceOfExpression) {
                this.visitExpression(((InstanceOfExpression)expr).expr);
                this.addCast(((InstanceOfExpression)expr));
            }
            else if (expr is BinaryExpression && ((BinaryExpression)expr).operator_ == "&&") {
                ((BinaryExpression)expr).left = this.visitExpression(((BinaryExpression)expr).left) ?? ((BinaryExpression)expr).left;
                ((BinaryExpression)expr).right = this.visitExpression(((BinaryExpression)expr).right) ?? ((BinaryExpression)expr).right;
            }
            else if (expr is ConditionalExpression) {
                this.pushContext();
                ((ConditionalExpression)expr).condition = this.visitExpression(((ConditionalExpression)expr).condition) ?? ((ConditionalExpression)expr).condition;
                ((ConditionalExpression)expr).whenTrue = this.visitExpression(((ConditionalExpression)expr).whenTrue) ?? ((ConditionalExpression)expr).whenTrue;
                this.popContext();
                
                ((ConditionalExpression)expr).whenFalse = this.visitExpression(((ConditionalExpression)expr).whenFalse) ?? ((ConditionalExpression)expr).whenFalse;
            }
            else if (expr is Reference && ((Reference)expr).parentNode is BinaryExpression && ((BinaryExpression)((Reference)expr).parentNode).operator_ == "=" && ((BinaryExpression)((Reference)expr).parentNode).left == ((Reference)expr)) { }
            else {
                this.pushContext();
                result = base.visitExpression(expr) ?? expr;
                this.popContext();
                var match = this.casts.find((InstanceOfExpression cast) => { return this.equals(result, cast.expr); });
                if (match != null)
                    result = new CastExpression(match.checkType, result, true);
            }
            return result;
        }
        
        protected override Statement visitStatement(Statement stmt) {
            this.currentStatement = stmt;
            
            if (stmt is IfStatement) {
                this.pushContext();
                ((IfStatement)stmt).condition = this.visitExpression(((IfStatement)stmt).condition) ?? ((IfStatement)stmt).condition;
                this.visitBlock(((IfStatement)stmt).then);
                this.popContext();
                
                if (((IfStatement)stmt).else_ != null)
                    this.visitBlock(((IfStatement)stmt).else_);
            }
            else if (stmt is WhileStatement) {
                this.pushContext();
                ((WhileStatement)stmt).condition = this.visitExpression(((WhileStatement)stmt).condition) ?? ((WhileStatement)stmt).condition;
                this.visitBlock(((WhileStatement)stmt).body);
                this.popContext();
            }
            else {
                this.pushContext();
                base.visitStatement(stmt);
                this.popContext();
            }
            
            return null;
        }
    }
}