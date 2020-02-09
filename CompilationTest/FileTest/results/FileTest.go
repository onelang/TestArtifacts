package main

import "fmt"
import "io/ioutil"


func main() {
    fileContentBytes, _ := ioutil.ReadFile("../../../input/test.txt")
    fileContent := string(fileContentBytes)
    fmt.Println(fileContent)
}