#include <OneLang-Core-v0.1/one.hpp>
#include <iostream>
#include <map>
#include <string>

class TestClass {
  public:
    int mapTest() {
        auto map_obj = one::make_shared_map<std::string, int>({
          { "x", 5 },
          { "y", 3 }
        });
        
        //let containsX = "x" in mapObj;
        map_obj->operator[](std::string("z")) = 9;
        map_obj->erase(std::string("x"));
        
        auto keys_var = OneMapHelper::keys(map_obj);
        auto values_var = OneMapHelper::values(map_obj);
        return map_obj->operator[](std::string("z"));
    }
    
    void explicitTypeTest() {
        auto op = std::string("");
        std::cout << op.size() << std::endl;
    }
    
    std::string ifTest(int x) {
        auto result = std::string("<unk>");
        
        if (x > 3) {
            result = std::string("hello");
        } else if (x < 1) {
            result = std::string("bello");
        } else if (x < 0) {
            result = std::string("bello2");
        } else {
            result = std::string("???");
        }
        
        if (x > 3) {
            result = std::string("z");
        }
        
        if (x > 3) {
            result = std::string("x");
        } else {
            result = std::string("y");
        }
        
        return result;
    }
    
    void arrayTest() {
        //const c2 = new Class2();
        
        auto mutable_arr = std::make_shared<std::vector<int>>(std::initializer_list<int>{ 1, 2 });
        mutable_arr->push_back(3);
        mutable_arr->push_back(4);
        // mutableArr.push(c2.property);
        // mutableArr.push(c2.child.property);
        // mutableArr.push(c2.child.child.property);
        
        auto constant_arr = std::make_shared<std::vector<int>>(std::initializer_list<int>{ 5, 6 });
        
        // some comment
        //   some comment line 2
        for (auto it = mutable_arr->begin(); it != mutable_arr->end(); ++it) {
            auto item = *it;
            std::cout << item << std::endl;
        }
        
        /* some other comment
           multiline and stuff
        */
        for (int i = 0; i < constant_arr->size(); i++) {
            std::cout << constant_arr->at(i) << std::endl;
        }
    }
    
    int calc() {
        return (1 + 2) * 3;
    }
    
    int methodWithArgs(int arg1, int arg2, int arg3) {
        int stuff = arg1 + arg2 + arg3 * this->calc();
        return stuff;
    }
    
    std::string stringTest() {
        auto x = std::string("x");
        auto y = std::string("y");
        
        auto z = std::string("z");
        z += std::string("Z");
        z += x;
        
        return z + std::string("|") + x + y;
    }
    
    std::string reverseString(std::string str) {
        auto result = std::string("");
        for (int i = str.size() - 1; i >= 0; i--) {
            result += str[i];
        }
        return result;
    }
    
    bool getBoolResult(bool value) {
        return value;
    }
    
    void testMethod() {
        this->arrayTest();
        std::cout << this->mapTest() << std::endl;
        std::cout << this->stringTest() << std::endl;
        std::cout << this->reverseString(std::string("print value")) << std::endl;
        std::cout << (this->getBoolResult(true) ? std::string("true") : std::string("false")) << std::endl;
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