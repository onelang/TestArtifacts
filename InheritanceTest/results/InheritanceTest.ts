interface IPrinterBase {
  someBaseFunc();
}

interface IPrinter extends IPrinterBase {
  printIt();
}

class BasePrinter implements IPrinter {
  getValue() {
    return "Base";
  }
  
  printIt() {
    console.log(`BasePrinter: ${this.getValue()}`);
  }
  
  someBaseFunc() {
    return 42;
  }
}

class ChildPrinter extends BasePrinter {
  getValue() {
    return "Child";
  }
}

class TestClass {
  getPrinter(name: string) {
    const result = name == "child" ? new ChildPrinter() : new BasePrinter();
    return result;
  }
  
  testMethod() {
    const basePrinter = this.getPrinter("base");
    const childPrinter = this.getPrinter("child");
    basePrinter.printIt();
    childPrinter.printIt();
    console.log(`${basePrinter.someBaseFunc()} == ${childPrinter.someBaseFunc()}`);
  }
}

try {
  new TestClass().testMethod();
} catch(e) {
  console.log(`Exception: ${e.message}`);
}