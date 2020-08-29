using Generator;
using One.Ast;

namespace Generator
{
    public interface IGenerator {
        string getLangName();
        
        string getExtension();
        
        GeneratedFile[] generate(Package pkg);
    }
}