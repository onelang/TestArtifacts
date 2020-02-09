use strict;
use warnings;

use bigint;

package MathUtils;

sub new
{
    my $class = shift;
    my $self = {};
    bless $self, $class;
    return $self;
}

sub calc {
    my ( $n ) = @_;
    my $result = 1;
    for (my $i = 2; $i <= $n; $i++) {
        $result = $result * $i;
        if ($result > 10) {
            $result = $result >> 2;
        }
    }
    return $result;
}

sub calc_big {
    my ( $n ) = @_;
    my $result = 1;
    for (my $i = 2; $i <= $n; $i++) {
        $result = $result * $i + 123;
        $result = $result + $result;
        if ($result > 10) {
            $result = $result >> 2;
        }
    }
    return $result;
}

print(("5 -> @{[MathUtils::calc(5)]}, 24 -> @{[MathUtils::calc_big(24)]}") . "\n");