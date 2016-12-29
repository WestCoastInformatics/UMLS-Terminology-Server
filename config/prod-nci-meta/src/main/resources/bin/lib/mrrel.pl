#!/usr/bin/perl

# cache RUI=>INVERSE_RUI
open (IN, "inverseRui.txt") || die "could not open inverseRui.txt: $! $?\n";
while (<IN>) {
  chop;
  @_ = split/\|/;
  $map{$_[0]} = $_[1];
}
close(IN);

# cache AUI=>SABs
open (IN, "MRCONSO.RRF") || die "could not open MRCONSO.RRF: $! $?\n";
while (<IN>) {
  @_ = split/\|/;
  $map{$_[7]} = $_[11];
  $map{"$_[7]ATOM"} = $_[7];
  $map{"$_[7]CODE"} = $_[13];
  $map{"$_[7]CONCEPT"} = $_[9];
  $map{"$_[7]DESCRIPTOR"} = $_[10];
}
close(IN);

# C0000039|A0016511|AUI|SY|C0000039|A1317687|AUI|permuted_term_of|R28482429||MSH|MSH|||N||
# CUI1,AUI1,STYPE1,REL,CUI2,AUI2,STYPE2,RELA,RUI,SRUI,SAB,SL,RG,DIR,SUPPRESS,CVF
while(<>) {
  chop;
  ($toCui, $toAui, $toType, $type, $fromCui, $fromAui, $fromType, $additionalType, $id, $terminologyId, $terminology, $d, $group, $dir, $d) = split /\|/;

  # source level rel
  if ($toAui) {
    $toType =~ s/AUI/ATOM/;
    $toType =~ s/SCUI/CONCEPT/;
    $toType =~ s/CUI/CONCEPT/;
    $toType =~ s/SDUI/DESCRIPTOR/;
    $toTerminology = $map{$toAui};
    $toId = $map{"$toAui$toType"};
    $fromType =~ s/AUI/ATOM/;
    $fromType =~ s/SCUI/CONCEPT/;
    $fromType =~ s/CUI/CONCEPT/;
    $fromType =~ s/SDUI/DESCRIPTOR/;
    $fromTerminology = $map{$fromAui};
    $fromId = $map{"$fromAui$fromType"}; 
  } 
  # concept level rel
  else {
  	$terminology = $ARGV[0];
  	$toId = $toCui;
  	$toType = "CONCEPT";
  	$toTerminology = $terminology;
  	$fromId = $fromCui;
  	$fromType = "CONCEPT";
  	$fromTerminology = $terminology;
  }
  $inverseId = $map{$id};
  $id =~ s/R0*//;
  $inverseId =~ s/R0*//;
  if ($inverseId) {
    print "$id|$terminology|$terminologyId|$type|$additionalType|$fromId|$fromType|$fromTerminology|$toId|$toType|$toTerminology|$inverseId|\n";
  } else {
    print STDERR "SKIP: $id|$terminology|$terminologyId|$type|$additionalType|$fromId|$fromType|$fromTerminology|$toId|$toType|$toTerminology|$inverseId|\n";
  }
}
