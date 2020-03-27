using System.Collections.Generic;
using One.Ast;
using One;

namespace One
{
    public class AstTransformer {
        public ErrorManager errorMan;
        public SourceFile currentFile;
        public IInterface currentInterface;
        public IMethodBase currentMethod;
        public Statement currentStatement;
        public string name;
        
        public AstTransformer(string name) {
            this.errorMan = new ErrorManager();
            this.currentFile = null;
            this.currentInterface = null;
            this.currentMethod = null;
            this.currentStatement = null;
            this.name = name;
        }
        
        protected virtual Type_ visitType(Type_ type) {
            if (type is ClassType || type is InterfaceType || type is UnresolvedType) {
                var type2 = ((IHasTypeArguments)type);
                type2.typeArguments = type2.typeArguments.map((Type_ x) => { return this.visitType(x) ?? x; });
            }
            else if (type is LambdaType) {
                foreach (var mp in ((LambdaType)type).parameters)
                    this.visitMethodParameter(mp);
                ((LambdaType)type).returnType = this.visitType(((LambdaType)type).returnType) ?? ((LambdaType)type).returnType;
            }
            return null;
        }
        
        protected virtual Expression visitIdentifier(Identifier id) {
            return null;
        }
        
        protected IVariable visitVariable(IVariable variable) {
            if (variable.type != null)
                variable.type = this.visitType(variable.type) ?? variable.type;
            return null;
        }
        
        protected virtual IVariableWithInitializer visitVariableWithInitializer(IVariableWithInitializer variable) {
            this.visitVariable(variable);
            if (variable.initializer != null)
                variable.initializer = this.visitExpression(variable.initializer) ?? variable.initializer;
            return null;
        }
        
        protected virtual VariableDeclaration visitVariableDeclaration(VariableDeclaration stmt) {
            this.visitVariableWithInitializer(stmt);
            return null;
        }
        
        protected Statement visitUnknownStatement(Statement stmt) {
            this.errorMan.throw_($"Unknown statement type");
            return null;
        }
        
        protected virtual Statement visitStatement(Statement stmt) {
            this.currentStatement = stmt;
            if (stmt is ReturnStatement)
                if (((ReturnStatement)stmt).expression != null)
                    ((ReturnStatement)stmt).expression = this.visitExpression(((ReturnStatement)stmt).expression) ?? ((ReturnStatement)stmt).expression;
            else if (stmt is ExpressionStatement)
                ((ExpressionStatement)stmt).expression = this.visitExpression(((ExpressionStatement)stmt).expression) ?? ((ExpressionStatement)stmt).expression;
            else if (stmt is IfStatement) {
                ((IfStatement)stmt).condition = this.visitExpression(((IfStatement)stmt).condition) ?? ((IfStatement)stmt).condition;
                ((IfStatement)stmt).then = this.visitBlock(((IfStatement)stmt).then) ?? ((IfStatement)stmt).then;
                if (((IfStatement)stmt).else_ != null)
                    ((IfStatement)stmt).else_ = this.visitBlock(((IfStatement)stmt).else_) ?? ((IfStatement)stmt).else_;
            }
            else if (stmt is ThrowStatement)
                ((ThrowStatement)stmt).expression = this.visitExpression(((ThrowStatement)stmt).expression) ?? ((ThrowStatement)stmt).expression;
            else if (stmt is VariableDeclaration)
                return this.visitVariableDeclaration(((VariableDeclaration)stmt));
            else if (stmt is WhileStatement) {
                ((WhileStatement)stmt).condition = this.visitExpression(((WhileStatement)stmt).condition) ?? ((WhileStatement)stmt).condition;
                ((WhileStatement)stmt).body = this.visitBlock(((WhileStatement)stmt).body) ?? ((WhileStatement)stmt).body;
            }
            else if (stmt is DoStatement) {
                ((DoStatement)stmt).condition = this.visitExpression(((DoStatement)stmt).condition) ?? ((DoStatement)stmt).condition;
                ((DoStatement)stmt).body = this.visitBlock(((DoStatement)stmt).body) ?? ((DoStatement)stmt).body;
            }
            else if (stmt is ForStatement) {
                if (((ForStatement)stmt).itemVar != null)
                    this.visitVariableWithInitializer(((ForStatement)stmt).itemVar);
                ((ForStatement)stmt).condition = this.visitExpression(((ForStatement)stmt).condition) ?? ((ForStatement)stmt).condition;
                ((ForStatement)stmt).incrementor = this.visitExpression(((ForStatement)stmt).incrementor) ?? ((ForStatement)stmt).incrementor;
                ((ForStatement)stmt).body = this.visitBlock(((ForStatement)stmt).body) ?? ((ForStatement)stmt).body;
            }
            else if (stmt is ForeachStatement) {
                this.visitVariable(((ForeachStatement)stmt).itemVar);
                ((ForeachStatement)stmt).items = this.visitExpression(((ForeachStatement)stmt).items) ?? ((ForeachStatement)stmt).items;
                ((ForeachStatement)stmt).body = this.visitBlock(((ForeachStatement)stmt).body) ?? ((ForeachStatement)stmt).body;
            }
            else if (stmt is TryStatement) {
                ((TryStatement)stmt).tryBody = this.visitBlock(((TryStatement)stmt).tryBody) ?? ((TryStatement)stmt).tryBody;
                if (((TryStatement)stmt).catchBody != null) {
                    this.visitVariable(((TryStatement)stmt).catchVar);
                    ((TryStatement)stmt).catchBody = this.visitBlock(((TryStatement)stmt).catchBody) ?? ((TryStatement)stmt).catchBody;
                }
                if (((TryStatement)stmt).finallyBody != null)
                    ((TryStatement)stmt).finallyBody = this.visitBlock(((TryStatement)stmt).finallyBody) ?? ((TryStatement)stmt).finallyBody;
            }
            else if (stmt is BreakStatement) { }
            else if (stmt is UnsetStatement)
                ((UnsetStatement)stmt).expression = this.visitExpression(((UnsetStatement)stmt).expression) ?? ((UnsetStatement)stmt).expression;
            else if (stmt is ContinueStatement) { }
            else
                return this.visitUnknownStatement(stmt);
            return null;
        }
        
