using System.Collections.Generic;
using System.Threading.Tasks;
using System;
using Parsers.Common;
using One.Ast;

namespace Parsers.Common
{
    public interface IExpressionParserHooks {
        Expression unaryPrehook();
        
        Expression infixPrehook(Expression left);
    }
    
    public class Operator {
        public string text;
        public int precedence;
        public bool isBinary;
        public bool isRightAssoc;
        public bool isPostfix;
        
        public Operator(string text, int precedence, bool isBinary, bool isRightAssoc, bool isPostfix) {
            this.text = text;
            this.precedence = precedence;
            this.isBinary = isBinary;
            this.isRightAssoc = isRightAssoc;
            this.isPostfix = isPostfix;
        }
    }
    
    public class PrecedenceLevel {
        public string name;
        public string[] operators;
        public bool binary;
        
        public PrecedenceLevel(string name, string[] operators, bool binary) {
            this.name = name;
            this.operators = operators;
            this.binary = binary;
        }
    }
    
    public class ExpressionParserConfig {
        public string[] unary;
        public PrecedenceLevel[] precedenceLevels;
        public string[] rightAssoc;
        public Dictionary<string, string> aliases;
        public string[] propertyAccessOps;
    }
    
    public class ExpressionParser {
        public Dictionary<string, Operator> operatorMap;
        public string[] operators;
        public int prefixPrecedence;
        public Type_ stringLiteralType;
        public Type_ numericLiteralType;
        public Reader reader;
        public IExpressionParserHooks hooks;
        public NodeManager nodeManager;
        public ExpressionParserConfig config;
        
        public ExpressionParser(Reader reader, IExpressionParserHooks hooks = null, NodeManager nodeManager = null, ExpressionParserConfig config = null) {
            this.stringLiteralType = null;
            this.numericLiteralType = null;
            this.reader = reader;
            this.hooks = hooks;
            this.nodeManager = nodeManager;
            this.config = config;
            if (this.config == null)
                this.config = ExpressionParser.defaultConfig();
            this.reconfigure();
        }
        
        static public ExpressionParserConfig defaultConfig() {
            var config = new ExpressionParserConfig();
            config.unary = new[] { "++", "--", "!", "not", "+", "-", "~" };
            config.precedenceLevels = new[] { new PrecedenceLevel("assignment", new[] { "=", "+=", "-=", "*=", "/=", "<<=", ">>=" }, true), new PrecedenceLevel("conditional", new[] { "?" }, false), new PrecedenceLevel("or", new[] { "||", "or" }, true), new PrecedenceLevel("and", new[] { "&&", "and" }, true), new PrecedenceLevel("comparison", new[] { ">=", "!=", "===", "!==", "==", "<=", ">", "<" }, true), new PrecedenceLevel("sum", new[] { "+", "-" }, true), new PrecedenceLevel("product", new[] { "*", "/", "%" }, true), new PrecedenceLevel("bitwise", new[] { "|", "&", "^" }, true), new PrecedenceLevel("exponent", new[] { "**" }, true), new PrecedenceLevel("shift", new[] { "<<", ">>" }, true), new PrecedenceLevel("range", new[] { "..." }, true), new PrecedenceLevel("in", new[] { "in" }, true), new PrecedenceLevel("prefix", new string[0], false), new PrecedenceLevel("postfix", new[] { "++", "--" }, false), new PrecedenceLevel("call", new[] { "(" }, false), new PrecedenceLevel("propertyAccess", new string[0], false), new PrecedenceLevel("elementAccess", new[] { "[" }, false) };
            config.rightAssoc = new[] { "**" };
            config.aliases = new Dictionary<string, string> {
                ["==="] = "==",
                ["!=="] = "!=",
                ["not"] = "!",
                ["and"] = "&&",
                ["or"] = "||"
            };
            config.propertyAccessOps = new[] { ".", "::" };
            return config;
        }
        
