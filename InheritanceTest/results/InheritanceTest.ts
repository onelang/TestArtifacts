interface IPrinterBase {
  someBaseFunc();
}

interface IPrinter extends IPrinterBase {
  printIt();
}

class BasePrinter implements IPrinter {
  numValue: number = 42;

  getValue() {
    return "Base";
  }
  
  printIt() {
    console.log(`BasePrinter: ${this.getValue()}`);
  }
  
  someBaseFunc() {
    return this.numValue;
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
    
    const baseP2 = new BasePrinter();
    const childP2 = new ChildPrinter();
    console.log(`${baseP2.numValue} == ${childP2.numValue}`);
  }
}

try {
  new TestClass().testMethod();
} catch(e) {
  console.log(`Exception: ${e.message}`);
}