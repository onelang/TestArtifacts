const BigInteger = require('big-integer');

class MathUtils {
  static calc(n: number) {
    let result = 1;
    for (let i = 2; i <= n; i++) {
        result = result * i;
        if (result > 10) {
            result = result >> 2;
        }
    }
    return result;
  }
  
  static calcBig(n: number) {
    let result = new BigInteger(1);
    for (let i = 2; i <= n; i++) {
        result = result.multiply(i).add(123);
        result = result.add(result);
        if (result.greater(10)) {
            result = result.shiftRight(2);
        }
    }
    return result;
  }
}

console.log(`5 -> ${MathUtils.calc(5)}, 24 -> ${MathUtils.calcBig(24).toString()}`);