from OneLangStdLib import *
import OneLang.One.AstTransformer as astTrans
import OneLang.One.Ast.Expressions as exprs

class DetectMethodCalls(astTrans.AstTransformer):
    def __init__(self):
        super().__init__("DetectMethodCalls")
    
    def visit_expression(self, expr):
        super().visit_expression(expr)
        if isinstance(expr, exprs.UnresolvedCallExpression) and isinstance(expr.func, exprs.PropertyAccessExpression):
            return exprs.UnresolvedMethodCallExpression(expr.func.object, expr.func.property_name, expr.type_args, expr.args)
        return None