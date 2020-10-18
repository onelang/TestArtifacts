import java.util.Map;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;

public class ExpressionParser {
    public Map<String, Operator> operatorMap;
    public String[] operators;
    public Integer prefixPrecedence;
    public IType stringLiteralType;
    public IType numericLiteralType;
    public Reader reader;
    public IExpressionParserHooks hooks;
    public NodeManager nodeManager;
    public ExpressionParserConfig config;
    
    public ExpressionParser(Reader reader, IExpressionParserHooks hooks, NodeManager nodeManager, ExpressionParserConfig config)
    {
        this.reader = reader;
        this.hooks = hooks;
        this.nodeManager = nodeManager;
        this.config = config;
        this.stringLiteralType = null;
        this.numericLiteralType = null;
        if (this.config == null)
            this.config = ExpressionParser.defaultConfig();
        this.reconfigure();
    }
    
    public ExpressionParser(Reader reader, IExpressionParserHooks hooks, NodeManager nodeManager) {
        this(reader, hooks, nodeManager, null);
    }
    
    public ExpressionParser(Reader reader, IExpressionParserHooks hooks) {
        this(reader, hooks, null, null);
    }
    
    public ExpressionParser(Reader reader) {
        this(reader, null, null, null);
    }
    
    public static ExpressionParserConfig defaultConfig()
    {
        var config = new ExpressionParserConfig();
        config.unary = new String[] { "++", "--", "!", "not", "+", "-", "~" };
        config.precedenceLevels = new PrecedenceLevel[] { new PrecedenceLevel("assignment", new String[] { "=", "+=", "-=", "*=", "/=", "<<=", ">>=" }, true), new PrecedenceLevel("conditional", new String[] { "?" }, false), new PrecedenceLevel("or", new String[] { "||", "or" }, true), new PrecedenceLevel("and", new String[] { "&&", "and" }, true), new PrecedenceLevel("comparison", new String[] { ">=", "!=", "===", "!==", "==", "<=", ">", "<" }, true), new PrecedenceLevel("sum", new String[] { "+", "-" }, true), new PrecedenceLevel("product", new String[] { "*", "/", "%" }, true), new PrecedenceLevel("bitwise", new String[] { "|", "&", "^" }, true), new PrecedenceLevel("exponent", new String[] { "**" }, true), new PrecedenceLevel("shift", new String[] { "<<", ">>" }, true), new PrecedenceLevel("range", new String[] { "..." }, true), new PrecedenceLevel("in", new String[] { "in" }, true), new PrecedenceLevel("prefix", new String[0], false), new PrecedenceLevel("postfix", new String[] { "++", "--" }, false), new PrecedenceLevel("call", new String[] { "(" }, false), new PrecedenceLevel("propertyAccess", new String[0], false), new PrecedenceLevel("elementAccess", new String[] { "[" }, false) };
        config.rightAssoc = new String[] { "**" };
        config.aliases = Map.of("===", "==", "!==", "!=", "not", "!", "and", "&&", "or", "||");
        config.propertyAccessOps = new String[] { ".", "::" };
        return config;
    }
    
    public void reconfigure()
    {
        Arrays.stream(this.config.precedenceLevels).filter(x -> x.name == "propertyAccess").findFirst().orElse(null).operators = this.config.propertyAccessOps;
        
        this.operatorMap = new HashMap<String, Operator>();
        
        for (Integer i = 0; i < this.config.precedenceLevels.length; i++) {
            var level = this.config.precedenceLevels[i];
            var precedence = i + 1;
            if (level.name == "prefix")
                this.prefixPrecedence = precedence;
            
            if (level.operators == null)
                continue;
            
            for (var opText : level.operators) {
                var op = new Operator(opText, precedence, level.binary, Arrays.stream(this.config.rightAssoc).anyMatch(opText::equals), level.name == "postfix");
                
                this.operatorMap.put(opText, op);
            }
        }
        
        this.operators = ArrayHelper.sortBy(this.operatorMap.keySet().toArray(String[]::new), x -> -x.length());
    }
    
    public MapLiteral parseMapLiteral(String keySeparator, String startToken, String endToken)
    {
        if (!this.reader.readToken(startToken))
            return null;
        
        var items = new ArrayList<MapLiteralItem>();
        do {
            if (this.reader.peekToken(endToken))
                break;
            
            var name = this.reader.readString();
            if (name == null)
                name = this.reader.expectIdentifier("expected string or identifier as map key");
            
            this.reader.expectToken(keySeparator);
            var initializer = this.parse();
            items.add(new MapLiteralItem(name, initializer));
        } while (this.reader.readToken(","));
        
        this.reader.expectToken(endToken);
        return new MapLiteral(items.toArray(MapLiteralItem[]::new));
    }
    
    public MapLiteral parseMapLiteral(String keySeparator, String startToken) {
        return this.parseMapLiteral(keySeparator, startToken, "}");
    }
    
    public MapLiteral parseMapLiteral(String keySeparator) {
        return this.parseMapLiteral(keySeparator, "{", "}");
    }
    
