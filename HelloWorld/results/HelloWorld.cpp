#include <one.hpp>
#include <memory>
#include <fstream>
#include <vector>
#include <map>
#include <iostream>

class TestClass {
  public:
    void testMethod() {
        int value = 1 + 2 * 3 - 4;
        auto map_ = map<string, int> {
          { "a", 5 },
          { "b", 6 }
        };
        auto text = string() + "Hello world! value = " + to_string(value) + ", map[a] = " + to_string(map_[string("a")]);
        cout << (text) << endl;
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