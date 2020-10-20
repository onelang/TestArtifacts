import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Arrays;

public class TypeScriptParser2 implements IParser, IExpressionParserHooks, IReaderHooks {
    public List<String> context;
    public Reader reader;
    public ExpressionParser expressionParser;
    public ExportScopeRef exportScope;
    public Boolean missingReturnTypeIsVoid = false;
    public SourcePath path;
    
    NodeManager nodeManager;
    public NodeManager getNodeManager() { return this.nodeManager; }
    public void setNodeManager(NodeManager value) { this.nodeManager = value; }
    
    public TypeScriptParser2(String source, SourcePath path)
    {
        this.path = path;
        this.context = new ArrayList<String>();
        this.reader = new Reader(source);
        this.reader.hooks = this;
        this.setNodeManager(new NodeManager(this.reader));
        this.expressionParser = this.createExpressionParser(this.reader, this.getNodeManager());
        this.exportScope = this.path != null ? new ExportScopeRef(this.path.pkg.name, this.path.path != null ? this.path.path.replaceAll(".ts$", "") : null) : null;
    }
    
    public TypeScriptParser2(String source) {
        this(source, null);
    }
    
    public ExpressionParser createExpressionParser(Reader reader, NodeManager nodeManager) {
        var expressionParser = new ExpressionParser(reader, this, nodeManager);
        expressionParser.stringLiteralType = new UnresolvedType("TsString", new IType[0]);
        expressionParser.numericLiteralType = new UnresolvedType("TsNumber", new IType[0]);
        return expressionParser;
    }
    
    public ExpressionParser createExpressionParser(Reader reader) {
        return this.createExpressionParser(reader, null);
    }
    
    public void errorCallback(ParseError error) {
        throw new Error("[TypeScriptParser] " + error.message + " at " + error.cursor.line + ":" + error.cursor.column + " (context: " + this.context.stream().collect(Collectors.joining("/")) + ")\n" + this.reader.linePreview(error.cursor));
    }
    
    public Expression infixPrehook(Expression left) {
        if (left instanceof PropertyAccessExpression && this.reader.peekRegex("<[A-Za-z0-9_<>]*?>\\(") != null) {
            var typeArgs = this.parseTypeArgs();
            this.reader.expectToken("(");
            var args = this.expressionParser.parseCallArguments();
            return new UnresolvedCallExpression(((PropertyAccessExpression)left), typeArgs, args);
        }
        else if (this.reader.readToken("instanceof")) {
            var type = this.parseType();
            return new InstanceOfExpression(left, type);
        }
        else if (left instanceof Identifier && this.reader.readToken("=>")) {
            var block = this.parseLambdaBlock();
            return new Lambda(new MethodParameter[] { new MethodParameter(((Identifier)left).text, null, null, null) }, block);
        }
        return null;
    }
    
    public MethodParameter[] parseLambdaParams() {
        if (!this.reader.readToken("("))
            return null;
        
        var params = new ArrayList<MethodParameter>();
        if (!this.reader.readToken(")")) {
            do {
                var paramName = this.reader.expectIdentifier();
                var type = this.reader.readToken(":") ? this.parseType() : null;
                params.add(new MethodParameter(paramName, type, null, null));
            } while (this.reader.readToken(","));
            this.reader.expectToken(")");
        }
        return params.toArray(MethodParameter[]::new);
    }
    
    public IType parseType() {
        if (this.reader.readToken("{")) {
            this.reader.expectToken("[");
            this.reader.readIdentifier();
            this.reader.expectToken(":");
            this.reader.expectToken("string");
            this.reader.expectToken("]");
            this.reader.expectToken(":");
            var mapValueType = this.parseType();
            this.reader.readToken(";");
            this.reader.expectToken("}");
            return new UnresolvedType("TsMap", new IType[] { mapValueType });
        }
        
        if (this.reader.peekToken("(")) {
            var params = this.parseLambdaParams();
            this.reader.expectToken("=>");
            var returnType = this.parseType();
            return new LambdaType(params, returnType);
        }
        
        var typeName = this.reader.expectIdentifier();
        var startPos = this.reader.prevTokenOffset;
        
        IType type;
        if (typeName.equals("string"))
            type = new UnresolvedType("TsString", new IType[0]);
        else if (typeName.equals("boolean"))
            type = new UnresolvedType("TsBoolean", new IType[0]);
        else if (typeName.equals("number"))
            type = new UnresolvedType("TsNumber", new IType[0]);
        else if (typeName.equals("any"))
            type = AnyType.instance;
        else if (typeName.equals("void"))
            type = VoidType.instance;
        else {
            var typeArguments = this.parseTypeArgs();
            type = new UnresolvedType(typeName, typeArguments);
        }
        
        this.getNodeManager().addNode(type, startPos);
        
        while (this.reader.readToken("[]")) {
            type = new UnresolvedType("TsArray", new IType[] { type });
            this.getNodeManager().addNode(type, startPos);
        }
        
        return type;
    }
    
