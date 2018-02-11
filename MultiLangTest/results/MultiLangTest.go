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
    fmt.Println(fmt.Sprintf("arr[1] = %v", arr[1]))
}