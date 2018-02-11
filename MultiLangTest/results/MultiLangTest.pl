use strict;
use warnings;

package Calculator;

sub new
{
    my $class = shift;
    my $self = {};
    bless $self, $class;
    return $self;
}

sub calc {
    my ( $self ) = @_;
    return 4;
}

print(("Hello!") . "\n");

my $calc = new Calculator();
print(("n = @{[$calc->calc()]}") . "\n");

my $arr = [1, 2, 3];
my $map = {
  a => 2,
  b => 3,
};
print(("map['a'] = @{[${$map}{\"a\"}]}, arr[1] = @{[${$arr}[1]]}") . "\n");

if (${$arr}[0] == 1) {
    print(("TRUE-X") . "\n");
} else {
    print(("FALSE") . "\n");
}

my $sum = 0;
for (my $i = 0; $i < 10; $i++) {
    $sum += $i + 2;
}
print(($sum) . "\n");