    public Expression parseExpression() {
        return this.expressionParser.parse();
    }
    
    public Expression unaryPrehook() {
        if (this.reader.readToken("null"))
            return new NullLiteral();
        else if (this.reader.readToken("true"))
            return new BooleanLiteral(true);
        else if (this.reader.readToken("false"))
            return new BooleanLiteral(false);
        else if (this.reader.readToken("`")) {
            var parts = new ArrayList<TemplateStringPart>();
            var litPart = "";
            while (true) {
                if (this.reader.readExactly("`")) {
                    if (!litPart.equals("")) {
                        parts.add(TemplateStringPart.Literal(litPart));
                        litPart = "";
                    }
                    
                    break;
                }
                else if (this.reader.readExactly("${")) {
                    if (!litPart.equals("")) {
                        parts.add(TemplateStringPart.Literal(litPart));
                        litPart = "";
                    }
                    
                    var expr = this.parseExpression();
                    parts.add(TemplateStringPart.Expression(expr));
                    this.reader.expectToken("}");
                }
                else if (this.reader.readExactly("\\")) {
                    var chr = this.reader.readChar();
                    if (chr.equals("n"))
                        litPart += "\n";
                    else if (chr.equals("r"))
                        litPart += "\r";
                    else if (chr.equals("t"))
                        litPart += "\t";
                    else if (chr.equals("`"))
                        litPart += "`";
                    else if (chr.equals("$"))
                        litPart += "$";
                    else if (chr.equals("\\"))
                        litPart += "\\";
                    else
                        this.reader.fail("invalid escape", this.reader.offset - 1);
                }
                else {
                    var chr = this.reader.readChar();
                    var chrCode = (int)chr.charAt(0);
                    if (!(32 <= chrCode && chrCode <= 126) || chr.equals("`") || chr.equals("\\"))
                        this.reader.fail("not allowed character (code=" + chrCode + ")", this.reader.offset - 1);
                    litPart += chr;
                }
            }
            return new TemplateString(parts.toArray(TemplateStringPart[]::new));
        }
        else if (this.reader.readToken("new")) {
            var type = this.parseType();
            if (type instanceof UnresolvedType) {
                this.reader.expectToken("(");
                var args = this.expressionParser.parseCallArguments();
                return new UnresolvedNewExpression(((UnresolvedType)type), args);
            }
            else
                throw new Error("[TypeScriptParser2] Expected UnresolvedType here!");
        }
        else if (this.reader.readToken("<")) {
            var newType = this.parseType();
            this.reader.expectToken(">");
            var expression = this.parseExpression();
            return new CastExpression(newType, expression, null);
        }
        else if (this.reader.readToken("/")) {
            var pattern = "";
            while (true) {
                var chr = this.reader.readChar();
                if (chr.equals("\\")) {
                    var chr2 = this.reader.readChar();
                    pattern += chr2.equals("/") ? "/" : "\\" + chr2;
                }
                else if (chr.equals("/"))
                    break;
                else
                    pattern += chr;
            }
            var modifiers = this.reader.readModifiers(new String[] { "g", "i" });
            return new RegexLiteral(pattern, Arrays.stream(modifiers).anyMatch("i"::equals), Arrays.stream(modifiers).anyMatch("g"::equals));
        }
        else if (this.reader.readToken("typeof")) {
            var expr = this.expressionParser.parse(this.expressionParser.prefixPrecedence);
            this.reader.expectToken("===");
            var check = this.reader.expectString();
            
            String tsType = null;
            if (check.equals("string"))
                tsType = "TsString";
            else if (check.equals("boolean"))
                tsType = "TsBoolean";
            else if (check.equals("object"))
                tsType = "Object";
            else if (check.equals("function"))
                // TODO: ???
                tsType = "Function";
            else if (check.equals("undefined"))
                // TODO: ???
                tsType = "Object";
            else
                this.reader.fail("unexpected typeof comparison");
            
            return new InstanceOfExpression(expr, new UnresolvedType(tsType, new IType[0]));
        }
        else if (this.reader.peekRegex("\\([A-Za-z0-9_]+\\s*[:,]|\\(\\)") != null) {
            var params = this.parseLambdaParams();
            this.reader.expectToken("=>");
            var block = this.parseLambdaBlock();
            return new Lambda(params, block);
        }
        else if (this.reader.readToken("await")) {
            var expression = this.parseExpression();
            return new AwaitExpression(expression);
        }
        
        var mapLiteral = this.expressionParser.parseMapLiteral();
        if (mapLiteral != null)
            return mapLiteral;
        
        var arrayLiteral = this.expressionParser.parseArrayLiteral();
        if (arrayLiteral != null)
            return arrayLiteral;
        
        return null;
    }
    
