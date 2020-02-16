class StrLenInferIssue {
    static test(str: string) {
        return str.length;
    }
}

console.log(StrLenInferIssue.test("hello"));