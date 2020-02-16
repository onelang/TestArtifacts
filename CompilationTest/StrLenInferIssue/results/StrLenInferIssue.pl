use strict;
use warnings;

package StrLenInferIssue;

sub new
{
    my $class = shift;
    my $self = {};
    bless $self, $class;
    return $self;
}

sub test {
    my ( $str ) = @_;
    return length($str);
}

print((StrLenInferIssue::test("hello")) . "\n");