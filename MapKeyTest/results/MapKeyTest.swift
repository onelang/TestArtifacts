class TestClass {
  func testMethod() -> Void {
      let map: [String: Any?]? = [
      ]
      let _: [String]? = Array(map!.keys)
  }
}

TestClass().testMethod()