        protected virtual Block visitBlock(Block block) {
            block.statements = block.statements.map((Statement x) => { return this.visitStatement(x) ?? x; });
            return null;
        }
        
        protected TemplateString visitTemplateString(TemplateString expr) {
            for (int i = 0; i < expr.parts.length(); i++) {
                var part = expr.parts.get(i);
                if (!part.isLiteral)
                    part.expression = this.visitExpression(part.expression) ?? part.expression;
            }
            return null;
        }
        
        protected Expression visitUnknownExpression(Expression expr) {
            this.errorMan.throw_($"Unknown expression type");
            return null;
        }
        
        protected virtual Lambda visitLambda(Lambda lambda) {
            this.visitMethodBase(lambda);
            return null;
        }
        
        protected virtual Expression visitExpression(Expression expr) {
            if (expr is BinaryExpression) {
                ((BinaryExpression)expr).left = this.visitExpression(((BinaryExpression)expr).left) ?? ((BinaryExpression)expr).left;
                ((BinaryExpression)expr).right = this.visitExpression(((BinaryExpression)expr).right) ?? ((BinaryExpression)expr).right;
            }
            else if (expr is NullCoalesceExpression) {
                ((NullCoalesceExpression)expr).defaultExpr = this.visitExpression(((NullCoalesceExpression)expr).defaultExpr) ?? ((NullCoalesceExpression)expr).defaultExpr;
                ((NullCoalesceExpression)expr).exprIfNull = this.visitExpression(((NullCoalesceExpression)expr).exprIfNull) ?? ((NullCoalesceExpression)expr).exprIfNull;
            }
            else if (expr is UnresolvedCallExpression) {
                ((UnresolvedCallExpression)expr).func = this.visitExpression(((UnresolvedCallExpression)expr).func) ?? ((UnresolvedCallExpression)expr).func;
                ((UnresolvedCallExpression)expr).typeArgs = ((UnresolvedCallExpression)expr).typeArgs.map((Type_ x) => { return this.visitType(x) ?? x; });
                ((UnresolvedCallExpression)expr).args = ((UnresolvedCallExpression)expr).args.map((Expression x) => { return this.visitExpression(x) ?? x; });
            }
            else if (expr is UnresolvedMethodCallExpression) {
                ((UnresolvedMethodCallExpression)expr).object_ = this.visitExpression(((UnresolvedMethodCallExpression)expr).object_) ?? ((UnresolvedMethodCallExpression)expr).object_;
                ((UnresolvedMethodCallExpression)expr).typeArgs = ((UnresolvedMethodCallExpression)expr).typeArgs.map((Type_ x) => { return this.visitType(x) ?? x; });
                ((UnresolvedMethodCallExpression)expr).args = ((UnresolvedMethodCallExpression)expr).args.map((Expression x) => { return this.visitExpression(x) ?? x; });
            }
            else if (expr is ConditionalExpression) {
                ((ConditionalExpression)expr).condition = this.visitExpression(((ConditionalExpression)expr).condition) ?? ((ConditionalExpression)expr).condition;
                ((ConditionalExpression)expr).whenTrue = this.visitExpression(((ConditionalExpression)expr).whenTrue) ?? ((ConditionalExpression)expr).whenTrue;
                ((ConditionalExpression)expr).whenFalse = this.visitExpression(((ConditionalExpression)expr).whenFalse) ?? ((ConditionalExpression)expr).whenFalse;
            }
            else if (expr is Identifier)
                return this.visitIdentifier(((Identifier)expr));
            else if (expr is UnresolvedNewExpression) {
                this.visitType(((UnresolvedNewExpression)expr).cls);
                ((UnresolvedNewExpression)expr).args = ((UnresolvedNewExpression)expr).args.map((Expression x) => { return this.visitExpression(x) ?? x; });
            }
            else if (expr is NewExpression) {
                this.visitType(((NewExpression)expr).cls);
                ((NewExpression)expr).args = ((NewExpression)expr).args.map((Expression x) => { return this.visitExpression(x) ?? x; });
            }
            else if (expr is TemplateString)
                return this.visitTemplateString(((TemplateString)expr));
            else if (expr is ParenthesizedExpression)
                ((ParenthesizedExpression)expr).expression = this.visitExpression(((ParenthesizedExpression)expr).expression) ?? ((ParenthesizedExpression)expr).expression;
            else if (expr is UnaryExpression)
                ((UnaryExpression)expr).operand = this.visitExpression(((UnaryExpression)expr).operand) ?? ((UnaryExpression)expr).operand;
            else if (expr is PropertyAccessExpression)
                ((PropertyAccessExpression)expr).object_ = this.visitExpression(((PropertyAccessExpression)expr).object_) ?? ((PropertyAccessExpression)expr).object_;
            else if (expr is ElementAccessExpression) {
                ((ElementAccessExpression)expr).object_ = this.visitExpression(((ElementAccessExpression)expr).object_) ?? ((ElementAccessExpression)expr).object_;
                ((ElementAccessExpression)expr).elementExpr = this.visitExpression(((ElementAccessExpression)expr).elementExpr) ?? ((ElementAccessExpression)expr).elementExpr;
            }
            else if (expr is ArrayLiteral)
                ((ArrayLiteral)expr).items = ((ArrayLiteral)expr).items.map((Expression x) => { return this.visitExpression(x) ?? x; });
            else if (expr is MapLiteral)
                foreach (var item in ((MapLiteral)expr).items)
                    item.value = this.visitExpression(item.value) ?? item.value;
            else if (expr is StringLiteral) { }
            else if (expr is BooleanLiteral) { }
            else if (expr is NumericLiteral) { }
            else if (expr is NullLiteral) { }
            else if (expr is RegexLiteral) { }
            else if (expr is CastExpression) {
                ((CastExpression)expr).newType = this.visitType(((CastExpression)expr).newType) ?? ((CastExpression)expr).newType;
                ((CastExpression)expr).expression = this.visitExpression(((CastExpression)expr).expression) ?? ((CastExpression)expr).expression;
            }
            else if (expr is InstanceOfExpression) {
                ((InstanceOfExpression)expr).expr = this.visitExpression(((InstanceOfExpression)expr).expr) ?? ((InstanceOfExpression)expr).expr;
                ((InstanceOfExpression)expr).checkType = this.visitType(((InstanceOfExpression)expr).checkType) ?? ((InstanceOfExpression)expr).checkType;
            }
            else if (expr is AwaitExpression)
                ((AwaitExpression)expr).expr = this.visitExpression(((AwaitExpression)expr).expr) ?? ((AwaitExpression)expr).expr;
            else if (expr is Lambda)
                return this.visitLambda(((Lambda)expr));
            else if (expr is ClassReference) { }
            else if (expr is EnumReference) { }
            else if (expr is ThisReference) { }
            else if (expr is StaticThisReference) { }
            else if (expr is MethodParameterReference) { }
            else if (expr is VariableDeclarationReference) { }
            else if (expr is ForVariableReference) { }
            else if (expr is ForeachVariableReference) { }
            else if (expr is CatchVariableReference) { }
            else if (expr is GlobalFunctionReference) { }
            else if (expr is SuperReference) { }
            else if (expr is InstanceFieldReference) { }
            else if (expr is InstancePropertyReference) { }
            else if (expr is StaticFieldReference) { }
            else if (expr is StaticPropertyReference) { }
            else if (expr is EnumMemberReference) { }
            else if (expr is StaticMethodCallExpression) {
                ((StaticMethodCallExpression)expr).typeArgs = ((StaticMethodCallExpression)expr).typeArgs.map((Type_ x) => { return this.visitType(x) ?? x; });
                ((StaticMethodCallExpression)expr).args = ((StaticMethodCallExpression)expr).args.map((Expression x) => { return this.visitExpression(x) ?? x; });
            }
            else if (expr is InstanceMethodCallExpression) {
                ((InstanceMethodCallExpression)expr).object_ = this.visitExpression(((InstanceMethodCallExpression)expr).object_) ?? ((InstanceMethodCallExpression)expr).object_;
                ((InstanceMethodCallExpression)expr).typeArgs = ((InstanceMethodCallExpression)expr).typeArgs.map((Type_ x) => { return this.visitType(x) ?? x; });
                ((InstanceMethodCallExpression)expr).args = ((InstanceMethodCallExpression)expr).args.map((Expression x) => { return this.visitExpression(x) ?? x; });
            }
            else
                return this.visitUnknownExpression(expr);
            return null;
        }
        
