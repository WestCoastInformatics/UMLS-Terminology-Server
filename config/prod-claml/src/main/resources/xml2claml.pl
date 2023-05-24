#!/usr/bin/perl
#
# File:     xml2claml.pl
# Author:   Brian Carlsen
#           Dave Xia (modified to handle UTF8 encoding issue)
# Requirements:
# * no <br> tags allowed
# * classes must be in DFS order (so each time "superclass" is made, it can be resolved).
# * M1a and M1A - need to resolve case
# 
# Input: directory containing TREF files
# Output: ClaML
# Usage: perl xml2claml.pl -episode icd10cm_tabular_2018.xml >icd10cm.xml
# Version Information
#
our $version = "1.1";
our $version_date = "02/08/2017";
our $version_authority="BAC";

use strict vars;

#
# Set Defaults & Environment
#
our $badvalue;
our $badargs;

# Check options
our $episode = 0;
our @ARGS=();
our $debug = 0;
while (@ARGV) {
    my $arg = shift(@ARGV);
    if ($arg !~ /^-/) {
	push @ARGS, $arg;
	next;
    }
    if ($arg eq "-v") {
	print "$version\n";
	exit(0);
    }
    elsif ($arg eq "-debug") {
	$debug = 1;
    }
    elsif ($arg eq "-help" || $arg eq "--help") {
	&PrintHelp;
	exit(0);
    }
    elsif ($arg eq "-episode" ) {
	$episode = 1;
    }
    else {
	# invalid merge switches may
	# be valid switches for the class being called
	push @ARGS, $arg;
    }
}

#
# Get command line params
#
our $file;
if (scalar(@ARGS) == 1) {
   ($file) = @ARGS;
} else {
  $badargs = 3;
  $badvalue = scalar(@ARGS);
}

# obtain year from filename
$file =~ /.*(\d\d\d\d).*/;
my $year = $1;

#
# Process errors
#
my %errors = (1 => "Illegal switch: $badvalue",
	   3 => "Bad number of arguments: $badvalue",
	   4 => "$badvalue must be set"
          );

if ($badargs) {
    &PrintUsage;
    print "\n$errors{$badargs}\n";
    exit(1);
}

#
# Load TREF into data structures
#
my %classesToRubrics = ();  # code => {"rubric" => @vals }
my %parChd = (); # parCode => @chdCodes
my %chdPar = (); # chdCode => @parCodes
my %preferred = (); # code => preferred name