    public Block parseLambdaBlock() {
        var block = this.parseBlock();
        if (block != null)
            return block;
        
        var returnExpr = this.parseExpression();
        if (returnExpr instanceof ParenthesizedExpression)
            returnExpr = ((ParenthesizedExpression)returnExpr).expression;
        return new Block(new ReturnStatement[] { new ReturnStatement(returnExpr) });
    }
    
    public TypeAndInit parseTypeAndInit() {
        var type = this.reader.readToken(":") ? this.parseType() : null;
        var init = this.reader.readToken("=") ? this.parseExpression() : null;
        
        if (type == null && init == null)
            this.reader.fail("expected type declaration or initializer");
        
        return new TypeAndInit(type, init);
    }
    
    public Block expectBlockOrStatement() {
        var block = this.parseBlock();
        if (block != null)
            return block;
        
        var stmts = new ArrayList<Statement>();
        var stmt = this.expectStatement();
        if (stmt != null)
            stmts.add(stmt);
        return new Block(stmts.toArray(Statement[]::new));
    }
    
    public Statement expectStatement() {
        Statement statement = null;
        
        var leadingTrivia = this.reader.readLeadingTrivia();
        var startPos = this.reader.offset;
        
        var requiresClosing = true;
        var varDeclMatches = this.reader.readRegex("(const|let|var)\\b");
        if (varDeclMatches != null) {
            var name = this.reader.expectIdentifier("expected variable name");
            var typeAndInit = this.parseTypeAndInit();
            statement = new VariableDeclaration(name, typeAndInit.type, typeAndInit.init);
        }
        else if (this.reader.readToken("delete"))
            statement = new UnsetStatement(this.parseExpression());
        else if (this.reader.readToken("if")) {
            requiresClosing = false;
            this.reader.expectToken("(");
            var condition = this.parseExpression();
            this.reader.expectToken(")");
            var then = this.expectBlockOrStatement();
            var else_ = this.reader.readToken("else") ? this.expectBlockOrStatement() : null;
            statement = new IfStatement(condition, then, else_);
        }
        else if (this.reader.readToken("while")) {
            requiresClosing = false;
            this.reader.expectToken("(");
            var condition = this.parseExpression();
            this.reader.expectToken(")");
            var body = this.expectBlockOrStatement();
            statement = new WhileStatement(condition, body);
        }
        else if (this.reader.readToken("do")) {
            requiresClosing = false;
            var body = this.expectBlockOrStatement();
            this.reader.expectToken("while");
            this.reader.expectToken("(");
            var condition = this.parseExpression();
            this.reader.expectToken(")");
            statement = new DoStatement(condition, body);
        }
        else if (this.reader.readToken("for")) {
            requiresClosing = false;
            this.reader.expectToken("(");
            var varDeclMod = this.reader.readAnyOf(new String[] { "const", "let", "var" });
            var itemVarName = varDeclMod == null ? null : this.reader.expectIdentifier();
            if (itemVarName != null && this.reader.readToken("of")) {
                var items = this.parseExpression();
                this.reader.expectToken(")");
                var body = this.expectBlockOrStatement();
                statement = new ForeachStatement(new ForeachVariable(itemVarName), items, body);
            }
            else {
                ForVariable forVar = null;
                if (itemVarName != null) {
                    var typeAndInit = this.parseTypeAndInit();
                    forVar = new ForVariable(itemVarName, typeAndInit.type, typeAndInit.init);
                }
                this.reader.expectToken(";");
                var condition = this.parseExpression();
                this.reader.expectToken(";");
                var incrementor = this.parseExpression();
                this.reader.expectToken(")");
                var body = this.expectBlockOrStatement();
                statement = new ForStatement(forVar, condition, incrementor, body);
            }
        }
        else if (this.reader.readToken("try")) {
            var block = this.expectBlock("try body is missing");
            
            CatchVariable catchVar = null;
            Block catchBody = null;
            if (this.reader.readToken("catch")) {
                this.reader.expectToken("(");
                catchVar = new CatchVariable(this.reader.expectIdentifier(), null);
                this.reader.expectToken(")");
                catchBody = this.expectBlock("catch body is missing");
            }
            
            var finallyBody = this.reader.readToken("finally") ? this.expectBlock() : null;
            return new TryStatement(block, catchVar, catchBody, finallyBody);
        }
        else if (this.reader.readToken("return")) {
            var expr = this.reader.peekToken(";") ? null : this.parseExpression();
            statement = new ReturnStatement(expr);
        }
        else if (this.reader.readToken("throw")) {
            var expr = this.parseExpression();
            statement = new ThrowStatement(expr);
        }
        else if (this.reader.readToken("break"))
            statement = new BreakStatement();
        else if (this.reader.readToken("continue"))
            statement = new ContinueStatement();
        else if (this.reader.readToken("debugger;"))
            return null;
        else {
            var expr = this.parseExpression();
            statement = new ExpressionStatement(expr);
            var isBinarySet = expr instanceof BinaryExpression && new ArrayList<>(List.of("=", "+=", "-=")).stream().anyMatch(((BinaryExpression)expr).operator::equals);
            var isUnarySet = expr instanceof UnaryExpression && new ArrayList<>(List.of("++", "--")).stream().anyMatch(((UnaryExpression)expr).operator::equals);
            if (!(expr instanceof UnresolvedCallExpression || isBinarySet || isUnarySet || expr instanceof AwaitExpression))
                this.reader.fail("this expression is not allowed as statement");
        }
        
        if (statement == null)
            this.reader.fail("unknown statement");
        
        statement.setLeadingTrivia(leadingTrivia);
        this.getNodeManager().addNode(statement, startPos);
        
        var statementLastLine = this.reader.wsLineCounter;
        if (!this.reader.readToken(";") && requiresClosing && this.reader.wsLineCounter == statementLastLine)
            this.reader.fail("statement is not closed", this.reader.wsOffset);
        
        return statement;
    }
    