        protected void visitMethodParameter(MethodParameter methodParameter) {
            this.visitVariableWithInitializer(methodParameter);
        }
        
        protected virtual void visitMethodBase(IMethodBase method) {
            foreach (var item in method.parameters)
                this.visitMethodParameter(item);
            
            if (method.body != null)
                method.body = this.visitBlock(method.body) ?? method.body;
        }
        
        protected void visitMethod(Method method) {
            this.currentMethod = method;
            this.visitMethodBase(method);
            method.returns = this.visitType(method.returns) ?? method.returns;
            this.currentMethod = null;
        }
        
        protected void visitGlobalFunction(GlobalFunction func) {
            this.visitMethodBase(func);
            func.returns = this.visitType(func.returns) ?? func.returns;
        }
        
        protected void visitConstructor(Constructor constructor) {
            this.currentMethod = constructor;
            this.visitMethodBase(constructor);
            this.currentMethod = null;
        }
        
        protected virtual void visitField(Field field) {
            this.visitVariableWithInitializer(field);
        }
        
        protected virtual void visitProperty(Property prop) {
            this.visitVariable(prop);
            if (prop.getter != null)
                prop.getter = this.visitBlock(prop.getter) ?? prop.getter;
            if (prop.setter != null)
                prop.setter = this.visitBlock(prop.setter) ?? prop.setter;
        }
        
