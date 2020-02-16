package main

import "fmt"

type StrLenInferIssue struct {
}

func NewStrLenInferIssue() *StrLenInferIssue {
    this := new(StrLenInferIssue)
    return this
}

func StrLenInferIssue_Test(str string) int {
    return len(str)
}

func main() {
    fmt.Println(StrLenInferIssue_Test("hello"))
}