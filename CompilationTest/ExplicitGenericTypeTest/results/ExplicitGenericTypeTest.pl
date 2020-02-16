use strict;
use warnings;

package TestClass;

sub new
{
    my $class = shift;
    my $self = {};
    bless $self, $class;
    return $self;
}

sub test_method {
    my ( $self ) = @_;
    my $result = ["y"];
    my $map = {
      x => 5,
    };
    my $keys = keys %{$map};
    print((${$result}[0]) . "\n");
    print((${$keys}[0]) . "\n");
}

package Program;

eval {
    my $c = new TestClass();
    $c->test_method();
};
if ($@) {
    print "Exception: " . $@
}