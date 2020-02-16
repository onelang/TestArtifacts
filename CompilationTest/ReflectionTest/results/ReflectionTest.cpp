#include <OneLang-Core-v0.1/one.hpp>
#include <OneLang-Reflect-v0.1/one.hpp>
#include <any>
#include <iostream>
#include <string>

class TargetClass : public ReflectedClass {
  public:
    int instance_field = 5;
    static std::string static_field;

    static std::string staticMethod(std::string arg1) {
        return std::string("arg1 = ") + arg1 + ", staticField = " + TargetClass::static_field;
    }
    
    std::string instanceMethod() {
        return std::string("instanceField = ") + std::to_string(this->instance_field);
    }

  private:
};

std::string TargetClass::static_field = std::string("hello");

static struct ReflectionTargetClass
{
  ReflectionTargetClass() {
    OneReflect::addClass("TargetClass", typeid(TargetClass))
      .addField(std::make_shared<OneField>("instance_field", /*isStatic*/ false, "int", 
          [](one::sp<ReflectedClass> obj){ return (std::any)std::static_pointer_cast<TargetClass>(obj)->instance_field; },
          [](one::sp<ReflectedClass> obj, std::any value){ std::static_pointer_cast<TargetClass>(obj)->instance_field = std::any_cast<int>(value); }))
      .addField(std::make_shared<OneField>("static_field", /*isStatic*/ true, "std::string", 
          [](one::sp<ReflectedClass> obj){ return (std::any)TargetClass::static_field; },
          [](one::sp<ReflectedClass> obj, std::any value){ TargetClass::static_field = std::any_cast<std::string>(value); }))
      .addMethod(std::make_shared<OneMethod>("staticMethod", /*isStatic*/ true, "std::string", std::vector<OneMethodArgument> {
          OneMethodArgument("arg1", "std::string"),
        }, 
        [](one::sp<ReflectedClass> obj, one::vec<std::any> args){ 
          return (std::any)TargetClass::staticMethod(std::any_cast<std::string>(args->at(0))); 
        }))
      .addMethod(std::make_shared<OneMethod>("instanceMethod", /*isStatic*/ false, "std::string", std::vector<OneMethodArgument> {
        }, 
        [](one::sp<ReflectedClass> obj, one::vec<std::any> args){ 
          return (std::any)std::static_pointer_cast<TargetClass>(obj)->instanceMethod(); 
        }))
      ;
  }
} _reflectionTargetClass;

class TestClass {
  public:
    void testMethod() {
        auto obj = std::make_shared<TargetClass>();
        //console.log(`instanceMethod (direct): ${obj.instanceMethod()}`);
        //console.log(`staticMethod (direct): ${TargetClass.staticMethod("arg1value")}`);
        //console.log(`instanceField (direct): ${obj.instanceField}`);
        //console.log(`staticField (direct): ${TargetClass.staticField}`);
        auto cls = OneReflect::getClass(obj);
        if (cls == nullptr) {
            std::cout << std::string("cls is null!") << std::endl;
            return;
        }
        auto cls2 = OneReflect::getClassByName(std::string("TargetClass"));
        if (cls2 == nullptr) {
            std::cout << std::string("cls2 is null!") << std::endl;
            return;
        }
        
        auto method1 = cls->getMethod(std::string("instanceMethod"));
        if (method1 == nullptr) {
            std::cout << std::string("method1 is null!") << std::endl;
            return;
        }
        auto method1_result = method1->call(obj, std::make_shared<std::vector<std::any>>(std::initializer_list<std::any>{  }));
        std::cout << std::string("instanceMethod: ") + method1_result << std::endl;
        
        auto method2 = cls->getMethod(std::string("staticMethod"));
        if (method2 == nullptr) {
            std::cout << std::string("method2 is null!") << std::endl;
            return;
        }
        auto method2_result = method2->call(nullptr, std::make_shared<std::vector<std::any>>(std::initializer_list<std::any>{ std::string("arg1value") }));
        std::cout << std::string("staticMethod: ") + method2_result << std::endl;
        
        auto field1 = cls->getField(std::string("instanceField"));
        if (field1 == nullptr) {
            std::cout << std::string("field1 is null!") << std::endl;
            return;
        }
        field1->setValue(obj, 6);
        auto field1_new_val = field1->getValue(obj);
        std::cout << std::string("new instance field value: ") + std::to_string(obj->instance_field) + " == " + field1_new_val << std::endl;
        
        auto field2 = cls->getField(std::string("staticField"));
        if (field2 == nullptr) {
            std::cout << std::string("field2 is null!") << std::endl;
            return;
        }
        field2->setValue(nullptr, std::string("bello"));
        auto field2_new_val = field2->getValue(nullptr);
        std::cout << std::string("new static field value: ") + TargetClass::static_field + " == " + field2_new_val << std::endl;
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