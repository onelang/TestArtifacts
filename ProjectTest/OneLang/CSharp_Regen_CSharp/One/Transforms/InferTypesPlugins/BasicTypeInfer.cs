using System.Collections.Generic;
using One.Transforms.InferTypesPlugins.Helpers;
using One.Ast;

namespace One.Transforms.InferTypesPlugins
{
    public class BasicTypeInfer : InferTypesPlugin {
        public BasicTypeInfer(): base("BasicTypeInfer")
        {
            
        }
        
        public override bool canDetectType(Expression expr) {
            return true;
        }
        
        public override bool detectType(Expression expr) {
            var litTypes = this.main.currentFile.literalTypes;
            
            if (expr is CastExpression)
                ((CastExpression)expr).setActualType(((CastExpression)expr).newType);
            else if (expr is ParenthesizedExpression)
                ((ParenthesizedExpression)expr).setActualType(((ParenthesizedExpression)expr).expression.getType());
            else if (expr is ThisReference)
                ((ThisReference)expr).setActualType(((ThisReference)expr).cls.type, false, false);
            else if (expr is SuperReference)
                ((SuperReference)expr).setActualType(((SuperReference)expr).cls.type, false, false);
            else if (expr is MethodParameterReference)
                ((MethodParameterReference)expr).setActualType(((MethodParameterReference)expr).decl.type, false, false);
            else if (expr is BooleanLiteral)
                ((BooleanLiteral)expr).setActualType(litTypes.boolean);
            else if (expr is NumericLiteral)
                ((NumericLiteral)expr).setActualType(litTypes.numeric);
            else if (expr is StringLiteral || expr is TemplateString)
                expr.setActualType(litTypes.string_);
            else if (expr is RegexLiteral)
                ((RegexLiteral)expr).setActualType(litTypes.regex);
            else if (expr is InstanceOfExpression)
                ((InstanceOfExpression)expr).setActualType(litTypes.boolean);
            else if (expr is NullLiteral)
                ((NullLiteral)expr).setActualType(((NullLiteral)expr).expectedType != null ? ((NullLiteral)expr).expectedType : NullType.instance);
            else if (expr is VariableDeclarationReference)
                ((VariableDeclarationReference)expr).setActualType(((VariableDeclarationReference)expr).decl.type);
            else if (expr is ForeachVariableReference)
                ((ForeachVariableReference)expr).setActualType(((ForeachVariableReference)expr).decl.type);
            else if (expr is ForVariableReference)
                ((ForVariableReference)expr).setActualType(((ForVariableReference)expr).decl.type);
            else if (expr is CatchVariableReference)
                ((CatchVariableReference)expr).setActualType(((CatchVariableReference)expr).decl.type ?? this.main.currentFile.literalTypes.error);
            else if (expr is UnaryExpression) {
                var operandType = ((UnaryExpression)expr).operand.getType();
                if (operandType is ClassType) {
                    var opId = $"{((UnaryExpression)expr).operator_}{((ClassType)operandType).decl.name}";
                    
                    if (opId == "-TsNumber")
                        ((UnaryExpression)expr).setActualType(litTypes.numeric);
                    else if (opId == "+TsNumber")
                        ((UnaryExpression)expr).setActualType(litTypes.numeric);
                    else if (opId == "!TsBoolean")
                        ((UnaryExpression)expr).setActualType(litTypes.boolean);
                    else if (opId == "++TsNumber")
                        ((UnaryExpression)expr).setActualType(litTypes.numeric);
                    else if (opId == "--TsNumber")
                        ((UnaryExpression)expr).setActualType(litTypes.numeric);
                    else { }
                }
                else if (operandType is AnyType)
                    ((UnaryExpression)expr).setActualType(AnyType.instance);
                else { }
            }
            else if (expr is BinaryExpression) {
                var leftType = ((BinaryExpression)expr).left.getType();
                var rightType = ((BinaryExpression)expr).right.getType();
                var isEqOrNeq = ((BinaryExpression)expr).operator_ == "==" || ((BinaryExpression)expr).operator_ == "!=";
                if (((BinaryExpression)expr).operator_ == "=") {
                    if (Type_.isAssignableTo(rightType, leftType))
                        ((BinaryExpression)expr).setActualType(leftType, false, true);
                    else
                        throw new Error($"Right-side expression ({rightType.repr()}) is not assignable to left-side ({leftType.repr()}).");
                }
                else if (isEqOrNeq)
                    ((BinaryExpression)expr).setActualType(litTypes.boolean);
                else if (leftType is ClassType && rightType is ClassType) {
                    if (((ClassType)leftType).decl == litTypes.numeric.decl && ((ClassType)rightType).decl == litTypes.numeric.decl && new List<string> { "-", "+", "-=", "+=", "%", "/" }.includes(((BinaryExpression)expr).operator_))
                        ((BinaryExpression)expr).setActualType(litTypes.numeric);
                    else if (((ClassType)leftType).decl == litTypes.numeric.decl && ((ClassType)rightType).decl == litTypes.numeric.decl && new List<string> { "<", "<=", ">", ">=" }.includes(((BinaryExpression)expr).operator_))
                        ((BinaryExpression)expr).setActualType(litTypes.boolean);
                    else if (((ClassType)leftType).decl == litTypes.string_.decl && ((ClassType)rightType).decl == litTypes.string_.decl && new List<string> { "+", "+=" }.includes(((BinaryExpression)expr).operator_))
                        ((BinaryExpression)expr).setActualType(litTypes.string_);
                    else if (((ClassType)leftType).decl == litTypes.string_.decl && ((ClassType)rightType).decl == litTypes.string_.decl && new List<string> { "<=" }.includes(((BinaryExpression)expr).operator_))
                        ((BinaryExpression)expr).setActualType(litTypes.boolean);
                    else if (((ClassType)leftType).decl == litTypes.boolean.decl && ((ClassType)rightType).decl == litTypes.boolean.decl && new List<string> { "||", "&&" }.includes(((BinaryExpression)expr).operator_))
                        ((BinaryExpression)expr).setActualType(litTypes.boolean);
                    else if (((ClassType)leftType).decl == litTypes.string_.decl && ((ClassType)rightType).decl == litTypes.map.decl && ((BinaryExpression)expr).operator_ == "in")
                        ((BinaryExpression)expr).setActualType(litTypes.boolean);
                    else { }
                }
                else if (leftType is EnumType && rightType is EnumType) {
                    if (((EnumType)leftType).decl == ((EnumType)rightType).decl && isEqOrNeq)
                        ((BinaryExpression)expr).setActualType(litTypes.boolean);
                    else { }
                }
                else if (leftType is AnyType && rightType is AnyType)
                    ((BinaryExpression)expr).setActualType(AnyType.instance);
                else { }
            }
            else if (expr is ConditionalExpression) {
                var trueType = ((ConditionalExpression)expr).whenTrue.getType();
                var falseType = ((ConditionalExpression)expr).whenFalse.getType();
                if (((ConditionalExpression)expr).expectedType != null) {
                    if (!Type_.isAssignableTo(trueType, ((ConditionalExpression)expr).expectedType))
                        throw new Error($"Conditional expression expects {((ConditionalExpression)expr).expectedType.repr()} but got {trueType.repr()} as true branch");
                    if (!Type_.isAssignableTo(falseType, ((ConditionalExpression)expr).expectedType))
                        throw new Error($"Conditional expression expects {((ConditionalExpression)expr).expectedType.repr()} but got {falseType.repr()} as false branch");
                    ((ConditionalExpression)expr).setActualType(((ConditionalExpression)expr).expectedType);
                }
                else if (Type_.isAssignableTo(trueType, falseType))
                    ((ConditionalExpression)expr).setActualType(falseType);
                else if (Type_.isAssignableTo(falseType, trueType))
                    ((ConditionalExpression)expr).setActualType(trueType);
                else
                    throw new Error($"Different types in the whenTrue ({trueType.repr()}) and whenFalse ({falseType.repr()}) expressions of a conditional expression");
            }
            else if (expr is NullCoalesceExpression) {
                var defaultType = ((NullCoalesceExpression)expr).defaultExpr.getType();
                var ifNullType = ((NullCoalesceExpression)expr).exprIfNull.getType();
                if (!Type_.isAssignableTo(ifNullType, defaultType))
                    this.errorMan.throw_($"Null-coalescing operator tried to assign incompatible type \"{ifNullType.repr()}\" to \"{defaultType.repr()}\"");
                else
                    ((NullCoalesceExpression)expr).setActualType(defaultType);
            }
            else if (expr is AwaitExpression) {
                var exprType = ((AwaitExpression)expr).expr.getType();
                if (exprType is ClassType && ((ClassType)exprType).decl == litTypes.promise.decl)
                    ((AwaitExpression)expr).setActualType((((ClassType)((ClassType)exprType))).typeArguments.get(0), true);
                else
                    this.errorMan.throw_($"Expected promise type ({litTypes.promise.repr()}) for await expression, but got {exprType.repr()}");
            }
            else
                return false;
            
            return true;
        }
    }
}