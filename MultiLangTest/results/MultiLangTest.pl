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

print(("Hello world!") . "\n");