using System.Collections.Generic;
using One.Ast;

namespace Utils
{
    public class TSOverviewGenerator {
        public static string leading(Statement item) {
            var result = "";
            if (item.leadingTrivia != null && item.leadingTrivia.length() > 0)
                result += item.leadingTrivia;
            if (item.attributes != null)
                result += Object.keys(item.attributes).map((string x) => { return $"/// {{ATTR}} name=\"{x}\", value={JSON.stringify(item.attributes.get(x))}\n"; }).join("");
            return result;
        }
        
        public static string preArr(string prefix, string[] value) {
            return value.length() > 0 ? $"{prefix}{value.join(", ")}" : "";
        }
        
        public static string preIf(string prefix, bool condition) {
            return condition ? prefix : "";
        }
        
        public static string pre(string prefix, string value) {
            return value != null ? $"{prefix}{value}" : "";
        }
        
        public static string typeArgs(string[] args) {
            return args != null && args.length() > 0 ? $"<{args.join(", ")}>" : "";
        }
        
        public static string type(Type_ t, bool raw = false) {
            var repr = t == null ? "???" : t.repr();
            if (repr == "U:UNKNOWN") { }
            return (raw ? "" : "{T}") + repr;
        }
        
        public static string var(IVariable v) {
            var result = "";
            var isProp = v is Property;
            if (v is Field || v is Property) {
                var m = ((IClassMember)v);
                result += TSOverviewGenerator.preIf("static ", m.isStatic);
                result += m.visibility == Visibility.Private ? "private " : m.visibility == Visibility.Protected ? "protected " : m.visibility == Visibility.Public ? "public " : "VISIBILITY-NOT-SET";
            }
            result += $"{(isProp ? "@prop " : "")}";
            if (v.mutability != null) {
                result += $"{(v.mutability.unused ? "@unused " : "")}";
                result += $"{(v.mutability.mutated ? "@mutated " : "")}";
                result += $"{(v.mutability.reassigned ? "@reass " : "")}";
            }
            result += $"{v.name}{(isProp ? "()" : "")}: {TSOverviewGenerator.type(v.type)}";
            if (v is VariableDeclaration || v is ForVariable || v is Field || v is MethodParameter) {
                var init = (((IVariableWithInitializer)v)).initializer;
                if (init != null)
                    result += TSOverviewGenerator.pre(" = ", TSOverviewGenerator.expr(init));
            }
            return result;
        }
        
