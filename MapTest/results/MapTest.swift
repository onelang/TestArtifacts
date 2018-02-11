class TestClass {
  func getResult() -> Int {
      var mapObj: [String: Int]? = [
        "x": 5
      ]
      //let containsX = "x" in mapObj;
      //delete mapObj["x"];
      mapObj!["x"] = 3
      return mapObj!["x"]!
  }

  func testMethod() -> Void {
      print("Result = \(self.getResult())")
  }
}

TestClass().testMethod()