    public Block parseBlock() {
        if (!this.reader.readToken("{"))
            return null;
        var startPos = this.reader.prevTokenOffset;
        
        var statements = new ArrayList<Statement>();
        if (!this.reader.readToken("}"))
            do {
                var statement = this.expectStatement();
                if (statement != null)
                    statements.add(statement);
            } while (!this.reader.readToken("}"));
        
        var block = new Block(statements.toArray(Statement[]::new));
        this.getNodeManager().addNode(block, startPos);
        return block;
    }
    
    public Block expectBlock(String errorMsg) {
        var block = this.parseBlock();
        if (block == null)
            this.reader.fail(errorMsg != null ? errorMsg : "expected block here");
        return block;
    }
    
    public Block expectBlock() {
        return this.expectBlock(null);
    }
    
    public IType[] parseTypeArgs() {
        var typeArguments = new ArrayList<IType>();
        if (this.reader.readToken("<")) {
            do {
                var generics = this.parseType();
                typeArguments.add(generics);
            } while (this.reader.readToken(","));
            this.reader.expectToken(">");
        }
        return typeArguments.toArray(IType[]::new);
    }
    
    public String[] parseGenericsArgs() {
        var typeArguments = new ArrayList<String>();
        if (this.reader.readToken("<")) {
            do {
                var generics = this.reader.expectIdentifier();
                typeArguments.add(generics);
            } while (this.reader.readToken(","));
            this.reader.expectToken(">");
        }
        return typeArguments.toArray(String[]::new);
    }
    
    public ExpressionStatement parseExprStmtFromString(String expression) {
        var expr = this.createExpressionParser(new Reader(expression)).parse();
        return new ExpressionStatement(expr);
    }
    
