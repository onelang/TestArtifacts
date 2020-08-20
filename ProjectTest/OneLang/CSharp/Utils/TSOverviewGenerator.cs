using One.Ast;
using System.Collections.Generic;

namespace Utils
{
    public class TSOverviewGenerator {
        public static string leading(IHasAttributesAndTrivia item) {
            var result = "";
            if (item.leadingTrivia != null && item.leadingTrivia.length() > 0)
                result += item.leadingTrivia;
            if (item.attributes != null)
                result += Object.keys(item.attributes).map(x => $"/// {{ATTR}} name=\"{x}\", value={JSON.stringify(item.attributes.get(x))}\n").join("");
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
            if (v is Field field || v is Property) {
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
            if (v is VariableDeclaration varDecl || v is ForVariable || v is Field || v is MethodParameter) {
                var init = (((IVariableWithInitializer)v)).initializer;
                if (init != null)
                    result += TSOverviewGenerator.pre(" = ", TSOverviewGenerator.expr(init));
            }
            return result;
        }
        
        public static string expr(IExpression expr, bool previewOnly = false) {
            var res = "UNKNOWN-EXPR";
            if (expr is NewExpression newExpr)
                res = $"new {TSOverviewGenerator.type(newExpr.cls)}({(previewOnly ? "..." : newExpr.args.map(x => TSOverviewGenerator.expr(x)).join(", "))})";
            else if (expr is UnresolvedNewExpression unrNewExpr)
                res = $"new {TSOverviewGenerator.type(unrNewExpr.cls)}({(previewOnly ? "..." : unrNewExpr.args.map(x => TSOverviewGenerator.expr(x)).join(", "))})";
            else if (expr is Identifier ident)
                res = $"{{ID}}{ident.text}";
            else if (expr is PropertyAccessExpression propAccExpr)
                res = $"{TSOverviewGenerator.expr(propAccExpr.object_)}.{{PA}}{propAccExpr.propertyName}";
            else if (expr is UnresolvedCallExpression unrCallExpr) {
                var typeArgs = unrCallExpr.typeArgs.length() > 0 ? $"<{unrCallExpr.typeArgs.map(x => TSOverviewGenerator.type(x)).join(", ")}>" : "";
                res = $"{TSOverviewGenerator.expr(unrCallExpr.func)}{typeArgs}({(previewOnly ? "..." : unrCallExpr.args.map(x => TSOverviewGenerator.expr(x)).join(", "))})";
            }
            else if (expr is UnresolvedMethodCallExpression unrMethCallExpr) {
                var typeArgs = unrMethCallExpr.typeArgs.length() > 0 ? $"<{unrMethCallExpr.typeArgs.map(x => TSOverviewGenerator.type(x)).join(", ")}>" : "";
                res = $"{TSOverviewGenerator.expr(unrMethCallExpr.object_)}.{{UM}}{unrMethCallExpr.methodName}{typeArgs}({(previewOnly ? "..." : unrMethCallExpr.args.map(x => TSOverviewGenerator.expr(x)).join(", "))})";
            }
            else if (expr is InstanceMethodCallExpression instMethCallExpr) {
                var typeArgs = instMethCallExpr.typeArgs.length() > 0 ? $"<{instMethCallExpr.typeArgs.map(x => TSOverviewGenerator.type(x)).join(", ")}>" : "";
                res = $"{TSOverviewGenerator.expr(instMethCallExpr.object_)}.{{M}}{instMethCallExpr.method.name}{typeArgs}({(previewOnly ? "..." : instMethCallExpr.args.map(x => TSOverviewGenerator.expr(x)).join(", "))})";
            }
            else if (expr is StaticMethodCallExpression statMethCallExpr) {
                var typeArgs = statMethCallExpr.typeArgs.length() > 0 ? $"<{statMethCallExpr.typeArgs.map(x => TSOverviewGenerator.type(x)).join(", ")}>" : "";
                res = $"{statMethCallExpr.method.parentInterface.name}.{{M}}{statMethCallExpr.method.name}{typeArgs}({(previewOnly ? "..." : statMethCallExpr.args.map(x => TSOverviewGenerator.expr(x)).join(", "))})";
            }
            else if (expr is GlobalFunctionCallExpression globFunctCallExpr)
                res = $"{globFunctCallExpr.func.name}({(previewOnly ? "..." : globFunctCallExpr.args.map(x => TSOverviewGenerator.expr(x)).join(", "))})";
            else if (expr is LambdaCallExpression lambdCallExpr)
                res = $"{TSOverviewGenerator.expr(lambdCallExpr.method)}({(previewOnly ? "..." : lambdCallExpr.args.map(x => TSOverviewGenerator.expr(x)).join(", "))})";
            else if (expr is BooleanLiteral boolLit)
                res = $"{boolLit.boolValue}";
            else if (expr is StringLiteral strLit)
                res = $"{JSON.stringify(strLit.stringValue)}";
            else if (expr is NumericLiteral numLit)
                res = $"{numLit.valueAsText}";
            else if (expr is CharacterLiteral charLit)
                res = $"'{charLit.charValue}'";
            else if (expr is ElementAccessExpression elemAccExpr)
                res = $"({TSOverviewGenerator.expr(elemAccExpr.object_)})[{TSOverviewGenerator.expr(elemAccExpr.elementExpr)}]";
            else if (expr is TemplateString templStr)
                res = "`" + templStr.parts.map(x => x.isLiteral ? x.literalText : "${" + TSOverviewGenerator.expr(x.expression) + "}").join("") + "`";
            else if (expr is BinaryExpression binExpr)
                res = $"{TSOverviewGenerator.expr(binExpr.left)} {binExpr.operator_} {TSOverviewGenerator.expr(binExpr.right)}";
            else if (expr is ArrayLiteral arrayLit)
                res = $"[{arrayLit.items.map(x => TSOverviewGenerator.expr(x)).join(", ")}]";
            else if (expr is CastExpression castExpr)
                res = $"<{TSOverviewGenerator.type(castExpr.newType)}>({TSOverviewGenerator.expr(castExpr.expression)})";
            else if (expr is ConditionalExpression condExpr)
                res = $"{TSOverviewGenerator.expr(condExpr.condition)} ? {TSOverviewGenerator.expr(condExpr.whenTrue)} : {TSOverviewGenerator.expr(condExpr.whenFalse)}";
            else if (expr is InstanceOfExpression instOfExpr)
                res = $"{TSOverviewGenerator.expr(instOfExpr.expr)} instanceof {TSOverviewGenerator.type(instOfExpr.checkType)}";
            else if (expr is ParenthesizedExpression parExpr)
                res = $"({TSOverviewGenerator.expr(parExpr.expression)})";
            else if (expr is RegexLiteral regexLit)
                res = $"/{regexLit.pattern}/{(regexLit.global ? "g" : "")}{(regexLit.caseInsensitive ? "g" : "")}";
            else if (expr is Lambda lambd)
                res = $"({lambd.parameters.map(x => x.name + (x.type != null ? ": " + TSOverviewGenerator.type(x.type) : "")).join(", ")}) => {{ {TSOverviewGenerator.rawBlock(lambd.body)} }}";
            else if (expr is UnaryExpression unaryExpr && unaryExpr.unaryType == UnaryType.Prefix)
                res = $"{unaryExpr.operator_}{TSOverviewGenerator.expr(unaryExpr.operand)}";
            else if (expr is UnaryExpression unaryExpr2 && unaryExpr2.unaryType == UnaryType.Postfix)
                res = $"{TSOverviewGenerator.expr(unaryExpr2.operand)}{unaryExpr2.operator_}";
            else if (expr is MapLiteral mapLit) {
                var repr = mapLit.items.map(item => $"{item.key}: {TSOverviewGenerator.expr(item.value)}").join(",\n");
                res = "{L:M}" + (repr == "" ? "{}" : repr.includes("\n") ? $"{{\n{TSOverviewGenerator.pad(repr)}\n}}" : $"{{ {repr} }}");
            }
            else if (expr is NullLiteral)
                res = $"null";
            else if (expr is AwaitExpression awaitExpr)
                res = $"await {TSOverviewGenerator.expr(awaitExpr.expr)}";
            else if (expr is ThisReference)
                res = $"{{R}}this";
            else if (expr is StaticThisReference)
                res = $"{{R:Static}}this";
            else if (expr is EnumReference enumRef)
                res = $"{{R:Enum}}{enumRef.decl.name}";
            else if (expr is ClassReference classRef)
                res = $"{{R:Cls}}{classRef.decl.name}";
            else if (expr is MethodParameterReference methParRef)
                res = $"{{R:MetP}}{methParRef.decl.name}";
            else if (expr is VariableDeclarationReference varDeclRef)
                res = $"{{V}}{varDeclRef.decl.name}";
            else if (expr is ForVariableReference forVarRef)
                res = $"{{R:ForV}}{forVarRef.decl.name}";
            else if (expr is ForeachVariableReference forVarRef2)
                res = $"{{R:ForEV}}{forVarRef2.decl.name}";
            else if (expr is CatchVariableReference catchVarRef)
                res = $"{{R:CatchV}}{catchVarRef.decl.name}";
            else if (expr is GlobalFunctionReference globFunctRef)
                res = $"{{R:GFunc}}{globFunctRef.decl.name}";
            else if (expr is SuperReference)
                res = $"{{R}}super";
            else if (expr is StaticFieldReference statFieldRef)
                res = $"{{R:StFi}}{statFieldRef.decl.parentInterface.name}::{statFieldRef.decl.name}";
            else if (expr is StaticPropertyReference statPropRef)
                res = $"{{R:StPr}}{statPropRef.decl.parentClass.name}::{statPropRef.decl.name}";
            else if (expr is InstanceFieldReference instFieldRef)
                res = $"{TSOverviewGenerator.expr(instFieldRef.object_)}.{{F}}{instFieldRef.field.name}";
            else if (expr is InstancePropertyReference instPropRef)
                res = $"{TSOverviewGenerator.expr(instPropRef.object_)}.{{P}}{instPropRef.property.name}";
            else if (expr is EnumMemberReference enumMembRef)
                res = $"{{E}}{enumMembRef.decl.parentEnum.name}::{enumMembRef.decl.name}";
            else if (expr is NullCoalesceExpression nullCoalExpr)
                res = $"{TSOverviewGenerator.expr(nullCoalExpr.defaultExpr)} ?? {TSOverviewGenerator.expr(nullCoalExpr.exprIfNull)}";
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
            else if (stmt is ReturnStatement retStat)
                res = retStat.expression == null ? "return;" : $"return {TSOverviewGenerator.expr(retStat.expression)};";
            else if (stmt is UnsetStatement unsetStat)
                res = $"unset {TSOverviewGenerator.expr(unsetStat.expression)};";
            else if (stmt is ThrowStatement throwStat)
                res = $"throw {TSOverviewGenerator.expr(throwStat.expression)};";
            else if (stmt is ExpressionStatement exprStat)
                res = $"{TSOverviewGenerator.expr(exprStat.expression)};";
            else if (stmt is VariableDeclaration varDecl2)
                res = $"var {TSOverviewGenerator.var(varDecl2)};";
            else if (stmt is ForeachStatement forStat)
                res = $"for (const {forStat.itemVar.name} of {TSOverviewGenerator.expr(forStat.items)})" + TSOverviewGenerator.block(forStat.body, previewOnly);
            else if (stmt is IfStatement ifStat) {
                var elseIf = ifStat.else_ != null && ifStat.else_.statements.length() == 1 && ifStat.else_.statements.get(0) is IfStatement;
                res = $"if ({TSOverviewGenerator.expr(ifStat.condition)}){TSOverviewGenerator.block(ifStat.then, previewOnly)}";
                if (!previewOnly)
                    res += (elseIf ? $"\nelse {TSOverviewGenerator.stmt(ifStat.else_.statements.get(0))}" : "") + (!elseIf && ifStat.else_ != null ? $"\nelse" + TSOverviewGenerator.block(ifStat.else_) : "");
            }
            else if (stmt is WhileStatement whileStat)
                res = $"while ({TSOverviewGenerator.expr(whileStat.condition)})" + TSOverviewGenerator.block(whileStat.body, previewOnly);
            else if (stmt is ForStatement forStat2)
                res = $"for ({(forStat2.itemVar != null ? TSOverviewGenerator.var(forStat2.itemVar) : "")}; {TSOverviewGenerator.expr(forStat2.condition)}; {TSOverviewGenerator.expr(forStat2.incrementor)})" + TSOverviewGenerator.block(forStat2.body, previewOnly);
            else if (stmt is DoStatement doStat)
                res = $"do{TSOverviewGenerator.block(doStat.body, previewOnly)} while ({TSOverviewGenerator.expr(doStat.condition)})";
            else if (stmt is TryStatement tryStat)
                res = "try" + TSOverviewGenerator.block(tryStat.tryBody, previewOnly, false) + (tryStat.catchBody != null ? $" catch ({tryStat.catchVar.name}){TSOverviewGenerator.block(tryStat.catchBody, previewOnly)}" : "") + (tryStat.finallyBody != null ? "finally" + TSOverviewGenerator.block(tryStat.finallyBody, previewOnly) : "");
            else if (stmt is ContinueStatement)
                res = $"continue;";
            else { }
            return previewOnly ? res : TSOverviewGenerator.leading(stmt) + res;
        }
        
        public static string rawBlock(Block block) {
            return block.statements.map(stmt => TSOverviewGenerator.stmt(stmt)).join("\n");
        }
        
        public static string methodBase(IMethodBase method, Type_ returns) {
            if (method == null)
                return "";
            var name = method is Method meth ? meth.name : method is Constructor ? "constructor" : method is GlobalFunction globFunct ? globFunct.name : "???";
            var typeArgs = method is Method meth2 ? meth2.typeArguments : null;
            return TSOverviewGenerator.preIf("/* throws */ ", method.throws) + $"{name}{TSOverviewGenerator.typeArgs(typeArgs)}({method.parameters.map(p => TSOverviewGenerator.leading(p) + TSOverviewGenerator.var(p)).join(", ")})" + (returns is VoidType ? "" : $": {TSOverviewGenerator.type(returns)}") + (method.body != null ? $" {{\n{TSOverviewGenerator.pad(TSOverviewGenerator.rawBlock(method.body))}\n}}" : ";");
        }
        
        public static string method(Method method) {
            return method == null ? "" : $"{(method.isStatic ? "static " : "")}{(method.attributes.hasKey("mutates") ? "@mutates " : "")}{TSOverviewGenerator.methodBase(method, method.returns)}";
        }
        
        public static string classLike(IInterface cls) {
            var resList = new List<string>();
            resList.push(cls.fields.map(field => TSOverviewGenerator.var(field) + ";").join("\n"));
            if (cls is Class class_) {
                resList.push(class_.properties.map(prop => TSOverviewGenerator.var(prop) + ";").join("\n"));
                resList.push(TSOverviewGenerator.methodBase(class_.constructor_, VoidType.instance));
            }
            resList.push(cls.methods.map(method => TSOverviewGenerator.method(method)).join("\n\n"));
            return TSOverviewGenerator.pad(resList.filter(x => x != "").join("\n\n"));
        }
        
        public static string pad(string str) {
            return str.split(new RegExp("\\n")).map(x => $"    {x}").join("\n");
        }
        
        public static string imp(IImportable imp) {
            return "" + (imp is UnresolvedImport ? "X" : imp is Class ? "C" : imp is Interface ? "I" : imp is Enum_ ? "E" : "???") + $":{imp.name}";
        }
        
        public static string nodeRepr(IAstNode node) {
            if (node is Statement stat)
                return TSOverviewGenerator.stmt(stat, true);
            else if (node is Expression expr)
                return TSOverviewGenerator.expr(expr, true);
            else
                return "/* TODO: missing */";
        }
        
        public static string generate(SourceFile sourceFile) {
            var imps = sourceFile.imports.map(imp => (imp.importAll ? $"import * as {imp.importAs}" : $"import {{ {imp.imports.map(x => TSOverviewGenerator.imp(x)).join(", ")} }}") + $" from \"{imp.exportScope.packageName}{TSOverviewGenerator.pre("/", imp.exportScope.scopeName)}\";");
            var enums = sourceFile.enums.map(enum_ => $"{TSOverviewGenerator.leading(enum_)}enum {enum_.name} {{ {enum_.values.map(x => x.name).join(", ")} }}");
            var intfs = sourceFile.interfaces.map(intf => $"{TSOverviewGenerator.leading(intf)}interface {intf.name}{TSOverviewGenerator.typeArgs(intf.typeArguments)}" + $"{TSOverviewGenerator.preArr(" extends ", intf.baseInterfaces.map(x => TSOverviewGenerator.type(x)))} {{\n{TSOverviewGenerator.classLike(intf)}\n}}");
            var classes = sourceFile.classes.map(cls => $"{TSOverviewGenerator.leading(cls)}class {cls.name}{TSOverviewGenerator.typeArgs(cls.typeArguments)}" + TSOverviewGenerator.pre(" extends ", cls.baseClass != null ? TSOverviewGenerator.type(cls.baseClass) : null) + TSOverviewGenerator.preArr(" implements ", cls.baseInterfaces.map(x => TSOverviewGenerator.type(x))) + $" {{\n{TSOverviewGenerator.classLike(cls)}\n}}");
            var funcs = sourceFile.funcs.map(func => $"{TSOverviewGenerator.leading(func)}function {func.name}{TSOverviewGenerator.methodBase(func, func.returns)}");
            var main = TSOverviewGenerator.rawBlock(sourceFile.mainBlock);
            var result = $"// export scope: {sourceFile.exportScope.packageName}/{sourceFile.exportScope.scopeName}\n" + new List<string> { imps.join("\n"), enums.join("\n"), intfs.join("\n\n"), classes.join("\n\n"), funcs.join("\n\n"), main }.filter(x => x != "").join("\n\n");
            return result;
        }
    }
}