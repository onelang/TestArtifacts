protocol IPrinterBase {
    func someBaseFunc() -> Int;
}

protocol IPrinter: IPrinterBase {
    func printIt() -> Void;
}

class BasePrinter: IPrinter {
  func getValue() -> String {
      return "Base"
  }

  func printIt() -> Void {
      print("BasePrinter: \(self.getValue())")
  }

  func someBaseFunc() -> Int {
      return 42
  }
}

class ChildPrinter: BasePrinter {
  override func getValue() -> String {
      return "Child"
  }
}

class TestClass {
  func getPrinter(name: String) -> IPrinter {
      let result = name == "child" ? ChildPrinter() : BasePrinter()
      return result
  }

  func testMethod() -> Void {
      let basePrinter = self.getPrinter(name: "base")
      let childPrinter = self.getPrinter(name: "child")
      basePrinter.printIt()
      childPrinter.printIt()
      print("\(basePrinter.someBaseFunc()) == \(childPrinter.someBaseFunc())")
  }
}

TestClass().testMethod()