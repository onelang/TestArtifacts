using System.Collections.Generic;
using One.Ast;
using One.Transforms;

namespace Generator
{
    public class GeneratedFile {
        public string path;
        public string content;
        
        public GeneratedFile(string path, string content)
        {
            this.path = path;
            this.content = content;
        }
    }
    
    public class CsharpGenerator {
        public Set<string> usings;
        public IInterface currentClass;
        public string[] reservedWords;
        public string[] fieldToMethodHack;
        
        public CsharpGenerator()
        {
            this.usings = new Set<string>();
            this.reservedWords = new string[] { "object", "else", "operator", "class", "enum", "void", "string", "implicit", "Type", "Enum", "params", "using", "throw", "ref", "base", "virtual", "interface" };
            this.fieldToMethodHack = new string[] { "length" };
        }
        
        public string name_(string name) {
            if (this.reservedWords.includes(name))
                name += "_";
            if (this.fieldToMethodHack.includes(name))
                name += "()";
            var nameParts = name.split(new RegExp("-"));
            for (int i = 1; i < nameParts.length(); i++)
                nameParts.set(i, nameParts.get(i).get(0).toUpperCase() + nameParts.get(i).substr(1));
            name = nameParts.join("");
            return name;
        }
        
        public string leading(Statement item) {
            var result = "";
            if (item.leadingTrivia != null && item.leadingTrivia.length() > 0)
                result += item.leadingTrivia;
            //if (item.attributes !== null)
            //    result += Object.keys(item.attributes).map(x => `// @${x} ${item.attributes[x]}\n`).join("");
            return result;
        }
        
        public string preArr(string prefix, string[] value) {
            return value.length() > 0 ? $"{prefix}{value.join(", ")}" : "";
        }
        
        public string preIf(string prefix, bool condition) {
            return condition ? prefix : "";
        }
        
        public string pre(string prefix, string value) {
            return value != null ? $"{prefix}{value}" : "";
        }
        
        public string typeArgs(string[] args) {
            return args != null && args.length() > 0 ? $"<{args.join(", ")}>" : "";
        }
        
        public string typeArgs2(Type_[] args) {
            return this.typeArgs(args.map((Type_ x) => { return this.type(x); }));
        }
        
        public string type(Type_ t, bool mutates = true) {
            if (t is ClassType) {
                var typeArgs = this.typeArgs(((ClassType)t).typeArguments.map((Type_ x) => { return this.type(x); }));
                if (((ClassType)t).decl.name == "TsString")
                    return "string";
                else if (((ClassType)t).decl.name == "TsBoolean")
                    return "bool";
                else if (((ClassType)t).decl.name == "TsNumber")
                    return "int";
                else if (((ClassType)t).decl.name == "TsArray") {
                    if (mutates) {
                        this.usings.add("System.Collections.Generic");
                        return $"List<{this.type(((ClassType)t).typeArguments.get(0))}>";
                    }
                    else
                        return $"{this.type(((ClassType)t).typeArguments.get(0))}[]";
                }
                else if (((ClassType)t).decl.name == "Promise") {
                    this.usings.add("System.Threading.Tasks");
                    return ((ClassType)t).typeArguments.get(0) is VoidType ? "Task" : $"Task{typeArgs}";
                }
                else if (((ClassType)t).decl.name == "Object") {
                    this.usings.add("System");
                    return $"object";
                }
                else if (((ClassType)t).decl.name == "TsMap") {
                    this.usings.add("System.Collections.Generic");
                    return $"Dictionary<string, {this.type(((ClassType)t).typeArguments.get(0))}>";
                }
                return this.name_(((ClassType)t).decl.name) + typeArgs;
            }
            else if (t is InterfaceType)
                return $"{this.name_(((InterfaceType)t).decl.name)}{this.typeArgs(((InterfaceType)t).typeArguments.map((Type_ x) => { return this.type(x); }))}";
            else if (t is VoidType)
                return "void";
            else if (t is EnumType)
                return $"{this.name_(((EnumType)t).decl.name)}";
            else if (t is AnyType)
                return $"object";
            else if (t is NullType)
                return $"null";
            else if (t is GenericsType)
                return $"{((GenericsType)t).typeVarName}";
            else if (t is LambdaType) {
                var isFunc = !(((LambdaType)t).returnType is VoidType);
                return $"{(isFunc ? "Func" : "Action")}<{((LambdaType)t).parameters.map((MethodParameter x) => { return this.type(x.type); }).join(", ")}{(isFunc ? this.type(((LambdaType)t).returnType) : "")}>";
            }
            else if (t == null)
                return "/* TODO */ object";
            else
                return "/* MISSING */";
        }
        
