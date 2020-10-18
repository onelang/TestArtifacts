public interface IGenerator {
    String getLangName();
    String getExtension();
    GeneratedFile[] generate(Package pkg);
}