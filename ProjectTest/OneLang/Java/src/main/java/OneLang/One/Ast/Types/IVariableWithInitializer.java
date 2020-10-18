public interface IVariableWithInitializer extends IVariable {
    Expression getInitializer();
    void setInitializer(Expression value);
}