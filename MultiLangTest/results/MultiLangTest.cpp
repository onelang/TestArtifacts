#include <one.hpp>
#include <iostream>
#include <vector>

class Calculator {
  public:
    int calc() {
        return 4;
    }

  private:
};

int main()
{
    cout << (string("Hello!")) << endl;
    
    auto calc = make_shared<Calculator>();
    cout << (string() + "n = " + to_string(calc->calc())) << endl;
    
    auto arr = make_shared<vector<int>>(initializer_list<int>{ 1, 2, 3 });
    cout << (string() + "arr[1] = " + to_string(arr->at(1))) << endl;
    return 0;
}