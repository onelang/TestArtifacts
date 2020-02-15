package main

import "fmt"
import "io/ioutil"


func main() {
    ioutil.WriteFile("test.txt", []byte("example content"), 0644)
    fileContentBytes, _ := ioutil.ReadFile("test.txt")
    fileContent := string(fileContentBytes)
    fmt.Println(fileContent)
}