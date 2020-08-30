from OneLangStdLib import *
import OneLang.One.Ast.Expressions as exprs
import OneLang.One.Ast.Statements as stats
import OneLang.One.Ast.Types as types
import OneLang.One.Ast.AstTypes as astTypes
import OneLang.One.Ast.References as refs
import OneLang.One.Ast.Interfaces as ints

class TSOverviewGenerator:
    def __init__(self):
        pass
    
    @classmethod
    def leading(cls, item):
        result = ""
        if item.leading_trivia != None and len(item.leading_trivia) > 0:
            result += item.leading_trivia
        if item.attributes != None:
            result += "".join(list(map(lambda x: f'''/// {{ATTR}} name="{x}", value={JSON.stringify(item.attributes.get(x))}\n''', item.attributes.keys())))
        return result
    
    @classmethod
    def pre_arr(cls, prefix, value):
        return f'''{prefix}{", ".join(value)}''' if len(value) > 0 else ""
    
    @classmethod
    def pre_if(cls, prefix, condition):
        return prefix if condition else ""
    
    @classmethod
    def pre(cls, prefix, value):
        return f'''{prefix}{value}''' if value != None else ""
    
    @classmethod
    def type_args(cls, args):
        return f'''<{", ".join(args)}>''' if args != None and len(args) > 0 else ""
    
    @classmethod
    def type(cls, t, raw = False):
        repr = "???" if t == None else t.repr()
        if repr == "U:UNKNOWN":
            pass
        return ("" if raw else "{T}") + repr
    
    @classmethod
    def var(cls, v):
        result = ""
        is_prop = isinstance(v, types.Property)
        if isinstance(v, types.Field) or isinstance(v, types.Property):
            m = v
            result += TSOverviewGenerator.pre_if("static ", m.is_static)
            result += "private " if m.visibility == types.VISIBILITY.PRIVATE else "protected " if m.visibility == types.VISIBILITY.PROTECTED else "public " if m.visibility == types.VISIBILITY.PUBLIC else "VISIBILITY-NOT-SET"
        result += f'''{("@prop " if is_prop else "")}'''
        if v.mutability != None:
            result += f'''{("@unused " if v.mutability.unused else "")}'''
            result += f'''{("@mutated " if v.mutability.mutated else "")}'''
            result += f'''{("@reass " if v.mutability.reassigned else "")}'''
        result += f'''{v.name}{("()" if is_prop else "")}: {TSOverviewGenerator.type(v.type)}'''
        if isinstance(v, stats.VariableDeclaration) or isinstance(v, stats.ForVariable) or isinstance(v, types.Field) or isinstance(v, types.MethodParameter):
            init = (v).initializer
            if init != None:
                result += TSOverviewGenerator.pre(" = ", TSOverviewGenerator.expr(init))
        return result
    
    @classmethod
    def expr(cls, expr, preview_only = False):
        res = "UNKNOWN-EXPR"
        if isinstance(expr, exprs.NewExpression):
            res = f'''new {TSOverviewGenerator.type(expr.cls_)}({("..." if preview_only else ", ".join(list(map(lambda x: TSOverviewGenerator.expr(x), expr.args))))})'''
        elif isinstance(expr, exprs.UnresolvedNewExpression):
            res = f'''new {TSOverviewGenerator.type(expr.cls_)}({("..." if preview_only else ", ".join(list(map(lambda x: TSOverviewGenerator.expr(x), expr.args))))})'''
        elif isinstance(expr, exprs.Identifier):
            res = f'''{{ID}}{expr.text}'''
        elif isinstance(expr, exprs.PropertyAccessExpression):
            res = f'''{TSOverviewGenerator.expr(expr.object)}.{{PA}}{expr.property_name}'''
        elif isinstance(expr, exprs.UnresolvedCallExpression):
            type_args = f'''<{", ".join(list(map(lambda x: TSOverviewGenerator.type(x), expr.type_args)))}>''' if len(expr.type_args) > 0 else ""
            res = f'''{TSOverviewGenerator.expr(expr.func)}{type_args}({("..." if preview_only else ", ".join(list(map(lambda x: TSOverviewGenerator.expr(x), expr.args))))})'''
        elif isinstance(expr, exprs.UnresolvedMethodCallExpression):
            type_args = f'''<{", ".join(list(map(lambda x: TSOverviewGenerator.type(x), expr.type_args)))}>''' if len(expr.type_args) > 0 else ""
            res = f'''{TSOverviewGenerator.expr(expr.object)}.{{UM}}{expr.method_name}{type_args}({("..." if preview_only else ", ".join(list(map(lambda x: TSOverviewGenerator.expr(x), expr.args))))})'''
        elif isinstance(expr, exprs.InstanceMethodCallExpression):
            type_args = f'''<{", ".join(list(map(lambda x: TSOverviewGenerator.type(x), expr.type_args)))}>''' if len(expr.type_args) > 0 else ""
            res = f'''{TSOverviewGenerator.expr(expr.object)}.{{M}}{expr.method.name}{type_args}({("..." if preview_only else ", ".join(list(map(lambda x: TSOverviewGenerator.expr(x), expr.args))))})'''
        elif isinstance(expr, exprs.StaticMethodCallExpression):
            type_args = f'''<{", ".join(list(map(lambda x: TSOverviewGenerator.type(x), expr.type_args)))}>''' if len(expr.type_args) > 0 else ""
            res = f'''{expr.method.parent_interface.name}.{{M}}{expr.method.name}{type_args}({("..." if preview_only else ", ".join(list(map(lambda x: TSOverviewGenerator.expr(x), expr.args))))})'''
        elif isinstance(expr, exprs.GlobalFunctionCallExpression):
            res = f'''{expr.func.name}({("..." if preview_only else ", ".join(list(map(lambda x: TSOverviewGenerator.expr(x), expr.args))))})'''
        elif isinstance(expr, exprs.LambdaCallExpression):
            res = f'''{TSOverviewGenerator.expr(expr.method)}({("..." if preview_only else ", ".join(list(map(lambda x: TSOverviewGenerator.expr(x), expr.args))))})'''
        elif isinstance(expr, exprs.BooleanLiteral):
            res = f'''{expr.bool_value}'''
        elif isinstance(expr, exprs.StringLiteral):
            res = f'''{JSON.stringify(expr.string_value)}'''
        elif isinstance(expr, exprs.NumericLiteral):
            res = f'''{expr.value_as_text}'''
        elif isinstance(expr, exprs.CharacterLiteral):
            res = f'''\'{expr.char_value}\''''
        elif isinstance(expr, exprs.ElementAccessExpression):
            res = f'''({TSOverviewGenerator.expr(expr.object)})[{TSOverviewGenerator.expr(expr.element_expr)}]'''
        elif isinstance(expr, exprs.TemplateString):
            res = "`" + "".join(list(map(lambda x: x.literal_text if x.is_literal else "${" + TSOverviewGenerator.expr(x.expression) + "}", expr.parts))) + "`"
        elif isinstance(expr, exprs.BinaryExpression):
            res = f'''{TSOverviewGenerator.expr(expr.left)} {expr.operator} {TSOverviewGenerator.expr(expr.right)}'''
        elif isinstance(expr, exprs.ArrayLiteral):
            res = f'''[{", ".join(list(map(lambda x: TSOverviewGenerator.expr(x), expr.items)))}]'''
        elif isinstance(expr, exprs.CastExpression):
            res = f'''<{TSOverviewGenerator.type(expr.new_type)}>({TSOverviewGenerator.expr(expr.expression)})'''
        elif isinstance(expr, exprs.ConditionalExpression):
            res = f'''{TSOverviewGenerator.expr(expr.condition)} ? {TSOverviewGenerator.expr(expr.when_true)} : {TSOverviewGenerator.expr(expr.when_false)}'''
        elif isinstance(expr, exprs.InstanceOfExpression):
            res = f'''{TSOverviewGenerator.expr(expr.expr)} instanceof {TSOverviewGenerator.type(expr.check_type)}'''
        elif isinstance(expr, exprs.ParenthesizedExpression):
            res = f'''({TSOverviewGenerator.expr(expr.expression)})'''
        elif isinstance(expr, exprs.RegexLiteral):
            res = f'''/{expr.pattern}/{("g" if expr.global_ else "")}{("g" if expr.case_insensitive else "")}'''
        elif isinstance(expr, types.Lambda):
            res = f'''({", ".join(list(map(lambda x: x.name + (": " + TSOverviewGenerator.type(x.type) if x.type != None else ""), expr.parameters)))}) => {{ {TSOverviewGenerator.raw_block(expr.body)} }}'''
        elif isinstance(expr, exprs.UnaryExpression) and expr.unary_type == exprs.UNARY_TYPE.PREFIX:
            res = f'''{expr.operator}{TSOverviewGenerator.expr(expr.operand)}'''
        elif isinstance(expr, exprs.UnaryExpression) and expr.unary_type == exprs.UNARY_TYPE.POSTFIX:
            res = f'''{TSOverviewGenerator.expr(expr.operand)}{expr.operator}'''
        elif isinstance(expr, exprs.MapLiteral):
            repr = ",\n".join(list(map(lambda item: f'''{item.key}: {TSOverviewGenerator.expr(item.value)}''', expr.items)))
            res = "{L:M}" + ("{}" if repr == "" else f'''{{\n{TSOverviewGenerator.pad(repr)}\n}}''' if "\n" in repr else f'''{{ {repr} }}''')
        elif isinstance(expr, exprs.NullLiteral):
            res = f'''null'''
        elif isinstance(expr, exprs.AwaitExpression):
            res = f'''await {TSOverviewGenerator.expr(expr.expr)}'''
        elif isinstance(expr, refs.ThisReference):
            res = f'''{{R}}this'''
        elif isinstance(expr, refs.StaticThisReference):
            res = f'''{{R:Static}}this'''
        elif isinstance(expr, refs.EnumReference):
            res = f'''{{R:Enum}}{expr.decl.name}'''
        elif isinstance(expr, refs.ClassReference):
            res = f'''{{R:Cls}}{expr.decl.name}'''
        elif isinstance(expr, refs.MethodParameterReference):
            res = f'''{{R:MetP}}{expr.decl.name}'''
        elif isinstance(expr, refs.VariableDeclarationReference):
            res = f'''{{V}}{expr.decl.name}'''
        elif isinstance(expr, refs.ForVariableReference):
            res = f'''{{R:ForV}}{expr.decl.name}'''
        elif isinstance(expr, refs.ForeachVariableReference):
            res = f'''{{R:ForEV}}{expr.decl.name}'''
        elif isinstance(expr, refs.CatchVariableReference):
            res = f'''{{R:CatchV}}{expr.decl.name}'''
        elif isinstance(expr, refs.GlobalFunctionReference):
            res = f'''{{R:GFunc}}{expr.decl.name}'''
        elif isinstance(expr, refs.SuperReference):
            res = f'''{{R}}super'''
        elif isinstance(expr, refs.StaticFieldReference):
            res = f'''{{R:StFi}}{expr.decl.parent_interface.name}::{expr.decl.name}'''
        elif isinstance(expr, refs.StaticPropertyReference):
            res = f'''{{R:StPr}}{expr.decl.parent_class.name}::{expr.decl.name}'''
        elif isinstance(expr, refs.InstanceFieldReference):
            res = f'''{TSOverviewGenerator.expr(expr.object)}.{{F}}{expr.field.name}'''
        elif isinstance(expr, refs.InstancePropertyReference):
            res = f'''{TSOverviewGenerator.expr(expr.object)}.{{P}}{expr.property.name}'''
        elif isinstance(expr, refs.EnumMemberReference):
            res = f'''{{E}}{expr.decl.parent_enum.name}::{expr.decl.name}'''
        elif isinstance(expr, exprs.NullCoalesceExpression):
            res = f'''{TSOverviewGenerator.expr(expr.default_expr)} ?? {TSOverviewGenerator.expr(expr.expr_if_null)}'''
        else:
            pass
        return res
    
    @classmethod
    def block(cls, block, preview_only = False, allow_one_liner = True):
        if preview_only:
            return " { ... }"
        stmt_len = len(block.statements)
        return " { }" if stmt_len == 0 else f'''\n{TSOverviewGenerator.pad(TSOverviewGenerator.raw_block(block))}''' if allow_one_liner and stmt_len == 1 else f''' {{\n{TSOverviewGenerator.pad(TSOverviewGenerator.raw_block(block))}\n}}'''
    
    @classmethod
    def stmt(cls, stmt, preview_only = False):
        res = "UNKNOWN-STATEMENT"
        if isinstance(stmt, stats.BreakStatement):
            res = "break;"
        elif isinstance(stmt, stats.ReturnStatement):
            res = "return;" if stmt.expression == None else f'''return {TSOverviewGenerator.expr(stmt.expression)};'''
        elif isinstance(stmt, stats.UnsetStatement):
            res = f'''unset {TSOverviewGenerator.expr(stmt.expression)};'''
        elif isinstance(stmt, stats.ThrowStatement):
            res = f'''throw {TSOverviewGenerator.expr(stmt.expression)};'''
        elif isinstance(stmt, stats.ExpressionStatement):
            res = f'''{TSOverviewGenerator.expr(stmt.expression)};'''
        elif isinstance(stmt, stats.VariableDeclaration):
            res = f'''var {TSOverviewGenerator.var(stmt)};'''
        elif isinstance(stmt, stats.ForeachStatement):
            res = f'''for (const {stmt.item_var.name} of {TSOverviewGenerator.expr(stmt.items)})''' + TSOverviewGenerator.block(stmt.body, preview_only)
        elif isinstance(stmt, stats.IfStatement):
            else_if = stmt.else_ != None and len(stmt.else_.statements) == 1 and isinstance(stmt.else_.statements[0], stats.IfStatement)
            res = f'''if ({TSOverviewGenerator.expr(stmt.condition)}){TSOverviewGenerator.block(stmt.then, preview_only)}'''
            if not preview_only:
                res += (f'''\nelse {TSOverviewGenerator.stmt(stmt.else_.statements[0])}''' if else_if else "") + (f'''\nelse''' + TSOverviewGenerator.block(stmt.else_) if not else_if and stmt.else_ != None else "")
        elif isinstance(stmt, stats.WhileStatement):
            res = f'''while ({TSOverviewGenerator.expr(stmt.condition)})''' + TSOverviewGenerator.block(stmt.body, preview_only)
        elif isinstance(stmt, stats.ForStatement):
            res = f'''for ({(TSOverviewGenerator.var(stmt.item_var) if stmt.item_var != None else "")}; {TSOverviewGenerator.expr(stmt.condition)}; {TSOverviewGenerator.expr(stmt.incrementor)})''' + TSOverviewGenerator.block(stmt.body, preview_only)
        elif isinstance(stmt, stats.DoStatement):
            res = f'''do{TSOverviewGenerator.block(stmt.body, preview_only)} while ({TSOverviewGenerator.expr(stmt.condition)})'''
        elif isinstance(stmt, stats.TryStatement):
            res = "try" + TSOverviewGenerator.block(stmt.try_body, preview_only, False) + (f''' catch ({stmt.catch_var.name}){TSOverviewGenerator.block(stmt.catch_body, preview_only)}''' if stmt.catch_body != None else "") + ("finally" + TSOverviewGenerator.block(stmt.finally_body, preview_only) if stmt.finally_body != None else "")
        elif isinstance(stmt, stats.ContinueStatement):
            res = f'''continue;'''
        else:
            pass
        return res if preview_only else TSOverviewGenerator.leading(stmt) + res
    
    @classmethod
    def raw_block(cls, block):
        return "\n".join(list(map(lambda stmt: TSOverviewGenerator.stmt(stmt), block.statements)))
    
    @classmethod
    def method_base(cls, method, returns):
        if method == None:
            return ""
        name = method.name if isinstance(method, types.Method) else "constructor" if isinstance(method, types.Constructor) else method.name if isinstance(method, types.GlobalFunction) else "???"
        type_args = method.type_arguments if isinstance(method, types.Method) else None
        return TSOverviewGenerator.pre_if("/* throws */ ", method.throws) + f'''{name}{TSOverviewGenerator.type_args(type_args)}({", ".join(list(map(lambda p: TSOverviewGenerator.leading(p) + TSOverviewGenerator.var(p), method.parameters)))})''' + ("" if isinstance(returns, astTypes.VoidType) else f''': {TSOverviewGenerator.type(returns)}''') + (f''' {{\n{TSOverviewGenerator.pad(TSOverviewGenerator.raw_block(method.body))}\n}}''' if method.body != None else ";")
    
    @classmethod
    def method(cls, method):
        return "" if method == None else ("static " if method.is_static else "") + ("@mutates " if method.attributes != None and "mutates" in method.attributes else "") + TSOverviewGenerator.method_base(method, method.returns)
    
    @classmethod
    def class_like(cls, cls_):
        res_list = []
        res_list.append("\n".join(list(map(lambda field: TSOverviewGenerator.var(field) + ";", cls_.fields))))
        if isinstance(cls_, types.Class):
            res_list.append("\n".join(list(map(lambda prop: TSOverviewGenerator.var(prop) + ";", cls_.properties))))
            res_list.append(TSOverviewGenerator.method_base(cls_.constructor_, astTypes.VoidType.instance))
        res_list.append("\n\n".join(list(map(lambda method: TSOverviewGenerator.method(method), cls_.methods))))
        return TSOverviewGenerator.pad("\n\n".join(list(filter(lambda x: x != "", res_list))))
    
    @classmethod
    def pad(cls, str):
        return "\n".join(list(map(lambda x: f'''    {x}''', str.split("\\n"))))
    
    @classmethod
    def imp(cls, imp):
        return "" + ("X" if isinstance(imp, types.UnresolvedImport) else "C" if isinstance(imp, types.Class) else "I" if isinstance(imp, types.Interface) else "E" if isinstance(imp, types.Enum) else "???") + f''':{imp.name}'''
    
    @classmethod
    def node_repr(cls, node):
        if isinstance(node, stats.Statement):
            return TSOverviewGenerator.stmt(node, True)
        elif isinstance(node, exprs.Expression):
            return TSOverviewGenerator.expr(node, True)
        else:
            return "/* TODO: missing */"
    
    @classmethod
    def generate(cls, source_file):
        imps = list(map(lambda imp: (f'''import * as {imp.import_as}''' if imp.import_all else f'''import {{ {", ".join(list(map(lambda x: TSOverviewGenerator.imp(x), imp.imports)))} }}''') + f''' from "{imp.export_scope.package_name}{TSOverviewGenerator.pre("/", imp.export_scope.scope_name)}";''', source_file.imports))
        enums = list(map(lambda enum_: f'''{TSOverviewGenerator.leading(enum_)}enum {enum_.name} {{ {", ".join(list(map(lambda x: x.name, enum_.values)))} }}''', source_file.enums))
        intfs = list(map(lambda intf: f'''{TSOverviewGenerator.leading(intf)}interface {intf.name}{TSOverviewGenerator.type_args(intf.type_arguments)}''' + f'''{TSOverviewGenerator.pre_arr(" extends ", list(map(lambda x: TSOverviewGenerator.type(x), intf.base_interfaces)))} {{\n{TSOverviewGenerator.class_like(intf)}\n}}''', source_file.interfaces))
        classes = list(map(lambda cls_: f'''{TSOverviewGenerator.leading(cls_)}class {cls_.name}{TSOverviewGenerator.type_args(cls_.type_arguments)}''' + TSOverviewGenerator.pre(" extends ", TSOverviewGenerator.type(cls_.base_class) if cls_.base_class != None else None) + TSOverviewGenerator.pre_arr(" implements ", list(map(lambda x: TSOverviewGenerator.type(x), cls_.base_interfaces))) + f''' {{\n{TSOverviewGenerator.class_like(cls_)}\n}}''', source_file.classes))
        funcs = list(map(lambda func: f'''{TSOverviewGenerator.leading(func)}function {func.name}{TSOverviewGenerator.method_base(func, func.returns)}''', source_file.funcs))
        main = TSOverviewGenerator.raw_block(source_file.main_block)
        result = f'''// export scope: {source_file.export_scope.package_name}/{source_file.export_scope.scope_name}\n''' + "\n\n".join(list(filter(lambda x: x != "", ["\n".join(imps), "\n".join(enums), "\n\n".join(intfs), "\n\n".join(classes), "\n\n".join(funcs), main])))
        return result