class TestClass:
    def test_method(self):
        op = "x"
        print len(op)

try:
    TestClass().test_method()
except Exception as err:
    print "Exception: " + err.message