#include <one.hpp>
#include <iostream>
#include <map>
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
    auto map = make_shared_map<string, int>({
      { "a", 2 },
      { "b", 3 }
    });
    cout << (string() + "map['a'] = " + to_string(map->operator[](string("a"))) + ", arr[1] = " + to_string(arr->at(1))) << endl;
    
    if (arr->at(0) == 1) {
        cout << (string("TRUE-X")) << endl;
    } else {
        cout << (string("FALSE")) << endl;
    }
    
    int sum = 0;
    for (int i = 0; i < 10; i++) {
        sum += i + 2;
    }
    cout << (sum) << endl;
    return 0;
}