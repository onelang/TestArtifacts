using System.Collections.Generic;
using One.Ast;

namespace One.Ast
{
    public enum TypeRestriction { NoRestriction, ShouldNotHaveType, MustBeGeneric, ShouldNotBeGeneric }
    public enum UnaryType { Postfix, Prefix }
    
    public interface IExpression {
        void setActualType(Type_ actualType, bool allowVoid, bool allowGeneric);
        
        void setExpectedType(Type_ type, bool allowVoid);
        
        Type_ getType();
    }
    
    public interface IMethodCallExpression : IExpression {
        Method method { get; set; }
        Type_[] typeArgs { get; set; }
        Expression[] args { get; set; }
    }
    
    public class Expression : IAstNode, IExpression {
        public IAstNode parentNode;
        public Type_ expectedType;
        public Type_ actualType;
        public string actualTypeStack;
        
        public Expression()
        {
            this.parentNode = null;
            this.expectedType = null;
            this.actualType = null;
            this.actualTypeStack = null;
        }
        
        protected void typeCheck(Type_ type, bool allowVoid) {
            if (type == null)
                throw new Error("New type cannot be null!");
            
            if (type is VoidType && !allowVoid)
                throw new Error("Expression's type cannot be VoidType!");
            
            if (type is UnresolvedType)
                throw new Error("Expression's type cannot be UnresolvedType!");
        }
        
        public virtual void setActualType(Type_ actualType, bool allowVoid = false, bool allowGeneric = false) {
            if (this.actualType != null)
                throw new Error($"Expression already has actual type (current type = {this.actualType.repr()}, new type = {actualType.repr()})");
            
            this.typeCheck(actualType, allowVoid);
            
            if (this.expectedType != null && !Type_.isAssignableTo(actualType, this.expectedType))
                throw new Error($"Actual type ({actualType.repr()}) is not assignable to the declared type ({this.expectedType.repr()})!");
            
            //if (!allowGeneric && Type.isGeneric(actualType))
            //    throw new Error(`Actual type cannot be generic (${actualType.repr()})!`);
            
            this.actualType = actualType;
            // TODO: debug only
            Error.stackTraceLimit = 999;
            this.actualTypeStack = (new Error()).stack;
        }
        
        public void setExpectedType(Type_ type, bool allowVoid = false) {
            if (this.actualType != null)
                throw new Error("Cannot set expected type after actual type was already set!");
            
            if (this.expectedType != null)
                throw new Error("Expression already has a expected type!");
            
            this.typeCheck(type, allowVoid);
            
            this.expectedType = type;
        }
        
        public Type_ getType() {
            return this.actualType ?? this.expectedType;
        }
    }
    
    public class Identifier : Expression {
        public string text;
        
        public Identifier(string text): base()
        {
            this.text = text;
        }
    }
    
    public class NumericLiteral : Expression {
        public string valueAsText;
        
        public NumericLiteral(string valueAsText): base()
        {
            this.valueAsText = valueAsText;
        }
    }
    
    public class BooleanLiteral : Expression {
        public bool boolValue;
        
        public BooleanLiteral(bool boolValue): base()
        {
            this.boolValue = boolValue;
        }
    }
    
    public class CharacterLiteral : Expression {
        public string charValue;
        
        public CharacterLiteral(string charValue): base()
        {
            this.charValue = charValue;
        }
    }
    
    public class StringLiteral : Expression {
        public string stringValue;
        
        public StringLiteral(string stringValue): base()
        {
            this.stringValue = stringValue;
        }
    }
    
    public class NullLiteral : Expression {
        
    }
    
    public class RegexLiteral : Expression {
        public string pattern;
        public bool caseInsensitive;
        public bool global;
        
        public RegexLiteral(string pattern, bool caseInsensitive, bool global): base()
        {
            this.pattern = pattern;
            this.caseInsensitive = caseInsensitive;
            this.global = global;
        }
    }
    
    public class TemplateStringPart {
        public bool isLiteral;
        public string literalText;
        public Expression expression;
        
        public TemplateStringPart(bool isLiteral, string literalText, Expression expression)
        {
            this.isLiteral = isLiteral;
            this.literalText = literalText;
            this.expression = expression;
        }
        
        public static TemplateStringPart Literal(string literalText) {
            return new TemplateStringPart(true, literalText, null);
        }
        
        public static TemplateStringPart Expression(Expression expr) {
            return new TemplateStringPart(false, null, expr);
        }
    }
    
    public class TemplateString : Expression {
        public TemplateStringPart[] parts;
        
        public TemplateString(TemplateStringPart[] parts): base()
        {
            this.parts = parts;
        }
    }
    
    public class ArrayLiteral : Expression {
        public Expression[] items;
        
        public ArrayLiteral(Expression[] items): base()
        {
            this.items = items;
        }
    }
    
    public class MapLiteralItem {
        public string key;
        public Expression value;
        
        public MapLiteralItem(string key, Expression value)
        {
            this.key = key;
            this.value = value;
        }
    }
    
    public class MapLiteral : Expression {
        public MapLiteralItem[] items;
        
        public MapLiteral(MapLiteralItem[] items): base()
        {
            this.items = items;
        }
    }
    
    public class UnresolvedNewExpression : Expression {
        public UnresolvedType cls;
        public Expression[] args;
        
        public UnresolvedNewExpression(UnresolvedType cls, Expression[] args): base()
        {
            this.cls = cls;
            this.args = args;
        }
    }
    
