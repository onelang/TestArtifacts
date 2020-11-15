package OneLang.One.AstTransformer;

import OneLang.One.Ast.AstTypes.IHasTypeArguments;
import OneLang.One.Ast.AstTypes.ClassType;
import OneLang.One.Ast.AstTypes.InterfaceType;
import OneLang.One.Ast.AstTypes.UnresolvedType;
import OneLang.One.Ast.AstTypes.LambdaType;
import OneLang.One.Ast.Expressions.Identifier;
import OneLang.One.Ast.Expressions.BinaryExpression;
import OneLang.One.Ast.Expressions.ConditionalExpression;
import OneLang.One.Ast.Expressions.NewExpression;
import OneLang.One.Ast.Expressions.TemplateString;
import OneLang.One.Ast.Expressions.ParenthesizedExpression;
import OneLang.One.Ast.Expressions.UnaryExpression;
import OneLang.One.Ast.Expressions.PropertyAccessExpression;
import OneLang.One.Ast.Expressions.ElementAccessExpression;
import OneLang.One.Ast.Expressions.ArrayLiteral;
import OneLang.One.Ast.Expressions.MapLiteral;
import OneLang.One.Ast.Expressions.Expression;
import OneLang.One.Ast.Expressions.CastExpression;
import OneLang.One.Ast.Expressions.UnresolvedCallExpression;
import OneLang.One.Ast.Expressions.InstanceOfExpression;
import OneLang.One.Ast.Expressions.AwaitExpression;
import OneLang.One.Ast.Expressions.StringLiteral;
import OneLang.One.Ast.Expressions.NumericLiteral;
import OneLang.One.Ast.Expressions.NullLiteral;
import OneLang.One.Ast.Expressions.RegexLiteral;
import OneLang.One.Ast.Expressions.BooleanLiteral;
import OneLang.One.Ast.Expressions.StaticMethodCallExpression;
import OneLang.One.Ast.Expressions.InstanceMethodCallExpression;
import OneLang.One.Ast.Expressions.UnresolvedNewExpression;
import OneLang.One.Ast.Expressions.NullCoalesceExpression;
import OneLang.One.Ast.Expressions.UnresolvedMethodCallExpression;
import OneLang.One.Ast.Expressions.GlobalFunctionCallExpression;
import OneLang.One.Ast.Expressions.LambdaCallExpression;
import OneLang.One.Ast.Statements.ReturnStatement;
import OneLang.One.Ast.Statements.ExpressionStatement;
import OneLang.One.Ast.Statements.IfStatement;
import OneLang.One.Ast.Statements.ThrowStatement;
import OneLang.One.Ast.Statements.VariableDeclaration;
import OneLang.One.Ast.Statements.WhileStatement;
import OneLang.One.Ast.Statements.ForStatement;
import OneLang.One.Ast.Statements.ForeachStatement;
import OneLang.One.Ast.Statements.Statement;
import OneLang.One.Ast.Statements.UnsetStatement;
import OneLang.One.Ast.Statements.BreakStatement;
import OneLang.One.Ast.Statements.ContinueStatement;
import OneLang.One.Ast.Statements.DoStatement;
import OneLang.One.Ast.Statements.TryStatement;
import OneLang.One.Ast.Statements.Block;
import OneLang.One.Ast.Types.Method;
import OneLang.One.Ast.Types.Constructor;
import OneLang.One.Ast.Types.Field;
import OneLang.One.Ast.Types.Property;
import OneLang.One.Ast.Types.Interface;
import OneLang.One.Ast.Types.Class;
import OneLang.One.Ast.Types.Enum;
import OneLang.One.Ast.Types.EnumMember;
import OneLang.One.Ast.Types.SourceFile;
import OneLang.One.Ast.Types.IVariable;
import OneLang.One.Ast.Types.IVariableWithInitializer;
import OneLang.One.Ast.Types.MethodParameter;
import OneLang.One.Ast.Types.Lambda;
import OneLang.One.Ast.Types.IMethodBase;
import OneLang.One.Ast.Types.Package;
import OneLang.One.Ast.Types.GlobalFunction;
import OneLang.One.Ast.Types.IInterface;
import OneLang.One.Ast.Types.IAstNode;
import OneLang.One.Ast.Types.IHasAttributesAndTrivia;
import OneLang.One.Ast.Types.Import;
import OneLang.One.Ast.References.ClassReference;
import OneLang.One.Ast.References.EnumReference;
import OneLang.One.Ast.References.ThisReference;
import OneLang.One.Ast.References.MethodParameterReference;
import OneLang.One.Ast.References.VariableDeclarationReference;
import OneLang.One.Ast.References.ForVariableReference;
import OneLang.One.Ast.References.ForeachVariableReference;
import OneLang.One.Ast.References.SuperReference;
import OneLang.One.Ast.References.InstanceFieldReference;
import OneLang.One.Ast.References.InstancePropertyReference;
import OneLang.One.Ast.References.StaticPropertyReference;
import OneLang.One.Ast.References.StaticFieldReference;
import OneLang.One.Ast.References.CatchVariableReference;
import OneLang.One.Ast.References.GlobalFunctionReference;
import OneLang.One.Ast.References.EnumMemberReference;
import OneLang.One.Ast.References.StaticThisReference;
import OneLang.One.Ast.References.VariableReference;
import OneLang.One.ErrorManager.ErrorManager;
import OneLang.One.ITransformer.ITransformer;
import OneLang.One.Ast.Interfaces.IType;

