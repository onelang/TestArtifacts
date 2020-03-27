using One.Ast;

namespace Generator
{
    public class GeneratedFile {
        public string path;
        public string content;
        
        public GeneratedFile(string path, string content) {
            this.path = path;
            this.content = content;
        }
    }
    
    public class CsharpGenerator {
        public static Set<string> usings;
        public static IInterface currentClass;
        public static string[] reservedWords;
        public static string[] fieldToMethodHack;
        
        static public string name_(string name) {
            if (CsharpGenerator.reservedWords.includes(name))
                name += "_";
            if (CsharpGenerator.fieldToMethodHack.includes(name))
                name += "()";
            name = name.replace(new RegExp("-"), "");
            return name;
        }
        
        static public string leading(Statement item) {
            var result = "";
            if (item.leadingTrivia != null && item.leadingTrivia.length() > 0)
                result += item.leadingTrivia;
            if (item.attributes != null)
                result += Object.keys(item.attributes).map((string x) => { return $"/// {{ATTR}} name=\"{x}\", value={JSON.stringify(item.attributes.get(x))}\\n"; }).join("");
            return result;
        }
        
        static public string preArr(string prefix, string[] value) {
            return value.length() > 0 ? $"{prefix}{value.join(", ")}" : "";
        }
        
        static public string preIf(string prefix, bool condition) {
            return condition ? prefix : "";
        }
        
        static public string pre(string prefix, string value) {
            return value != null ? $"{prefix}{value}" : "";
        }
        
        static public string typeArgs(string[] args) {
            return args != null && args.length() > 0 ? $"<{args.join(", ")}>" : "";
        }
        
        static public string type(Type_ t) {
            if (t is ClassType) {
                var typeArgs = CsharpGenerator.typeArgs(((ClassType)t).typeArguments.map((Type_ x) => { return CsharpGenerator.type(x); }));
                if (((ClassType)t).decl.name == "TsString")
                    return "string";
                else if (((ClassType)t).decl.name == "TsBoolean")
                    return "bool";
                else if (((ClassType)t).decl.name == "TsNumber")
                    return "int";
                else if (((ClassType)t).decl.name == "TsArray")
                    return $"{CsharpGenerator.type(((ClassType)t).typeArguments.get(0))}[]";
                else if (((ClassType)t).decl.name == "Promise") {
                    CsharpGenerator.usings.add("System.Threading.Tasks");
                    return ((ClassType)t).typeArguments.get(0) is VoidType ? "Task" : $"Task{typeArgs}";
                }
                else if (((ClassType)t).decl.name == "Object") {
                    CsharpGenerator.usings.add("System");
                    return $"object";
                }
                else if (((ClassType)t).decl.name == "TsMap") {
                    CsharpGenerator.usings.add("System.Collections.Generic");
                    return $"Dictionary<string, {CsharpGenerator.type(((ClassType)t).typeArguments.get(0))}>";
                }
                return CsharpGenerator.name_(((ClassType)t).decl.name) + typeArgs;
            }
            else if (t is InterfaceType)
                return $"{CsharpGenerator.name_(((InterfaceType)t).decl.name)}{CsharpGenerator.typeArgs(((InterfaceType)t).typeArguments.map((Type_ x) => { return CsharpGenerator.type(x); }))}";
            else if (t is VoidType)
                return "void";
            else if (t is EnumType)
                return $"{CsharpGenerator.name_(((EnumType)t).decl.name)}";
            else if (t is AnyType)
                return $"object";
            else if (t is NullType)
                return $"null";
            else if (t is GenericsType)
                return $"{((GenericsType)t).typeVarName}";
            else if (t is LambdaType) {
                var isFunc = !(((LambdaType)t).returnType is VoidType);
                return $"{(isFunc ? "Func" : "Action")}<{((LambdaType)t).parameters.map((MethodParameter x) => { return CsharpGenerator.type(x.type); }).join(", ")}{(isFunc ? CsharpGenerator.type(((LambdaType)t).returnType) : "")}>";
            }
            else if (t == null)
                return "/* TODO */ object";
            else
                return "/* MISSING */";
        }
        