        public bool isTsArray(Type_ type) {
            return type is ClassType && ((ClassType)type).decl.name == "TsArray";
        }
        
        public string vis(Visibility v) {
            return v == Visibility.Private ? "private" : v == Visibility.Protected ? "protected" : v == Visibility.Public ? "public" : "/* TODO: not set */public";
        }
        
        public string varWoInit(IVariable v, IHasAttributesAndTrivia attr) {
            string type;
            if (attr != null && attr.attributes != null && attr.attributes.hasKey("csharp-type"))
                type = attr.attributes.get("csharp-type");
            else if (v.type is ClassType && ((ClassType)v.type).decl.name == "TsArray") {
                if (v.mutability.mutated) {
                    this.usings.add("System.Collections.Generic");
                    type = $"List<{this.type(((ClassType)v.type).typeArguments.get(0))}>";
                }
                else
                    type = $"{this.type(((ClassType)v.type).typeArguments.get(0))}[]";
            }
            else
                type = this.type(v.type);
            return $"{type} {this.name_(v.name)}";
        }
        
        public string var(IVariableWithInitializer v, IHasAttributesAndTrivia attrs) {
            return $"{this.varWoInit(v, attrs)}{(v.initializer != null ? $" = {this.expr(v.initializer)}" : "")}";
        }
        
        public string exprCall(Type_[] typeArgs, Expression[] args) {
            return this.typeArgs2(typeArgs) + $"({args.map((Expression x) => { return this.expr(x); }).join(", ")})";
        }
        
        public string mutateArg(Expression arg, bool shouldBeMutable) {
            if (this.isTsArray(arg.actualType)) {
                if (arg is ArrayLiteral && !shouldBeMutable) {
                    var itemType = (((ClassType)((ArrayLiteral)arg).actualType)).typeArguments.get(0);
                    return ((ArrayLiteral)arg).items.length() == 0 && !this.isTsArray(itemType) ? $"new {this.type(itemType)}[0]" : $"new {this.type(itemType)}[] {{ {((ArrayLiteral)arg).items.map((Expression x) => { return this.expr(x); }).join(", ")} }}";
                }
                
                var currentlyMutable = shouldBeMutable;
                if (arg is VariableReference)
                    currentlyMutable = ((VariableReference)arg).getVariable().mutability.mutated;
                else if (arg is InstanceMethodCallExpression || arg is StaticMethodCallExpression)
                    currentlyMutable = false;
                
                if (currentlyMutable && !shouldBeMutable)
                    return $"{this.expr(arg)}.ToArray()";
                else if (!currentlyMutable && shouldBeMutable) {
                    this.usings.add("System.Linq");
                    return $"{this.expr(arg)}.ToList()";
                }
            }
            return this.expr(arg);
        }
        
        public string mutatedExpr(Expression expr, Expression toWhere) {
            if (toWhere is VariableReference) {
                var v = ((VariableReference)toWhere).getVariable();
                if (this.isTsArray(v.type))
                    return this.mutateArg(expr, v.mutability.mutated);
            }
            return this.expr(expr);
        }
        
        public string callParams(Expression[] args, MethodParameter[] params_) {
            var argReprs = new List<string>();
            for (int i = 0; i < args.length(); i++)
                argReprs.push(this.isTsArray(params_.get(i).type) ? this.mutateArg(args.get(i), params_.get(i).mutability.mutated) : this.expr(args.get(i)));
            return $"({argReprs.join(", ")})";
        }
        
        public string methodCall(IMethodCallExpression expr) {
            return this.name_(expr.method.name) + this.typeArgs2(expr.typeArgs) + this.callParams(expr.args, expr.method.parameters);
        }
        