import OneLang.One.ITransformer.ITransformer;
import OneLang.One.ErrorManager.ErrorManager;
import OneLang.One.Ast.Types.SourceFile;
import OneLang.One.Ast.Types.IInterface;
import OneLang.One.Ast.Types.IMethodBase;
import OneLang.One.Ast.Statements.Statement;
import OneLang.One.Ast.Types.IHasAttributesAndTrivia;
import OneLang.One.Ast.Interfaces.IType;
import OneLang.One.Ast.AstTypes.ClassType;
import OneLang.One.Ast.AstTypes.InterfaceType;
import OneLang.One.Ast.AstTypes.UnresolvedType;
import OneLang.One.Ast.AstTypes.IHasTypeArguments;
import java.util.Arrays;
import OneLang.One.Ast.AstTypes.LambdaType;
import OneLang.One.Ast.Expressions.Expression;
import OneLang.One.Ast.Expressions.Identifier;
import OneLang.One.Ast.Types.IVariable;
import OneLang.One.Ast.Types.IVariableWithInitializer;
import OneLang.One.Ast.Statements.VariableDeclaration;
import OneLang.One.Ast.Statements.ReturnStatement;
import OneLang.One.Ast.Statements.ExpressionStatement;
import OneLang.One.Ast.Statements.IfStatement;
import OneLang.One.Ast.Statements.ThrowStatement;
import OneLang.One.Ast.Statements.WhileStatement;
import OneLang.One.Ast.Statements.DoStatement;
import OneLang.One.Ast.Statements.ForStatement;
import OneLang.One.Ast.Statements.ForeachStatement;
import OneLang.One.Ast.Statements.TryStatement;
import OneLang.One.Ast.Statements.BreakStatement;
import OneLang.One.Ast.Statements.UnsetStatement;
import OneLang.One.Ast.Statements.ContinueStatement;
import OneLang.One.Ast.Statements.Block;
import java.util.ArrayList;
import OneLang.One.Ast.Expressions.TemplateString;
import OneLang.One.Ast.Types.Lambda;
import OneLang.One.Ast.References.VariableReference;
import OneLang.One.Ast.Expressions.BinaryExpression;
import OneLang.One.Ast.Expressions.NullCoalesceExpression;
import OneLang.One.Ast.Expressions.UnresolvedCallExpression;
import OneLang.One.Ast.Expressions.UnresolvedMethodCallExpression;
import OneLang.One.Ast.Expressions.ConditionalExpression;
import OneLang.One.Ast.Expressions.UnresolvedNewExpression;
import OneLang.One.Ast.Expressions.NewExpression;
import OneLang.One.Ast.Expressions.ParenthesizedExpression;
import OneLang.One.Ast.Expressions.UnaryExpression;
import OneLang.One.Ast.Expressions.PropertyAccessExpression;
import OneLang.One.Ast.Expressions.ElementAccessExpression;
import OneLang.One.Ast.Expressions.ArrayLiteral;
import OneLang.One.Ast.Expressions.MapLiteral;
import OneLang.One.Ast.Expressions.StringLiteral;
import OneLang.One.Ast.Expressions.BooleanLiteral;
import OneLang.One.Ast.Expressions.NumericLiteral;
import OneLang.One.Ast.Expressions.NullLiteral;
import OneLang.One.Ast.Expressions.RegexLiteral;
import OneLang.One.Ast.Expressions.CastExpression;
import OneLang.One.Ast.Expressions.InstanceOfExpression;
import OneLang.One.Ast.Expressions.AwaitExpression;
import OneLang.One.Ast.References.ClassReference;
import OneLang.One.Ast.References.EnumReference;
import OneLang.One.Ast.References.ThisReference;
import OneLang.One.Ast.References.StaticThisReference;
import OneLang.One.Ast.References.MethodParameterReference;
import OneLang.One.Ast.References.VariableDeclarationReference;
import OneLang.One.Ast.References.ForVariableReference;
import OneLang.One.Ast.References.ForeachVariableReference;
import OneLang.One.Ast.References.CatchVariableReference;
import OneLang.One.Ast.References.GlobalFunctionReference;
import OneLang.One.Ast.References.SuperReference;
import OneLang.One.Ast.References.InstanceFieldReference;
import OneLang.One.Ast.References.InstancePropertyReference;
import OneLang.One.Ast.References.StaticFieldReference;
import OneLang.One.Ast.References.StaticPropertyReference;
import OneLang.One.Ast.References.EnumMemberReference;
import OneLang.One.Ast.Expressions.StaticMethodCallExpression;
import OneLang.One.Ast.Expressions.GlobalFunctionCallExpression;
import OneLang.One.Ast.Expressions.InstanceMethodCallExpression;
import OneLang.One.Ast.Expressions.LambdaCallExpression;
import OneLang.One.Ast.Types.MethodParameter;
import OneLang.One.Ast.Types.Method;
import OneLang.One.Ast.Types.GlobalFunction;
import OneLang.One.Ast.Types.Constructor;
import OneLang.One.Ast.Types.Field;
import OneLang.One.Ast.Types.Property;
import OneLang.One.Ast.Types.Interface;
import OneLang.One.Ast.Types.Class;
import OneLang.One.Ast.Types.Enum;
import OneLang.One.Ast.Types.EnumMember;
import OneLang.One.Ast.Types.Import;
import OneLang.One.Ast.Types.Package;