    public MethodSignature parseMethodSignature(Boolean isConstructor, Boolean declarationOnly) {
        var params = new ArrayList<MethodParameter>();
        var fields = new ArrayList<Field>();
        if (!this.reader.readToken(")")) {
            do {
                var leadingTrivia = this.reader.readLeadingTrivia();
                var paramStart = this.reader.offset;
                var isPublic = this.reader.readToken("public");
                if (isPublic && !isConstructor)
                    this.reader.fail("public modifier is only allowed in constructor definition");
                
                var paramName = this.reader.expectIdentifier();
                this.context.add("arg:" + paramName);
                var typeAndInit = this.parseTypeAndInit();
                var param = new MethodParameter(paramName, typeAndInit.type, typeAndInit.init, leadingTrivia);
                params.add(param);
                
                // init should be used as only the constructor's method parameter, but not again as a field initializer too
                //   (otherwise it would called twice if cloned or cause AST error is just referenced from two separate places)
                if (isPublic)
                    fields.add(new Field(paramName, typeAndInit.type, null, Visibility.Public, false, param, param.getLeadingTrivia()));
                
                this.getNodeManager().addNode(param, paramStart);
                this.context.remove(this.context.size() - 1);
            } while (this.reader.readToken(","));
            
            this.reader.expectToken(")");
        }
        
        IType returns = null;
        if (!isConstructor)
            // in case of constructor, "returns" won't be used
            returns = this.reader.readToken(":") ? this.parseType() : this.missingReturnTypeIsVoid ? VoidType.instance : null;
        
        Block body = null;
        Expression[] superCallArgs = null;
        if (declarationOnly)
            this.reader.expectToken(";");
        else {
            body = this.expectBlock("method body is missing");
            var firstStmt = body.statements.size() > 0 ? body.statements.get(0) : null;
            if (firstStmt instanceof ExpressionStatement && ((ExpressionStatement)firstStmt).expression instanceof UnresolvedCallExpression && ((UnresolvedCallExpression)((ExpressionStatement)firstStmt).expression).func instanceof Identifier && ((Identifier)((UnresolvedCallExpression)((ExpressionStatement)firstStmt).expression).func).text.equals("super")) {
                superCallArgs = ((UnresolvedCallExpression)((ExpressionStatement)firstStmt).expression).args;
                body.statements.remove(0);
            }
        }
        
        return new MethodSignature(params.toArray(MethodParameter[]::new), fields.toArray(Field[]::new), body, returns, superCallArgs);
    }
    
    public String parseIdentifierOrString() {
        return this.reader.readString() != null ? this.reader.readString() : this.reader.expectIdentifier();
    }
    
    public Interface parseInterface(String leadingTrivia, Boolean isExported) {
        if (!this.reader.readToken("interface"))
            return null;
        var intfStart = this.reader.prevTokenOffset;
        
        var intfName = this.reader.expectIdentifier("expected identifier after 'interface' keyword");
        this.context.add("I:" + intfName);
        
        var intfTypeArgs = this.parseGenericsArgs();
        
        var baseInterfaces = new ArrayList<IType>();
        if (this.reader.readToken("extends"))
            do
                baseInterfaces.add(this.parseType()); while (this.reader.readToken(","));
        
        var methods = new ArrayList<Method>();
        var fields = new ArrayList<Field>();
        
        this.reader.expectToken("{");
        while (!this.reader.readToken("}")) {
            var memberLeadingTrivia = this.reader.readLeadingTrivia();
            
            var memberStart = this.reader.offset;
            var memberName = this.parseIdentifierOrString();
            if (this.reader.readToken(":")) {
                this.context.add("F:" + memberName);
                
                var fieldType = this.parseType();
                this.reader.expectToken(";");
                
                var field = new Field(memberName, fieldType, null, Visibility.Public, false, null, memberLeadingTrivia);
                fields.add(field);
                
                this.getNodeManager().addNode(field, memberStart);
                this.context.remove(this.context.size() - 1);
            }
            else {
                this.context.add("M:" + memberName);
                var methodTypeArgs = this.parseGenericsArgs();
                this.reader.expectToken("(");
                // method
                   
                var sig = this.parseMethodSignature(false, true);
                
                var method = new Method(memberName, methodTypeArgs, sig.params, sig.body, Visibility.Public, false, sig.returns, false, memberLeadingTrivia);
                methods.add(method);
                this.getNodeManager().addNode(method, memberStart);
                this.context.remove(this.context.size() - 1);
            }
        }
        
        var intf = new Interface(intfName, intfTypeArgs, baseInterfaces.toArray(IType[]::new), fields.toArray(Field[]::new), methods.toArray(Method[]::new), isExported, leadingTrivia);
        this.getNodeManager().addNode(intf, intfStart);
        this.context.remove(this.context.size() - 1);
        return intf;
    }
    
