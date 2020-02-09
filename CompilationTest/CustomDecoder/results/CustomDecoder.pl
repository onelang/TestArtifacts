use strict;
use warnings;

package XorByte;

sub new
{
    my $class = shift;
    my $self = {};
    bless $self, $class;
    my ( $xor_value ) = @_;
    $self->{xor_value} = $xor_value;
    return $self;
}

sub decode {
    my ( $self, $src ) = @_;
    my $dest = [];
    
    for (my $i = 0; $i < scalar(@{$src}); $i++) {
        push @{$dest}, ${$src}[$i] ^ $self->{xor_value};
    }
    
    return $dest;
}

package Base64;

sub new
{
    my $class = shift;
    my $self = {};
    bless $self, $class;
    return $self;
}

sub decode {
    my ( $self, $src ) = @_;
    my $dest = [];
    
    # 4 base64 chars => 3 bytes
    for (my $i = 0; $i < scalar(@{$src}); $i += 4) {
        my $ch0 = $self->decode_char(${$src}[$i]);
        my $ch1 = $self->decode_char(${$src}[$i + 1]);
        my $ch2 = $self->decode_char(${$src}[$i + 2]);
        my $ch3 = $self->decode_char(${$src}[$i + 3]);
        my $trinity = ($ch0 << 18) + ($ch1 << 12) + ($ch2 << 6) + ($ch3);
        push @{$dest}, $trinity >> 16;
        push @{$dest}, ($trinity >> 8) & 0xff;
        push @{$dest}, $trinity & 0xff;
    }
    
    return $dest;
}

sub decode_char {
    my ( $self, $ch ) = @_;
    my $value = -1;
    if ($ch >= 65 && $ch <= 90) {
        $value = $ch - 65;
    } elsif ($ch >= 97 && $ch <= 122) {
        $value = $ch - 97 + 26;
    } elsif ($ch >= 48 && $ch <= 57) {
        $value = $ch - 48 + 52;
    } elsif ($ch == 43 || $ch == 45) {
        $value = 62;
    } elsif ($ch == 47 || $ch == 95) {
        $value = 63;
    } elsif ($ch == 61) {
        $value = 0;
    } else {
    }
    return $value;
}

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
    my $src1 = [4, 5, 6];
    my $decoder = new XorByte(0xff);
    my $dst1 = $decoder->decode($src1);
    foreach my $x (@{$dst1}) {
        print(($x) . "\n");
    }
    
    print(("|") . "\n");
    
    my $src2 = [97, 71, 86, 115, 98, 71, 56, 61];
    my $decoder2 = new Base64();
    my $dst2 = $decoder2->decode($src2);
    foreach my $x (@{$dst2}) {
        print(($x) . "\n");
    }
}

package Program;

eval {
    my $c = new TestClass();
    $c->test_method();
};
if ($@) {
    print "Exception: " . $@
}