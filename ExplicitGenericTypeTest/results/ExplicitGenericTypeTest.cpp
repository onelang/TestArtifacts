#include <one.hpp>
#include <map>
#include <iostream>

class TestClass {
  public:
    void testMethod() {
        auto result = make_shared<vector<string>>(initializer_list<string>{  });
        auto map = make_shared_map<string, int>({
          { "x", 5 }
        });
        auto keys = OneMapHelper::keys(map);
        cout << result << endl;
        cout << keys << endl;
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