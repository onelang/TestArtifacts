public class GenericsType implements IType {
    public String typeVarName;
    
    public GenericsType(String typeVarName)
    {
        this.typeVarName = typeVarName;
    }
    
    public String repr()
    {
        return "G:" + this.typeVarName;
    }
}