        public string expr(IExpression expr) {
            var res = "UNKNOWN-EXPR";
            if (expr is NewExpression)
                res = $"new {this.type(((NewExpression)expr).cls)}{this.callParams(((NewExpression)expr).args, ((NewExpression)expr).cls.decl.constructor_ != null ? ((NewExpression)expr).cls.decl.constructor_.parameters : new MethodParameter[0])}";
            else if (expr is UnresolvedNewExpression)
                res = $"/* TODO: UnresolvedNewExpression */ new {this.type(((UnresolvedNewExpression)expr).cls)}({((UnresolvedNewExpression)expr).args.map((Expression x) => { return this.expr(x); }).join(", ")})";
            else if (expr is Identifier)
                res = $"/* TODO: Identifier */ {((Identifier)expr).text}";
            else if (expr is PropertyAccessExpression)
                res = $"/* TODO: PropertyAccessExpression */ {this.expr(((PropertyAccessExpression)expr).object_)}.{((PropertyAccessExpression)expr).propertyName}";
            else if (expr is UnresolvedCallExpression)
                res = $"/* TODO: UnresolvedCallExpression */ {this.expr(((UnresolvedCallExpression)expr).func)}{this.exprCall(((UnresolvedCallExpression)expr).typeArgs, ((UnresolvedCallExpression)expr).args)}";
            else if (expr is UnresolvedMethodCallExpression)
                res = $"/* TODO: UnresolvedMethodCallExpression */ {this.expr(((UnresolvedMethodCallExpression)expr).object_)}.{((UnresolvedMethodCallExpression)expr).methodName}{this.exprCall(((UnresolvedMethodCallExpression)expr).typeArgs, ((UnresolvedMethodCallExpression)expr).args)}";
            else if (expr is InstanceMethodCallExpression)
                res = $"{this.expr(((InstanceMethodCallExpression)expr).object_)}.{this.methodCall(((InstanceMethodCallExpression)expr))}";
            else if (expr is StaticMethodCallExpression)
                res = $"{this.name_(((StaticMethodCallExpression)expr).method.parentInterface.name)}.{this.methodCall(((StaticMethodCallExpression)expr))}";
            else if (expr is GlobalFunctionCallExpression)
                res = $"Global.{this.name_(((GlobalFunctionCallExpression)expr).func.name)}{this.exprCall(new Type_[0], ((GlobalFunctionCallExpression)expr).args)}";
            else if (expr is LambdaCallExpression)
                res = $"{this.expr(((LambdaCallExpression)expr).method)}({((LambdaCallExpression)expr).args.map((Expression x) => { return this.expr(x); }).join(", ")})";
            else if (expr is BooleanLiteral)
                res = $"{((BooleanLiteral)expr).boolValue}";
            else if (expr is StringLiteral)
                res = $"{JSON.stringify(((StringLiteral)expr).stringValue)}";
            else if (expr is NumericLiteral)
                res = $"{((NumericLiteral)expr).valueAsText}";
            else if (expr is CharacterLiteral)
                res = $"'{((CharacterLiteral)expr).charValue}'";
            else if (expr is ElementAccessExpression)
                res = $"{this.expr(((ElementAccessExpression)expr).object_)}[{this.expr(((ElementAccessExpression)expr).elementExpr)}]";
            else if (expr is TemplateString) {
                var parts = new List<string>();
                foreach (var part in ((TemplateString)expr).parts) {
                    if (part.isLiteral)
                        parts.push(part.literalText.replace(new RegExp("\\\\"), $"\\\\$").replace(new RegExp("\""), $"\\\"").replace(new RegExp("\\n"), $"\\\n").replace(new RegExp("\\r"), $"\\\r").replace(new RegExp("\\t"), $"\\\t").replace(new RegExp("{"), "{{").replace(new RegExp("}"), "}}"));
                    else {
                        var repr = this.expr(part.expression);
                        parts.push(part.expression is ConditionalExpression ? $"{{({repr})}}" : $"{{{repr}}}");
                    }
                }
                res = $"$\"{parts.join("")}\"";
            }
            else if (expr is BinaryExpression)
                res = $"{this.expr(((BinaryExpression)expr).left)} {((BinaryExpression)expr).operator_} {this.mutatedExpr(((BinaryExpression)expr).right, ((BinaryExpression)expr).operator_ == "=" ? ((BinaryExpression)expr).left : null)}";
            else if (expr is ArrayLiteral) {
                if (((ArrayLiteral)expr).items.length() == 0)
                    res = $"new {this.type(((ArrayLiteral)expr).actualType)}()";
                else
                    res = $"new {this.type(((ArrayLiteral)expr).actualType)} {{ {((ArrayLiteral)expr).items.map((Expression x) => { return this.expr(x); }).join(", ")} }}";
            }
            else if (expr is CastExpression)
                res = $"(({this.type(((CastExpression)expr).newType)}){this.expr(((CastExpression)expr).expression)})";
            else if (expr is ConditionalExpression)
                res = $"{this.expr(((ConditionalExpression)expr).condition)} ? {this.expr(((ConditionalExpression)expr).whenTrue)} : {this.mutatedExpr(((ConditionalExpression)expr).whenFalse, ((ConditionalExpression)expr).whenTrue)}";
            else if (expr is InstanceOfExpression)
                res = $"{this.expr(((InstanceOfExpression)expr).expr)} is {this.type(((InstanceOfExpression)expr).checkType)}";
            else if (expr is ParenthesizedExpression)
                res = $"({this.expr(((ParenthesizedExpression)expr).expression)})";
            else if (expr is RegexLiteral)
                res = $"new RegExp({JSON.stringify(((RegexLiteral)expr).pattern)})";
            else if (expr is Lambda)
                res = $"({((Lambda)expr).parameters.map((MethodParameter x) => { return $"{this.type(x.type)} {this.name_(x.name)}"; }).join(", ")}) => {{ {this.rawBlock(((Lambda)expr).body)} }}";
            else if (expr is UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Prefix)
                res = $"{((UnaryExpression)expr).operator_}{this.expr(((UnaryExpression)expr).operand)}";
            else if (expr is UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Postfix)
                res = $"{this.expr(((UnaryExpression)expr).operand)}{((UnaryExpression)expr).operator_}";
            else if (expr is MapLiteral) {
                var repr = ((MapLiteral)expr).items.map((MapLiteralItem item) => { return $"[{JSON.stringify(item.key)}] = {this.expr(item.value)}"; }).join(",\n");
                res = $"new {this.type(((MapLiteral)expr).actualType)} " + (repr == "" ? "{}" : repr.includes("\n") ? $"{{\n{this.pad(repr)}\n}}" : $"{{ {repr} }}");
            }
            else if (expr is NullLiteral)
                res = $"null";
            else if (expr is AwaitExpression)
                res = $"await {this.expr(((AwaitExpression)expr).expr)}";
            else if (expr is ThisReference)
                res = $"this";
            else if (expr is StaticThisReference)
                res = $"{this.currentClass.name}";
            else if (expr is EnumReference)
                res = $"{this.name_(((EnumReference)expr).decl.name)}";
            else if (expr is ClassReference)
                res = $"{this.name_(((ClassReference)expr).decl.name)}";
            else if (expr is MethodParameterReference)
                res = $"{this.name_(((MethodParameterReference)expr).decl.name)}";
            else if (expr is VariableDeclarationReference)
                res = $"{this.name_(((VariableDeclarationReference)expr).decl.name)}";
            else if (expr is ForVariableReference)
                res = $"{this.name_(((ForVariableReference)expr).decl.name)}";
            else if (expr is ForeachVariableReference)
                res = $"{this.name_(((ForeachVariableReference)expr).decl.name)}";
            else if (expr is CatchVariableReference)
                res = $"{this.name_(((CatchVariableReference)expr).decl.name)}";
            else if (expr is GlobalFunctionReference)
                res = $"{this.name_(((GlobalFunctionReference)expr).decl.name)}";
            else if (expr is SuperReference)
                res = $"base";
            else if (expr is StaticFieldReference)
                res = $"{this.name_(((StaticFieldReference)expr).decl.parentInterface.name)}.{this.name_(((StaticFieldReference)expr).decl.name)}";
            else if (expr is StaticPropertyReference)
                res = $"{this.name_(((StaticPropertyReference)expr).decl.parentClass.name)}.{this.name_(((StaticPropertyReference)expr).decl.name)}";
            else if (expr is InstanceFieldReference)
                res = $"{this.expr(((InstanceFieldReference)expr).object_)}.{this.name_(((InstanceFieldReference)expr).field.name)}";
            else if (expr is InstancePropertyReference)
                res = $"{this.expr(((InstancePropertyReference)expr).object_)}.{this.name_(((InstancePropertyReference)expr).property.name)}";
            else if (expr is EnumMemberReference)
                res = $"{this.name_(((EnumMemberReference)expr).decl.parentEnum.name)}.{this.name_(((EnumMemberReference)expr).decl.name)}";
            else if (expr is NullCoalesceExpression)
                res = $"{this.expr(((NullCoalesceExpression)expr).defaultExpr)} ?? {this.mutatedExpr(((NullCoalesceExpression)expr).exprIfNull, ((NullCoalesceExpression)expr).defaultExpr)}";
            else { }
            return res;
        }
        
