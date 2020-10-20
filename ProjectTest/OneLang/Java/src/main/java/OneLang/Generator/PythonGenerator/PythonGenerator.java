import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.HashSet;

public class PythonGenerator implements IGenerator {
    public Integer tmplStrLevel = 0;
    public Package package_;
    public SourceFile currentFile;
    public Set<String> imports;
    public Set<String> importAllScopes;
    public IInterface currentClass;
    public String[] reservedWords;
    public String[] fieldToMethodHack;
    public List<IGeneratorPlugin> plugins;
    
    public PythonGenerator()
    {
        this.reservedWords = new String[] { "from", "async", "global", "lambda", "cls", "import", "pass" };
        this.fieldToMethodHack = new String[0];
        this.plugins = new ArrayList<IGeneratorPlugin>();
        this.plugins.add(new JsToPython(this));
    }
    
    public String getLangName() {
        return "Python";
    }
    
    public String getExtension() {
        return "py";
    }
    
    public String type(IType type) {
        if (type instanceof ClassType) {
            if (((ClassType)type).decl.getName().equals("TsString"))
                return "str";
            else if (((ClassType)type).decl.getName().equals("TsBoolean"))
                return "bool";
            else if (((ClassType)type).decl.getName().equals("TsNumber"))
                return "int";
            else
                return this.clsName(((ClassType)type).decl);
        }
        else
            return "NOT-HANDLED-TYPE";
    }
    
    public String[] splitName(String name) {
        var nameParts = new ArrayList<String>();
        var partStartIdx = 0;
        for (Integer i = 1; i < name.length(); i++) {
            var prevChrCode = (int)name.charAt(i - 1);
            var chrCode = (int)name.charAt(i);
            if (65 <= chrCode && chrCode <= 90 && !(65 <= prevChrCode && prevChrCode <= 90)) {
                // 'A' .. 'Z'
                nameParts.add(name.substring(partStartIdx, i).toLowerCase());
                partStartIdx = i;
            }
            else if (chrCode == 95) {
                // '-'
                nameParts.add(name.substring(partStartIdx, i));
                partStartIdx = i + 1;
            }
        }
        nameParts.add(name.substring(partStartIdx).toLowerCase());
        return nameParts.toArray(String[]::new);
    }
    
    public String name_(String name) {
        if (Arrays.stream(this.reservedWords).anyMatch(name::equals))
            name += "_";
        if (Arrays.stream(this.fieldToMethodHack).anyMatch(name::equals))
            name += "()";
        return Arrays.stream(this.splitName(name)).collect(Collectors.joining("_"));
    }
    
    public String calcImportedName(ExportScopeRef exportScope, String name) {
        if (this.importAllScopes.contains(exportScope.getId()))
            return name;
        else
            return this.calcImportAlias(exportScope) + "." + name;
    }
    
    public String enumName(Enum enum_, Boolean isDecl) {
        var name = this.name_(enum_.getName()).toUpperCase();
        if (isDecl || enum_.getParentFile().exportScope == null || enum_.getParentFile() == this.currentFile)
            return name;
        return this.calcImportedName(enum_.getParentFile().exportScope, name);
    }
    
    public String enumName(Enum enum_) {
        return this.enumName(enum_, false);
    }
    
    public String enumMemberName(String name) {
        return this.name_(name).toUpperCase();
    }
    
    public String clsName(IInterface cls, Boolean isDecl) {
        // TODO: hack
        if (cls.getName().equals("Set"))
            return "dict";
        if (isDecl || cls.getParentFile().exportScope == null || cls.getParentFile() == this.currentFile)
            return cls.getName();
        return this.calcImportedName(cls.getParentFile().exportScope, cls.getName());
    }
    
    public String clsName(IInterface cls) {
        return this.clsName(cls, false);
    }
    
    public String leading(Statement item) {
        var result = "";
        if (item.getLeadingTrivia() != null && item.getLeadingTrivia().length() > 0)
            result += item.getLeadingTrivia().replaceAll("//", "#");
        //if (item.attributes !== null)
        //    result += Object.keys(item.attributes).map(x => `// @${x} ${item.attributes[x]}\n`).join("");
        return result;
    }
    
