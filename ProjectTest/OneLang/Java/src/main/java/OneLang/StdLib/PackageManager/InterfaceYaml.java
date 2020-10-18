import java.util.Arrays;

public class InterfaceYaml {
    public Integer fileVersion;
    public String vendor;
    public String name;
    public Integer version;
    public String definitionFile;
    public InterfaceDependency[] dependencies;
    
    public InterfaceYaml(Integer fileVersion, String vendor, String name, Integer version, String definitionFile, InterfaceDependency[] dependencies)
    {
        this.fileVersion = fileVersion;
        this.vendor = vendor;
        this.name = name;
        this.version = version;
        this.definitionFile = definitionFile;
        this.dependencies = dependencies;
    }
    
    public static InterfaceYaml fromYaml(YamlValue obj)
    {
        return new InterfaceYaml(obj.dbl("file-version"), obj.str("vendor"), obj.str("name"), obj.dbl("version"), obj.str("definition-file"), Arrays.stream(obj.arr("dependencies")).map(dep -> new InterfaceDependency(dep.str("name"), dep.dbl("minver"))).toArray(InterfaceDependency[]::new));
    }
}