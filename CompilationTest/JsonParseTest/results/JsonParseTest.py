import json

obj1 = json.loads("{ \"a\":1, \"b\":2 }")
if not isinstance(obj1, dict):
    raise Exception("expected to be object!")
obj1_props = obj1.keys()
if len(obj1_props) != 2:
    raise Exception("expected 2 properties")
if obj1_props[0] != "a":
    raise Exception("expected first property to be named 'a'")
obj1_prop0_value = obj1[obj1_props[0]]
if not isinstance(obj1_prop0_value, (int, long, float)) or obj1_prop0_value != 1:
    raise Exception("expected 'a' to be 1 (number)")
print "b = %s" % (obj1["b"])