    public UnresolvedType parseSpecifiedType() {
        var typeName = this.reader.readIdentifier();
        var typeArgs = this.parseTypeArgs();
        return new UnresolvedType(typeName, typeArgs);
    }
    
    public Class parseClass(String leadingTrivia, Boolean isExported, Boolean declarationOnly) {
        var clsModifiers = this.reader.readModifiers(new String[] { "abstract" });
        if (!this.reader.readToken("class"))
            return null;
        var clsStart = this.reader.prevTokenOffset;
        
        var clsName = this.reader.expectIdentifier("expected identifier after 'class' keyword");
        this.context.add("C:" + clsName);
        
        var typeArgs = this.parseGenericsArgs();
        var baseClass = this.reader.readToken("extends") ? this.parseSpecifiedType() : null;
        
        var baseInterfaces = new ArrayList<IType>();
        if (this.reader.readToken("implements"))
            do
                baseInterfaces.add(this.parseSpecifiedType()); while (this.reader.readToken(","));
        
        Constructor constructor = null;
        var fields = new ArrayList<Field>();
        var methods = new ArrayList<Method>();
        var properties = new ArrayList<Property>();
        
        this.reader.expectToken("{");
        while (!this.reader.readToken("}")) {
            var memberLeadingTrivia = this.reader.readLeadingTrivia();
            
            var memberStart = this.reader.offset;
            var modifiers = this.reader.readModifiers(new String[] { "static", "public", "protected", "private", "readonly", "async" });
            var isStatic = Arrays.stream(modifiers).anyMatch("static"::equals);
            var isAsync = Arrays.stream(modifiers).anyMatch("async"::equals);
            var visibility = Arrays.stream(modifiers).anyMatch("private"::equals) ? Visibility.Private : Arrays.stream(modifiers).anyMatch("protected"::equals) ? Visibility.Protected : Visibility.Public;
            
            var memberName = this.parseIdentifierOrString();
            var methodTypeArgs = this.parseGenericsArgs();
            if (this.reader.readToken("(")) {
                // method
                var isConstructor = memberName.equals("constructor");
                
                IMethodBase member;
                var sig = this.parseMethodSignature(isConstructor, declarationOnly);
                if (isConstructor) {
                    member = constructor = new Constructor(sig.params, sig.body, sig.superCallArgs, memberLeadingTrivia);
                    for (var field : sig.fields)
                        fields.add(field);
                }
                else {
                    var method = new Method(memberName, methodTypeArgs, sig.params, sig.body, visibility, isStatic, sig.returns, isAsync, memberLeadingTrivia);
                    methods.add(method);
                    member = method;
                }
                
                this.getNodeManager().addNode(member, memberStart);
            }
            else if (memberName.equals("get") || memberName.equals("set")) {
                // property
                var propName = this.reader.expectIdentifier();
                var prop = properties.stream().filter(x -> x.getName().equals(propName)).findFirst().orElse(null);
                IType propType = null;
                Block getter = null;
                Block setter = null;
                
                if (memberName.equals("get")) {
                    // get propName(): propType { return ... }
                    this.context.add("P[G]:" + propName);
                    this.reader.expectToken("()", "expected '()' after property getter name");
                    propType = this.reader.readToken(":") ? this.parseType() : null;
                    if (declarationOnly) {
                        if (propType == null)
                            this.reader.fail("Type is missing for property in declare class");
                        this.reader.expectToken(";");
                    }
                    else {
                        getter = this.expectBlock("property getter body is missing");
                        if (prop != null)
                            prop.getter = getter;
                    }
                }
                else if (memberName.equals("set")) {
                    // set propName(value: propType) { ... }
                    this.context.add("P[S]:" + propName);
                    this.reader.expectToken("(", "expected '(' after property setter name");
                    this.reader.expectIdentifier();
                    propType = this.reader.readToken(":") ? this.parseType() : null;
                    this.reader.expectToken(")");
                    if (declarationOnly) {
                        if (propType == null)
                            this.reader.fail("Type is missing for property in declare class");
                        this.reader.expectToken(";");
                    }
                    else {
                        setter = this.expectBlock("property setter body is missing");
                        if (prop != null)
                            prop.setter = setter;
                    }
                }
                
                if (prop == null) {
                    prop = new Property(propName, propType, getter, setter, visibility, isStatic, memberLeadingTrivia);
                    properties.add(prop);
                    this.getNodeManager().addNode(prop, memberStart);
                }
                
                this.context.remove(this.context.size() - 1);
            }
            else {
                this.context.add("F:" + memberName);
                
                var typeAndInit = this.parseTypeAndInit();
                this.reader.expectToken(";");
                
                var field = new Field(memberName, typeAndInit.type, typeAndInit.init, visibility, isStatic, null, memberLeadingTrivia);
                fields.add(field);
                
                this.getNodeManager().addNode(field, memberStart);
                this.context.remove(this.context.size() - 1);
            }
        }
        
        var cls = new Class(clsName, typeArgs, baseClass, baseInterfaces.toArray(IType[]::new), fields.toArray(Field[]::new), properties.toArray(Property[]::new), constructor, methods.toArray(Method[]::new), isExported, leadingTrivia);
        this.getNodeManager().addNode(cls, clsStart);
        this.context.remove(this.context.size() - 1);
        return cls;
    }
    
