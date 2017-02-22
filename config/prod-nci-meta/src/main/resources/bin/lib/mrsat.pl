#!/usr/bin/perl
use Digest::MD5  qw(md5_hex);
# cache AUI SABs
open (IN, "MRCONSO.RRF") || die "could not open MRCONSO.RRF: $! $?\n";
while (<IN>) {
  @_ = split/\|/;
  $map{$_[7]} = $_[11];
}
close(IN);
# cache RUI SAB
open (IN, "MRREL.RRF") || die "could not open MRREL.RRF: $! $?\n";
while (<IN>) {
  @_ = split/\|/;
  $map{$_[8]} = $_[10];
}
close(IN);
while(<>) {
  ($d, $d, $d, $componentId, $componentType, $d, $id, $terminologyId, $name, $terminology, $value, $d) = split /\|/;
  $hashcode = md5_hex($value);  $id =~ s/AT0*//; $type = $componentType;
  $type =~ s/AUI/ATOM/;
  $type =~ s/SCUI/CONCEPT/;
  $type =~ s/CUI/CONCEPT/;
  $type =~ s/RUI/RELATIONSHIP/;
  $type =~ s/SDUI/DESCRIPTOR/;
  print "$id|$terminologyId|$terminology|$componentId|$type|$map{$componentId}|$name|$hashcode|\n";
}
