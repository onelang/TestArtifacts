class StrLenInferIssue {
    public static Integer test(String str) throws Exception
    {
        return str.length();
    }
}

class Program {
    public static void main(String[] args) throws Exception {
        System.out.println(StrLenInferIssue.test("hello"));
    }
}