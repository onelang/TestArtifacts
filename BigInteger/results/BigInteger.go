package main

import "fmt"
import "math/big"
import "one"


type MathUtils struct {
}

func NewMathUtils() *MathUtils {
    this := new(MathUtils)
    return this
}

func MathUtils_Calc(n int) int {
    result := 1
    for i := 2; i <= n; i++ {
        result = result * i
        if result > 10 {
            result = result >> 2
        }
    }
    return result
}

func MathUtils_CalcBig(n int) *big.Int {
    result := big.NewInt(1)
    for i := 2; i <= n; i++ {
        result = one.BI().Add(one.BI().Mul(result, big.NewInt(int64(i))), big.NewInt(int64(123)))
        result = one.BI().Add(result, result)
        if (result).Cmp(big.NewInt(int64(10))) > 0 {
            result = one.BI().Rsh(result, 2)
        }
    }
    return result
}

func main() {
    
    fmt.Println(fmt.Sprintf("5 -> %v, 24 -> %v", MathUtils_Calc(5), MathUtils_CalcBig(24)))
}