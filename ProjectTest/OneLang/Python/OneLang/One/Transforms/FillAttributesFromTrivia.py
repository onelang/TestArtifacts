from OneLangStdLib import *
import OneLang.One.Ast.Types as types
import OneLang.One.Ast.Statements as stats

class FillAttributesFromTrivia:
    def __init__(self):
        pass
    
    @classmethod
    def process_trivia(cls, trivia):
        result = {}
        if trivia != None and trivia != "":
            regex = RegExp("(?:\\n|^)\\s*(?://|#|/\\*\\*?)\\s*@([a-z0-9_.-]+) ?((?!\\n|\\*/|$).+)?")
            while True:
                match = regex.exec(trivia)
                if match == None:
                    break
                result[match[1]] = match[2] or "true"
        return result
    
    @classmethod
    def process(cls, items):
        for item in items:
            item.attributes = FillAttributesFromTrivia.process_trivia(item.leading_trivia)
    
    @classmethod
    def process_block(cls, block):
        if block == None:
            return
        FillAttributesFromTrivia.process(block.statements)
        for stmt in block.statements:
            if isinstance(stmt, stats.ForeachStatement):
                FillAttributesFromTrivia.process_block(stmt.body)
            elif isinstance(stmt, stats.ForStatement):
                FillAttributesFromTrivia.process_block(stmt.body)
            elif isinstance(stmt, stats.IfStatement):
                FillAttributesFromTrivia.process_block(stmt.then)
                FillAttributesFromTrivia.process_block(stmt.else_)
    
    @classmethod
    def process_method(cls, method):
        if method == None:
            return
        FillAttributesFromTrivia.process([method])
        FillAttributesFromTrivia.process(method.parameters)
        FillAttributesFromTrivia.process_block(method.body)
    
    @classmethod
    def process_file(cls, file):
        FillAttributesFromTrivia.process(file.imports)
        FillAttributesFromTrivia.process(file.enums)
        FillAttributesFromTrivia.process(file.interfaces)
        FillAttributesFromTrivia.process(file.classes)
        FillAttributesFromTrivia.process_block(file.main_block)
        
        for intf in file.interfaces:
            for method in intf.methods:
                FillAttributesFromTrivia.process_method(method)
        
        for cls_ in file.classes:
            FillAttributesFromTrivia.process_method(cls_.constructor_)
            FillAttributesFromTrivia.process(cls_.fields)
            FillAttributesFromTrivia.process(cls_.properties)
            for prop in cls_.properties:
                FillAttributesFromTrivia.process_block(prop.getter)
                FillAttributesFromTrivia.process_block(prop.setter)
            for method in cls_.methods:
                FillAttributesFromTrivia.process_method(method)
    
    @classmethod
    def process_package(cls, pkg):
        for file in pkg.files.values():
            FillAttributesFromTrivia.process_file(file)