        public static string expr(IExpression expr, bool previewOnly = false) {
            var res = "UNKNOWN-EXPR";
            if (expr is NewExpression)
                res = $"new {TSOverviewGenerator.type(((NewExpression)expr).cls)}({(previewOnly ? "..." : ((NewExpression)expr).args.map((Expression x) => { return TSOverviewGenerator.expr(x); }).join(", "))})";
            else if (expr is UnresolvedNewExpression)
                res = $"new {TSOverviewGenerator.type(((UnresolvedNewExpression)expr).cls)}({(previewOnly ? "..." : ((UnresolvedNewExpression)expr).args.map((Expression x) => { return TSOverviewGenerator.expr(x); }).join(", "))})";
            else if (expr is Identifier)
                res = $"{{ID}}{((Identifier)expr).text}";
            else if (expr is PropertyAccessExpression)
                res = $"{TSOverviewGenerator.expr(((PropertyAccessExpression)expr).object_)}.{{PA}}{((PropertyAccessExpression)expr).propertyName}";
            else if (expr is UnresolvedCallExpression) {
                var typeArgs = ((UnresolvedCallExpression)expr).typeArgs.length() > 0 ? $"<{((UnresolvedCallExpression)expr).typeArgs.map((Type_ x) => { return TSOverviewGenerator.type(x); }).join(", ")}>" : "";
                res = $"{TSOverviewGenerator.expr(((UnresolvedCallExpression)expr).func)}{typeArgs}({(previewOnly ? "..." : ((UnresolvedCallExpression)expr).args.map((Expression x) => { return TSOverviewGenerator.expr(x); }).join(", "))})";
            }
            else if (expr is UnresolvedMethodCallExpression) {
                var typeArgs = ((UnresolvedMethodCallExpression)expr).typeArgs.length() > 0 ? $"<{((UnresolvedMethodCallExpression)expr).typeArgs.map((Type_ x) => { return TSOverviewGenerator.type(x); }).join(", ")}>" : "";
                res = $"{TSOverviewGenerator.expr(((UnresolvedMethodCallExpression)expr).object_)}.{{UM}}{((UnresolvedMethodCallExpression)expr).methodName}{typeArgs}({(previewOnly ? "..." : ((UnresolvedMethodCallExpression)expr).args.map((Expression x) => { return TSOverviewGenerator.expr(x); }).join(", "))})";
            }
            else if (expr is InstanceMethodCallExpression) {
                var typeArgs = ((InstanceMethodCallExpression)expr).typeArgs.length() > 0 ? $"<{((InstanceMethodCallExpression)expr).typeArgs.map((Type_ x) => { return TSOverviewGenerator.type(x); }).join(", ")}>" : "";
                res = $"{TSOverviewGenerator.expr(((InstanceMethodCallExpression)expr).object_)}.{{M}}{((InstanceMethodCallExpression)expr).method.name}{typeArgs}({(previewOnly ? "..." : ((InstanceMethodCallExpression)expr).args.map((Expression x) => { return TSOverviewGenerator.expr(x); }).join(", "))})";
            }
            else if (expr is StaticMethodCallExpression) {
                var typeArgs = ((StaticMethodCallExpression)expr).typeArgs.length() > 0 ? $"<{((StaticMethodCallExpression)expr).typeArgs.map((Type_ x) => { return TSOverviewGenerator.type(x); }).join(", ")}>" : "";
                res = $"{((StaticMethodCallExpression)expr).method.parentInterface.name}.{{M}}{((StaticMethodCallExpression)expr).method.name}{typeArgs}({(previewOnly ? "..." : ((StaticMethodCallExpression)expr).args.map((Expression x) => { return TSOverviewGenerator.expr(x); }).join(", "))})";
            }
            else if (expr is GlobalFunctionCallExpression)
                res = $"{((GlobalFunctionCallExpression)expr).func.name}({(previewOnly ? "..." : ((GlobalFunctionCallExpression)expr).args.map((Expression x) => { return TSOverviewGenerator.expr(x); }).join(", "))})";
            else if (expr is LambdaCallExpression)
                res = $"{TSOverviewGenerator.expr(((LambdaCallExpression)expr).method)}({(previewOnly ? "..." : ((LambdaCallExpression)expr).args.map((Expression x) => { return TSOverviewGenerator.expr(x); }).join(", "))})";
            else if (expr is BooleanLiteral)
                res = $"{((BooleanLiteral)expr).boolValue}";
            else if (expr is StringLiteral)
                res = $"{JSON.stringify(((StringLiteral)expr).stringValue)}";
            else if (expr is NumericLiteral)
                res = $"{((NumericLiteral)expr).valueAsText}";
            else if (expr is CharacterLiteral)
                res = $"'{((CharacterLiteral)expr).charValue}'";
            else if (expr is ElementAccessExpression)
                res = $"({TSOverviewGenerator.expr(((ElementAccessExpression)expr).object_)})[{TSOverviewGenerator.expr(((ElementAccessExpression)expr).elementExpr)}]";
            else if (expr is TemplateString)
                res = "`" + ((TemplateString)expr).parts.map((TemplateStringPart x) => { return x.isLiteral ? x.literalText : "${" + TSOverviewGenerator.expr(x.expression) + "}"; }).join("") + "`";
            else if (expr is BinaryExpression)
                res = $"{TSOverviewGenerator.expr(((BinaryExpression)expr).left)} {((BinaryExpression)expr).operator_} {TSOverviewGenerator.expr(((BinaryExpression)expr).right)}";
            else if (expr is ArrayLiteral)
                res = $"[{((ArrayLiteral)expr).items.map((Expression x) => { return TSOverviewGenerator.expr(x); }).join(", ")}]";
            else if (expr is CastExpression)
                res = $"<{TSOverviewGenerator.type(((CastExpression)expr).newType)}>({TSOverviewGenerator.expr(((CastExpression)expr).expression)})";
            else if (expr is ConditionalExpression)
                res = $"{TSOverviewGenerator.expr(((ConditionalExpression)expr).condition)} ? {TSOverviewGenerator.expr(((ConditionalExpression)expr).whenTrue)} : {TSOverviewGenerator.expr(((ConditionalExpression)expr).whenFalse)}";
            else if (expr is InstanceOfExpression)
                res = $"{TSOverviewGenerator.expr(((InstanceOfExpression)expr).expr)} instanceof {TSOverviewGenerator.type(((InstanceOfExpression)expr).checkType)}";
            else if (expr is ParenthesizedExpression)
                res = $"({TSOverviewGenerator.expr(((ParenthesizedExpression)expr).expression)})";
            else if (expr is RegexLiteral)
                res = $"/{((RegexLiteral)expr).pattern}/{(((RegexLiteral)expr).global ? "g" : "")}{(((RegexLiteral)expr).caseInsensitive ? "g" : "")}";
            else if (expr is Lambda)
                res = $"({((Lambda)expr).parameters.map((MethodParameter x) => { return x.name + (x.type != null ? $": {TSOverviewGenerator.type(x.type)}" : ""); }).join(", ")}) => {{ {TSOverviewGenerator.rawBlock(((Lambda)expr).body)} }}";
            else if (expr is UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Prefix)
                res = $"{((UnaryExpression)expr).operator_}{TSOverviewGenerator.expr(((UnaryExpression)expr).operand)}";
            else if (expr is UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Postfix)
                res = $"{TSOverviewGenerator.expr(((UnaryExpression)expr).operand)}{((UnaryExpression)expr).operator_}";
            else if (expr is MapLiteral) {
                var repr = ((MapLiteral)expr).items.map((MapLiteralItem item) => { return $"{item.key}: {TSOverviewGenerator.expr(item.value)}"; }).join(",\n");
                res = "{L:M}" + (repr == "" ? "{}" : repr.includes("\n") ? $"{{\n{TSOverviewGenerator.pad(repr)}\n}}" : $"{{ {repr} }}");
            }
            else if (expr is NullLiteral)
                res = $"null";
            else if (expr is AwaitExpression)
                res = $"await {TSOverviewGenerator.expr(((AwaitExpression)expr).expr)}";
            else if (expr is ThisReference)
                res = $"{{R}}this";
            else if (expr is StaticThisReference)
                res = $"{{R:Static}}this";
            else if (expr is EnumReference)
                res = $"{{R:Enum}}{((EnumReference)expr).decl.name}";
            else if (expr is ClassReference)
                res = $"{{R:Cls}}{((ClassReference)expr).decl.name}";
            else if (expr is MethodParameterReference)
                res = $"{{R:MetP}}{((MethodParameterReference)expr).decl.name}";
            else if (expr is VariableDeclarationReference)
                res = $"{{V}}{((VariableDeclarationReference)expr).decl.name}";
            else if (expr is ForVariableReference)
                res = $"{{R:ForV}}{((ForVariableReference)expr).decl.name}";
            else if (expr is ForeachVariableReference)
                res = $"{{R:ForEV}}{((ForeachVariableReference)expr).decl.name}";
            else if (expr is CatchVariableReference)
                res = $"{{R:CatchV}}{((CatchVariableReference)expr).decl.name}";
            else if (expr is GlobalFunctionReference)
                res = $"{{R:GFunc}}{((GlobalFunctionReference)expr).decl.name}";
            else if (expr is SuperReference)
                res = $"{{R}}super";
            else if (expr is StaticFieldReference)
                res = $"{{R:StFi}}{((StaticFieldReference)expr).decl.parentInterface.name}::{((StaticFieldReference)expr).decl.name}";
            else if (expr is StaticPropertyReference)
                res = $"{{R:StPr}}{((StaticPropertyReference)expr).decl.parentClass.name}::{((StaticPropertyReference)expr).decl.name}";
            else if (expr is InstanceFieldReference)
                res = $"{TSOverviewGenerator.expr(((InstanceFieldReference)expr).object_)}.{{F}}{((InstanceFieldReference)expr).field.name}";
            else if (expr is InstancePropertyReference)
                res = $"{TSOverviewGenerator.expr(((InstancePropertyReference)expr).object_)}.{{P}}{((InstancePropertyReference)expr).property.name}";
            else if (expr is EnumMemberReference)
                res = $"{{E}}{((EnumMemberReference)expr).decl.parentEnum.name}::{((EnumMemberReference)expr).decl.name}";
            else if (expr is NullCoalesceExpression)
                res = $"{TSOverviewGenerator.expr(((NullCoalesceExpression)expr).defaultExpr)} ?? {TSOverviewGenerator.expr(((NullCoalesceExpression)expr).exprIfNull)}";
            else { }
            return res;
        }
        
