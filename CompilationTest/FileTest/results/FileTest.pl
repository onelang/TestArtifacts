use strict;
use warnings;

open my $fh, '<', "../../../input/test.txt" or die "Can't open file $!";
read $fh, my $file_content, -s $fh;
close($fh);
print(($file_content) . "\n");