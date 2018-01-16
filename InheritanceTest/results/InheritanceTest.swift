protocol IPrinterBase {
    func someBaseFunc() -> Int;
}

protocol IPrinter: IPrinterBase {
    func printIt() -> Void;
}

class BasePrinter: IPrinter {
  var numValue: Int = 42

  func getValue() -> String {
      return "Base"
  }

  func printIt() -> Void {
      print("BasePrinter: \(self.getValue())")
  }

  func someBaseFunc() -> Int {
      return self.numValue
  }
}

class ChildPrinter: BasePrinter {
  override func getValue() -> String {
      return "Child"
  }
}

class TestClass {
  func getPrinter(name: String) -> IPrinter? {
      let result: IPrinter? = name == "child" ? ChildPrinter() : BasePrinter()
      return result
  }

  func testMethod() -> Void {
      let basePrinter: IPrinter? = self.getPrinter(name: "base")
      let childPrinter: IPrinter? = self.getPrinter(name: "child")
      basePrinter!.printIt()
      childPrinter!.printIt()
      print("\(basePrinter!.someBaseFunc()) == \(childPrinter!.someBaseFunc())")
      
      let baseP2: BasePrinter? = BasePrinter()
      let childP2: ChildPrinter? = ChildPrinter()
      print("\(baseP2!.numValue) == \(childP2!.numValue)")
  }
}

TestClass().testMethod()