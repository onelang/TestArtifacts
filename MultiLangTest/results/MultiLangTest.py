class Calculator:
    def calc(self):
        return 4

print "Hello!"

calc = Calculator()
print "n = %s" % (calc.calc())

arr = [1, 2, 3]
map = {
  "a": 2,
  "b": 3,
}
print "map['a'] = %s, arr[1] = %s" % (map["a"], arr[1])

if arr[0] == 1:
    print "TRUE-X"
else:
    print "FALSE"

sum = 0
i = 0
while i < 10:
    sum += i + 2
    i += 1
print sum