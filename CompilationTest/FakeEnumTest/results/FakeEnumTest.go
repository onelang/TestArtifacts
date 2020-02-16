package main

import "fmt"

type TokenType struct {
}

func NewTokenType() *TokenType {
    this := new(TokenType)
    return this
}

var TokenTypeEndToken string = "EndToken";
var TokenTypeWhitespace string = "Whitespace";
var TokenTypeIdentifier string = "Identifier";
var TokenTypeOperatorX string = "Operator";
var TokenTypeNoInitializer string;

func main() {
    casingTest := TokenTypeEndToken
    fmt.Println(casingTest)
}