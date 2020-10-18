public class main {
  public static void main(String[] args) {
    System.exit(new SelfTestRunner("../../../../../").runTest(new CsharpGenerator()) ? 0 : 1);
  }
}