        static public string vis(Visibility v) {
            return v == Visibility.Private ? "private" : v == Visibility.Protected ? "protected" : v == Visibility.Public ? "public" : "/* TODO: not set */public";
        }
        
        static public string varWoInit(IVariable v) {
            return $"{CsharpGenerator.type(v.type)} {CsharpGenerator.name_(v.name)}";
        }
        
        static public string var(IVariableWithInitializer v) {
            return $"{CsharpGenerator.varWoInit(v)}{(v.initializer != null ? $" = {CsharpGenerator.expr(v.initializer)}" : "")}";
        }
        
        static public string expr(IExpression expr) {
            var res = "UNKNOWN-EXPR";
            if (expr is NewExpression)
                res = $"new {CsharpGenerator.type(((NewExpression)expr).cls)}({((NewExpression)expr).args.map((Expression x) => { return CsharpGenerator.expr(x); }).join(", ")})";
            else if (expr is UnresolvedNewExpression)
                res = $"/* TODO: UnresolvedNewExpression */ new {CsharpGenerator.type(((UnresolvedNewExpression)expr).cls)}({((UnresolvedNewExpression)expr).args.map((Expression x) => { return CsharpGenerator.expr(x); }).join(", ")})";
            else if (expr is Identifier)
                res = $"/* TODO: Identifier */ {((Identifier)expr).text}";
            else if (expr is PropertyAccessExpression)
                res = $"/* TODO: PropertyAccessExpression */ {CsharpGenerator.expr(((PropertyAccessExpression)expr).object_)}.{((PropertyAccessExpression)expr).propertyName}";
            else if (expr is UnresolvedCallExpression) {
                var typeArgs = ((UnresolvedCallExpression)expr).typeArgs.length() > 0 ? $"<{((UnresolvedCallExpression)expr).typeArgs.map((Type_ x) => { return CsharpGenerator.type(x); }).join(", ")}>" : "";
                res = $"/* TODO: UnresolvedCallExpression */ {CsharpGenerator.expr(((UnresolvedCallExpression)expr).func)}{typeArgs}({((UnresolvedCallExpression)expr).args.map((Expression x) => { return CsharpGenerator.expr(x); }).join(", ")})";
            }
            else if (expr is UnresolvedMethodCallExpression) {
                var typeArgs = ((UnresolvedMethodCallExpression)expr).typeArgs.length() > 0 ? $"<{((UnresolvedMethodCallExpression)expr).typeArgs.map((Type_ x) => { return CsharpGenerator.type(x); }).join(", ")}>" : "";
                res = $"/* TODO: UnresolvedMethodCallExpression */ {CsharpGenerator.expr(((UnresolvedMethodCallExpression)expr).object_)}.{((UnresolvedMethodCallExpression)expr).methodName}{typeArgs}({((UnresolvedMethodCallExpression)expr).args.map((Expression x) => { return CsharpGenerator.expr(x); }).join(", ")})";
            }
            else if (expr is InstanceMethodCallExpression) {
                var typeArgs = ((InstanceMethodCallExpression)expr).typeArgs.length() > 0 ? $"<{((InstanceMethodCallExpression)expr).typeArgs.map((Type_ x) => { return CsharpGenerator.type(x); }).join(", ")}>" : "";
                //if (expr.object instanceof ThisReference) debugger;
                res = $"{CsharpGenerator.expr(((InstanceMethodCallExpression)expr).object_)}.{CsharpGenerator.name_(((InstanceMethodCallExpression)expr).method.name)}{typeArgs}({((InstanceMethodCallExpression)expr).args.map((Expression x) => { return CsharpGenerator.expr(x); }).join(", ")})";
            }
            else if (expr is StaticMethodCallExpression) {
                var typeArgs = ((StaticMethodCallExpression)expr).typeArgs.length() > 0 ? $"<{((StaticMethodCallExpression)expr).typeArgs.map((Type_ x) => { return CsharpGenerator.type(x); }).join(", ")}>" : "";
                var intfPrefix = $"{CsharpGenerator.name_(((StaticMethodCallExpression)expr).method.parentInterface.name)}.";
                res = $"{intfPrefix}{CsharpGenerator.name_(((StaticMethodCallExpression)expr).method.name)}{typeArgs}({((StaticMethodCallExpression)expr).args.map((Expression x) => { return CsharpGenerator.expr(x); }).join(", ")})";
            }
            else if (expr is GlobalFunctionCallExpression)
                res = $"Global.{CsharpGenerator.name_(((GlobalFunctionCallExpression)expr).func.name)}({((GlobalFunctionCallExpression)expr).args.map((Expression x) => { return CsharpGenerator.expr(x); }).join(", ")})";
            else if (expr is LambdaCallExpression)
                res = $"{CsharpGenerator.expr(((LambdaCallExpression)expr).method)}({((LambdaCallExpression)expr).args.map((Expression x) => { return CsharpGenerator.expr(x); }).join(", ")})";
            else if (expr is BooleanLiteral)
                res = $"{((BooleanLiteral)expr).boolValue}";
            else if (expr is StringLiteral)
                res = $"{JSON.stringify(((StringLiteral)expr).stringValue)}";
            else if (expr is NumericLiteral)
                res = $"{((NumericLiteral)expr).valueAsText}";
            else if (expr is CharacterLiteral)
                res = $"'{((CharacterLiteral)expr).charValue}'";
            else if (expr is ElementAccessExpression)
                res = $"{CsharpGenerator.expr(((ElementAccessExpression)expr).object_)}[{CsharpGenerator.expr(((ElementAccessExpression)expr).elementExpr)}]";
            else if (expr is TemplateString) {
                var parts = new string[0];
                foreach (var part in ((TemplateString)expr).parts)
                    if (part.isLiteral)
                        parts.push(part.literalText.replace(new RegExp("\\"), $"\\\\$").replace(new RegExp("\""), $"\\\"").replace(new RegExp("{"), "{{").replace(new RegExp("}"), "}}"));
                    else {
                        var repr = CsharpGenerator.expr(part.expression);
                        parts.push(part.expression is ConditionalExpression ? $"{{({repr})}}" : $"{{{repr}}}");
                    }
                res = $"$\"{parts.join("")}\"";
            }
            else if (expr is BinaryExpression)
                res = $"{CsharpGenerator.expr(((BinaryExpression)expr).left)} {((BinaryExpression)expr).operator_} {CsharpGenerator.expr(((BinaryExpression)expr).right)}";
            else if (expr is ArrayLiteral)
                if (((ArrayLiteral)expr).items.length() == 0) {
                    var arrayType = (((ClassType)((ArrayLiteral)expr).actualType)).typeArguments.get(0);
                    if (arrayType is ClassType && ((ClassType)arrayType).decl.name == "TsArray")
                        res = $"new {CsharpGenerator.type(((ArrayLiteral)expr).actualType)} {{ }}";
                    else
                        res = $"new {CsharpGenerator.type(arrayType)}[0]";
                }
                else
                    res = $"new[] {{ {((ArrayLiteral)expr).items.map((Expression x) => { return CsharpGenerator.expr(x); }).join(", ")} }}";
            else if (expr is CastExpression)
                res = $"(({CsharpGenerator.type(((CastExpression)expr).newType)}){CsharpGenerator.expr(((CastExpression)expr).expression)})";
            else if (expr is ConditionalExpression)
                res = $"{CsharpGenerator.expr(((ConditionalExpression)expr).condition)} ? {CsharpGenerator.expr(((ConditionalExpression)expr).whenTrue)} : {CsharpGenerator.expr(((ConditionalExpression)expr).whenFalse)}";
            else if (expr is InstanceOfExpression)
                res = $"{CsharpGenerator.expr(((InstanceOfExpression)expr).expr)} is {CsharpGenerator.type(((InstanceOfExpression)expr).checkType)}";
            else if (expr is ParenthesizedExpression)
                res = $"({CsharpGenerator.expr(((ParenthesizedExpression)expr).expression)})";
            else if (expr is RegexLiteral)
                res = $"new RegExp({JSON.stringify(((RegexLiteral)expr).pattern)})";
            else if (expr is Lambda)
                res = $"({((Lambda)expr).parameters.map((MethodParameter x) => { return $"{CsharpGenerator.type(x.type)} {CsharpGenerator.name_(x.name)}"; }).join(", ")}) => {{ {CsharpGenerator.rawBlock(((Lambda)expr).body)} }}";
            else if (expr is UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Prefix)
                res = $"{((UnaryExpression)expr).operator_}{CsharpGenerator.expr(((UnaryExpression)expr).operand)}";
            else if (expr is UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Postfix)
                res = $"{CsharpGenerator.expr(((UnaryExpression)expr).operand)}{((UnaryExpression)expr).operator_}";
            else if (expr is MapLiteral) {
                var repr = ((MapLiteral)expr).items.map((MapLiteralItem item) => { return $"[{JSON.stringify(item.key)}] = {CsharpGenerator.expr(item.value)}"; }).join(",\n");
                res = $"new {CsharpGenerator.type(((MapLiteral)expr).actualType)} " + (repr == "" ? "{}" : repr.includes("\n") ? $"{{\\n{CsharpGenerator.pad(repr)}\\n}}" : $"{{ {repr} }}");
            }
            else if (expr is NullLiteral)
                res = $"null";
            else if (expr is AwaitExpression)
                res = $"await {CsharpGenerator.expr(((AwaitExpression)expr).expr)}";
            else if (expr is ThisReference)
                res = $"this";
            else if (expr is StaticThisReference)
                res = $"{CsharpGenerator.currentClass.name}";
            else if (expr is EnumReference)
                res = $"{CsharpGenerator.name_(((EnumReference)expr).decl.name)}";
            else if (expr is ClassReference)
                res = $"{CsharpGenerator.name_(((ClassReference)expr).decl.name)}";
            else if (expr is MethodParameterReference)
                res = $"{CsharpGenerator.name_(((MethodParameterReference)expr).decl.name)}";
            else if (expr is VariableDeclarationReference)
                res = $"{CsharpGenerator.name_(((VariableDeclarationReference)expr).decl.name)}";
            else if (expr is ForVariableReference)
                res = $"{CsharpGenerator.name_(((ForVariableReference)expr).decl.name)}";
            else if (expr is ForeachVariableReference)
                res = $"{CsharpGenerator.name_(((ForeachVariableReference)expr).decl.name)}";
            else if (expr is CatchVariableReference)
                res = $"{CsharpGenerator.name_(((CatchVariableReference)expr).decl.name)}";
            else if (expr is GlobalFunctionReference)
                res = $"{CsharpGenerator.name_(((GlobalFunctionReference)expr).decl.name)}";
            else if (expr is SuperReference)
                res = $"base";
            else if (expr is StaticFieldReference)
                res = $"{CsharpGenerator.name_(((StaticFieldReference)expr).decl.parentInterface.name)}.{CsharpGenerator.name_(((StaticFieldReference)expr).decl.name)}";
            else if (expr is StaticPropertyReference)
                res = $"{CsharpGenerator.name_(((StaticPropertyReference)expr).decl.parentClass.name)}.{CsharpGenerator.name_(((StaticPropertyReference)expr).decl.name)}";
            else if (expr is InstanceFieldReference)
                res = $"{CsharpGenerator.expr(((InstanceFieldReference)expr).object_)}.{CsharpGenerator.name_(((InstanceFieldReference)expr).field.name)}";
            else if (expr is InstancePropertyReference)
                res = $"{CsharpGenerator.expr(((InstancePropertyReference)expr).object_)}.{CsharpGenerator.name_(((InstancePropertyReference)expr).property.name)}";
            else if (expr is EnumMemberReference)
                res = $"{CsharpGenerator.name_(((EnumMemberReference)expr).decl.parentEnum.name)}.{CsharpGenerator.name_(((EnumMemberReference)expr).decl.name)}";
            else if (expr is NullCoalesceExpression)
                res = $"{CsharpGenerator.expr(((NullCoalesceExpression)expr).defaultExpr)} ?? {CsharpGenerator.expr(((NullCoalesceExpression)expr).exprIfNull)}";
            else { }
            return res;
        }
        
