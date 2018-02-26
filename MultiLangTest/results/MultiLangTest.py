class Calculator:
    def factor(self, n):
        if n <= 1:
            return 1
        else:
            return self.factor(n - 1) * n

print "Hello!"

arr = [1, 2, 3]
arr.append(4)

print "n = %s, arr[0] = %s" % (len(arr), arr[0])

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

calc = Calculator()
print "5! = %s" % (calc.factor(5))