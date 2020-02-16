class StrLenInferIssue {
  class func test(str: String) -> Int {
      return str.count
  }
}

print(StrLenInferIssue.test(str: "hello"))