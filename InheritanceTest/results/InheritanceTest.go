package main

import "fmt"
type IPrinterBase interface {
    SomeBaseFunc() int 
}

type IPrinter interface {
    IPrinterBase
    PrintIt() 
}

type BasePrinter struct {
    NumValue int
}

func NewBasePrinter() *BasePrinter {
    this := new(BasePrinter)
    this.NumValue = 42
    return this
}

func (this *BasePrinter) GetValue() string {
    return "Base"
}

func (this *BasePrinter) PrintIt() {
    fmt.Println(fmt.Sprintf("BasePrinter: %v", this.GetValue()))
}

func (this *BasePrinter) SomeBaseFunc() int {
    return this.NumValue
}

type ChildPrinter struct {
    BasePrinter
}

func NewChildPrinter() *ChildPrinter {
    this := new(ChildPrinter)
    return this
}

func (this *ChildPrinter) GetValue() string {
    return "Child"
}

type TestClass struct {
}

func NewTestClass() *TestClass {
    this := new(TestClass)
    return this
}

func (this *TestClass) GetPrinter(name string) IPrinter {
    var tmp0 IPrinter
    if name == "child" {
      tmp0 = NewChildPrinter()
    } else {
      tmp0 = NewBasePrinter()
    }
    result := tmp0
    return result
}

func (this *TestClass) TestMethod() {
    basePrinter := this.GetPrinter("base")
    childPrinter := this.GetPrinter("child")
    basePrinter.PrintIt()
    childPrinter.PrintIt()
    fmt.Println(fmt.Sprintf("%v == %v", basePrinter.SomeBaseFunc(), childPrinter.SomeBaseFunc()))
    
    baseP2 := NewBasePrinter()
    childP2 := NewChildPrinter()
    fmt.Println(fmt.Sprintf("%v == %v", baseP2.NumValue, childP2.NumValue))
}

func init() {
}

func main() {
    defer func() {
      if r := recover(); r != nil {
          fmt.Print("Exception: ", r)
      }
    }()

    c := NewTestClass()
    c.TestMethod();
}