<?php

file_put_contents("test.txt", "example content");
$file_content = file_get_contents("test.txt");
print($file_content . "\n");