public class AstTransformer implements ITransformer {
    public ErrorManager errorMan;
    public SourceFile currentFile;
    public IInterface currentInterface;
    public IMethodBase currentMethod;
    public IMethodBase currentClosure;
    public Statement currentStatement;
    
    String name;
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    public AstTransformer(String name)
    {
        this.setName(name);
        this.errorMan = new ErrorManager();
        this.currentFile = null;
        this.currentInterface = null;
        this.currentMethod = null;
        this.currentClosure = null;
        this.currentStatement = null;
    }
    
    protected void visitAttributesAndTrivia(IHasAttributesAndTrivia node) {
        
    }
    
    protected IType visitType(IType type) {
        if (type instanceof ClassType || type instanceof InterfaceType || type instanceof UnresolvedType) {
            var type2 = ((IHasTypeArguments)type);
            type2.setTypeArguments(Arrays.stream(type2.getTypeArguments()).map(x -> {
                var visitTypeResult = this.visitType(x);
                return visitTypeResult != null ? visitTypeResult : x;
            }).toArray(IType[]::new));
        }
        else if (type instanceof LambdaType) {
            for (var mp : ((LambdaType)type).parameters)
                this.visitMethodParameter(mp);
            var visitTypeResult2 = this.visitType(((LambdaType)type).returnType);
            ((LambdaType)type).returnType = visitTypeResult2 != null ? visitTypeResult2 : ((LambdaType)type).returnType;
        }
        return null;
    }
    
    protected Expression visitIdentifier(Identifier id) {
        return null;
    }
    
    protected IVariable visitVariable(IVariable variable) {
        if (variable.getType() != null) {
            var visitTypeResult = this.visitType(variable.getType());
            variable.setType(visitTypeResult != null ? visitTypeResult : variable.getType());
        }
        return null;
    }
    
    protected IVariableWithInitializer visitVariableWithInitializer(IVariableWithInitializer variable) {
        this.visitVariable(variable);
        if (variable.getInitializer() != null) {
            var visitExpressionResult = this.visitExpression(variable.getInitializer());
            variable.setInitializer(visitExpressionResult != null ? visitExpressionResult : variable.getInitializer());
        }
        return null;
    }
    
    protected VariableDeclaration visitVariableDeclaration(VariableDeclaration stmt) {
        this.visitVariableWithInitializer(stmt);
        return null;
    }
    
    protected Statement visitUnknownStatement(Statement stmt) {
        this.errorMan.throw_("Unknown statement type");
        return null;
    }
    
