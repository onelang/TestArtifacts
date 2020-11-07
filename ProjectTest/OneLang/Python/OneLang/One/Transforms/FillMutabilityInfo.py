from OneLangStdLib import *
import OneLang.One.AstTransformer as astTrans
import OneLang.One.Ast.Types as types
import OneLang.One.Ast.Expressions as exprs
import OneLang.One.Ast.References as refs
import OneLang.One.Ast.Statements as stats

class FillMutabilityInfo(astTrans.AstTransformer):
    def __init__(self):
        super().__init__("FillMutabilityInfo")
    
    def get_var(self, var_ref):
        v = var_ref.get_variable()
        v.mutability = v.mutability or types.MutabilityInfo(True, False, False)
        return v
    
    def visit_variable_reference(self, var_ref):
        self.get_var(var_ref).mutability.unused = False
        return None
    
    def visit_variable_declaration(self, stmt):
        super().visit_variable_declaration(stmt)
        if stmt.attributes != None and stmt.attributes.get("mutated") == "true":
            stmt.mutability.mutated = True
        return None
    
    def visit_expression(self, expr):
        super().visit_expression(expr)
        
        if isinstance(expr, exprs.BinaryExpression) and isinstance(expr.left, refs.VariableReference) and expr.operator == "=":
            self.get_var(expr.left).mutability.reassigned = True
        elif isinstance(expr, exprs.InstanceMethodCallExpression) and isinstance(expr.object, refs.VariableReference) and "mutates" in expr.method.attributes:
            self.get_var(expr.object).mutability.mutated = True
        return None
    
    def visit_variable(self, variable):
        variable.mutability = variable.mutability or types.MutabilityInfo(True, False, False)
        return None