use strict;
use warnings;

package TokenType;

sub new
{
    my $class = shift;
    my $self = {};
    bless $self, $class;
    return $self;
}

our $end_token = "EndToken";
our $whitespace = "Whitespace";
our $identifier = "Identifier";
our $operator_x = "Operator";
our $no_initializer;

my $casing_test = $TokenType::end_token;
print(($casing_test) . "\n");