        static public string block(Block block, bool allowOneLiner = true) {
            var stmtLen = block.statements.length();
            return stmtLen == 0 ? " { }" : allowOneLiner && stmtLen == 1 ? $"\\n{CsharpGenerator.pad(CsharpGenerator.rawBlock(block))}" : $" {{\\n{CsharpGenerator.pad(CsharpGenerator.rawBlock(block))}\\n}}";
        }
        
        static public string stmt(Statement stmt) {
            var res = "UNKNOWN-STATEMENT";
            if (stmt is BreakStatement)
                res = "break;";
            else if (stmt is ReturnStatement)
                res = ((ReturnStatement)stmt).expression == null ? "return;" : $"return {CsharpGenerator.expr(((ReturnStatement)stmt).expression)};";
            else if (stmt is UnsetStatement)
                res = $"/* unset {CsharpGenerator.expr(((UnsetStatement)stmt).expression)}; */";
            else if (stmt is ThrowStatement)
                res = $"throw {CsharpGenerator.expr(((ThrowStatement)stmt).expression)};";
            else if (stmt is ExpressionStatement)
                res = $"{CsharpGenerator.expr(((ExpressionStatement)stmt).expression)};";
            else if (stmt is VariableDeclaration)
                if (((VariableDeclaration)stmt).initializer is NullLiteral)
                    res = $"{CsharpGenerator.type(((VariableDeclaration)stmt).type)} {CsharpGenerator.name_(((VariableDeclaration)stmt).name)} = null;";
                else if (((VariableDeclaration)stmt).initializer != null)
                    res = $"var {CsharpGenerator.name_(((VariableDeclaration)stmt).name)} = {CsharpGenerator.expr(((VariableDeclaration)stmt).initializer)};";
                else
                    res = $"{CsharpGenerator.type(((VariableDeclaration)stmt).type)} {CsharpGenerator.name_(((VariableDeclaration)stmt).name)};";
            else if (stmt is ForeachStatement)
                res = $"foreach (var {CsharpGenerator.name_(((ForeachStatement)stmt).itemVar.name)} in {CsharpGenerator.expr(((ForeachStatement)stmt).items)})" + CsharpGenerator.block(((ForeachStatement)stmt).body);
            else if (stmt is IfStatement) {
                var elseIf = ((IfStatement)stmt).else_ != null && ((IfStatement)stmt).else_.statements.length() == 1 && ((IfStatement)stmt).else_.statements.get(0) is IfStatement;
                res = $"if ({CsharpGenerator.expr(((IfStatement)stmt).condition)}){CsharpGenerator.block(((IfStatement)stmt).then)}";
                res += (elseIf ? $"\\nelse {CsharpGenerator.stmt(((IfStatement)stmt).else_.statements.get(0))}" : "") + (!elseIf && ((IfStatement)stmt).else_ != null ? $"\\nelse" + CsharpGenerator.block(((IfStatement)stmt).else_) : "");
            }
            else if (stmt is WhileStatement)
                res = $"while ({CsharpGenerator.expr(((WhileStatement)stmt).condition)})" + CsharpGenerator.block(((WhileStatement)stmt).body);
            else if (stmt is ForStatement)
                res = $"for ({(((ForStatement)stmt).itemVar != null ? CsharpGenerator.var(((ForStatement)stmt).itemVar) : "")}; {CsharpGenerator.expr(((ForStatement)stmt).condition)}; {CsharpGenerator.expr(((ForStatement)stmt).incrementor)})" + CsharpGenerator.block(((ForStatement)stmt).body);
            else if (stmt is DoStatement)
                res = $"do{CsharpGenerator.block(((DoStatement)stmt).body)} while ({CsharpGenerator.expr(((DoStatement)stmt).condition)});";
            else if (stmt is TryStatement) {
                res = "try" + CsharpGenerator.block(((TryStatement)stmt).tryBody, false);
                if (((TryStatement)stmt).catchBody != null) {
                    CsharpGenerator.usings.add("System");
                    res += $" catch (Exception {CsharpGenerator.name_(((TryStatement)stmt).catchVar.name)}) {CsharpGenerator.block(((TryStatement)stmt).catchBody, false)}";
                }
                if (((TryStatement)stmt).finallyBody != null)
                    res += "finally" + CsharpGenerator.block(((TryStatement)stmt).finallyBody);
            }
            else if (stmt is ContinueStatement)
                res = $"continue;";
            else { }
            return CsharpGenerator.leading(stmt) + res;
        }
        