        public string block(Block block, bool allowOneLiner = true) {
            var stmtLen = block.statements.length();
            return stmtLen == 0 ? " { }" : allowOneLiner && stmtLen == 1 && !(block.statements.get(0) is IfStatement) ? $"\n{this.pad(this.rawBlock(block))}" : $" {{\n{this.pad(this.rawBlock(block))}\n}}";
        }
        
        public string stmt(Statement stmt) {
            var res = "UNKNOWN-STATEMENT";
            if (stmt.attributes != null && stmt.attributes.hasKey("csharp"))
                res = stmt.attributes.get("csharp");
            else if (stmt is BreakStatement)
                res = "break;";
            else if (stmt is ReturnStatement)
                res = ((ReturnStatement)stmt).expression == null ? "return;" : $"return {this.mutateArg(((ReturnStatement)stmt).expression, false)};";
            else if (stmt is UnsetStatement)
                res = $"/* unset {this.expr(((UnsetStatement)stmt).expression)}; */";
            else if (stmt is ThrowStatement)
                res = $"throw {this.expr(((ThrowStatement)stmt).expression)};";
            else if (stmt is ExpressionStatement)
                res = $"{this.expr(((ExpressionStatement)stmt).expression)};";
            else if (stmt is VariableDeclaration) {
                if (((VariableDeclaration)stmt).initializer is NullLiteral)
                    res = $"{this.type(((VariableDeclaration)stmt).type, ((VariableDeclaration)stmt).mutability.mutated)} {this.name_(((VariableDeclaration)stmt).name)} = null;";
                else if (((VariableDeclaration)stmt).initializer != null)
                    res = $"var {this.name_(((VariableDeclaration)stmt).name)} = {this.expr(((VariableDeclaration)stmt).initializer)};";
                else
                    res = $"{this.type(((VariableDeclaration)stmt).type)} {this.name_(((VariableDeclaration)stmt).name)};";
            }
            else if (stmt is ForeachStatement)
                res = $"foreach (var {this.name_(((ForeachStatement)stmt).itemVar.name)} in {this.expr(((ForeachStatement)stmt).items)})" + this.block(((ForeachStatement)stmt).body);
            else if (stmt is IfStatement) {
                var elseIf = ((IfStatement)stmt).else_ != null && ((IfStatement)stmt).else_.statements.length() == 1 && ((IfStatement)stmt).else_.statements.get(0) is IfStatement;
                res = $"if ({this.expr(((IfStatement)stmt).condition)}){this.block(((IfStatement)stmt).then)}";
                res += (elseIf ? $"\nelse {this.stmt(((IfStatement)stmt).else_.statements.get(0))}" : "") + (!elseIf && ((IfStatement)stmt).else_ != null ? $"\nelse" + this.block(((IfStatement)stmt).else_) : "");
            }
            else if (stmt is WhileStatement)
                res = $"while ({this.expr(((WhileStatement)stmt).condition)})" + this.block(((WhileStatement)stmt).body);
            else if (stmt is ForStatement)
                res = $"for ({(((ForStatement)stmt).itemVar != null ? this.var(((ForStatement)stmt).itemVar, null) : "")}; {this.expr(((ForStatement)stmt).condition)}; {this.expr(((ForStatement)stmt).incrementor)})" + this.block(((ForStatement)stmt).body);
            else if (stmt is DoStatement)
                res = $"do{this.block(((DoStatement)stmt).body)} while ({this.expr(((DoStatement)stmt).condition)});";
            else if (stmt is TryStatement) {
                res = "try" + this.block(((TryStatement)stmt).tryBody, false);
                if (((TryStatement)stmt).catchBody != null) {
                    this.usings.add("System");
                    res += $" catch (Exception {this.name_(((TryStatement)stmt).catchVar.name)}) {this.block(((TryStatement)stmt).catchBody, false)}";
                }
                if (((TryStatement)stmt).finallyBody != null)
                    res += "finally" + this.block(((TryStatement)stmt).finallyBody);
            }
            else if (stmt is ContinueStatement)
                res = $"continue;";
            else { }
            return this.leading(stmt) + res;
        }
        
