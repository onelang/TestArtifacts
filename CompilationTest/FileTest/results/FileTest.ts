const fs = require('fs');

fs.writeFileSync("test.txt", "example content");
const fileContent = fs.readFileSync("test.txt", 'utf-8');
console.log(fileContent);