        static public string stmts(Statement[] stmts) {
            return stmts.map((Statement stmt) => { return CsharpGenerator.stmt(stmt); }).join("\n");
        }
        
        static public string rawBlock(Block block) {
            return CsharpGenerator.stmts(block.statements);
        }
        
        static public string methodBase(IMethodBase method, Type_ returns, Visibility visibility, Statement[] bodyPrefix = null) {
            if (method == null)
                return "";
            var name = method is Method ? ((Method)method).name : method is Constructor ? ((Constructor)method).parentClass.name : "???";
            var typeArgs = method is Method ? ((Method)method).typeArguments : null;
            var overrides = method is Method ? ((Method)method).overrides != null : false;
            var virtual_ = method is Method ? !overrides && ((Method)method).overriddenBy.length() > 0 : false;
            var intfMethod = method is Method ? ((Method)method).parentInterface is Interface : false;
            var async = method is Method ? ((Method)method).async : false;
            return CsharpGenerator.preIf("/* throws */ ", method.throws) + (intfMethod ? "" : CsharpGenerator.vis(visibility) + " ") + CsharpGenerator.preIf("virtual ", virtual_) + CsharpGenerator.preIf("override ", overrides) + CsharpGenerator.preIf("async ", async) + (method is Constructor ? "" : $"{CsharpGenerator.type(returns)} ") + CsharpGenerator.name_(name) + CsharpGenerator.typeArgs(typeArgs) + $"({method.parameters.map((MethodParameter p) => { return CsharpGenerator.var(p); }).join(", ")})" + (method is Constructor && ((Constructor)method).superCallArgs != null ? $": base({((Constructor)method).superCallArgs.map((Expression x) => { return CsharpGenerator.expr(x); }).join(", ")})" : "") + (method.body != null ? $" {{\\n{CsharpGenerator.pad(CsharpGenerator.stmts((bodyPrefix ?? new Statement[0]).concat(method.body.statements)))}\\n}}" : ";");
        }
        
