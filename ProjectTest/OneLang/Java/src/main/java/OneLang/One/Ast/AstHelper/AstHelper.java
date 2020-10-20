import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class AstHelper {
    public static IInterface[] collectAllBaseInterfaces(IInterface intf) {
        var result = new HashSet<IInterface>();
        var toBeProcessed = new ArrayList<>(List.of(intf));
        
        while (toBeProcessed.size() > 0) {
            var curr = toBeProcessed.remove(toBeProcessed.size() - 1);
            result.add(curr);
            
            if (curr instanceof Class && ((Class)curr).baseClass != null)
                toBeProcessed.add((((ClassType)((Class)curr).baseClass)).decl);
            
            for (var baseIntf : curr.getBaseInterfaces())
                toBeProcessed.add((((InterfaceType)baseIntf)).decl);
        }
        
        return result.toArray(IInterface[]::new);
    }
}