    public String preArr(String prefix, String[] value) {
        return value.length > 0 ? prefix + Arrays.stream(value).collect(Collectors.joining(", ")) : "";
    }
    
    public String preIf(String prefix, Boolean condition) {
        return condition ? prefix : "";
    }
    
    public String pre(String prefix, String value) {
        return value != null ? prefix + value : "";
    }
    
    public Boolean isTsArray(IType type) {
        return type instanceof ClassType && ((ClassType)type).decl.getName().equals("TsArray");
    }
    
    public String vis(Visibility v) {
        return v == Visibility.Private ? "__" : v == Visibility.Protected ? "_" : v == Visibility.Public ? "" : "/* TODO: not set */public";
    }
    
    public String varWoInit(IVariable v, IHasAttributesAndTrivia attr) {
        return this.name_(v.getName());
    }
    
    public String var(IVariableWithInitializer v, IHasAttributesAndTrivia attrs) {
        return this.varWoInit(v, attrs) + (v.getInitializer() != null ? " = " + this.expr(v.getInitializer()) : "");
    }
    
    public String exprCall(Expression[] args) {
        return "(" + Arrays.stream(Arrays.stream(args).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")";
    }
    
    public String callParams(Expression[] args) {
        var argReprs = new ArrayList<String>();
        for (Integer i = 0; i < args.length; i++)
            argReprs.add(this.expr(args[i]));
        return "(" + argReprs.stream().collect(Collectors.joining(", ")) + ")";
    }
    
    public String methodCall(IMethodCallExpression expr) {
        return this.name_(expr.getMethod().name) + this.callParams(expr.getArgs());
    }
    
    public String expr(IExpression expr) {
        for (var plugin : this.plugins) {
            var result = plugin.expr(expr);
            if (result != null)
                return result;
        }
        
        var res = "UNKNOWN-EXPR";
        if (expr instanceof NewExpression) {
            // TODO: hack
            if (((NewExpression)expr).cls.decl.getName().equals("Set"))
                res = ((NewExpression)expr).args.length == 0 ? "dict()" : "dict.fromkeys" + this.callParams(((NewExpression)expr).args);
            else
                res = this.clsName(((NewExpression)expr).cls.decl) + this.callParams(((NewExpression)expr).args);
        }
        else if (expr instanceof UnresolvedNewExpression)
            res = "/* TODO: UnresolvedNewExpression */ " + ((UnresolvedNewExpression)expr).cls.typeName + "(" + Arrays.stream(Arrays.stream(((UnresolvedNewExpression)expr).args).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")";
        else if (expr instanceof Identifier)
            res = "/* TODO: Identifier */ " + ((Identifier)expr).text;
        else if (expr instanceof PropertyAccessExpression)
            res = "/* TODO: PropertyAccessExpression */ " + this.expr(((PropertyAccessExpression)expr).object) + "." + ((PropertyAccessExpression)expr).propertyName;
        else if (expr instanceof UnresolvedCallExpression)
            res = "/* TODO: UnresolvedCallExpression */ " + this.expr(((UnresolvedCallExpression)expr).func) + this.exprCall(((UnresolvedCallExpression)expr).args);
        else if (expr instanceof UnresolvedMethodCallExpression)
            res = "/* TODO: UnresolvedMethodCallExpression */ " + this.expr(((UnresolvedMethodCallExpression)expr).object) + "." + ((UnresolvedMethodCallExpression)expr).methodName + this.exprCall(((UnresolvedMethodCallExpression)expr).args);
        else if (expr instanceof InstanceMethodCallExpression)
            res = this.expr(((InstanceMethodCallExpression)expr).object) + "." + this.methodCall(((InstanceMethodCallExpression)expr));
        else if (expr instanceof StaticMethodCallExpression) {
            //const parent = expr.method.parentInterface === this.currentClass ? "cls" : this.clsName(expr.method.parentInterface);
            var parent = this.clsName(((StaticMethodCallExpression)expr).getMethod().parentInterface);
            res = parent + "." + this.methodCall(((StaticMethodCallExpression)expr));
        }
        else if (expr instanceof GlobalFunctionCallExpression) {
            this.imports.add("from OneLangStdLib import *");
            res = this.name_(((GlobalFunctionCallExpression)expr).func.getName()) + this.exprCall(((GlobalFunctionCallExpression)expr).args);
        }
        else if (expr instanceof LambdaCallExpression)
            res = this.expr(((LambdaCallExpression)expr).method) + "(" + Arrays.stream(Arrays.stream(((LambdaCallExpression)expr).args).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")";
        else if (expr instanceof BooleanLiteral)
            res = (((BooleanLiteral)expr).boolValue ? "True" : "False");
        else if (expr instanceof StringLiteral)
            res = JSON.stringify(((StringLiteral)expr).stringValue);
        else if (expr instanceof NumericLiteral)
            res = ((NumericLiteral)expr).valueAsText;
        else if (expr instanceof CharacterLiteral)
            res = "'" + ((CharacterLiteral)expr).charValue + "'";
        else if (expr instanceof ElementAccessExpression)
            res = this.expr(((ElementAccessExpression)expr).object) + "[" + this.expr(((ElementAccessExpression)expr).elementExpr) + "]";
        else if (expr instanceof TemplateString) {
            var parts = new ArrayList<String>();
            for (var part : ((TemplateString)expr).parts) {
                if (part.isLiteral) {
                    var lit = "";
                    for (Integer i = 0; i < part.literalText.length(); i++) {
                        var chr = part.literalText.substring(i, i + 1);
                        if (chr.equals("\n"))
                            lit += "\\n";
                        else if (chr.equals("\r"))
                            lit += "\\r";
                        else if (chr.equals("\t"))
                            lit += "\\t";
                        else if (chr.equals("\\"))
                            lit += "\\\\";
                        else if (chr.equals("'"))
                            lit += "\\'";
                        else if (chr.equals("{"))
                            lit += "{{";
                        else if (chr.equals("}"))
                            lit += "}}";
                        else {
                            var chrCode = (int)chr.charAt(0);
                            if (32 <= chrCode && chrCode <= 126)
                                lit += chr;
                            else
                                throw new Error("invalid char in template string (code=" + chrCode + ")");
                        }
                    }
                    parts.add(lit);
                }
                else {
                    this.tmplStrLevel++;
                    var repr = this.expr(part.expression);
                    this.tmplStrLevel--;
                    parts.add(part.expression instanceof ConditionalExpression ? "{(" + repr + ")}" : "{" + repr + "}");
                }
            }
            res = this.tmplStrLevel == 1 ? "f'" + parts.stream().collect(Collectors.joining("")) + "'" : "f'''" + parts.stream().collect(Collectors.joining("")) + "'''";
        }
        else if (expr instanceof BinaryExpression) {
            var op = ((BinaryExpression)expr).operator.equals("&&") ? "and" : ((BinaryExpression)expr).operator.equals("||") ? "or" : ((BinaryExpression)expr).operator;
            res = this.expr(((BinaryExpression)expr).left) + " " + op + " " + this.expr(((BinaryExpression)expr).right);
        }
        else if (expr instanceof ArrayLiteral)
            res = "[" + Arrays.stream(Arrays.stream(((ArrayLiteral)expr).items).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + "]";
        else if (expr instanceof CastExpression)
            res = this.expr(((CastExpression)expr).expression);
        else if (expr instanceof ConditionalExpression)
            res = this.expr(((ConditionalExpression)expr).whenTrue) + " if " + this.expr(((ConditionalExpression)expr).condition) + " else " + this.expr(((ConditionalExpression)expr).whenFalse);
        else if (expr instanceof InstanceOfExpression)
            res = "isinstance(" + this.expr(((InstanceOfExpression)expr).expr) + ", " + this.type(((InstanceOfExpression)expr).checkType) + ")";
        else if (expr instanceof ParenthesizedExpression)
            res = "(" + this.expr(((ParenthesizedExpression)expr).expression) + ")";
        else if (expr instanceof RegexLiteral)
            res = "RegExp(" + JSON.stringify(((RegexLiteral)expr).pattern) + ")";
        else if (expr instanceof Lambda) {
            var body = "INVALID-BODY";
            if (((Lambda)expr).getBody().statements.size() == 1 && ((Lambda)expr).getBody().statements.get(0) instanceof ReturnStatement)
                body = this.expr((((ReturnStatement)((Lambda)expr).getBody().statements.get(0))).expression);
            else
                console.error("Multi-line lambda is not yet supported for Python: " + TSOverviewGenerator.preview.nodeRepr(((Lambda)expr)));
            
            var params = Arrays.stream(((Lambda)expr).getParameters()).map(x -> this.name_(x.getName())).toArray(String[]::new);
            
            res = "lambda " + Arrays.stream(params).collect(Collectors.joining(", ")) + ": " + body;
        }
        else if (expr instanceof UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Prefix) {
            var op = ((UnaryExpression)expr).operator.equals("!") ? "not " : ((UnaryExpression)expr).operator;
            if (op.equals("++"))
                res = this.expr(((UnaryExpression)expr).operand) + " = " + this.expr(((UnaryExpression)expr).operand) + " + 1";
            else if (op.equals("--"))
                res = this.expr(((UnaryExpression)expr).operand) + " = " + this.expr(((UnaryExpression)expr).operand) + " - 1";
            else
                res = op + this.expr(((UnaryExpression)expr).operand);
        }
        else if (expr instanceof UnaryExpression && ((UnaryExpression)expr).unaryType == UnaryType.Postfix) {
            if (((UnaryExpression)expr).operator.equals("++"))
                res = this.expr(((UnaryExpression)expr).operand) + " = " + this.expr(((UnaryExpression)expr).operand) + " + 1";
            else if (((UnaryExpression)expr).operator.equals("--"))
                res = this.expr(((UnaryExpression)expr).operand) + " = " + this.expr(((UnaryExpression)expr).operand) + " - 1";
            else
                res = this.expr(((UnaryExpression)expr).operand) + ((UnaryExpression)expr).operator;
        }
        else if (expr instanceof MapLiteral) {
            var repr = Arrays.stream(Arrays.stream(((MapLiteral)expr).items).map(item -> JSON.stringify(item.key) + ": " + this.expr(item.value)).toArray(String[]::new)).collect(Collectors.joining(",\n"));
            res = ((MapLiteral)expr).items.length == 0 ? "{}" : "{\n" + this.pad(repr) + "\n}";
        }
        else if (expr instanceof NullLiteral)
            res = "None";
        else if (expr instanceof AwaitExpression)
            res = this.expr(((AwaitExpression)expr).expr);
        else if (expr instanceof ThisReference)
            res = "self";
        else if (expr instanceof StaticThisReference)
            res = "cls";
        else if (expr instanceof EnumReference)
            res = this.enumName(((EnumReference)expr).decl);
        else if (expr instanceof ClassReference)
            res = this.name_(((ClassReference)expr).decl.getName());
        else if (expr instanceof MethodParameterReference)
            res = this.name_(((MethodParameterReference)expr).decl.getName());
        else if (expr instanceof VariableDeclarationReference)
            res = this.name_(((VariableDeclarationReference)expr).decl.getName());
        else if (expr instanceof ForVariableReference)
            res = this.name_(((ForVariableReference)expr).decl.getName());
        else if (expr instanceof ForeachVariableReference)
            res = this.name_(((ForeachVariableReference)expr).decl.getName());
        else if (expr instanceof CatchVariableReference)
            res = this.name_(((CatchVariableReference)expr).decl.getName());
        else if (expr instanceof GlobalFunctionReference)
            res = this.name_(((GlobalFunctionReference)expr).decl.getName());
        else if (expr instanceof SuperReference)
            res = "super()";
        else if (expr instanceof StaticFieldReference)
            res = this.clsName(((StaticFieldReference)expr).decl.parentInterface) + "." + this.name_(((StaticFieldReference)expr).decl.getName());
        else if (expr instanceof StaticPropertyReference)
            res = this.clsName(((StaticPropertyReference)expr).decl.parentClass) + ".get_" + this.name_(((StaticPropertyReference)expr).decl.getName()) + "()";
        else if (expr instanceof InstanceFieldReference)
            res = this.expr(((InstanceFieldReference)expr).object) + "." + this.name_(((InstanceFieldReference)expr).field.getName());
        else if (expr instanceof InstancePropertyReference)
            res = this.expr(((InstancePropertyReference)expr).object) + ".get_" + this.name_(((InstancePropertyReference)expr).property.getName()) + "()";
        else if (expr instanceof EnumMemberReference)
            res = this.enumName(((EnumMemberReference)expr).decl.parentEnum) + "." + this.enumMemberName(((EnumMemberReference)expr).decl.name);
        else if (expr instanceof NullCoalesceExpression)
            res = this.expr(((NullCoalesceExpression)expr).defaultExpr) + " or " + this.expr(((NullCoalesceExpression)expr).exprIfNull);
        else { }
        return res;
    }
    
    public String stmtDefault(Statement stmt) {
        var nl = "\n";
        if (stmt instanceof BreakStatement)
            return "break";
        else if (stmt instanceof ReturnStatement)
            return ((ReturnStatement)stmt).expression == null ? "return" : "return " + this.expr(((ReturnStatement)stmt).expression);
        else if (stmt instanceof UnsetStatement)
            return "/* unset " + this.expr(((UnsetStatement)stmt).expression) + "; */";
        else if (stmt instanceof ThrowStatement)
            return "raise " + this.expr(((ThrowStatement)stmt).expression);
        else if (stmt instanceof ExpressionStatement)
            return this.expr(((ExpressionStatement)stmt).expression);
        else if (stmt instanceof VariableDeclaration)
            return ((VariableDeclaration)stmt).getInitializer() != null ? this.name_(((VariableDeclaration)stmt).getName()) + " = " + this.expr(((VariableDeclaration)stmt).getInitializer()) : "";
        else if (stmt instanceof ForeachStatement)
            return "for " + this.name_(((ForeachStatement)stmt).itemVar.getName()) + " in " + this.expr(((ForeachStatement)stmt).items) + ":\n" + this.block(((ForeachStatement)stmt).body);
        else if (stmt instanceof IfStatement) {
            var elseIf = ((IfStatement)stmt).else_ != null && ((IfStatement)stmt).else_.statements.size() == 1 && ((IfStatement)stmt).else_.statements.get(0) instanceof IfStatement;
            return "if " + this.expr(((IfStatement)stmt).condition) + ":\n" + this.block(((IfStatement)stmt).then) + (elseIf ? "\nel" + this.stmt(((IfStatement)stmt).else_.statements.get(0)) : "") + (!elseIf && ((IfStatement)stmt).else_ != null ? "\nelse:\n" + this.block(((IfStatement)stmt).else_) : "");
        }
        else if (stmt instanceof WhileStatement)
            return "while " + this.expr(((WhileStatement)stmt).condition) + ":\n" + this.block(((WhileStatement)stmt).body);
        else if (stmt instanceof ForStatement)
            return (((ForStatement)stmt).itemVar != null ? this.var(((ForStatement)stmt).itemVar, null) + "\n" : "") + "\nwhile " + this.expr(((ForStatement)stmt).condition) + ":\n" + this.block(((ForStatement)stmt).body) + "\n" + this.pad(this.expr(((ForStatement)stmt).incrementor));
        else if (stmt instanceof DoStatement)
            return "while True:\n" + this.block(((DoStatement)stmt).body) + "\n" + this.pad("if not (" + this.expr(((DoStatement)stmt).condition) + "):" + nl + this.pad("break"));
        else if (stmt instanceof TryStatement)
            return "try:\n" + this.block(((TryStatement)stmt).tryBody) + (((TryStatement)stmt).catchBody != null ? "\nexcept Exception as " + this.name_(((TryStatement)stmt).catchVar.getName()) + ":\n" + this.block(((TryStatement)stmt).catchBody) : "") + (((TryStatement)stmt).finallyBody != null ? "\nfinally:\n" + this.block(((TryStatement)stmt).finallyBody) : "");
        else if (stmt instanceof ContinueStatement)
            return "continue";
        else
            return "UNKNOWN-STATEMENT";
    }
    
    public String stmt(Statement stmt) {
        String res = null;
        
        if (stmt.getAttributes() != null && stmt.getAttributes().containsKey("python"))
            res = stmt.getAttributes().get("python");
        else {
            for (var plugin : this.plugins) {
                res = plugin.stmt(stmt);
                if (res != null)
                    break;
            }
            
            if (res == null)
                res = this.stmtDefault(stmt);
        }
        
        return this.leading(stmt) + res;
    }
    
    public String stmts(Statement[] stmts, Boolean skipPass) {
        return this.pad(stmts.length == 0 && !skipPass ? "pass" : Arrays.stream(Arrays.stream(stmts).map(stmt -> this.stmt(stmt)).toArray(String[]::new)).collect(Collectors.joining("\n")));
    }
    
    public String stmts(Statement[] stmts) {
        return this.stmts(stmts, false);
    }
    
    public String block(Block block, Boolean skipPass) {
        return this.stmts(block.statements.toArray(Statement[]::new), skipPass);
    }
    
    public String block(Block block) {
        return this.block(block, false);
    }
    
    public String pass(String str) {
        return str.equals("") ? "pass" : str;
    }
    
    public String cls(Class cls) {
        if (cls.getAttributes().get("external").equals("true"))
            return "";
        this.currentClass = cls;
        var resList = new ArrayList<String>();
        var classAttributes = new ArrayList<String>();
        
        var staticFields = Arrays.stream(cls.getFields()).filter(x -> x.getIsStatic()).toArray(Field[]::new);
        
        if (staticFields.length > 0) {
            this.imports.add("import OneLangStdLib as one");
            classAttributes.add("@one.static_init");
            var fieldInits = Arrays.stream(staticFields).map(f -> "cls." + this.vis(f.getVisibility()) + cls.getName().replace(this.var(f, f), "cls")).toArray(String[]::new);
            resList.add("@classmethod\ndef static_init(cls):\n" + this.pad(Arrays.stream(fieldInits).collect(Collectors.joining("\n"))));
        }
        
        var constrStmts = new ArrayList<String>();
        
        for (var field : Arrays.stream(cls.getFields()).filter(x -> !x.getIsStatic()).toArray(Field[]::new)) {
            var init = field.constructorParam != null ? this.name_(field.constructorParam.getName()) : field.getInitializer() != null ? this.expr(field.getInitializer()) : "None";
            constrStmts.add("self." + this.name_(field.getName()) + " = " + init);
        }
        
        if (cls.baseClass != null) {
            if (cls.constructor_ != null && cls.constructor_.superCallArgs != null)
                constrStmts.add("super().__init__(" + Arrays.stream(Arrays.stream(cls.constructor_.superCallArgs).map(x -> this.expr(x)).toArray(String[]::new)).collect(Collectors.joining(", ")) + ")");
            else
                constrStmts.add("super().__init__()");
        }
        
        if (cls.constructor_ != null)
            for (var stmt : cls.constructor_.getBody().statements)
                constrStmts.add(this.stmt(stmt));
        
        resList.add("def __init__(self" + (cls.constructor_ == null ? "" : Arrays.stream(Arrays.stream(cls.constructor_.getParameters()).map(p -> ", " + this.var(p, null)).toArray(String[]::new)).collect(Collectors.joining(""))) + "):\n" + this.pad(this.pass(constrStmts.stream().collect(Collectors.joining("\n")))));
        
        for (var prop : cls.properties) {
            if (prop.getter != null)
                resList.add("def get_" + this.name_(prop.getName()) + "(self):\n" + this.block(prop.getter));
        }
        
        var methods = new ArrayList<String>();
        for (var method : cls.getMethods()) {
            if (method.getBody() == null)
                continue;
            // declaration only
            methods.add((method.getIsStatic() ? "@classmethod\n" : "") + "def " + this.name_(method.name) + "(" + (method.getIsStatic() ? "cls" : "self") + Arrays.stream(Arrays.stream(method.getParameters()).map(p -> ", " + this.var(p, null)).toArray(String[]::new)).collect(Collectors.joining("")) + "):" + "\n" + this.block(method.getBody()));
        }
        resList.add(methods.stream().collect(Collectors.joining("\n\n")));
        var resList2 = resList.stream().filter(x -> !x.equals("")).toArray(String[]::new);
        
        var clsHdr = "class " + this.clsName(cls, true) + (cls.baseClass != null ? "(" + this.clsName((((ClassType)cls.baseClass)).decl) + ")" : "") + ":\n";
        return Arrays.stream(classAttributes.stream().map(x -> x + "\n").toArray(String[]::new)).collect(Collectors.joining("")) + clsHdr + this.pad(resList2.length > 0 ? Arrays.stream(resList2).collect(Collectors.joining("\n\n")) : "pass");
    }
    
    public String pad(String str) {
        return str.equals("") ? "" : Arrays.stream(Arrays.stream(str.split("\\n", -1)).map(x -> "    " + x).toArray(String[]::new)).collect(Collectors.joining("\n"));
    }
    
    public String calcRelImport(ExportScopeRef targetPath, ExportScopeRef fromPath) {
        var targetParts = targetPath.scopeName.split("/", -1);
        var fromParts = fromPath.scopeName.split("/", -1);
        
        var sameLevel = 0;
        while (sameLevel < targetParts.length && sameLevel < fromParts.length && targetParts[sameLevel].equals(fromParts[sameLevel]))
            sameLevel++;
        
        var result = "";
        for (Integer i = 1; i < fromParts.length - sameLevel; i++)
            result += ".";
        
        for (Integer i = sameLevel; i < targetParts.length; i++)
            result += "." + targetParts[i];
        
        return result;
    }
    
    public String calcImportAlias(ExportScopeRef targetPath) {
        var parts = targetPath.scopeName.split("/", -1);
        var filename = parts[parts.length - 1];
        return NameUtils.shortName(filename);
    }
    
    public String genFile(SourceFile sourceFile) {
        this.currentFile = sourceFile;
        this.imports = new HashSet<String>();
        this.importAllScopes = new HashSet<String>();
        this.imports.add("from OneLangStdLib import *");
        // TODO: do not add this globally, just for nativeResolver methods
               
        if (sourceFile.enums.length > 0)
            this.imports.add("from enum import Enum");
        
        for (var import_ : Arrays.stream(sourceFile.imports).filter(x -> !x.importAll).toArray(Import[]::new)) {
            if (import_.getAttributes().get("python-ignore").equals("true"))
                continue;
            
            if (import_.getAttributes().containsKey("python-import-all")) {
                this.imports.add("from " + import_.getAttributes().get("python-import-all") + " import *");
                this.importAllScopes.add(import_.exportScope.getId());
            }
            else {
                var alias = this.calcImportAlias(import_.exportScope);
                this.imports.add("import " + this.package_.name + "." + import_.exportScope.scopeName.replaceAll("/", ".") + " as " + alias);
            }
        }
        
        var enums = new ArrayList<String>();
        for (var enum_ : sourceFile.enums) {
            var values = new ArrayList<String>();
            for (Integer i = 0; i < enum_.values.length; i++)
                values.add(this.enumMemberName(enum_.values[i].name) + " = " + i + 1);
            enums.add("class " + this.enumName(enum_, true) + "(Enum):\n" + this.pad(values.stream().collect(Collectors.joining("\n"))));
        }
        
        var classes = new ArrayList<String>();
        for (var cls : sourceFile.classes)
            classes.add(this.cls(cls));
        
        var main = sourceFile.mainBlock.statements.size() > 0 ? this.block(sourceFile.mainBlock) : "";
        
        var imports = new ArrayList<String>();
        for (var imp : this.imports)
            imports.add(imp);
        
        return Arrays.stream(new ArrayList<>(List.of(imports.stream().collect(Collectors.joining("\n")), enums.stream().collect(Collectors.joining("\n\n")), classes.stream().collect(Collectors.joining("\n\n")), main)).stream().filter(x -> !x.equals("")).toArray(String[]::new)).collect(Collectors.joining("\n\n"));
    }
    
    public GeneratedFile[] generate(Package pkg) {
        this.package_ = pkg;
        var result = new ArrayList<GeneratedFile>();
        for (var path : pkg.files.keySet().toArray(String[]::new))
            result.add(new GeneratedFile(pkg.name + "/" + path, this.genFile(pkg.files.get(path))));
        return result.toArray(GeneratedFile[]::new);
    }
}