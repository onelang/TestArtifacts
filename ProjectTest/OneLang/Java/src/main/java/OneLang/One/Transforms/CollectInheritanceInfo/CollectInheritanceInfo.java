import java.util.Arrays;

public class CollectInheritanceInfo implements ITransformer {
    String name = "CollectInheritanceInfo";
    public String getName() { return this.name; }
    public void setName(String value) { this.name = value; }
    
    public CollectInheritanceInfo()
    {
        // C# fix
        this.setName("CollectInheritanceInfo");
    }
    
    public void visitClass(Class cls) {
        var allBaseIIntfs = cls.getAllBaseInterfaces();
        var intfs = Arrays.stream(Arrays.stream(allBaseIIntfs).map(x -> x instanceof Interface ? ((Interface)x) : null).toArray(Interface[]::new)).filter(x -> x != null).toArray(Interface[]::new);
        var clses = Arrays.stream(Arrays.stream(allBaseIIntfs).map(x -> x instanceof Class ? ((Class)x) : null).toArray(Class[]::new)).filter(x -> x != null && x != cls).toArray(Class[]::new);
        
        for (var field : cls.getFields())
            field.interfaceDeclarations = Arrays.stream(Arrays.stream(intfs).map(x -> Arrays.stream(x.getFields()).filter(f -> f.getName().equals(field.getName())).findFirst().orElse(null)).toArray(Field[]::new)).filter(x -> x != null).toArray(Field[]::new);
        
        for (var method : cls.getMethods()) {
            method.interfaceDeclarations = Arrays.stream(Arrays.stream(intfs).map(x -> Arrays.stream(x.getMethods()).filter(m -> m.name.equals(method.name)).findFirst().orElse(null)).toArray(Method[]::new)).filter(x -> x != null).toArray(Method[]::new);
            method.overrides = Arrays.stream(Arrays.stream(clses).map(x -> Arrays.stream(x.getMethods()).filter(m -> m.name.equals(method.name)).findFirst().orElse(null)).toArray(Method[]::new)).filter(x -> x != null).findFirst().orElse(null);
            if (method.overrides != null)
                method.overrides.overriddenBy.add(method);
        }
    }
    
    public void visitFiles(SourceFile[] files) {
        for (var file : files)
            for (var cls : file.classes)
                this.visitClass(cls);
    }
}