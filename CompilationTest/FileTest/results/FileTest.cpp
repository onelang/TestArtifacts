#include <OneLang-File-v0.1/OneFile.hpp>
#include <iostream>

int main()
{
    OneFile::writeText(std::string("test.txt"), std::string("example content"));
    auto file_content = OneFile::readText(std::string("test.txt"));
    std::cout << file_content << std::endl;
    return 0;
}