    public Enum parseEnum(String leadingTrivia, Boolean isExported) {
        if (!this.reader.readToken("enum"))
            return null;
        var enumStart = this.reader.prevTokenOffset;
        
        var name = this.reader.expectIdentifier("expected identifier after 'enum' keyword");
        this.context.add("E:" + name);
        
        var members = new ArrayList<EnumMember>();
        
        this.reader.expectToken("{");
        if (!this.reader.readToken("}")) {
            do {
                if (this.reader.peekToken("}"))
                    break;
                // eg. "enum { A, B, }" (but multiline)
                
                var enumMember = new EnumMember(this.reader.expectIdentifier());
                members.add(enumMember);
                this.getNodeManager().addNode(enumMember, this.reader.prevTokenOffset);
                
                // TODO: generated code compatibility
                this.reader.readToken("= \"" + enumMember.name + "\"");
            } while (this.reader.readToken(","));
            this.reader.expectToken("}");
        }
        
        var enumObj = new Enum(name, members.toArray(EnumMember[]::new), isExported, leadingTrivia);
        this.getNodeManager().addNode(enumObj, enumStart);
        this.context.remove(this.context.size() - 1);
        return enumObj;
    }
    
    public static String calculateRelativePath(String currFile, String relPath) {
        if (!relPath.startsWith("."))
            throw new Error("relPath must start with '.', but got '" + relPath + "'");
        
        var curr = new ArrayList<>(Arrays.asList(currFile.split("/", -1)));
        curr.remove(curr.size() - 1);
        // filename does not matter
        for (var part : relPath.split("/", -1)) {
            if (part.equals(""))
                throw new Error("relPath should not contain multiple '/' next to each other (relPath='" + relPath + "')");
            if (part.equals("."))
                // "./" == stay in current directory
                continue;
            else if (part.equals("..")) {
                // "../" == parent directory
                if (curr.size() == 0)
                    throw new Error("relPath goes out of root (curr='" + currFile + "', relPath='" + relPath + "')");
                curr.remove(curr.size() - 1);
            }
            else
                curr.add(part);
        }
        return curr.stream().collect(Collectors.joining("/"));
    }
    
    public static ExportScopeRef calculateImportScope(ExportScopeRef currScope, String importFile) {
        if (importFile.startsWith("."))
            // relative
            return new ExportScopeRef(currScope.packageName, TypeScriptParser2.calculateRelativePath(currScope.scopeName, importFile));
        else {
            var path = new ArrayList<>(Arrays.asList(importFile.split("/", -1)));
            var pkgName = path.remove(0);
            return new ExportScopeRef(pkgName, path.size() == 0 ? Package.INDEX : path.stream().collect(Collectors.joining("/")));
        }
    }
    
