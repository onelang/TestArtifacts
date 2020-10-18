public class InferTypesPlugin {
    public InferTypes main;
    public ErrorManager errorMan;
    public String name;
    
    public InferTypesPlugin(String name)
    {
        this.name = name;
        this.errorMan = null;
    }
    
    public Boolean canTransform(Expression expr)
    {
        return false;
    }
    
    public Boolean canDetectType(Expression expr)
    {
        return false;
    }
    
    public Expression transform(Expression expr)
    {
        return null;
    }
    
    public Boolean detectType(Expression expr)
    {
        return false;
    }
    
    public Boolean handleProperty(Property prop)
    {
        return false;
    }
    
    public Boolean handleLambda(Lambda lambda)
    {
        return false;
    }
    
    public Boolean handleMethod(IMethodBase method)
    {
        return false;
    }
    
    public Boolean handleStatement(Statement stmt)
    {
        return false;
    }
}