    public MapLiteral parseMapLiteral() {
        return this.parseMapLiteral(":", "{", "}");
    }
    
    public ArrayLiteral parseArrayLiteral(String startToken, String endToken)
    {
        if (!this.reader.readToken(startToken))
            return null;
        
        var items = new ArrayList<Expression>();
        if (!this.reader.readToken(endToken)) {
            do {
                var item = this.parse();
                items.add(item);
            } while (this.reader.readToken(","));
            
            this.reader.expectToken(endToken);
        }
        return new ArrayLiteral(items.toArray(Expression[]::new));
    }
    
    public ArrayLiteral parseArrayLiteral(String startToken) {
        return this.parseArrayLiteral(startToken, "]");
    }
    
    public ArrayLiteral parseArrayLiteral() {
        return this.parseArrayLiteral("[", "]");
    }
    
    public Expression parseLeft(Boolean required)
    {
        var result = this.hooks != null ? this.hooks.unaryPrehook() : null;
        if (result != null)
            return result;
        
        var unary = this.reader.readAnyOf(this.config.unary);
        if (unary != null) {
            var right = this.parse(this.prefixPrecedence);
            return new UnaryExpression(UnaryType.Prefix, unary, right);
        }
        
        var id = this.reader.readIdentifier();
        if (id != null)
            return new Identifier(id);
        
        var num = this.reader.readNumber();
        if (num != null)
            return new NumericLiteral(num);
        
        var str = this.reader.readString();
        if (str != null)
            return new StringLiteral(str);
        
        if (this.reader.readToken("(")) {
            var expr = this.parse();
            this.reader.expectToken(")");
            return new ParenthesizedExpression(expr);
        }
        
        if (required)
            this.reader.fail("unknown (literal / unary) token in expression");
        
        return null;
    }
    
    public Expression parseLeft() {
        return this.parseLeft(true);
    }
    
    public Operator parseOperator()
    {
        for (var opText : this.operators) {
            if (this.reader.peekToken(opText))
                return this.operatorMap.get(opText);
        }
        
        return null;
    }
    
    public Expression[] parseCallArguments()
    {
        var args = new ArrayList<Expression>();
        
        if (!this.reader.readToken(")")) {
            do {
                var arg = this.parse();
                args.add(arg);
            } while (this.reader.readToken(","));
            
            this.reader.expectToken(")");
        }
        
        return args.toArray(Expression[]::new);
    }
    
    public void addNode(Object node, Integer start)
    {
        if (this.nodeManager != null)
            this.nodeManager.addNode(node, start);
    }
    
    public Expression parse(Integer precedence, Boolean required)
    {
        this.reader.skipWhitespace();
        var leftStart = this.reader.offset;
        var left = this.parseLeft(required);
        if (left == null)
            return null;
        this.addNode(left, leftStart);
        
        while (true) {
            if (this.hooks != null) {
                var parsed = this.hooks.infixPrehook(left);
                if (parsed != null) {
                    left = parsed;
                    this.addNode(left, leftStart);
                    continue;
                }
            }
            
            var op = this.parseOperator();
            if (op == null || op.precedence <= precedence)
                break;
            this.reader.expectToken(op.text);
            var opText = this.config.aliases.containsKey(op.text) ? this.config.aliases.get(op.text) : op.text;
            
            if (op.isBinary) {
                var right = this.parse(op.isRightAssoc ? op.precedence - 1 : op.precedence);
                left = new BinaryExpression(left, opText, right);
            }
            else if (op.isPostfix)
                left = new UnaryExpression(UnaryType.Postfix, opText, left);
            else if (op.text == "?") {
                var whenTrue = this.parse();
                this.reader.expectToken(":");
                var whenFalse = this.parse(op.precedence - 1);
                left = new ConditionalExpression(left, whenTrue, whenFalse);
            }
            else if (op.text == "(") {
                var args = this.parseCallArguments();
                left = new UnresolvedCallExpression(left, new IType[0], args);
            }
            else if (op.text == "[") {
                var elementExpr = this.parse();
                this.reader.expectToken("]");
                left = new ElementAccessExpression(left, elementExpr);
            }
            else if (Arrays.stream(this.config.propertyAccessOps).anyMatch(op.text::equals)) {
                var prop = this.reader.expectIdentifier("expected identifier as property name");
                left = new PropertyAccessExpression(left, prop);
            }
            else
                this.reader.fail("parsing '" + op.text + "' is not yet implemented");
            
            this.addNode(left, leftStart);
        }
        
        if (left instanceof ParenthesizedExpression && ((ParenthesizedExpression)left).expression instanceof Identifier) {
            var expr = this.parse(0, false);
            if (expr != null)
                return new CastExpression(new UnresolvedType(((Identifier)((ParenthesizedExpression)left).expression).text, new IType[0]), expr, null);
        }
        
        return left;
    }
    
    public Expression parse(Integer precedence) {
        return this.parse(precedence, true);
    }
    
    public Expression parse() {
        return this.parse(0, true);
    }
}