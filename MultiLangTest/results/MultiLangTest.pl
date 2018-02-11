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
print(("arr[1] = @{[${$arr}[1]]}") . "\n");