my $xml;
open($xml, "<:encoding(utf8)", "$file") || die "Could not open $file: $! $?\n";
my $chapterCt = 0;
my $sectionCt = 0;
my $diagCt = 0;
my @sduis = ();
my $sdui = "";
my $diagCt = 0;
my $includesCt = 0;
my $noteCt = 0;
my $sevenChrDefCt = 0;
my %sevenChrExtension = ();
my %sevenChrExtensionMap = ();
my $excludesCt = 0;
my @tags = ();
my $atn = "";
my $oldStyle = 0;
my $thisDiagHadChildren = 0;
# fake the treetop entry
$preferred{"A00-Z99"} = "treetop";
while (<$xml>) {
    
    # Handle "chapter"

    if (/<chapter>/) {
	$chapterCt++;
	push @tags, "chapter";
    }

    elsif (/<\/chapter>/) {
	$chapterCt--;
	pop @sduis;
	my $tag = pop @tags;
	print "ERROR: expected tag chapter: $tag\n" unless $tag eq "chapter";
    }

    elsif ($tags[$#tags] eq "chapter" && /<desc>(.*) \((.*)\)<\/desc>/) {
	$sdui = $2;
	push @sduis, $sdui;
	$preferred{$sdui} = "$1 ($2)";
        unshift @{ $parChd{"A00-Z99"} }, $sdui;
        unshift @{ $chdPar{$sdui} }, "A00-Z99";
	print "1SDUIS: ",(join ",",@sduis),"\n" if $debug;
	print "1TAGS: ",(join ",",@tags),"\n" if $debug;
    }

    # handle "section"

    elsif (/<section id="(.*)">/) {
	$sectionCt++;
	push @tags, "section";
	$sdui = $1;
	# if a section SDUI doesn't have a range, make it an inclusive range
	if ($sdui !~ /\-/) {
	    $sdui = "$sdui-$sdui";
	}
	push @sduis, $sdui;
	unshift @{ $parChd{$sduis[$#sduis-1]} }, $sduis[$#sduis];
	unshift @{ $chdPar{$sduis[$#sduis]} }, $sduis[$#sduis-1];
	print "2SDUIS: ",(join ",",@sduis),"\n" if $debug;
	print "2TAGS: ",(join ",",@tags),"\n" if $debug;
	print "2PAR($sdui): ",(join ",",@{ $chdPar{$sdui}}),"\n" if $debug;
	print "2CHD($sdui): ",(join ",",@{ $parChd{$sdui}}),"\n" if $debug;
    } 
    
    # handle older style
    elsif (/<section>/) {
	$sectionCt++;
	$oldStyle++;
	push @tags, "section";
    }
    
    elsif (/<\/section>/) {
	$sectionCt--;
	$oldStyle-- if $oldStyle;
	pop @sduis;
	my $tag = pop @tags;
	print "ERROR: expected tag section: $tag\n" unless $tag eq "section";
    }

    elsif ($tags[$#tags] eq "section" && /<desc>(.*) \((.*)\)<\/desc>/) {
	if ($oldStyle) {
	    $sdui = $2;
	    # if a section SDUI doesn't have a range, make it an inclusive range
	    if ($sdui !~ /\-/) {
		$sdui = "$sdui-$sdui";
	    }
	    push @sduis, $sdui;
	    unshift @{ $parChd{$sduis[$#sduis-1]} }, $sduis[$#sduis];
	    unshift @{ $chdPar{$sduis[$#sduis]} }, $sduis[$#sduis-1];
	    print "2SDUIS: ",(join ",",@sduis),"\n" if $debug;
	    print "2TAGS: ",(join ",",@tags),"\n" if $debug;
	    print "2PAR($sdui): ",(join ",",@{ $chdPar{$sdui}}),"\n" if $debug;
	    print "2CHD($sdui): ",(join ",",@{ $parChd{$sdui}}),"\n" if $debug;
	}

	$preferred{$sdui} = "$1 ($2)";
    }

    # handle "diag"

    elsif (/<diag>/ || /<diag .*>/) {
	$diagCt++;
	push @tags, "diag";
	# do not know if we have children yet
	$thisDiagHadChildren = 0;
    } 

    elsif ($tags[$#tags] eq "diag" && /<name>(.*)<\/name>/) {
	$sdui = $1;
	push @sduis, $sdui;
	unshift @{ $parChd{$sduis[$#sduis-1]} }, $sduis[$#sduis];
	unshift @{ $chdPar{$sduis[$#sduis]} }, $sduis[$#sduis-1];
	print "3SDUIS: ",(join ",",@sduis),"\n" if $debug;
	print "3TAGS: ",(join ",",@tags),"\n" if $debug;
    }

    elsif ($tags[$#tags] eq "diag" && /<desc>(.*)<\/desc>/) {
	$preferred{$sdui} = $1;
    }
    
    elsif (/<\/diag>/) {
	# determine use of sevenChrDef
	# If this <diag> had no children, and sevenChrExtension is set, use it
	# If we've returned to the "diag" where the sevenChrDef was expressed, clear it
	if (scalar(%sevenChrExtension) != 0 && !$thisDiagHadChildren) {
	    my $paddedSdui = "${sdui}XXXX";
	    if ($sdui !~ /\./) {
		if ($sdui !~ /^...$/) {
		    die "ERROR: unexpected code with sevenChrExtension: $sdui\n";
		}
		$paddedSdui = "${sdui}.XXXX";
	    }
	    $paddedSdui =~ s/(...\....).*/$1/;
	    my $key;
	    foreach $key (keys %sevenChrExtension) {
		my $chdSdui = "$paddedSdui$key";
		$preferred{$chdSdui} = "$preferred{$sdui}, $sevenChrExtension{$key}";
		unshift @{ $parChd{$sdui} }, $chdSdui;
		unshift @{ $chdPar{$chdSdui} }, $sdui;
	    }
	}

	if ($sevenChrExtensionMap{scalar(@sduis)}) {
	    delete $sevenChrExtensionMap{scalar(@sduis)};
	    my @depths = sort keys %sevenChrExtensionMap;
	    if (scalar(@depths) > 0) {
		my $maxDepth = $depths[scalar(@depths)-1];
		%sevenChrExtension = %{$sevenChrExtensionMap{$maxDepth}}; 
	    } else {
		%sevenChrExtension = (); 
	    }
	}


	# handle end diag tag
	$diagCt--;
	pop @sduis;
	my $tag = pop @tags;
	print "ERROR: expected tag diag: $tag\n" unless $tag eq "diag";
	$thisDiagHadChildren = 1;
    } 


    elsif ($tags[$#tags] eq "section" && /<desc>(.*)<\/desc>/) {
	$preferred{$sdui} = $1;
    }

    # Handle includes and inclusionTerm

    elsif (/<includes>/ || /<inclusionTerm>/) {
	$includesCt++;
	push @tags, "includes";
    }
    elsif (/<\/includes>/ || /<\/inclusionTerm>/) {
	$includesCt--;
	my $tag = pop @tags;
	print "ERROR: expected tag includes: $tag\n" unless $tag eq "includes";
    }
    elsif ($tags[$#tags] eq "includes" && /<note>(.*)<\/note>/) {
	push @{ $classesToRubrics{$sdui}->{"inclusion"} },"$1";
    }

    # Handle excludes

    elsif (/<excludes(.)>/) {
	$excludesCt++;
	push @tags, "excludes$1";
    }
    elsif (/<\/excludes(.)>/) {
	$excludesCt--;
	my $tag = pop @tags;
	print "ERROR: expected tag excludes$1: $tag\n" unless $tag eq "excludes$1";
	$atn = "";
    }
    elsif ($tags[$#tags] =~ /excludes[12]/ && /<note>(.*)<\/note>/) {
        $atn = uc($tags[$#tags]);
	push @{ $classesToRubrics{$sdui}->{"exclusion"} },"$atn: $1";
    }

    # Handle notes

    elsif (/<notes>/) {
	$noteCt++;
	push @tags, "note";
	$atn = "NOTE";
    }
    elsif (/<useAdditionalCode>/) {
	$noteCt++;
	push @tags, "note";
	$atn = "USE_ADDITIONAL";
    }
    elsif (/<codeFirst>/) {
	$noteCt++;
	push @tags, "note";
	$atn = "CODE_FIRST";
    }
    elsif (/<codeAlso>/) {
	$noteCt++;
	push @tags, "note";
	$atn = "CODE_ALSO";
    }
    elsif (/<sevenChrDef>/) {
	$noteCt++;
	push @tags, "note";
	$atn = "SEVEN_CHR_DEF";
	%sevenChrExtension = ();
	$sevenChrDefCt++;
    }
    elsif (/<\/useAdditionalCode>/ || /<\/codeFirst>/ || /<\/codeAlso>/ || /<\/notes>/ || /<\/sevenChrDef>/) {
	if (/<\/sevenChrDef>/) {
	    my %map = %sevenChrExtension;
	    $sevenChrExtensionMap{scalar(@sduis)} = \%map;
	    $sevenChrDefCt--;
	}
	$noteCt--;
	my $tag = pop @tags;
	print "ERROR: expected tag note: $tag\n" unless $tag eq "note";
	$atn = "";
    }
    elsif ($tags[$#tags] eq "note" && /<note>(.*)<\/note>/) {
	push @{ $classesToRubrics{$sdui}->{"note"} },"$atn: $1";
    }

    elsif ($sevenChrDefCt && /<extension char="(.*)">(.*)<\/extension>/) {
	$sevenChrExtension{$1} = $2;
    } 

}
close($xml);


#
# Find tree-tops
#  This logic assumes there's a single tree-top from which the "tree tops" we care about hang
#
my $treeTop;
my @treeTops = ();
my $key;
foreach $key (keys %preferred) {
    if (! defined($chdPar{$key})) {
        $treeTop = $key;
        last;
    }
}
foreach $key (keys %preferred) {
    my @f = @{ $chdPar{$key} };
    if ($f[0] eq $treeTop) {
        unshift @treeTops, $key;
    }
}

#
# Write XML
#
#binmode(STDOUT, ":utf8");
print qq {<?xml version="1.0" encoding="UTF-8"?>
<ClaML xmlns="http://icd10.who.org/claml20" version="2.0.0">
        <Meta name="TopLevelSort" value="};

my $code;
my $i = 0;
foreach $code (sort @treeTops) {
    print " " if $i++ > 0;
    print "$code";
}
print qq{"/>
        <Meta name="lang" value="en"/>
        <Identifier authority="WHO" uid="SRFSFto be added later"/>
        <Title date="$year-01-01" name="ICD-10-CM" version="$year"/>
        <ClassKinds>
                <ClassKind name="category"/>
                <ClassKind name="block"/>
        </ClassKinds>
        <!-- not used -->
        <UsageKinds>
                <UsageKind mark="*" name="aster"/>
                <UsageKind mark="+" name="dagger"/>
        </UsageKinds>
        <RubricKinds>
                <RubricKind inherited="false" name="footnote"/>
                <RubricKind inherited="false" name="text"/>
                <RubricKind inherited="false" name="coding-hint"/>
                <RubricKind inherited="false" name="definition"/>
                <RubricKind inherited="false" name="introduction"/>
                <RubricKind inherited="false" name="modifierlink"/>
                <RubricKind inherited="false" name="note"/>
                <RubricKind inherited="false" name="exclusion"/>
                <RubricKind inherited="false" name="inclusion"/>
                <RubricKind inherited="false" name="preferredLong"/>
                <RubricKind inherited="false" name="preferred"/>
        </RubricKinds>
};

my $treeTopCode = "";
my $code;
foreach $code (sort dfs keys %preferred) {
    if (!$treeTopCode) {
    	# set and skip the tree top code (presumed to be first)
       $treeTopCode = $code;
    }
    elsif ($code =~ /[\-,]/) {
	writeClass($code);
    }
}

foreach $code (sort {lc($a) cmp lc($b)} keys %preferred) {
    if ($code !~ /[\-,]/) {
	writeClass($code);
    }
}

print qq{</ClaML>
};
exit(0);

######################### LOCAL PROCEDURES #######################

sub dfs  {
    my $rv;
    #print "a = $a, b = $b\n";
    if ($a =~ /[\-,]/ && $b !~ /[\-,]/) { return -1; }
    if ($b =~ /[\-,]/ && $a !~ /[\-,]/) { return 1; }
    if ($a !~ /[\-,]/ && $b !~ /[\-,]/) { $a cmp $b; }
    if ($a =~ /[\-,]/ && $b =~ /[\-,]/) { 
	my ($a1, $a2) = split /\-/, $a;
	my ($b1, $b2) = split /\-/, $b;
	if ($a1 lt $b1) { $rv = -1; }
	elsif ($a1 gt $b1) { $rv = 1; }
	elsif ($a2 gt $b2) { $rv = -1; }
	else {$rv = 1; }
	#print "a = $a, b = $b, $a1, $a2, $b1, $b2, $rv\n";
	return $rv;
    }   
    return 0;
}

our $rubricId = 0;
sub writeClass {
    my ($code) = @_;
    my $kind = "category";
    $kind = "block" if $code =~ /\-/;
    #
    # If the global "-episode" flag is set, write the class entry for that
    #
    if ($episode && $code =~ /...\....A/) {
	my $code2 = "$code";
	$code2 =~ s/.$/\?/;
	print qq{        <Class code="$code2" kind="category">
};
	# fake parents
	my $par;
	foreach $par (sort @{ $chdPar{$code} }) {
	    next if $par eq $treeTopCode;
	    print qq{                <SuperClass code="$par"/>
};
	}
	# no children
	# preferred label
	my $paddedRubricId = "D" . LPad($rubricId++,7,"0");
	my $label = $preferred{$code};
	$label =~ s/(.*)(, .*encounter.*)$/$1, episode of care unspecified/;
	print qq{                <Rubric id="$paddedRubricId" kind="preferred">
                        <Label xml:lang="en" xml:space="default">$label</Label>
                </Rubric>
};
	print qq{        </Class>
};
    }

    print qq{
        <Class code="$code" kind="$kind">
};
    my $par;
    foreach $par (sort @{ $chdPar{$code} }) {
	next if $par eq $treeTopCode;
	print qq{                <SuperClass code="$par"/>
};
    }
    my $chd;
    foreach $chd (sort @{ $parChd{$code} }) {
    #
    # unspecified episode of care child
    #
	if ($episode && $chd =~ /...\....A/) {
	    my $chd2 = "$chd";
	    $chd2 =~ s/.$/?/;
	    print qq{                <SubClass code="$chd2"/>
};
	}
	print qq{                <SubClass code="$chd"/>
};
    }
    my $paddedRubricId = "D" . LPad($rubricId++,7,"0");
    print qq{                <Rubric id="$paddedRubricId" kind="preferred">
                        <Label xml:lang="en" xml:space="default">$preferred{$code}</Label>
                </Rubric>
};

    my @texts = @{ $classesToRubrics{$code}->{"text"} };
    my $text;
    foreach $text (sort @texts) {
	my $paddedRubricId = "D" . LPad($rubricId++,7,"0");
	print qq{                <Rubric id="$paddedRubricId" kind="text">
                        <Label xml:lang="en" xml:space="default">
                                <Para>$text</Para>
                        </Label>
                </Rubric>
};
    }

    my @inclusions = @{ $classesToRubrics{$code}->{"inclusion"} };
    my $inclusion;
    foreach $inclusion (sort @inclusions) {
	my $paddedRubricId = "D" . LPad($rubricId++,7,"0");
	print qq{                <Rubric id="$paddedRubricId" kind="inclusion">
                        <Label xml:lang="en" xml:space="default">$inclusion</Label>
                </Rubric>
};
    }

    my @definitions = @{ $classesToRubrics{$code}->{"definition"} };
    my $def;
    foreach $def (sort @definitions) {
	my $paddedRubricId = "D" . LPad($rubricId++,7,"0");
	print qq{        <Rubric id="$paddedRubricId" kind="definition">
                        <Label xml:lang="en" xml:space="default">
                                <Para>$def</Para>
                        </Label>
                </Rubric>
};
    }

    my @notes = @{ $classesToRubrics{$code}->{"note"} };
    my $note;
    foreach $note (sort @notes) {
	my $paddedRubricId = "D" . LPad($rubricId++,7,"0");
	print qq{        <Rubric id="$paddedRubricId" kind="note">
                        <Label xml:lang="en" xml:space="default">
                                <Para>$note</Para>
                        </Label>
                </Rubric>
};
    }

    my @exclusions = @{ $classesToRubrics{$code}->{"exclusion"} };
    my $excl;
    foreach $excl (sort @exclusions) {
	# if ends with " (Z34.53)" create a reference to "Z34.53"
	if ($excl =~ / \([A-Z0-9\.-]+\)$/) {
	    $excl =~ s/(.*) \(([A-Z0-9\.-]+)\)$/$1<Reference class="in brackets">$2<\/Reference>/;
	} 
	$paddedRubricId = "D" . LPad($rubricId++,7,"0");
	print qq{        <Rubric id="$paddedRubricId" kind="exclusion">
                        <Label xml:lang="en" xml:space="default">$excl</Label>
                </Rubric>
};
    }


    print qq{        </Class>
};
}

sub LPad {
    my ($str, $len, $chr) = @_;
    $chr = " " unless (defined($chr));
    return substr(($chr x $len) . $str, -1 * $len, $len);
} # LPad

sub PrintUsage {

	print qq{ This script has the following usage:
    xml2claml.pl [-episode] <input file>
};
}

sub PrintHelp {
	&PrintUsage;
	print qq{
 This script is used to batch insert core data.

 Options:
       -episode:            Create "episode of care unspecified" entries (recommended for use)
       -debug:              Turn debug flag on
       -v[ersion]:          Print version information.
       -[-]help:            On-line help

 Arguments:
       input file:          The XML file to convert

 Version $version, $version_date ($version_authority)
};
}
