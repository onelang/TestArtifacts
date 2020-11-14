from OneLangStdLib import *
import OneLang.One.Ast.AstTypes as astTypes
import OneLang.One.Ast.Interfaces as ints
import OneLang.One.Ast.Types as types
import OneLang.index as index

class JsonSerializer:
    def __init__(self):
        pass
    
    @classmethod
    def serialize(cls, obj):
        if isinstance(obj.type, astTypes.ClassType):
            members = []
            for field in obj.type.decl.fields:
                members.append(f'''"{field.name}": {JsonSerializer.serialize(obj.get_field(field.name))}''')
            return "{}" if len(members) == 0 else f'''{{\n{pad(/* TODO: UnresolvedMethodCallExpression */ members.join(",\n"))}\n}}'''
        else:
            return "\"<UNKNOWN-TYPE>\""