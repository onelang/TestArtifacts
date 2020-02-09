package main

import "fmt"

type TestClass struct {
}

func NewTestClass() *TestClass {
    this := new(TestClass)
    return this
}

func (this *TestClass) TestMethod() {
    value := 1 + 2 * 3 - 4
    map_ := map[string]int{
      "a": 5,
      "b": 6,
    }
    text := fmt.Sprintf("Hello world! value = %v, map[a] = %v", value, map_["a"])
    fmt.Println(text)
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