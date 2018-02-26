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

sub factor {
    my ( $self, $n ) = @_;
    if ($n <= 1) {
        return 1;
    } else {
        return $self->factor($n - 1) * $n;
    }
}

print(("Hello!") . "\n");

my $arr = [1, 2, 3];
push @{$arr}, 4;

print(("n = @{[scalar(@{$arr})]}, arr[0] = @{[${$arr}[0]]}") . "\n");

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

my $calc = new Calculator();
print(("5! = @{[$calc->factor(5)]}") . "\n");