        public string stmts(Statement[] stmts) {
            return stmts.map((Statement stmt) => { return this.stmt(stmt); }).join("\n");
        }
        
        public string rawBlock(Block block) {
            return this.stmts(block.statements.ToArray());
        }
        
        public string classLike(IInterface cls) {
            this.currentClass = cls;
            var resList = new List<string>();
            
            var staticConstructorStmts = new List<Statement>();
            var complexFieldInits = new List<Statement>();
            if (cls is Class) {
                resList.push(((Class)cls).fields.map((Field field) => { var isInitializerComplex = field.initializer != null && !(field.initializer is StringLiteral) && !(field.initializer is BooleanLiteral) && !(field.initializer is NumericLiteral);
                
                var prefix = $"{this.vis(field.visibility)} {this.preIf("static ", field.isStatic)}";
                if (field.interfaceDeclarations.length() > 0)
                    return $"{prefix}{this.varWoInit(field, field)} {{ get; set; }}";
                else if (isInitializerComplex) {
                    if (field.isStatic)
                        staticConstructorStmts.push(new ExpressionStatement(new BinaryExpression(new StaticFieldReference(field), "=", field.initializer)));
                    else
                        complexFieldInits.push(new ExpressionStatement(new BinaryExpression(new InstanceFieldReference(new ThisReference(((Class)cls)), field), "=", field.initializer)));
                    
                    return $"{prefix}{this.varWoInit(field, field)};";
                }
                else
                    return $"{prefix}{this.var(field, field)};"; }).join("\n"));
                
                resList.push(((Class)cls).properties.map((Property prop) => { return $"{this.vis(prop.visibility)} {this.preIf("static ", prop.isStatic)}" + this.varWoInit(prop, prop) + (prop.getter != null ? $" {{\n    get {{\n{this.pad(this.block(prop.getter))}\n    }}\n}}" : "") + (prop.setter != null ? $" {{\n    set {{\n{this.pad(this.block(prop.setter))}\n    }}\n}}" : ""); }).join("\n"));
                
                if (staticConstructorStmts.length() > 0)
                    resList.push($"static {this.name_(((Class)cls).name)}()\n{{\n{this.pad(this.stmts(staticConstructorStmts.ToArray()))}\n}}");
                
                if (((Class)cls).constructor_ != null) {
                    var constrFieldInits = ((Class)cls).fields.filter((Field x) => { return x.constructorParam != null; }).map((Field field) => { var fieldRef = new InstanceFieldReference(new ThisReference(((Class)cls)), field);
                    var mpRef = new MethodParameterReference(field.constructorParam);
                    // TODO: decide what to do with "after-TypeEngine" transformations
                    mpRef.setActualType(field.type, false, false);
                    return new ExpressionStatement(new BinaryExpression(fieldRef, "=", mpRef)); });
                    
                    resList.push("public " + this.preIf("/* throws */ ", ((Class)cls).constructor_.throws) + this.name_(((Class)cls).name) + $"({((Class)cls).constructor_.parameters.map((MethodParameter p) => { return this.var(p, null); }).join(", ")})" + (((Class)cls).constructor_.superCallArgs != null ? $": base({((Class)cls).constructor_.superCallArgs.map((Expression x) => { return this.expr(x); }).join(", ")})" : "") + $"\n{{\n{this.pad(this.stmts(constrFieldInits.concat(complexFieldInits.ToArray()).concat(((Class)cls).constructor_.body.statements.ToArray())))}\n}}");
                }
                else if (complexFieldInits.length() > 0)
                    resList.push($"public {this.name_(((Class)cls).name)}()\n{{\n{this.pad(this.stmts(complexFieldInits.ToArray()))}\n}}");
            }
            else if (cls is Interface)
                resList.push(((Interface)cls).fields.map((Field field) => { return $"{this.varWoInit(field, field)} {{ get; set; }}"; }).join("\n"));
            
            var methods = new List<string>();
            foreach (var method in cls.methods) {
                if (cls is Class && method.body == null)
                    continue;
                // declaration only
                methods.push((method.parentInterface is Interface ? "" : this.vis(method.visibility) + " ") + this.preIf("static ", method.isStatic) + this.preIf("virtual ", method.overrides == null && method.overriddenBy.length() > 0) + this.preIf("override ", method.overrides != null) + this.preIf("async ", method.async) + this.preIf("/* throws */ ", method.throws) + $"{this.type(method.returns, false)} " + this.name_(method.name) + this.typeArgs(method.typeArguments) + $"({method.parameters.map((MethodParameter p) => { return this.var(p, null); }).join(", ")})" + (method.body != null ? $" {{\n{this.pad(this.stmts(method.body.statements.ToArray()))}\n}}" : ";"));
            }
            resList.push(methods.join("\n\n"));
            return this.pad(resList.filter((string x) => { return x != ""; }).join("\n\n"));
        }
        