    protected Statement visitStatement(Statement stmt) {
        this.currentStatement = stmt;
        this.visitAttributesAndTrivia(stmt);
        if (stmt instanceof ReturnStatement) {
            if (((ReturnStatement)stmt).expression != null) {
                var visitExpressionResult = this.visitExpression(((ReturnStatement)stmt).expression);
                ((ReturnStatement)stmt).expression = visitExpressionResult != null ? visitExpressionResult : ((ReturnStatement)stmt).expression;
            }
        }
        else if (stmt instanceof ExpressionStatement) {
            var visitExpressionResult2 = this.visitExpression(((ExpressionStatement)stmt).expression);
            ((ExpressionStatement)stmt).expression = visitExpressionResult2 != null ? visitExpressionResult2 : ((ExpressionStatement)stmt).expression;
        }
        else if (stmt instanceof IfStatement) {
            var visitExpressionResult3 = this.visitExpression(((IfStatement)stmt).condition);
            ((IfStatement)stmt).condition = visitExpressionResult3 != null ? visitExpressionResult3 : ((IfStatement)stmt).condition;
            var visitBlockResult = this.visitBlock(((IfStatement)stmt).then);
            ((IfStatement)stmt).then = visitBlockResult != null ? visitBlockResult : ((IfStatement)stmt).then;
            if (((IfStatement)stmt).else_ != null) {
                var visitBlockResult2 = this.visitBlock(((IfStatement)stmt).else_);
                ((IfStatement)stmt).else_ = visitBlockResult2 != null ? visitBlockResult2 : ((IfStatement)stmt).else_;
            }
        }
        else if (stmt instanceof ThrowStatement) {
            var visitExpressionResult4 = this.visitExpression(((ThrowStatement)stmt).expression);
            ((ThrowStatement)stmt).expression = visitExpressionResult4 != null ? visitExpressionResult4 : ((ThrowStatement)stmt).expression;
        }
        else if (stmt instanceof VariableDeclaration)
            return this.visitVariableDeclaration(((VariableDeclaration)stmt));
        else if (stmt instanceof WhileStatement) {
            var visitExpressionResult5 = this.visitExpression(((WhileStatement)stmt).condition);
            ((WhileStatement)stmt).condition = visitExpressionResult5 != null ? visitExpressionResult5 : ((WhileStatement)stmt).condition;
            var visitBlockResult3 = this.visitBlock(((WhileStatement)stmt).body);
            ((WhileStatement)stmt).body = visitBlockResult3 != null ? visitBlockResult3 : ((WhileStatement)stmt).body;
        }
        else if (stmt instanceof DoStatement) {
            var visitExpressionResult6 = this.visitExpression(((DoStatement)stmt).condition);
            ((DoStatement)stmt).condition = visitExpressionResult6 != null ? visitExpressionResult6 : ((DoStatement)stmt).condition;
            var visitBlockResult4 = this.visitBlock(((DoStatement)stmt).body);
            ((DoStatement)stmt).body = visitBlockResult4 != null ? visitBlockResult4 : ((DoStatement)stmt).body;
        }
        else if (stmt instanceof ForStatement) {
            if (((ForStatement)stmt).itemVar != null)
                this.visitVariableWithInitializer(((ForStatement)stmt).itemVar);
            var visitExpressionResult7 = this.visitExpression(((ForStatement)stmt).condition);
            ((ForStatement)stmt).condition = visitExpressionResult7 != null ? visitExpressionResult7 : ((ForStatement)stmt).condition;
            var visitExpressionResult8 = this.visitExpression(((ForStatement)stmt).incrementor);
            ((ForStatement)stmt).incrementor = visitExpressionResult8 != null ? visitExpressionResult8 : ((ForStatement)stmt).incrementor;
            var visitBlockResult5 = this.visitBlock(((ForStatement)stmt).body);
            ((ForStatement)stmt).body = visitBlockResult5 != null ? visitBlockResult5 : ((ForStatement)stmt).body;
        }
        else if (stmt instanceof ForeachStatement) {
            this.visitVariable(((ForeachStatement)stmt).itemVar);
            var visitExpressionResult9 = this.visitExpression(((ForeachStatement)stmt).items);
            ((ForeachStatement)stmt).items = visitExpressionResult9 != null ? visitExpressionResult9 : ((ForeachStatement)stmt).items;
            var visitBlockResult6 = this.visitBlock(((ForeachStatement)stmt).body);
            ((ForeachStatement)stmt).body = visitBlockResult6 != null ? visitBlockResult6 : ((ForeachStatement)stmt).body;
        }
        else if (stmt instanceof TryStatement) {
            var visitBlockResult7 = this.visitBlock(((TryStatement)stmt).tryBody);
            ((TryStatement)stmt).tryBody = visitBlockResult7 != null ? visitBlockResult7 : ((TryStatement)stmt).tryBody;
            if (((TryStatement)stmt).catchBody != null) {
                this.visitVariable(((TryStatement)stmt).catchVar);
                var visitBlockResult8 = this.visitBlock(((TryStatement)stmt).catchBody);
                ((TryStatement)stmt).catchBody = visitBlockResult8 != null ? visitBlockResult8 : ((TryStatement)stmt).catchBody;
            }
            if (((TryStatement)stmt).finallyBody != null) {
                var visitBlockResult9 = this.visitBlock(((TryStatement)stmt).finallyBody);
                ((TryStatement)stmt).finallyBody = visitBlockResult9 != null ? visitBlockResult9 : ((TryStatement)stmt).finallyBody;
            }
        }
        else if (stmt instanceof BreakStatement) { }
        else if (stmt instanceof UnsetStatement) {
            var visitExpressionResult10 = this.visitExpression(((UnsetStatement)stmt).expression);
            ((UnsetStatement)stmt).expression = visitExpressionResult10 != null ? visitExpressionResult10 : ((UnsetStatement)stmt).expression;
        }
        else if (stmt instanceof ContinueStatement) { }
        else
            return this.visitUnknownStatement(stmt);
        return null;
    }
    
