class MathUtils:
    @staticmethod
    def calc(n):
        result = 1
        i = 2
        while i <= n:
            result = result * i
            if result > 10:
                result = result >> 2
            i += 1
        return result
    
    @staticmethod
    def calc_big(n):
        result = 1
        i = 2
        while i <= n:
            result = result * i + 123
            result = result + result
            if result > 10:
                result = result >> 2
            i += 1
        return result


print "5 -> %s, 24 -> %s" % (MathUtils.calc(5), MathUtils.calc_big(24))