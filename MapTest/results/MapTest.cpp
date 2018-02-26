#include <one.hpp>
#include <iostream>
#include <map>

class TestClass {
  public:
    int getResult() {
        auto map_obj = make_shared_map<string, int>({
          { "x", 5 }
        });
        //let containsX = "x" in mapObj;
        //delete mapObj["x"];
        map_obj->operator[](string("x")) = 3;
        return map_obj->operator[](string("x"));
    }
    
    void testMethod() {
        cout << string("Result = ") + to_string(this->getResult()) << endl;
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