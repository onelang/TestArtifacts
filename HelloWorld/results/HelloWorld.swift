class TestClass {
  func testMethod() -> Void {
      let value = 1 + 2 * 3 - 4
      let map_: [String: Int]? = [
        "a": 5,
        "b": 6
      ]
      let text = "Hello world! value = \(value), map[a] = \(map_!["a"]!)"
      print(text)
  }
}

TestClass().testMethod()