    public String readIdentifier() {
        var rawId = this.reader.readIdentifier();
        return rawId.replaceAll("_+$", "");
    }
    
    public Import[] parseImport(String leadingTrivia) {
        if (!this.reader.readToken("import"))
            return null;
        var importStart = this.reader.prevTokenOffset;
        
        String importAllAlias = null;
        var importParts = new ArrayList<UnresolvedImport>();
        
        if (this.reader.readToken("*")) {
            this.reader.expectToken("as");
            importAllAlias = this.reader.expectIdentifier();
        }
        else {
            this.reader.expectToken("{");
            do {
                if (this.reader.peekToken("}"))
                    break;
                
                var imp = this.reader.expectIdentifier();
                if (this.reader.readToken("as"))
                    this.reader.fail("This is not yet supported");
                importParts.add(new UnresolvedImport(imp));
                this.getNodeManager().addNode(imp, this.reader.prevTokenOffset);
            } while (this.reader.readToken(","));
            this.reader.expectToken("}");
        }
        
        this.reader.expectToken("from");
        var moduleName = this.reader.expectString();
        this.reader.expectToken(";");
        
        var importScope = this.exportScope != null ? TypeScriptParser2.calculateImportScope(this.exportScope, moduleName) : null;
        
        var imports = new ArrayList<Import>();
        if (importParts.size() > 0)
            imports.add(new Import(importScope, false, importParts.toArray(UnresolvedImport[]::new), null, leadingTrivia));
        
        if (importAllAlias != null)
            imports.add(new Import(importScope, true, null, importAllAlias, leadingTrivia));
        //this.nodeManager.addNode(imports, importStart);
        return imports.toArray(Import[]::new);
    }
    
    public SourceFile parseSourceFile() {
        var imports = new ArrayList<Import>();
        var enums = new ArrayList<Enum>();
        var intfs = new ArrayList<Interface>();
        var classes = new ArrayList<Class>();
        var funcs = new ArrayList<GlobalFunction>();
        while (true) {
            var leadingTrivia = this.reader.readLeadingTrivia();
            if (this.reader.getEof())
                break;
            
            var imps = this.parseImport(leadingTrivia);
            if (imps != null) {
                for (var imp : imps)
                    imports.add(imp);
                continue;
            }
            
            var modifiers = this.reader.readModifiers(new String[] { "export", "declare" });
            var isExported = Arrays.stream(modifiers).anyMatch("export"::equals);
            var isDeclaration = Arrays.stream(modifiers).anyMatch("declare"::equals);
            
            var cls = this.parseClass(leadingTrivia, isExported, isDeclaration);
            if (cls != null) {
                classes.add(cls);
                continue;
            }
            
            var enumObj = this.parseEnum(leadingTrivia, isExported);
            if (enumObj != null) {
                enums.add(enumObj);
                continue;
            }
            
            var intf = this.parseInterface(leadingTrivia, isExported);
            if (intf != null) {
                intfs.add(intf);
                continue;
            }
            
            if (this.reader.readToken("function")) {
                var funcName = this.readIdentifier();
                this.reader.expectToken("(");
                var sig = this.parseMethodSignature(false, isDeclaration);
                funcs.add(new GlobalFunction(funcName, sig.params, sig.body, sig.returns, isExported, leadingTrivia));
                continue;
            }
            
            break;
        }
        
        this.reader.skipWhitespace();
        
        var stmts = new ArrayList<Statement>();
        while (true) {
            var leadingTrivia = this.reader.readLeadingTrivia();
            if (this.reader.getEof())
                break;
            
            var stmt = this.expectStatement();
            if (stmt == null)
                continue;
            
            stmt.setLeadingTrivia(leadingTrivia);
            stmts.add(stmt);
        }
        
        return new SourceFile(imports.toArray(Import[]::new), intfs.toArray(Interface[]::new), classes.toArray(Class[]::new), enums.toArray(Enum[]::new), funcs.toArray(GlobalFunction[]::new), new Block(stmts.toArray(Statement[]::new)), this.path, this.exportScope);
    }
    
    public SourceFile parse() {
        return this.parseSourceFile();
    }
    
    public static SourceFile parseFile(String source, SourcePath path) {
        return new TypeScriptParser2(source, path).parseSourceFile();
    }
    
    public static SourceFile parseFile(String source) {
        return TypeScriptParser2.parseFile(source, null);
    }
}