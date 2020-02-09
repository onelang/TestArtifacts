#include <one.hpp>
#include <iostream>
#include <stdexcept>
#include <vector>

int main()
{
    auto obj1 = OneJson::parse(std::string("{ \"a\":1, \"b\":2 }"));
    if (!obj1->isObject()) {
        throw std::runtime_error(std::string("expected to be object!"));
    }
    auto obj1_props = obj1->asObject()->getProperties();
    if (obj1_props->size() != 2) {
        throw std::runtime_error(std::string("expected 2 properties"));
    }
    if (obj1_props->at(0)->getName() != std::string("a")) {
        throw std::runtime_error(std::string("expected first property to be named 'a'"));
    }
    auto obj1_prop0_value = obj1_props->at(0)->getValue(obj1);
    if (!obj1_prop0_value->isNumber() || obj1_prop0_value->asNumber() != 1) {
        throw std::runtime_error(std::string("expected 'a' to be 1 (number)"));
    }
    std::cout << std::string("b = ") + std::to_string(obj1->asObject()->get(std::string("b"))->asNumber()) << std::endl;
    return 0;
}