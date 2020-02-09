class Calculator {
  func factor(n: Int) -> Int {
      if n <= 1 {
          return 1
      } else {
          return self.factor(n: n - 1) * n
      }
  }
}

print("Hello!")

var arr: [Int]? = [1, 2, 3]
arr!.append(4)

print("n = \(arr!.count), arr[0] = \(arr![0])")

let map: [String: Int]? = [
  "a": 2,
  "b": 3
]
print("map['a'] = \(map!["a"]!), arr[1] = \(arr![1])")

if arr![0] == 1 {
    print("TRUE-X")
} else {
    print("FALSE")
}

var sum = 0
var i = 0
while i < 10 {
    sum += i + 2
    i += 1
}
print(sum)

let calc: Calculator? = Calculator()
print("5! = \(calc!.factor(n: 5))")