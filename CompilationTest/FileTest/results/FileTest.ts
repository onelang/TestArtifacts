const fs = require('fs');

const fileContent = fs.readFileSync("../../../input/test.txt", 'utf-8');
console.log(fileContent);