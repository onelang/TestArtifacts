class TestClass {
    getResult() {
        const mapObj = {
            x: 5
        };
        //let containsX = "x" in mapObj;
        //delete mapObj["x"];
        mapObj["x"] = 3;
        return mapObj["x"];
    }
  
    testMethod() {
        console.log(`Result = ${this.getResult()}`);
    }
}

try {
    new TestClass().testMethod();
} catch(e) {
    console.log(`Exception: ${e.message}`);
}