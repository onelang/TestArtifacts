package main

import "fmt"

type TestClass struct {
}

func NewTestClass() *TestClass {
    this := new(TestClass)
    return this
}

func (this *TestClass) TestMethod() {
    map := map[string]interface{}{
    }
    keys := make([]string, 0, len(map))
    for  key, _ := range map {
      keys = append(keys, key)
    }
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