use strict;
use warnings;

open my $fh2, '>', "test.txt" or die "Can't open file $!";
print $fh2, "example content", -s $fh2;
close $fh2;;
open my $fh, '<', "test.txt" or die "Can't open file $!";
read $fh, my $file_content, -s $fh;
close($fh);
print(($file_content) . "\n");