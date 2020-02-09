package main

import "fmt"

type TestClass struct {
}

func NewTestClass() *TestClass {
    this := new(TestClass)
    return this
}

func (this *TestClass) GetResult() int {
    mapObj := map[string]int{
      "x": 5,
    }
    //let containsX = "x" in mapObj;
    //delete mapObj["x"];
    mapObj["x"] = 3
    return mapObj["x"]
}

func (this *TestClass) TestMethod() {
    fmt.Println(fmt.Sprintf("Result = %v", this.GetResult()))
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