        public static string block(Block block, bool previewOnly = false, bool allowOneLiner = true) {
            if (previewOnly)
                return " { ... }";
            var stmtLen = block.statements.length();
            return stmtLen == 0 ? " { }" : allowOneLiner && stmtLen == 1 ? $"\n{TSOverviewGenerator.pad(TSOverviewGenerator.rawBlock(block))}" : $" {{\n{TSOverviewGenerator.pad(TSOverviewGenerator.rawBlock(block))}\n}}";
        }
        
        public static string stmt(Statement stmt, bool previewOnly = false) {
            var res = "UNKNOWN-STATEMENT";
            if (stmt is BreakStatement)
                res = "break;";
            else if (stmt is ReturnStatement)
                res = ((ReturnStatement)stmt).expression == null ? "return;" : $"return {TSOverviewGenerator.expr(((ReturnStatement)stmt).expression)};";
            else if (stmt is UnsetStatement)
                res = $"unset {TSOverviewGenerator.expr(((UnsetStatement)stmt).expression)};";
            else if (stmt is ThrowStatement)
                res = $"throw {TSOverviewGenerator.expr(((ThrowStatement)stmt).expression)};";
            else if (stmt is ExpressionStatement)
                res = $"{TSOverviewGenerator.expr(((ExpressionStatement)stmt).expression)};";
            else if (stmt is VariableDeclaration)
                res = $"var {TSOverviewGenerator.var(((VariableDeclaration)stmt))};";
            else if (stmt is ForeachStatement)
                res = $"for (const {((ForeachStatement)stmt).itemVar.name} of {TSOverviewGenerator.expr(((ForeachStatement)stmt).items)})" + TSOverviewGenerator.block(((ForeachStatement)stmt).body, previewOnly);
            else if (stmt is IfStatement) {
                var elseIf = ((IfStatement)stmt).else_ != null && ((IfStatement)stmt).else_.statements.length() == 1 && ((IfStatement)stmt).else_.statements.get(0) is IfStatement;
                res = $"if ({TSOverviewGenerator.expr(((IfStatement)stmt).condition)}){TSOverviewGenerator.block(((IfStatement)stmt).then, previewOnly)}";
                if (!previewOnly)
                    res += (elseIf ? $"\nelse {TSOverviewGenerator.stmt(((IfStatement)stmt).else_.statements.get(0))}" : "") + (!elseIf && ((IfStatement)stmt).else_ != null ? $"\nelse" + TSOverviewGenerator.block(((IfStatement)stmt).else_) : "");
            }
            else if (stmt is WhileStatement)
                res = $"while ({TSOverviewGenerator.expr(((WhileStatement)stmt).condition)})" + TSOverviewGenerator.block(((WhileStatement)stmt).body, previewOnly);
            else if (stmt is ForStatement)
                res = $"for ({(((ForStatement)stmt).itemVar != null ? TSOverviewGenerator.var(((ForStatement)stmt).itemVar) : "")}; {TSOverviewGenerator.expr(((ForStatement)stmt).condition)}; {TSOverviewGenerator.expr(((ForStatement)stmt).incrementor)})" + TSOverviewGenerator.block(((ForStatement)stmt).body, previewOnly);
            else if (stmt is DoStatement)
                res = $"do{TSOverviewGenerator.block(((DoStatement)stmt).body, previewOnly)} while ({TSOverviewGenerator.expr(((DoStatement)stmt).condition)})";
            else if (stmt is TryStatement)
                res = "try" + TSOverviewGenerator.block(((TryStatement)stmt).tryBody, previewOnly, false) + (((TryStatement)stmt).catchBody != null ? $" catch ({((TryStatement)stmt).catchVar.name}){TSOverviewGenerator.block(((TryStatement)stmt).catchBody, previewOnly)}" : "") + (((TryStatement)stmt).finallyBody != null ? "finally" + TSOverviewGenerator.block(((TryStatement)stmt).finallyBody, previewOnly) : "");
            else if (stmt is ContinueStatement)
                res = $"continue;";
            else { }
            return previewOnly ? res : TSOverviewGenerator.leading(stmt) + res;
        }
        
