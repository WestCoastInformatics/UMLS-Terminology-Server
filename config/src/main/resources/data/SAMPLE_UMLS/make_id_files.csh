#!/bin/csh -f
#
# Make identifier files from RRF data
# 1. attributeIdentity.txt
# 2. relationshipIdentity.txt
# 3. atomIdentity.txt
# ??
#
set terminology = UMLS
echo "------------------------------------------"
echo "Starting `/bin/date`"
echo "------------------------------------------"
echo "terminology = $terminology"

#
# Attribute Identity
#  id|terminologyId|terminology|componentId|componentType|componentTerminology|name|hashcode
#

# C0000039|A0016515|AT38152019||MSH|Synthetic phospholipid used in liposomes and lipid bilayers to study biological membranes. It is also a major constituent of PULMONARY SURFACTANTS.|N||
# 
echo "  Compute definition identity for MRDEF"
/bin/rm -f attributeIdentity.txt
perl -ne ' BEGIN { use Digest::MD5  qw(md5_hex); } ($d, $componentId, $id, $terminologyId, $terminology, $value, $d) = split /\|/; $hashcode = md5_hex($value);  $id =~ s/AT0*//; print "$id|$terminologyId|$terminology|$componentId|ATOM|$terminology|DEFINITION|$hashcode|\n";' MRDEF.RRF > attributeIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRDEF.RRF"
	exit 1
    echo "/var/log/messages exists."
endif

# C0000005|L6215648|S7133916|A11385078|AUI|D012711|AT88546662||TERMUI|MSHFRE|fre0069916|N||
#
echo "  Compute attribute identity for MRSAT"
# skip SUBSET_MEMBER -> deal with those separately
grep -v SUBSET_MEMBER MRSAT.RRF | lib/mrsat.pl >> attributeIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRSAT.RRF"
	exit 1
endif


echo "  Compute attribute identity for MRMAP"
# TODO: do for later - opportunity to change this... can have MappingIdentity...

echo "  Compute attribute identity for SUBSET_MEMBER in MRSAT"
# TODO: do for later - opportunity to change this... can have SubsetMemberIdentity...

#
# Semantic Type Component Identity
#  id|conceptTerminologyId|terminology|semanticType
#
# C0000098|T131|A1.4.1.1.5|Hazardous or Poisonous Substance|AT17620025|256|
# 
echo "  Compute identity for MRSTY"
/bin/rm -f semanticTypeComponentIdentity.txt
perl -ne '($conceptTerminologyId, $d, $d, $semanticType, $id, $d) = split /\|/; $id =~ s/AT0*//; print "$id|$conceptTerminologyId|'$terminology'|$semanticType|\n";' MRSTY.RRF > semanticTypeComponentIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRSTY.RRF"
	exit 1
endif


# ATOM identity -> need to have $LVG_HOME resolve to a real LVG installation
# need to watch out for same normalized string, different LUIs

#
# Atom Identity
#  id|stringClassId|terminology|terminologyId|termType|code|conceptId|descriptorId
#

# C0000039|ENG|P|L0000039|PF|S1357296|Y|A1317708||M0023172|D015060|MSH|PM|D015060|1,2 Dipalmitoylphosphatidylcholine|0|N||
#
echo "  Compute atom identity for MRCONSO"
/bin/rm -f atomIdentity.txt
perl -ne '($d, $language, $d, $d, $d, $stringClassId, $d, $id, $terminologyId, $conceptId, $descriptorId, $terminology, $termType, $code, $name) = split /\|/; $id =~ s/A0*//; print "$id|$stringClassId|$terminology|$terminologyId|$termType|$code|$conceptId|$descriptorId|\n";' MRCONSO.RRF > atomIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRCONSO.RRF for AUI"
	exit 1
endif

# String class identity
#  id|language|string
#
echo "  Compute string identity for MRCONSO"
/bin/rm -f stringClassIdentity.txt
perl -ne '($d, $language, $d, $d, $d, $id, $d, $d, $d, $d, $d, $d, $d, $d, $string) = split /\|/; $id =~ s/S0*//; print "$id|$language|$string|\n";' MRCONSO.RRF | sort -u -o stringClassIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRCONSO.RRF for SUI"
	exit 1
endif

# Lexical class identity
#  id|normString
#
echo "  Compute lexical class identity for MRCONSO"
setenv LVG_HOME d:/data/lvg2016
/bin/rm -f lexicalClassIdentity.txt
# handle ENG
perl -ne '($d, $language, $d, $id, $d, $d, $d, $d, $d, $d, $d, $d, $d, $d, $string) = split /\|/; $id =~ s/L0*//; print "$id|$string\n" if $language eq "ENG";' MRCONSO.RRF | $LVG_HOME/bin/luiNorm.bat -t:2 | cut -d\| -f 1,3 | sed 's/$/\|/' | sort -u -o lexicalClassIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRCONSO.RRF for LUI - ENG"
	exit 1
endif
# verify that we don't have the same norm string for 2 different LUIs - e.g. norm string should be unique in the file
cut -d\| -f 2 lexicalClassIdentity.txt | sort | uniq -d | sed 's/$/\\\|\$/; s/^/\\\|/;' >! x.$$
egrep -f x.$$ lexicalClassIdentity.txt | sort -n | perl -ne 'chop; @_=split/\|/; if ($map{$_[1]}) { $_[1] = "$_[1]$map{$_[1]}";} $map{$_[1]}++; print join "|", @_; print "|\n";' >! y.$$
egrep -v -f x.$$ lexicalClassIdentity.txt >> y.$$
/bin/mv -f y.$$ lexicalClassIdentity.txt
/bin/rm -f x.$$
if (`cut -d\| -f 2 lexicalClassIdentity.txt | sort | uniq -d | wc -l` > 0) then
	echo "ERROR problem with lexicalClassIdentity.txt"
	exit 1
endif

# handle non-ENG
perl -ne '($d, $language, $d, $id, $d, $d, $d, $d, $d, $d, $d, $d, $d, $d, $string) = split /\|/; $id =~ s/L0*//; print "$id|$string|\n" if $language ne "ENG";' MRCONSO.RRF | sort -u >> lexicalClassIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRCONSO.RRF for LUI - non ENG"
	exit 1
endif

echo "------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------"
