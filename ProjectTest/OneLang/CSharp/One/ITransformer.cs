using One.Ast;

namespace One
{
    public interface ITransformer {
        string name { get; set; }
        
        void visitPackage(Package pkg);
    }
}