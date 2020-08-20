from OneLangStdLib import *

class ArrayHelper:
    def __init__(self):
        pass
    
    @classmethod
    def sort_by(cls, items, key_selector):
        return items.sort(lambda a, b: key_selector(a) - key_selector(b))
    
    @classmethod
    def remove_last_n(cls, items, count):
        items.splice(len(items) - count, count)