    protected Block visitBlock(Block block) {
        block.statements = new ArrayList<>(Arrays.asList(block.statements.stream().map(x -> {
            var visitStatementResult = this.visitStatement(x);
            return visitStatementResult != null ? visitStatementResult : x;
        }).toArray(Statement[]::new)));
        return null;
    }
    
    protected TemplateString visitTemplateString(TemplateString expr) {
        for (Integer i = 0; i < expr.parts.length; i++) {
            var part = expr.parts[i];
            if (!part.isLiteral) {
                var visitExpressionResult = this.visitExpression(part.expression);
                part.expression = visitExpressionResult != null ? visitExpressionResult : part.expression;
            }
        }
        return null;
    }
    
    protected Expression visitUnknownExpression(Expression expr) {
        this.errorMan.throw_("Unknown expression type");
        return null;
    }
    
    protected Lambda visitLambda(Lambda lambda) {
        var prevClosure = this.currentClosure;
        this.currentClosure = lambda;
        this.visitMethodBase(lambda);
        this.currentClosure = prevClosure;
        return null;
    }
    
    protected VariableReference visitVariableReference(VariableReference varRef) {
        return null;
    }
    
    protected Expression visitExpression(Expression expr) {
        if (expr instanceof BinaryExpression) {
            var visitExpressionResult = this.visitExpression(((BinaryExpression)expr).left);
            ((BinaryExpression)expr).left = visitExpressionResult != null ? visitExpressionResult : ((BinaryExpression)expr).left;
            var visitExpressionResult2 = this.visitExpression(((BinaryExpression)expr).right);
            ((BinaryExpression)expr).right = visitExpressionResult2 != null ? visitExpressionResult2 : ((BinaryExpression)expr).right;
        }
        else if (expr instanceof NullCoalesceExpression) {
            var visitExpressionResult3 = this.visitExpression(((NullCoalesceExpression)expr).defaultExpr);
            ((NullCoalesceExpression)expr).defaultExpr = visitExpressionResult3 != null ? visitExpressionResult3 : ((NullCoalesceExpression)expr).defaultExpr;
            var visitExpressionResult4 = this.visitExpression(((NullCoalesceExpression)expr).exprIfNull);
            ((NullCoalesceExpression)expr).exprIfNull = visitExpressionResult4 != null ? visitExpressionResult4 : ((NullCoalesceExpression)expr).exprIfNull;
        }
        else if (expr instanceof UnresolvedCallExpression) {
            var visitExpressionResult5 = this.visitExpression(((UnresolvedCallExpression)expr).func);
            ((UnresolvedCallExpression)expr).func = visitExpressionResult5 != null ? visitExpressionResult5 : ((UnresolvedCallExpression)expr).func;
            ((UnresolvedCallExpression)expr).typeArgs = Arrays.stream(((UnresolvedCallExpression)expr).typeArgs).map(x -> {
                var visitTypeResult = this.visitType(x);
                return visitTypeResult != null ? visitTypeResult : x;
            }).toArray(IType[]::new);
            ((UnresolvedCallExpression)expr).args = Arrays.stream(((UnresolvedCallExpression)expr).args).map(x -> {
                var visitExpressionResult6 = this.visitExpression(x);
                return visitExpressionResult6 != null ? visitExpressionResult6 : x;
            }).toArray(Expression[]::new);
        }
        else if (expr instanceof UnresolvedMethodCallExpression) {
            var visitExpressionResult7 = this.visitExpression(((UnresolvedMethodCallExpression)expr).object);
            ((UnresolvedMethodCallExpression)expr).object = visitExpressionResult7 != null ? visitExpressionResult7 : ((UnresolvedMethodCallExpression)expr).object;
            ((UnresolvedMethodCallExpression)expr).typeArgs = Arrays.stream(((UnresolvedMethodCallExpression)expr).typeArgs).map(x -> {
                var visitTypeResult2 = this.visitType(x);
                return visitTypeResult2 != null ? visitTypeResult2 : x;
            }).toArray(IType[]::new);
            ((UnresolvedMethodCallExpression)expr).args = Arrays.stream(((UnresolvedMethodCallExpression)expr).args).map(x -> {
                var visitExpressionResult8 = this.visitExpression(x);
                return visitExpressionResult8 != null ? visitExpressionResult8 : x;
            }).toArray(Expression[]::new);
        }
        else if (expr instanceof ConditionalExpression) {
            var visitExpressionResult9 = this.visitExpression(((ConditionalExpression)expr).condition);
            ((ConditionalExpression)expr).condition = visitExpressionResult9 != null ? visitExpressionResult9 : ((ConditionalExpression)expr).condition;
            var visitExpressionResult10 = this.visitExpression(((ConditionalExpression)expr).whenTrue);
            ((ConditionalExpression)expr).whenTrue = visitExpressionResult10 != null ? visitExpressionResult10 : ((ConditionalExpression)expr).whenTrue;
            var visitExpressionResult11 = this.visitExpression(((ConditionalExpression)expr).whenFalse);
            ((ConditionalExpression)expr).whenFalse = visitExpressionResult11 != null ? visitExpressionResult11 : ((ConditionalExpression)expr).whenFalse;
        }
        else if (expr instanceof Identifier)
            return this.visitIdentifier(((Identifier)expr));
        else if (expr instanceof UnresolvedNewExpression) {
            this.visitType(((UnresolvedNewExpression)expr).cls);
            ((UnresolvedNewExpression)expr).args = Arrays.stream(((UnresolvedNewExpression)expr).args).map(x -> {
                var visitExpressionResult12 = this.visitExpression(x);
                return visitExpressionResult12 != null ? visitExpressionResult12 : x;
            }).toArray(Expression[]::new);
        }
        else if (expr instanceof NewExpression) {
            this.visitType(((NewExpression)expr).cls);
            ((NewExpression)expr).args = Arrays.stream(((NewExpression)expr).args).map(x -> {
                var visitExpressionResult13 = this.visitExpression(x);
                return visitExpressionResult13 != null ? visitExpressionResult13 : x;
            }).toArray(Expression[]::new);
        }
        else if (expr instanceof TemplateString)
            return this.visitTemplateString(((TemplateString)expr));
        else if (expr instanceof ParenthesizedExpression) {
            var visitExpressionResult14 = this.visitExpression(((ParenthesizedExpression)expr).expression);
            ((ParenthesizedExpression)expr).expression = visitExpressionResult14 != null ? visitExpressionResult14 : ((ParenthesizedExpression)expr).expression;
        }
        else if (expr instanceof UnaryExpression) {
            var visitExpressionResult15 = this.visitExpression(((UnaryExpression)expr).operand);
            ((UnaryExpression)expr).operand = visitExpressionResult15 != null ? visitExpressionResult15 : ((UnaryExpression)expr).operand;
        }
        else if (expr instanceof PropertyAccessExpression) {
            var visitExpressionResult16 = this.visitExpression(((PropertyAccessExpression)expr).object);
            ((PropertyAccessExpression)expr).object = visitExpressionResult16 != null ? visitExpressionResult16 : ((PropertyAccessExpression)expr).object;
        }
        else if (expr instanceof ElementAccessExpression) {
            var visitExpressionResult17 = this.visitExpression(((ElementAccessExpression)expr).object);
            ((ElementAccessExpression)expr).object = visitExpressionResult17 != null ? visitExpressionResult17 : ((ElementAccessExpression)expr).object;
            var visitExpressionResult18 = this.visitExpression(((ElementAccessExpression)expr).elementExpr);
            ((ElementAccessExpression)expr).elementExpr = visitExpressionResult18 != null ? visitExpressionResult18 : ((ElementAccessExpression)expr).elementExpr;
        }
        else if (expr instanceof ArrayLiteral)
            ((ArrayLiteral)expr).items = Arrays.stream(((ArrayLiteral)expr).items).map(x -> {
                var visitExpressionResult19 = this.visitExpression(x);
                return visitExpressionResult19 != null ? visitExpressionResult19 : x;
            }).toArray(Expression[]::new);
        else if (expr instanceof MapLiteral)
            for (var item : ((MapLiteral)expr).items) {
                var visitExpressionResult20 = this.visitExpression(item.value);
                item.value = visitExpressionResult20 != null ? visitExpressionResult20 : item.value;
            }
        else if (expr instanceof StringLiteral) { }
        else if (expr instanceof BooleanLiteral) { }
        else if (expr instanceof NumericLiteral) { }
        else if (expr instanceof NullLiteral) { }
        else if (expr instanceof RegexLiteral) { }
        else if (expr instanceof CastExpression) {
            var visitTypeResult3 = this.visitType(((CastExpression)expr).newType);
            ((CastExpression)expr).newType = visitTypeResult3 != null ? visitTypeResult3 : ((CastExpression)expr).newType;
            var visitExpressionResult21 = this.visitExpression(((CastExpression)expr).expression);
            ((CastExpression)expr).expression = visitExpressionResult21 != null ? visitExpressionResult21 : ((CastExpression)expr).expression;
        }
        else if (expr instanceof InstanceOfExpression) {
            var visitExpressionResult22 = this.visitExpression(((InstanceOfExpression)expr).expr);
            ((InstanceOfExpression)expr).expr = visitExpressionResult22 != null ? visitExpressionResult22 : ((InstanceOfExpression)expr).expr;
            var visitTypeResult4 = this.visitType(((InstanceOfExpression)expr).checkType);
            ((InstanceOfExpression)expr).checkType = visitTypeResult4 != null ? visitTypeResult4 : ((InstanceOfExpression)expr).checkType;
        }
        else if (expr instanceof AwaitExpression) {
            var visitExpressionResult23 = this.visitExpression(((AwaitExpression)expr).expr);
            ((AwaitExpression)expr).expr = visitExpressionResult23 != null ? visitExpressionResult23 : ((AwaitExpression)expr).expr;
        }
        else if (expr instanceof Lambda)
            return this.visitLambda(((Lambda)expr));
        else if (expr instanceof ClassReference) { }
        else if (expr instanceof EnumReference) { }
        else if (expr instanceof ThisReference) { }
        else if (expr instanceof StaticThisReference) { }
        else if (expr instanceof MethodParameterReference)
            return this.visitVariableReference(((MethodParameterReference)expr));
        else if (expr instanceof VariableDeclarationReference)
            return this.visitVariableReference(((VariableDeclarationReference)expr));
        else if (expr instanceof ForVariableReference)
            return this.visitVariableReference(((ForVariableReference)expr));
        else if (expr instanceof ForeachVariableReference)
            return this.visitVariableReference(((ForeachVariableReference)expr));
        else if (expr instanceof CatchVariableReference)
            return this.visitVariableReference(((CatchVariableReference)expr));
        else if (expr instanceof GlobalFunctionReference) { }
        else if (expr instanceof SuperReference) { }
        else if (expr instanceof InstanceFieldReference) {
            var visitExpressionResult24 = this.visitExpression(((InstanceFieldReference)expr).object);
            ((InstanceFieldReference)expr).object = visitExpressionResult24 != null ? visitExpressionResult24 : ((InstanceFieldReference)expr).object;
            return this.visitVariableReference(((InstanceFieldReference)expr));
        }
        else if (expr instanceof InstancePropertyReference) {
            var visitExpressionResult25 = this.visitExpression(((InstancePropertyReference)expr).object);
            ((InstancePropertyReference)expr).object = visitExpressionResult25 != null ? visitExpressionResult25 : ((InstancePropertyReference)expr).object;
            return this.visitVariableReference(((InstancePropertyReference)expr));
        }
        else if (expr instanceof StaticFieldReference)
            return this.visitVariableReference(((StaticFieldReference)expr));
        else if (expr instanceof StaticPropertyReference)
            return this.visitVariableReference(((StaticPropertyReference)expr));
        else if (expr instanceof EnumMemberReference) { }
        else if (expr instanceof StaticMethodCallExpression) {
            ((StaticMethodCallExpression)expr).setTypeArgs(Arrays.stream(((StaticMethodCallExpression)expr).getTypeArgs()).map(x -> {
                var visitTypeResult5 = this.visitType(x);
                return visitTypeResult5 != null ? visitTypeResult5 : x;
            }).toArray(IType[]::new));
            ((StaticMethodCallExpression)expr).setArgs(Arrays.stream(((StaticMethodCallExpression)expr).getArgs()).map(x -> {
                var visitExpressionResult26 = this.visitExpression(x);
                return visitExpressionResult26 != null ? visitExpressionResult26 : x;
            }).toArray(Expression[]::new));
        }
        else if (expr instanceof GlobalFunctionCallExpression)
            ((GlobalFunctionCallExpression)expr).args = Arrays.stream(((GlobalFunctionCallExpression)expr).args).map(x -> {
                var visitExpressionResult27 = this.visitExpression(x);
                return visitExpressionResult27 != null ? visitExpressionResult27 : x;
            }).toArray(Expression[]::new);
        else if (expr instanceof InstanceMethodCallExpression) {
            var visitExpressionResult28 = this.visitExpression(((InstanceMethodCallExpression)expr).object);
            ((InstanceMethodCallExpression)expr).object = visitExpressionResult28 != null ? visitExpressionResult28 : ((InstanceMethodCallExpression)expr).object;
            ((InstanceMethodCallExpression)expr).setTypeArgs(Arrays.stream(((InstanceMethodCallExpression)expr).getTypeArgs()).map(x -> {
                var visitTypeResult6 = this.visitType(x);
                return visitTypeResult6 != null ? visitTypeResult6 : x;
            }).toArray(IType[]::new));
            ((InstanceMethodCallExpression)expr).setArgs(Arrays.stream(((InstanceMethodCallExpression)expr).getArgs()).map(x -> {
                var visitExpressionResult29 = this.visitExpression(x);
                return visitExpressionResult29 != null ? visitExpressionResult29 : x;
            }).toArray(Expression[]::new));
        }
        else if (expr instanceof LambdaCallExpression)
            ((LambdaCallExpression)expr).args = Arrays.stream(((LambdaCallExpression)expr).args).map(x -> {
                var visitExpressionResult30 = this.visitExpression(x);
                return visitExpressionResult30 != null ? visitExpressionResult30 : x;
            }).toArray(Expression[]::new);
        else
            return this.visitUnknownExpression(expr);
        return null;
    }
    
