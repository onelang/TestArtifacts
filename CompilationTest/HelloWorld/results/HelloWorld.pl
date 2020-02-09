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
    my $value = 1 + 2 * 3 - 4;
    my $map_ = {
      a => 5,
      b => 6,
    };
    my $text = "Hello world! value = @{[$value]}, map[a] = @{[${$map_}{\"a\"}]}";
    print(($text) . "\n");
}

package Program;

eval {
    my $c = new TestClass();
    $c->test_method();
};
if ($@) {
    print "Exception: " . $@
}