        static public string classLike(IInterface cls) {
            CsharpGenerator.currentClass = cls;
            var resList = new string[0];
            
            var bodyPrefix = new Statement[0];
            if (cls is Class) {
                resList.push(((Class)cls).fields.map((Field field) => { var isInitializerComplex = field.initializer != null && !(field.initializer is StringLiteral) && !(field.initializer is BooleanLiteral) && !(field.initializer is NumericLiteral);
                
                var prefix = $"{CsharpGenerator.vis(field.visibility)} {CsharpGenerator.preIf("static ", field.isStatic)}";
                if (field.interfaceDeclarations.length() > 0)
                    return $"{prefix}{CsharpGenerator.varWoInit(field)} {{ get; set; }}";
                else if (isInitializerComplex) {
                    var fieldRef = field.isStatic ? ((Reference)new StaticFieldReference(field)) : new InstanceFieldReference(new ThisReference(((Class)cls)), field);
                    bodyPrefix.push(new ExpressionStatement(new BinaryExpression(fieldRef, "=", field.initializer)));
                    return $"{prefix}{CsharpGenerator.varWoInit(field)};";
                }
                else
                    return $"{prefix}{CsharpGenerator.var(field)};"; }).join("\n"));
                
                resList.push(((Class)cls).properties.map((Property prop) => { return $"{CsharpGenerator.vis(prop.visibility)} {CsharpGenerator.preIf("static ", prop.isStatic)}" + CsharpGenerator.varWoInit(prop) + (prop.getter != null ? $" {{\\n    get {{\\n{CsharpGenerator.pad(CsharpGenerator.block(prop.getter))}\\n    }}\\n}}" : "") + (prop.setter != null ? $" {{\\n    set {{\\n{CsharpGenerator.pad(CsharpGenerator.block(prop.setter))}\\n    }}\\n}}" : ""); }).join("\n"));
                
                resList.push(CsharpGenerator.methodBase(((Class)cls).constructor_, VoidType.instance, Visibility.Public, bodyPrefix));
            }
            else if (cls is Interface)
                resList.push(((Interface)cls).fields.map((Field field) => { return $"{CsharpGenerator.type(field.type)} {CsharpGenerator.name_(field.name)} {{ get; set; }}"; }).join("\n"));
            
            var methods = new string[0];
            foreach (var method in cls.methods) {
                if (cls is Class && method.body == null)
                    continue;
                // declaration only
                methods.push(CsharpGenerator.preIf("static ", method.isStatic) + CsharpGenerator.preIf("/* mutates */ ", method.mutates) + CsharpGenerator.methodBase(method, method.returns, method.visibility));
            }
            resList.push(methods.join("\n\n"));
            return CsharpGenerator.pad(resList.filter((string x) => { return x != ""; }).join("\n\n"));
        }
        
