#include <iostream>
#include <string>

template<typename K, typename V>
class MapX {
  public:
    V value;

    void set(K key, V value) {
        this->value = value;
    }
    
    V get(K key) {
        return this->value;
    }

  private:
};

class TestClass {
  public:
    void testMethod() {
        auto map_x = std::make_shared<MapX<std::string, int>>();
        map_x->set(std::string("hello"), 3);
        int num_value = map_x->get(std::string("hello2"));
        std::cout << std::string(std::to_string(num_value)) << std::endl;
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