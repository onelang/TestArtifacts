require 'json'

obj1 = JSON.parse("{ \"a\":1, \"b\":2 }")
if !obj1.is_a?(Hash)
    raise "expected to be object!"
end
obj1_props = obj1.keys
if obj1_props.length != 2
    raise "expected 2 properties"
end
if obj1_props[0] != "a"
    raise "expected first property to be named 'a'"
end
obj1_prop0_value = obj1[obj1_props[0]]
if !obj1_prop0_value.is_a?(Numeric) || obj1_prop0_value != 1
    raise "expected 'a' to be 1 (number)"
end
puts "b = #{obj1["b"]}"