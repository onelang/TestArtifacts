from OneLangStdLib import *
import OneLang.One.Ast.Expressions as exprs
import OneLang.One.Ast.Types as types
import OneLang.One.AstTransformer as astTrans

class ConvertDefaultMethodParams(astTrans.AstTransformer):
    def __init__(self):
        super().__init__("ConvertDefaultMethodParams")
    
    def visit_expression(self, expr):
        if isinstance(expr, exprs.InstanceMethodCallExpression) or isinstance(expr, exprs.StaticMethodCallExpression):
            method_call = (expr)
            i = len(method_call.args)
            
            while i < len(method_call.method.parameters):
                init = method_call.method.parameters[i].initializer
                if init == None:
                    self.error_man.throw(f'''Missing default value for parameter #{i + 1}!''')
                method_call.args.append(init)
                i = i + 1
        return None