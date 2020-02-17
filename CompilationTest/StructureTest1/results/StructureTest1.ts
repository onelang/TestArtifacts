class MyList<T> {
    items: OneArray;
}

class Item {
    offset: number = 5;
    strTest: string = "test" + "test2";
    strConstr: string = "constr";

    constructor(strConstr: string) {
        this.strConstr = strConstr;
    }
}

class Container {
    itemList: MyList<Item>;
    stringList: MyList<string>;

    method0() {
    }
  
    method1(str: string) {
        return str;
    }
}

console.log("ok");