        public static string rawBlock(Block block) {
            return block.statements.map((Statement stmt) => { return TSOverviewGenerator.stmt(stmt); }).join("\n");
        }
        
        public static string methodBase(IMethodBase method, Type_ returns) {
            if (method == null)
                return "";
            var name = method is Method ? ((Method)method).name : method is Constructor ? "constructor" : method is GlobalFunction ? ((GlobalFunction)method).name : "???";
            var typeArgs = method is Method ? ((Method)method).typeArguments : null;
            return TSOverviewGenerator.preIf("/* throws */ ", method.throws) + $"{name}{TSOverviewGenerator.typeArgs(typeArgs)}({method.parameters.map((MethodParameter p) => { return TSOverviewGenerator.var(p); }).join(", ")})" + (returns is VoidType ? "" : $": {TSOverviewGenerator.type(returns)}") + (method.body != null ? $" {{\n{TSOverviewGenerator.pad(TSOverviewGenerator.rawBlock(method.body))}\n}}" : ";");
        }
        
        public static string method(Method method) {
            return method == null ? "" : $"{(method.isStatic ? "static " : "")}{(method.attributes.hasKey("mutates") ? "@mutates " : "")}{TSOverviewGenerator.methodBase(method, method.returns)}";
        }
        
        public static string classLike(IInterface cls) {
            var resList = new List<string>();
            resList.push(cls.fields.map((Field field) => { return TSOverviewGenerator.var(field) + ";"; }).join("\n"));
            if (cls is Class) {
                resList.push(((Class)cls).properties.map((Property prop) => { return TSOverviewGenerator.var(prop) + ";"; }).join("\n"));
                resList.push(TSOverviewGenerator.methodBase(((Class)cls).constructor_, VoidType.instance));
            }
            resList.push(cls.methods.map((Method method) => { return TSOverviewGenerator.method(method); }).join("\n\n"));
            return TSOverviewGenerator.pad(resList.filter((string x) => { return x != ""; }).join("\n\n"));
        }
        
