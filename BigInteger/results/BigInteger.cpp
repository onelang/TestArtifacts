#include <one.hpp>
#include <iostream>

class MathUtils {
  public:
    static int calc(int n) {
        int result = 1;
        for (int i = 2; i <= n; i++) {
            result = result * i;
            if (result > 10) {
                result = result >> 2;
            }
        }
        return result;
    }
    
    static sp<OneBigInteger> calcBig(int n) {
        auto result = OneBigInteger::fromInt(1);
        for (int i = 2; i <= n; i++) {
            result = result * i + 123;
            result = result + result;
            if (result > 10) {
                result = result >> 2;
            }
        }
        return result;
    }

  private:
};

int main()
{
    
    cout << (string() + "5 -> " + to_string(MathUtils::calc(5)) + ", 24 -> " + MathUtils::calcBig(24)) << endl;
    return 0;
}