class TestClass {
  func testMethod() -> Void {
      let result: [String]? = ["y"]
      let map: [String: Int]? = [
        "x": 5
      ]
      let keys: [String]? = Array(map!.keys)
      print(result![0])
      print(keys![0])
  }
}

TestClass().testMethod()