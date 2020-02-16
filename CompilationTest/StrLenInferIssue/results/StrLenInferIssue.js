class StrLenInferIssue {
  static test(str) {
    return str.length;
  }
}

console.log(StrLenInferIssue.test("hello"));