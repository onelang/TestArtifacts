import java.util.List;
import java.util.ArrayList;

public class EnumMember {
    public String name;
    public Enum parentEnum;
    public List<EnumMemberReference> references;
    
    public EnumMember(String name)
    {
        this.name = name;
        this.references = new ArrayList<EnumMemberReference>();
    }
}