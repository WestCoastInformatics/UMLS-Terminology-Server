#!/usr/bin/perl

# cache REL=> inverse
open (IN, "rel.txt") || die "could not open rel.txt: $! $?\n";
while (<IN>) {
  @_ = split/\|/;
  $map{$_[0]} = $_[1];
}
close(IN);

# cache RELA=> inverse
open (IN, "rela.txt") || die "could not open rela.txt: $! $?\n";
while (<IN>) {
  @_ = split/\|/;
  $map{$_[0]} = $_[1];
}
close(IN);

# cache RELA=> inverse
open (IN, "unpublished/ruiDaFlags.txt") || die "could not open unpublished/ruiDaFlags.txt: $! $?\n";
while (<IN>) {
  chop;
  @_ = split/\|/;
  $map{$_[0]} = "~DA:$_[1]";
}
close(IN);

# CUI1,AUI1,STYPE1,REL,CUI2,AUI2,STYPE2,RELA,RUI,SRUI,SAB,SL,RG,DIR,SUPPRESS,CVF
while(<>) {
  my ($cui1, $aui1, $stype1, $rel, $cui2, $aui2, $stype2, $rela, $rui, $srui, $sab, $sl, $dir, $suppress) = split /\|/;
  if (!$srui) {
     $srui = $map{$rui};
  }
  print "$rui|$cui1 $aui1 $stype1 $rel $cui2 $aui2 $stype2 $rela, $srui, $sab|\n";
  print "$rui|$cui2 $aui2 $stype2 $map{$rel} $cui1 $aui1 $stype1 $map{$rela}, $srui, $sab|\n";
}
