package main

import "fmt"

type Calculator struct {
}

func NewCalculator() *Calculator {
    this := new(Calculator)
    return this
}

func (this *Calculator) Calc() int {
    return 4
}

func main() {
    fmt.Println("Hello!")
    
    calc := NewCalculator()
    fmt.Println(fmt.Sprintf("n = %v", calc.Calc()))
    
    arr := []int{1, 2, 3}
    map := map[string]int{
      "a": 2,
      "b": 3,
    }
    fmt.Println(fmt.Sprintf("map['a'] = %v, arr[1] = %v", map["a"], arr[1]))
    
    if arr[0] == 1 {
        fmt.Println("TRUE-X")
    } else {
        fmt.Println("FALSE")
    }
    
    sum := 0
    for i := 0; i < 10; i++ {
        sum += i + 2
    }
    fmt.Println(sum)
}