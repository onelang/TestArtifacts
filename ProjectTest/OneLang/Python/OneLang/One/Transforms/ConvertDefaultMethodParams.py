from OneLangStdLib import *
import OneLang.One.AstTransformer as astTrans

class ConvertDefaultMethodParams(astTrans.AstTransformer):
    def __init__(self):
        super().__init__("ConvertDefaultMethodParams")