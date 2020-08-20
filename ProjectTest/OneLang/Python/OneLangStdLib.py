import glob
import os.path
import re
import math
import json

def static_init(cls):
    if getattr(cls, "static_init", None):
        cls.static_init()
    return cls

class Fs:
    @staticmethod
    def read_file_sync(fn, encoding):
        with open(fn, "rt") as f: return f.read()

class Glob:
    def sync(self, dir, config):
        files = glob.glob(dir, recursive=True)
        if "nodir" in config:
            return list(filter(os.path.isfile, files))
        return files

class Path:
    @staticmethod
    def relative(dir, fn):
        if fn.startswith(dir):
            return fn[len(dir):]
        raise "not implemented"

class Object:
    @staticmethod
    def keys(obj):
        return obj.keys()

    @staticmethod
    def values(obj):
        return obj.values()

class RegExp:
    def __init__(self, pattern, modifiers = ""):
        self.last_index = 0
        self.pattern = pattern
        self.modifiers = modifiers
    
    def exec(self, input):
        patternObj = re.compile(self.pattern)
        match = patternObj.match(input, self.last_index)
        if not match:
            return None
        g0 = match.group(0)
        self.last_index = match.pos + len(g0)
        return [g0] + list(match.groups())

def parseInt(str):
    return int(str)

def import_(name):
    if name == "fs":
        return Fs()
    elif name == "glob":
        return Glob()
    elif name == "path":
        return Path()
    raise f"import not found: {name}"

def find(predicate, items):
    for item in items:
        if predicate(item):
            return item
    return None

class Map:
    def __init__(self):
        self.data = {}

    def set(self, key, value):
        self.data[key] = value

    def get(self, key):
        return self.data[key]
    
    def has(self, key):
        return key in self.data

    def delete(self, key):
        del self.data[key]

    def values(self):
        return self.data.values()

class Array:
    @staticmethod
    def from_(arrayLike):
        return list(arrayLike)

class Math:
    @staticmethod
    def floor(num):
        return math.floor(num)

class Error(BaseException):
    def __init__(self, msg):
        self.msg = msg

class Console:
    @staticmethod
    def error(msg):
        print(f"\033[91m{msg}\033[0m")

console = Console()

class JSON:
    @staticmethod
    def stringify(obj):
        return json.dumps(obj)

class ArrayHelper:
    def every(predicate, items):
        for i in range(len(items)):
            if not predicate(items[i], i):
                return False
        return True

    def some(predicate, items):
        for item in items:
            if predicate(item):
                return True
        return False
        