package main

import "fmt"
import "encoding/json"
import "reflect"


func main() {
    var obj1 interface{}
    json.Unmarshal([]byte("{ \"a\":1, \"b\":2 }"), &obj1)
    if !(reflect.ValueOf(obj1).Kind() == reflect.Map) {
        panic("expected to be object!")
    }
    obj1Props := make([]string, 0, len(obj1.(map[string]interface{})))
    for key, _ := range obj1.(map[string]interface{}) {
      obj1Props = append(obj1Props, key)
    }
    if len(obj1Props) != 2 {
        panic("expected 2 properties")
    }
    if obj1Props[0] != "a" {
        panic("expected first property to be named 'a'")
    }
    obj1Prop0Value := obj1.(map[string]interface{})[obj1Props[0]]
    if !(reflect.ValueOf(obj1Prop0Value).Kind() == reflect.Float64) || int(obj1Prop0Value.(float64)) != 1 {
        panic("expected 'a' to be 1 (number)")
    }
    fmt.Println(fmt.Sprintf("b = %v", int(obj1.(map[string]interface{})["b"].(float64))))
}