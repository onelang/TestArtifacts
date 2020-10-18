public interface IGeneratorPlugin {
    String expr(IExpression expr);
    String stmt(Statement stmt);
}