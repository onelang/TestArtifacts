package main

import "fmt"

type Calculator struct {
}

func NewCalculator() *Calculator {
    this := new(Calculator)
    return this
}

func (this *Calculator) Factor(n int) int {
    if n <= 1 {
        return 1
    } else {
        return this.Factor(n - 1) * n
    }
}

func main() {
    fmt.Println("Hello!")
    
    arr := []int{1, 2, 3}
    arr = append(arr, 4)
    
    fmt.Println(fmt.Sprintf("n = %v, arr[0] = %v", len(arr), arr[0]))
    
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
    
    calc := NewCalculator()
    fmt.Println(fmt.Sprintf("5! = %v", calc.Factor(5)))
}