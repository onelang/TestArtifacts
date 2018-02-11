class Calculator {
  func calc() -> Int {
      return 4
  }
}

print("Hello!")

let calc: Calculator? = Calculator()
print("n = \(calc!.calc())")

let arr: [Int]? = [1, 2, 3]
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