        public string pad(string str) {
            return str.split(new RegExp("\\n")).map((string x) => { return $"    {x}"; }).join("\n");
        }
        
        public string pathToNs(string path) {
            // Generator/ExprLang/ExprLangAst.ts -> Generator.ExprLang
            var parts = path.split(new RegExp("\\/"));
            parts.pop();
            return parts.join(".");
        }
        
        public string genFile(SourceFile sourceFile) {
            var enums = sourceFile.enums.map((Enum_ enum_) => { return $"public enum {this.name_(enum_.name)} {{ {enum_.values.map((EnumMember x) => { return this.name_(x.name); }).join(", ")} }}"; });
            
            var intfs = sourceFile.interfaces.map((Interface intf) => { return $"public interface {this.name_(intf.name)}{this.typeArgs(intf.typeArguments)}" + $"{this.preArr(" : ", intf.baseInterfaces.map((Type_ x) => { return this.type(x); }))} {{\n{this.classLike(intf)}\n}}"; });
            
            var classes = new List<string>();
            foreach (var cls in sourceFile.classes) {
                var baseClasses = new List<Type_>();
                if (cls.baseClass != null)
                    baseClasses.push(cls.baseClass);
                foreach (var intf in cls.baseInterfaces)
                    baseClasses.push(intf);
                classes.push($"public class {this.name_(cls.name)}{this.typeArgs(cls.typeArguments)}{this.preArr(" : ", baseClasses.map((Type_ x) => { return this.type(x); }))} {{\n{this.classLike(cls)}\n}}");
            }
            
            var main = sourceFile.mainBlock.statements.length() > 0 ? $"public class Program\n{{\n    static void Main(string[] args)\n    {{\n{this.pad(this.rawBlock(sourceFile.mainBlock))}\n    }}\n}}" : "";
            
            var usingsSet = new Set<string>(sourceFile.imports.map((Import x) => { return this.pathToNs(x.exportScope.scopeName); }).filter((string x) => { return x != ""; }));
            var usings = new List<string>();
            foreach (var using_ in this.usings)
                usings.push($"using {using_};");
            foreach (var using_ in usingsSet)
                usings.push($"using {using_};");
            
            var result = new List<string> { enums.join("\n"), intfs.join("\n\n"), classes.join("\n\n"), main }.filter((string x) => { return x != ""; }).join("\n\n");
            result = $"{usings.join("\n")}\n\nnamespace {this.pathToNs(sourceFile.sourcePath.path)}\n{{\n{this.pad(result)}\n}}";
            return result;
        }
        
        public GeneratedFile[] generate(Package pkg) {
            var result = new List<GeneratedFile>();
            foreach (var path in Object.keys(pkg.files))
                result.push(new GeneratedFile(path, this.genFile(pkg.files.get(path))));
            return result.ToArray();
        }
    }
}