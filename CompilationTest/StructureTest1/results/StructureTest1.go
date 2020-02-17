package main

import "fmt"

type MyList struct {
    Items []interface{}
}

func NewMyList() *MyList {
    this := new(MyList)
    return this
}

type Item struct {
    Offset int
    StrTest string
    StrConstr string
}

func NewItem(strConstr string) *Item {
    this := new(Item)
    this.Offset = 5
    this.StrTest = "test" + "test2"
    this.StrConstr = "constr"
    this.StrConstr = strConstr
    return this
}

type Container struct {
    ItemList *MyList<Item>
    StringList *MyList<string>
}

func NewContainer() *Container {
    this := new(Container)
    return this
}

func (this *Container) Method0() {
}

func (this *Container) Method1(str string) string {
    return str
}

func main() {
    fmt.Println("ok")
}