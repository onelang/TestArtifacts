#include <one.hpp>
#include <iostream>

int main()
{
    auto file_content = OneFile::readText(std::string("../../../input/test.txt"));
    std::cout << file_content << std::endl;
    return 0;
}