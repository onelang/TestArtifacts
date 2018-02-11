#include <one.hpp>
#include <iostream>

class TestClass {
  public:
    void testMethod() {
        auto op = nullptr;
        cout << (op.size()) << endl;
    }

  private:
};

int main()
{
    try {
        TestClass c;
        c.testMethod();
    } catch(std::exception& err) {
        cout << "Exception: " << err.what() << '\n';
    }
    return 0;
}