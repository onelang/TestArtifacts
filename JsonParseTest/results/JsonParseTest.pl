use strict;
use warnings;

my $obj1 = OneJson::parse("{ \"a\":1, \"b\":2 }");
if (!$obj1->is_object()) {
    die "expected to be object!"."\n";
}
my $obj1_props = $obj1->as_object()->get_properties();
if (scalar(@{$obj1_props}) != 2) {
    die "expected 2 properties"."\n";
}
if (${$obj1_props}[0]->get_name() ne "a") {
    die "expected first property to be named 'a'"."\n";
}
my $obj1_prop0_value = ${$obj1_props}[0]->get_value($obj1);
if (!$obj1_prop0_value->is_number() || $obj1_prop0_value->as_number() != 1) {
    die "expected 'a' to be 1 (number)"."\n";
}
print(("b = @{[$obj1->as_object()->get(\"b\")->as_number()]}") . "\n");