    protected void visitMethodParameter(MethodParameter methodParameter) {
        this.visitAttributesAndTrivia(methodParameter);
        this.visitVariableWithInitializer(methodParameter);
    }
    
    protected void visitMethodBase(IMethodBase method) {
        for (var item : method.getParameters())
            this.visitMethodParameter(item);
        
        if (method.getBody() != null) {
            var visitBlockResult = this.visitBlock(method.getBody());
            method.setBody(visitBlockResult != null ? visitBlockResult : method.getBody());
        }
    }
    
    protected void visitMethod(Method method) {
        this.currentMethod = method;
        this.currentClosure = method;
        this.visitAttributesAndTrivia(method);
        this.visitMethodBase(method);
        var visitTypeResult = this.visitType(method.returns);
        method.returns = visitTypeResult != null ? visitTypeResult : method.returns;
        this.currentClosure = null;
        this.currentMethod = null;
    }
    
    protected void visitGlobalFunction(GlobalFunction func) {
        this.visitMethodBase(func);
        var visitTypeResult = this.visitType(func.returns);
        func.returns = visitTypeResult != null ? visitTypeResult : func.returns;
    }
    
    protected void visitConstructor(Constructor constructor) {
        this.currentMethod = constructor;
        this.currentClosure = constructor;
        this.visitAttributesAndTrivia(constructor);
        this.visitMethodBase(constructor);
        this.currentClosure = null;
        this.currentMethod = null;
    }
    
