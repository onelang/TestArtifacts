class Calculator {
  func calc() -> Int {
      return 4
  }
}

print("Hello!")

let calc: Calculator? = Calculator()
print("n = \(calc!.calc())")

let arr: [Int]? = [1, 2, 3]
print("arr[1] = \(arr![1])")