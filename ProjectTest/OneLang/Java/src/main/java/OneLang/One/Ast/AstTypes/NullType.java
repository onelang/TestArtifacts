package OneLang.One.Ast.AstTypes;

import OneLang.One.Ast.Types.Enum;
import OneLang.One.Ast.Types.Interface;
import OneLang.One.Ast.Types.Class;
import OneLang.One.Ast.Types.MethodParameter;
import OneLang.One.Ast.Types.IInterface;
import OneLang.One.Ast.Interfaces.IType;

import OneLang.One.Ast.AstTypes.IPrimitiveType;
import OneLang.One.Ast.AstTypes.NullType;

public class NullType implements IPrimitiveType {
    public static NullType instance;
    
    static {
        NullType.instance = new NullType();
    }
    
    public String repr() {
        return "Null";
    }
}