package main

import "fmt"

type TestClass struct {
}

func NewTestClass() *TestClass {
    this := new(TestClass)
    return this
}

func (this *TestClass) TestMethod() {
    result := []string{"y"}
    map := map[string]int{
      "x": 5,
    }
    keys := make([]string, 0, len(map))
    for  key, _ := range map {
      keys = append(keys, key)
    }
    fmt.Println(result[0])
    fmt.Println(keys[0])
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