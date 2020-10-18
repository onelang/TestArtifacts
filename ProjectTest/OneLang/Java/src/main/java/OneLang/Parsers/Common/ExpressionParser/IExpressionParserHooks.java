public interface IExpressionParserHooks {
    Expression unaryPrehook();
    Expression infixPrehook(Expression left);
}