        protected virtual void visitInterface(Interface intf) {
            this.currentInterface = intf;
            intf.baseInterfaces = intf.baseInterfaces.map((Type_ x) => { return this.visitType(x) ?? x; });
            foreach (var field in intf.fields)
                this.visitField(field);
            foreach (var method in intf.methods)
                this.visitMethod(method);
            this.currentInterface = null;
        }
        
        protected virtual void visitClass(Class cls) {
            this.currentInterface = cls;
            if (cls.constructor_ != null)
                this.visitConstructor(cls.constructor_);
            
            cls.baseClass = this.visitType(cls.baseClass) ?? cls.baseClass;
            cls.baseInterfaces = cls.baseInterfaces.map((Type_ x) => { return this.visitType(x) ?? x; });
            foreach (var field in cls.fields)
                this.visitField(field);
            foreach (var prop in cls.properties)
                this.visitProperty(prop);
            foreach (var method in cls.methods)
                this.visitMethod(method);
            this.currentInterface = null;
        }
        
        protected virtual void visitEnum(Enum_ enum_) {
            foreach (var value in enum_.values)
                this.visitEnumMember(value);
        }
        
        protected void visitEnumMember(EnumMember enumMember) {
            
        }
        
        public virtual void visitSourceFile(SourceFile sourceFile) {
            this.errorMan.resetContext(this);
            this.currentFile = sourceFile;
            foreach (var enum_ in sourceFile.enums)
                this.visitEnum(enum_);
            foreach (var intf in sourceFile.interfaces)
                this.visitInterface(intf);
            foreach (var cls in sourceFile.classes)
                this.visitClass(cls);
            foreach (var func in sourceFile.funcs)
                this.visitGlobalFunction(func);
            sourceFile.mainBlock = this.visitBlock(sourceFile.mainBlock) ?? sourceFile.mainBlock;
            this.currentFile = null;
        }
        
        public virtual void visitPackage(Package pkg) {
            foreach (var file in Object.values(pkg.files))
                this.visitSourceFile(file);
        }
    }
}