        static public string pad(string str) {
            return str.split(new RegExp("n")).map((string x) => { return $"    {x}"; }).join("\n");
        }
        
        static public string pathToNs(string path) {
            // Generator/ExprLang/ExprLangAst.ts -> Generator.ExprLang
            var parts = path.split(new RegExp("/"));
            parts.pop();
            return parts.join(".");
        }
        
        static public string genFile(SourceFile sourceFile) {
            var enums = sourceFile.enums.map((Enum_ enum_) => { return $"public enum {CsharpGenerator.name_(enum_.name)} {{ {enum_.values.map((EnumMember x) => { return CsharpGenerator.name_(x.name); }).join(", ")} }}"; });
            
            var intfs = sourceFile.interfaces.map((Interface intf) => { return $"public interface {CsharpGenerator.name_(intf.name)}{CsharpGenerator.typeArgs(intf.typeArguments)}" + $"{CsharpGenerator.preArr(" : ", intf.baseInterfaces.map((Type_ x) => { return CsharpGenerator.type(x); }))} {{\\n{CsharpGenerator.classLike(intf)}\\n}}"; });
            
            var classes = new string[0];
            foreach (var cls in sourceFile.classes) {
                var baseClasses = new Type_[0];
                if (cls.baseClass != null)
                    baseClasses.push(cls.baseClass);
                foreach (var intf in cls.baseInterfaces)
                    baseClasses.push(intf);
                classes.push($"public class {CsharpGenerator.name_(cls.name)}{CsharpGenerator.typeArgs(cls.typeArguments)}{CsharpGenerator.preArr(" : ", baseClasses.map((Type_ x) => { return CsharpGenerator.type(x); }))} {{\\n{CsharpGenerator.classLike(cls)}\\n}}");
            }
            
            var main = sourceFile.mainBlock.statements.length() > 0 ? $"public class Program\\n{{\\n    static void Main(string[] args)\\n    {{\\n{CsharpGenerator.pad(CsharpGenerator.rawBlock(sourceFile.mainBlock))}\\n    }}\\n}}" : "";
            
            var usingsSet = new Set<string>(sourceFile.imports.map((Import x) => { return CsharpGenerator.pathToNs(x.exportScope.scopeName); }).filter((string x) => { return x != ""; }));
            var usings = new string[0];
            foreach (var using_ in CsharpGenerator.usings)
                usings.push($"using {using_};");
            foreach (var using_ in usingsSet)
                usings.push($"using {using_};");
            
            var result = new[] { enums.join("\n"), intfs.join("\n\n"), classes.join("\n\n"), main }.filter((string x) => { return x != ""; }).join("\n\n");
            result = $"{usings.join("\n")}\\n\\nnamespace {CsharpGenerator.pathToNs(sourceFile.sourcePath.path)}\\n{{\\n{CsharpGenerator.pad(result)}\\n}}";
            return result;
        }
        
        static public GeneratedFile[] generate(Package pkg) {
            var result = new GeneratedFile[0];
            foreach (var path in Object.keys(pkg.files))
                result.push(new GeneratedFile(path, CsharpGenerator.genFile(pkg.files.get(path))));
            return result;
        }
    }
}