    protected void visitField(Field field) {
        this.visitAttributesAndTrivia(field);
        this.visitVariableWithInitializer(field);
    }
    
    protected void visitProperty(Property prop) {
        this.visitAttributesAndTrivia(prop);
        this.visitVariable(prop);
        if (prop.getter != null) {
            var visitBlockResult = this.visitBlock(prop.getter);
            prop.getter = visitBlockResult != null ? visitBlockResult : prop.getter;
        }
        if (prop.setter != null) {
            var visitBlockResult2 = this.visitBlock(prop.setter);
            prop.setter = visitBlockResult2 != null ? visitBlockResult2 : prop.setter;
        }
    }
    
    protected void visitInterface(Interface intf) {
        this.currentInterface = intf;
        this.visitAttributesAndTrivia(intf);
        intf.setBaseInterfaces(Arrays.stream(intf.getBaseInterfaces()).map(x -> {
            var visitTypeResult = this.visitType(x);
            return visitTypeResult != null ? visitTypeResult : x;
        }).toArray(IType[]::new));
        for (var field : intf.getFields())
            this.visitField(field);
        for (var method : intf.getMethods())
            this.visitMethod(method);
        this.currentInterface = null;
    }
    
    protected void visitClass(Class cls) {
        this.currentInterface = cls;
        this.visitAttributesAndTrivia(cls);
        if (cls.constructor_ != null)
            this.visitConstructor(cls.constructor_);
        var visitTypeResult = this.visitType(cls.baseClass);
        
        cls.baseClass = visitTypeResult != null ? visitTypeResult : cls.baseClass;
        cls.setBaseInterfaces(Arrays.stream(cls.getBaseInterfaces()).map(x -> {
            var visitTypeResult2 = this.visitType(x);
            return visitTypeResult2 != null ? visitTypeResult2 : x;
        }).toArray(IType[]::new));
        for (var field : cls.getFields())
            this.visitField(field);
        for (var prop : cls.properties)
            this.visitProperty(prop);
        for (var method : cls.getMethods())
            this.visitMethod(method);
        this.currentInterface = null;
    }
    