        public static string pad(string str) {
            return str.split(new RegExp("\\n")).map((string x) => { return $"    {x}"; }).join("\n");
        }
        
        public static string imp(IImportable imp) {
            return "" + (imp is UnresolvedImport ? "X" : imp is Class ? "C" : imp is Interface ? "I" : imp is Enum_ ? "E" : "???") + $":{imp.name}";
        }
        
        public static string nodeRepr(IAstNode node) {
            if (node is Statement)
                return TSOverviewGenerator.stmt(((Statement)node), true);
            else if (node is Expression)
                return TSOverviewGenerator.expr(((Expression)node), true);
            else
                return "/* TODO: missing */";
        }
        
        public static string generate(SourceFile sourceFile) {
            var imps = sourceFile.imports.map((Import imp) => { return (imp.importAll ? $"import * as {imp.importAs}" : $"import {{ {imp.imports.map((IImportable x) => { return TSOverviewGenerator.imp(x); }).join(", ")} }}") + $" from \"{imp.exportScope.packageName}{TSOverviewGenerator.pre("/", imp.exportScope.scopeName)}\";"; });
            var enums = sourceFile.enums.map((Enum_ enum_) => { return $"enum {enum_.name} {{ {enum_.values.map((EnumMember x) => { return x.name; }).join(", ")} }}"; });
            var intfs = sourceFile.interfaces.map((Interface intf) => { return $"interface {intf.name}{TSOverviewGenerator.typeArgs(intf.typeArguments)}" + $"{TSOverviewGenerator.preArr(" extends ", intf.baseInterfaces.map((Type_ x) => { return TSOverviewGenerator.type(x); }))} {{\n{TSOverviewGenerator.classLike(intf)}\n}}"; });
            var classes = sourceFile.classes.map((Class cls) => { return $"class {cls.name}{TSOverviewGenerator.typeArgs(cls.typeArguments)}" + TSOverviewGenerator.pre(" extends ", cls.baseClass != null ? TSOverviewGenerator.type(cls.baseClass) : null) + TSOverviewGenerator.preArr(" implements ", cls.baseInterfaces.map((Type_ x) => { return TSOverviewGenerator.type(x); })) + $" {{\n{TSOverviewGenerator.classLike(cls)}\n}}"; });
            var funcs = sourceFile.funcs.map((GlobalFunction func) => { return $"function {func.name}{TSOverviewGenerator.methodBase(func, func.returns)}"; });
            var main = TSOverviewGenerator.rawBlock(sourceFile.mainBlock);
            var result = $"// export scope: {sourceFile.exportScope.packageName}/{sourceFile.exportScope.scopeName}\n" + new List<string> { imps.join("\n"), enums.join("\n"), intfs.join("\n\n"), classes.join("\n\n"), funcs.join("\n\n"), main }.filter((string x) => { return x != ""; }).join("\n\n");
            return result;
        }
    }
}