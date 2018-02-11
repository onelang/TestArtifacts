package main

import "fmt"

type Calculator struct {
}

func NewCalculator() *Calculator {
    this := new(Calculator)
    return this
}

func (this *Calculator) Calc() {
    return 4
}

func main() {
    fmt.Println("Hello world!")
}