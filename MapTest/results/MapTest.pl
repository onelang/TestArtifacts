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

sub get_result {
    my ( $self ) = @_;
    my $map_obj = {
      x => 5,
    };
    #let containsX = "x" in mapObj;
    #delete mapObj["x"];
    ${$map_obj}{"x"} = 3;
    return ${$map_obj}{"x"};
}

sub test_method {
    my ( $self ) = @_;
    print(("Result = @{[$self->get_result()]}") . "\n");
}

package Program;

eval {
    my $c = new TestClass();
    $c->test_method();
};
if ($@) {
    print "Exception: " . $@
}