    public class NewExpression : Expression {
        public ClassType cls;
        public Expression[] args;
        
        public NewExpression(ClassType cls, Expression[] args): base()
        {
            this.cls = cls;
            this.args = args;
        }
    }
    
    public class BinaryExpression : Expression {
        public Expression left;
        public string operator_;
        public Expression right;
        
        public BinaryExpression(Expression left, string operator_, Expression right): base()
        {
            this.left = left;
            this.operator_ = operator_;
            this.right = right;
        }
    }
    
    public class NullCoalesceExpression : Expression {
        public Expression defaultExpr;
        public Expression exprIfNull;
        
        public NullCoalesceExpression(Expression defaultExpr, Expression exprIfNull): base()
        {
            this.defaultExpr = defaultExpr;
            this.exprIfNull = exprIfNull;
        }
    }
    
    public class UnaryExpression : Expression {
        public UnaryType unaryType;
        public string operator_;
        public Expression operand;
        
        public UnaryExpression(UnaryType unaryType, string operator_, Expression operand): base()
        {
            this.unaryType = unaryType;
            this.operator_ = operator_;
            this.operand = operand;
        }
    }
    
    public class CastExpression : Expression {
        public Type_ newType;
        public Expression expression;
        public bool implicit_;
        
        public CastExpression(Type_ newType, Expression expression, bool implicit_ = false): base()
        {
            this.newType = newType;
            this.expression = expression;
            this.implicit_ = implicit_;
        }
    }
    
    public class ParenthesizedExpression : Expression {
        public Expression expression;
        
        public ParenthesizedExpression(Expression expression): base()
        {
            this.expression = expression;
        }
    }
    
    public class ConditionalExpression : Expression {
        public Expression condition;
        public Expression whenTrue;
        public Expression whenFalse;
        
        public ConditionalExpression(Expression condition, Expression whenTrue, Expression whenFalse): base()
        {
            this.condition = condition;
            this.whenTrue = whenTrue;
            this.whenFalse = whenFalse;
        }
    }
    
    public class PropertyAccessExpression : Expression {
        public Expression object_;
        public string propertyName;
        
        public PropertyAccessExpression(Expression object_, string propertyName): base()
        {
            this.object_ = object_;
            this.propertyName = propertyName;
        }
    }
    
    public class ElementAccessExpression : Expression {
        public Expression object_;
        public Expression elementExpr;
        
        public ElementAccessExpression(Expression object_, Expression elementExpr): base()
        {
            this.object_ = object_;
            this.elementExpr = elementExpr;
        }
    }
    
    public class UnresolvedCallExpression : Expression {
        public Expression func;
        public Type_[] typeArgs;
        public Expression[] args;
        
        public UnresolvedCallExpression(Expression func, Type_[] typeArgs, Expression[] args): base()
        {
            this.func = func;
            this.typeArgs = typeArgs;
            this.args = args;
        }
    }
    
    public class UnresolvedMethodCallExpression : Expression {
        public Expression object_;
        public string methodName;
        public Type_[] typeArgs;
        public Expression[] args;
        
        public UnresolvedMethodCallExpression(Expression object_, string methodName, Type_[] typeArgs, Expression[] args): base()
        {
            this.object_ = object_;
            this.methodName = methodName;
            this.typeArgs = typeArgs;
            this.args = args;
        }
    }
    
    public class StaticMethodCallExpression : Expression, IMethodCallExpression {
        public Method method { get; set; }
        public Type_[] typeArgs { get; set; }
        public Expression[] args { get; set; }
        public bool isThisCall;
        
        public StaticMethodCallExpression(Method method, Type_[] typeArgs, Expression[] args, bool isThisCall): base()
        {
            this.method = method;
            this.typeArgs = typeArgs;
            this.args = args;
            this.isThisCall = isThisCall;
        }
    }
    
    public class InstanceMethodCallExpression : Expression, IMethodCallExpression {
        public Expression object_;
        public Method method { get; set; }
        public Type_[] typeArgs { get; set; }
        public Expression[] args { get; set; }
        
        public InstanceMethodCallExpression(Expression object_, Method method, Type_[] typeArgs, Expression[] args): base()
        {
            this.object_ = object_;
            this.method = method;
            this.typeArgs = typeArgs;
            this.args = args;
        }
    }
    
    public class GlobalFunctionCallExpression : Expression {
        public GlobalFunction func;
        public Expression[] args;
        
        public GlobalFunctionCallExpression(GlobalFunction func, Expression[] args): base()
        {
            this.func = func;
            this.args = args;
        }
    }
    
    public class LambdaCallExpression : Expression {
        public Expression method;
        public Expression[] args;
        
        public LambdaCallExpression(Expression method, Expression[] args): base()
        {
            this.method = method;
            this.args = args;
        }
    }
    
    public class TodoExpression : Expression {
        public Expression expr;
        
        public TodoExpression(Expression expr): base()
        {
            this.expr = expr;
        }
    }
    
    public class InstanceOfExpression : Expression {
        public Expression expr;
        public Type_ checkType;
        
        public InstanceOfExpression(Expression expr, Type_ checkType): base()
        {
            this.expr = expr;
            this.checkType = checkType;
        }
    }
    
    public class AwaitExpression : Expression {
        public Expression expr;
        
        public AwaitExpression(Expression expr): base()
        {
            this.expr = expr;
        }
    }
}