    protected void visitEnum(Enum enum_) {
        this.visitAttributesAndTrivia(enum_);
        for (var value : enum_.values)
            this.visitEnumMember(value);
    }
    
    protected void visitEnumMember(EnumMember enumMember) {
        
    }
    
    protected void visitImport(Import imp) {
        this.visitAttributesAndTrivia(imp);
    }
    
    public void visitFile(SourceFile sourceFile) {
        this.errorMan.resetContext(this);
        this.currentFile = sourceFile;
        for (var imp : sourceFile.imports)
            this.visitImport(imp);
        for (var enum_ : sourceFile.enums)
            this.visitEnum(enum_);
        for (var intf : sourceFile.interfaces)
            this.visitInterface(intf);
        for (var cls : sourceFile.classes)
            this.visitClass(cls);
        for (var func : sourceFile.funcs)
            this.visitGlobalFunction(func);
        var visitBlockResult = this.visitBlock(sourceFile.mainBlock);
        sourceFile.mainBlock = visitBlockResult != null ? visitBlockResult : sourceFile.mainBlock;
        this.currentFile = null;
    }
    
    public void visitFiles(SourceFile[] files) {
        for (var file : files)
            this.visitFile(file);
    }
    
    public void visitPackage(Package pkg) {
        this.visitFiles(pkg.files.values().toArray(SourceFile[]::new));
    }
}