        public void reconfigure() {
            this.config.precedenceLevels.find((PrecedenceLevel x) => { return x.name == "propertyAccess"; }).operators = this.config.propertyAccessOps;
            
            this.operatorMap = new Dictionary<string, Operator> {};
            
            for (int i = 0; i < this.config.precedenceLevels.length(); i++) {
                var level = this.config.precedenceLevels.get(i);
                var precedence = i + 1;
                if (level.name == "prefix")
                    this.prefixPrecedence = precedence;
                
                if (level.operators == null)
                    continue;
                
                foreach (var opText in level.operators) {
                    var op = new Operator(opText, precedence, level.binary, this.config.rightAssoc.includes(opText), level.name == "postfix");
                    
                    this.operatorMap.set(opText, op);
                }
            }
            
            this.operators = Object.keys(this.operatorMap).sort((string a, string b) => { return b.length() - a.length(); });
        }
        
        public MapLiteral parseMapLiteral(string keySeparator = ":", string startToken = "{", string endToken = "}") {
            if (!this.reader.readToken(startToken))
                return null;
            
            var items = new MapLiteralItem[0];
            do {
                if (this.reader.peekToken(endToken))
                    break;
                
                var name = this.reader.readString();
                if (name == null)
                    name = this.reader.expectIdentifier("expected string or identifier as map key");
                
                this.reader.expectToken(keySeparator);
                var initializer = this.parse();
                items.push(new MapLiteralItem(name, initializer));
            } while (this.reader.readToken(","));
            
            this.reader.expectToken(endToken);
            return new MapLiteral(items);
        }
        
        public ArrayLiteral parseArrayLiteral(string startToken = "[", string endToken = "]") {
            if (!this.reader.readToken(startToken))
                return null;
            
            var items = new Expression[0];
            if (!this.reader.readToken(endToken)) {
                do {
                    var item = this.parse();
                    items.push(item);
                } while (this.reader.readToken(","));
                
                this.reader.expectToken(endToken);
            }
            return new ArrayLiteral(items);
        }
        
        public Expression parseLeft(bool required = true) {
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
                this.reader.fail($"unknown (literal / unary) token in expression");
            
            return null;
        }
        
        public Operator parseOperator() {
            foreach (var opText in this.operators)
                if (this.reader.peekToken(opText))
                    return this.operatorMap.get(opText);
            
            return null;
        }
        
        public Expression[] parseCallArguments() {
            var args = new Expression[0];
            
            if (!this.reader.readToken(")")) {
                do {
                    var arg = this.parse();
                    args.push(arg);
                } while (this.reader.readToken(","));
                
                this.reader.expectToken(")");
            }
            
            return args;
        }
        
        public void addNode(object node, int start) {
            if (this.nodeManager != null)
                this.nodeManager.addNode(node, start);
        }
        
        public Expression parse(int precedence = 0, bool required = true) {
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
                var opText = this.config.aliases.hasKey(op.text) ? this.config.aliases.get(op.text) : op.text;
                
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
                    left = new UnresolvedCallExpression(left, new Type_[0], args);
                }
                else if (op.text == "[") {
                    var elementExpr = this.parse();
                    this.reader.expectToken("]");
                    left = new ElementAccessExpression(left, elementExpr);
                }
                else if (this.config.propertyAccessOps.includes(op.text)) {
                    var prop = this.reader.expectIdentifier("expected identifier as property name");
                    left = new PropertyAccessExpression(left, prop);
                }
                else
                    this.reader.fail($"parsing '{op.text}' is not yet implemented");
                
                this.addNode(left, leftStart);
            }
            
            if (left is ParenthesizedExpression && ((ParenthesizedExpression)left).expression is Identifier) {
                var expr = this.parse(0, false);
                if (expr != null)
                    return new CastExpression(new UnresolvedType(((Identifier)((ParenthesizedExpression)left).expression).text, new Type_[0]), expr);
            }
            
            return left;
        }
    }
}