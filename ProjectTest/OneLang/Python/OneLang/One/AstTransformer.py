from OneLangStdLib import *
import OneLang.One.Ast.AstTypes as astTypes
import OneLang.One.Ast.Expressions as exprs
import OneLang.One.Ast.Statements as stats
import OneLang.One.Ast.Types as types
import OneLang.One.Ast.References as refs
import OneLang.One.ErrorManager as errorMan
import OneLang.One.ITransform as iTrans

class AstTransformer:
    def __init__(self, name):
        self.error_man = errorMan.ErrorManager()
        self.current_file = None
        self.current_interface = None
        self.current_method = None
        self.current_statement = None
        self.name = name
    
    def visit_type(self, type):
        if isinstance(type, astTypes.ClassType) or isinstance(type, astTypes.InterfaceType) or isinstance(type, astTypes.UnresolvedType):
            type2 = type
            type2.type_arguments = list(map(lambda x: self.visit_type(x) or x, type2.type_arguments))
        elif isinstance(type, astTypes.LambdaType):
            for mp in type.parameters:
                self.visit_method_parameter(mp)
            type.return_type = self.visit_type(type.return_type) or type.return_type
        return None
    
    def visit_identifier(self, id):
        return None
    
    def visit_variable(self, variable):
        if variable.type != None:
            variable.type = self.visit_type(variable.type) or variable.type
        return None
    
    def visit_variable_with_initializer(self, variable):
        self.visit_variable(variable)
        if variable.initializer != None:
            variable.initializer = self.visit_expression(variable.initializer) or variable.initializer
        return None
    
    def visit_variable_declaration(self, stmt):
        self.visit_variable_with_initializer(stmt)
        return None
    
    def visit_unknown_statement(self, stmt):
        self.error_man.throw(f'''Unknown statement type''')
        return None
    
    def visit_statement(self, stmt):
        self.current_statement = stmt
        if isinstance(stmt, stats.ReturnStatement):
            if stmt.expression != None:
                stmt.expression = self.visit_expression(stmt.expression) or stmt.expression
        elif isinstance(stmt, stats.ExpressionStatement):
            stmt.expression = self.visit_expression(stmt.expression) or stmt.expression
        elif isinstance(stmt, stats.IfStatement):
            stmt.condition = self.visit_expression(stmt.condition) or stmt.condition
            stmt.then = self.visit_block(stmt.then) or stmt.then
            if stmt.else_ != None:
                stmt.else_ = self.visit_block(stmt.else_) or stmt.else_
        elif isinstance(stmt, stats.ThrowStatement):
            stmt.expression = self.visit_expression(stmt.expression) or stmt.expression
        elif isinstance(stmt, stats.VariableDeclaration):
            return self.visit_variable_declaration(stmt)
        elif isinstance(stmt, stats.WhileStatement):
            stmt.condition = self.visit_expression(stmt.condition) or stmt.condition
            stmt.body = self.visit_block(stmt.body) or stmt.body
        elif isinstance(stmt, stats.DoStatement):
            stmt.condition = self.visit_expression(stmt.condition) or stmt.condition
            stmt.body = self.visit_block(stmt.body) or stmt.body
        elif isinstance(stmt, stats.ForStatement):
            if stmt.item_var != None:
                self.visit_variable_with_initializer(stmt.item_var)
            stmt.condition = self.visit_expression(stmt.condition) or stmt.condition
            stmt.incrementor = self.visit_expression(stmt.incrementor) or stmt.incrementor
            stmt.body = self.visit_block(stmt.body) or stmt.body
        elif isinstance(stmt, stats.ForeachStatement):
            self.visit_variable(stmt.item_var)
            stmt.items = self.visit_expression(stmt.items) or stmt.items
            stmt.body = self.visit_block(stmt.body) or stmt.body
        elif isinstance(stmt, stats.TryStatement):
            stmt.try_body = self.visit_block(stmt.try_body) or stmt.try_body
            if stmt.catch_body != None:
                self.visit_variable(stmt.catch_var)
                stmt.catch_body = self.visit_block(stmt.catch_body) or stmt.catch_body
            if stmt.finally_body != None:
                stmt.finally_body = self.visit_block(stmt.finally_body) or stmt.finally_body
        elif isinstance(stmt, stats.BreakStatement):
            pass
        elif isinstance(stmt, stats.UnsetStatement):
            stmt.expression = self.visit_expression(stmt.expression) or stmt.expression
        elif isinstance(stmt, stats.ContinueStatement):
            pass
        else:
            return self.visit_unknown_statement(stmt)
        return None
    
    def visit_block(self, block):
        block.statements = list(map(lambda x: self.visit_statement(x) or x, block.statements))
        return None
    
    def visit_template_string(self, expr):
        i = 0
        
        while i < len(expr.parts):
            part = expr.parts[i]
            if not part.is_literal:
                part.expression = self.visit_expression(part.expression) or part.expression
            i = i + 1
        return None
    
    def visit_unknown_expression(self, expr):
        self.error_man.throw(f'''Unknown expression type''')
        return None
    
    def visit_lambda(self, lambda_):
        self.visit_method_base(lambda_)
        return None
    
    def visit_variable_reference(self, var_ref):
        return None
    
    def visit_expression(self, expr):
        if isinstance(expr, exprs.BinaryExpression):
            expr.left = self.visit_expression(expr.left) or expr.left
            expr.right = self.visit_expression(expr.right) or expr.right
        elif isinstance(expr, exprs.NullCoalesceExpression):
            expr.default_expr = self.visit_expression(expr.default_expr) or expr.default_expr
            expr.expr_if_null = self.visit_expression(expr.expr_if_null) or expr.expr_if_null
        elif isinstance(expr, exprs.UnresolvedCallExpression):
            expr.func = self.visit_expression(expr.func) or expr.func
            expr.type_args = list(map(lambda x: self.visit_type(x) or x, expr.type_args))
            expr.args = list(map(lambda x: self.visit_expression(x) or x, expr.args))
        elif isinstance(expr, exprs.UnresolvedMethodCallExpression):
            expr.object = self.visit_expression(expr.object) or expr.object
            expr.type_args = list(map(lambda x: self.visit_type(x) or x, expr.type_args))
            expr.args = list(map(lambda x: self.visit_expression(x) or x, expr.args))
        elif isinstance(expr, exprs.ConditionalExpression):
            expr.condition = self.visit_expression(expr.condition) or expr.condition
            expr.when_true = self.visit_expression(expr.when_true) or expr.when_true
            expr.when_false = self.visit_expression(expr.when_false) or expr.when_false
        elif isinstance(expr, exprs.Identifier):
            return self.visit_identifier(expr)
        elif isinstance(expr, exprs.UnresolvedNewExpression):
            self.visit_type(expr.cls_)
            expr.args = list(map(lambda x: self.visit_expression(x) or x, expr.args))
        elif isinstance(expr, exprs.NewExpression):
            self.visit_type(expr.cls_)
            expr.args = list(map(lambda x: self.visit_expression(x) or x, expr.args))
        elif isinstance(expr, exprs.TemplateString):
            return self.visit_template_string(expr)
        elif isinstance(expr, exprs.ParenthesizedExpression):
            expr.expression = self.visit_expression(expr.expression) or expr.expression
        elif isinstance(expr, exprs.UnaryExpression):
            expr.operand = self.visit_expression(expr.operand) or expr.operand
        elif isinstance(expr, exprs.PropertyAccessExpression):
            expr.object = self.visit_expression(expr.object) or expr.object
        elif isinstance(expr, exprs.ElementAccessExpression):
            expr.object = self.visit_expression(expr.object) or expr.object
            expr.element_expr = self.visit_expression(expr.element_expr) or expr.element_expr
        elif isinstance(expr, exprs.ArrayLiteral):
            expr.items = list(map(lambda x: self.visit_expression(x) or x, expr.items))
        elif isinstance(expr, exprs.MapLiteral):
            for item in expr.items:
                item.value = self.visit_expression(item.value) or item.value
        elif isinstance(expr, exprs.StringLiteral):
            pass
        elif isinstance(expr, exprs.BooleanLiteral):
            pass
        elif isinstance(expr, exprs.NumericLiteral):
            pass
        elif isinstance(expr, exprs.NullLiteral):
            pass
        elif isinstance(expr, exprs.RegexLiteral):
            pass
        elif isinstance(expr, exprs.CastExpression):
            expr.new_type = self.visit_type(expr.new_type) or expr.new_type
            expr.expression = self.visit_expression(expr.expression) or expr.expression
        elif isinstance(expr, exprs.InstanceOfExpression):
            expr.expr = self.visit_expression(expr.expr) or expr.expr
            expr.check_type = self.visit_type(expr.check_type) or expr.check_type
        elif isinstance(expr, exprs.AwaitExpression):
            expr.expr = self.visit_expression(expr.expr) or expr.expr
        elif isinstance(expr, types.Lambda):
            return self.visit_lambda(expr)
        elif isinstance(expr, refs.ClassReference):
            pass
        elif isinstance(expr, refs.EnumReference):
            pass
        elif isinstance(expr, refs.ThisReference):
            pass
        elif isinstance(expr, refs.StaticThisReference):
            pass
        elif isinstance(expr, refs.MethodParameterReference):
            return self.visit_variable_reference(expr)
        elif isinstance(expr, refs.VariableDeclarationReference):
            return self.visit_variable_reference(expr)
        elif isinstance(expr, refs.ForVariableReference):
            return self.visit_variable_reference(expr)
        elif isinstance(expr, refs.ForeachVariableReference):
            return self.visit_variable_reference(expr)
        elif isinstance(expr, refs.CatchVariableReference):
            return self.visit_variable_reference(expr)
        elif isinstance(expr, refs.GlobalFunctionReference):
            pass
        elif isinstance(expr, refs.SuperReference):
            pass
        elif isinstance(expr, refs.InstanceFieldReference):
            return self.visit_variable_reference(expr)
        elif isinstance(expr, refs.InstancePropertyReference):
            return self.visit_variable_reference(expr)
        elif isinstance(expr, refs.StaticFieldReference):
            return self.visit_variable_reference(expr)
        elif isinstance(expr, refs.StaticPropertyReference):
            return self.visit_variable_reference(expr)
        elif isinstance(expr, refs.EnumMemberReference):
            pass
        elif isinstance(expr, exprs.StaticMethodCallExpression):
            expr.type_args = list(map(lambda x: self.visit_type(x) or x, expr.type_args))
            expr.args = list(map(lambda x: self.visit_expression(x) or x, expr.args))
        elif isinstance(expr, exprs.GlobalFunctionCallExpression):
            expr.args = list(map(lambda x: self.visit_expression(x) or x, expr.args))
        elif isinstance(expr, exprs.InstanceMethodCallExpression):
            expr.object = self.visit_expression(expr.object) or expr.object
            expr.type_args = list(map(lambda x: self.visit_type(x) or x, expr.type_args))
            expr.args = list(map(lambda x: self.visit_expression(x) or x, expr.args))
        elif isinstance(expr, exprs.LambdaCallExpression):
            expr.args = list(map(lambda x: self.visit_expression(x) or x, expr.args))
        else:
            return self.visit_unknown_expression(expr)
        return None
    
    def visit_method_parameter(self, method_parameter):
        self.visit_variable_with_initializer(method_parameter)
    
    def visit_method_base(self, method):
        for item in method.parameters:
            self.visit_method_parameter(item)
        
        if method.body != None:
            method.body = self.visit_block(method.body) or method.body
    
    def visit_method(self, method):
        self.current_method = method
        self.visit_method_base(method)
        method.returns = self.visit_type(method.returns) or method.returns
        self.current_method = None
    
    def visit_global_function(self, func):
        self.visit_method_base(func)
        func.returns = self.visit_type(func.returns) or func.returns
    
    def visit_constructor(self, constructor):
        self.current_method = constructor
        self.visit_method_base(constructor)
        self.current_method = None
    
    def visit_field(self, field):
        self.visit_variable_with_initializer(field)
    
    def visit_property(self, prop):
        self.visit_variable(prop)
        if prop.getter != None:
            prop.getter = self.visit_block(prop.getter) or prop.getter
        if prop.setter != None:
            prop.setter = self.visit_block(prop.setter) or prop.setter
    
    def visit_interface(self, intf):
        self.current_interface = intf
        intf.base_interfaces = list(map(lambda x: self.visit_type(x) or x, intf.base_interfaces))
        for field in intf.fields:
            self.visit_field(field)
        for method in intf.methods:
            self.visit_method(method)
        self.current_interface = None
    
    def visit_class(self, cls_):
        self.current_interface = cls_
        if cls_.constructor_ != None:
            self.visit_constructor(cls_.constructor_)
        
        cls_.base_class = self.visit_type(cls_.base_class) or cls_.base_class
        cls_.base_interfaces = list(map(lambda x: self.visit_type(x) or x, cls_.base_interfaces))
        for field in cls_.fields:
            self.visit_field(field)
        for prop in cls_.properties:
            self.visit_property(prop)
        for method in cls_.methods:
            self.visit_method(method)
        self.current_interface = None
    
    def visit_enum(self, enum_):
        for value in enum_.values:
            self.visit_enum_member(value)
    
    def visit_enum_member(self, enum_member):
        pass
    
    def visit_source_file(self, source_file):
        self.error_man.reset_context(self)
        self.current_file = source_file
        for enum_ in source_file.enums:
            self.visit_enum(enum_)
        for intf in source_file.interfaces:
            self.visit_interface(intf)
        for cls_ in source_file.classes:
            self.visit_class(cls_)
        for func in source_file.funcs:
            self.visit_global_function(func)
        source_file.main_block = self.visit_block(source_file.main_block) or source_file.main_block
        self.current_file = None
    
    def visit_package(self, pkg):
        for file in pkg.files.values():
            self.visit_source_file(file)