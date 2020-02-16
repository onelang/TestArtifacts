class StrLenInferIssue:
    @staticmethod
    def test(str):
        return len(str)

print StrLenInferIssue.test("hello")