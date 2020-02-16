class TestClass {
  public:
    void methodTest(vec<std::string> method_param) {
    }
    
    void testMethod() {
    }

  private:
};

int main()
{
    try {
        TestClass c;
        c.testMethod();
    } catch(std::exception& err) {
        std::cout << "Exception: " << err.what() << '\n';
    }
    return 0;
}