#include <iostream>
#include <string>

class IPrinterBase {
  public:
    virtual int someBaseFunc() = 0;
};

class IPrinter : public IPrinterBase {
  public:
    virtual void printIt() = 0;
};

class BasePrinter : public IPrinter {
  public:
    int num_value = 42;

    virtual std::string getValue() {
        return std::string("Base");
    }
    
    void printIt() {
        std::cout << std::string("BasePrinter: ") + this->getValue() << std::endl;
    }
    
    int someBaseFunc() {
        return this->num_value;
    }

  private:
};

class ChildPrinter : public BasePrinter {
  public:
    std::string getValue() {
        return std::string("Child");
    }

  private:
};

class TestClass {
  public:
    one::sp<IPrinter> getPrinter(std::string name) {
        auto result = name == std::string("child") ? std::make_shared<ChildPrinter>() : std::make_shared<BasePrinter>();
        return result;
    }
    
    void testMethod() {
        auto base_printer = this->getPrinter(std::string("base"));
        auto child_printer = this->getPrinter(std::string("child"));
        base_printer->printIt();
        child_printer->printIt();
        std::cout << std::string(std::to_string(base_printer->someBaseFunc())) + " == " + std::to_string(child_printer->someBaseFunc()) << std::endl;
        
        auto base_p2 = std::make_shared<BasePrinter>();
        auto child_p2 = std::make_shared<ChildPrinter>();
        std::cout << std::string(std::to_string(base_p2->num_value)) + " == " + std::to_string(child_p2->num_value) << std::endl;
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