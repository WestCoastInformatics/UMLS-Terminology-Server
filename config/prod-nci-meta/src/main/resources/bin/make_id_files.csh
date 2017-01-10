#!/bin/csh -f
#
# Make identifier files from RRF data
# 1. attributeIdentity.txt
# 2. relationshipIdentity.txt
# 3. atomIdentity.txt
# ...
#
set terminology = $1
if ("x$terminology" == "x") then
  echo "Usage: $0 <terminology>"
endif
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
lib/mrsat.pl MRSAT.RRF >> attributeIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRSAT.RRF"
	exit 1
endif

# add in "XMAP" entries for mappings
# MAPSETCUI,MAPSETSAB,MAPSUBSETID,MAPRANK,MAPID,MAPSID,FROMID,FROMSID,FROMEXPR,FROMTYPE,FROMRULE,FROMRES,REL,RELA,TOID,TOSID,\
# TOEXPR,TOTYPE,TORULE,TORES,MAPRULE,MAPRES,MAPTYPE,MAPATN,MAPATV,CVF
echo "  Compute attribute identity for MRMAP"
/bin/rm -f xmCuiAui.txt
 grep '|XM|' MRCONSO.RRF  | cut -d\| -f 1,8 > xmCuiAui.txt

perl -ne 'BEGIN {open(X,"xmCuiAui.txt"); while (<X>) { chop; @_=split/\|/; $map{$_[0]}=$_[1]; }; close(X); } @_ = split/\|/;  print "$_[0]|L|S|$map{$_[0]}|AUI|$_[5]|$_[4]||XMAP|$_[1]|$_[2]~$_[3]~$_[6]~$_[12]~$_[13]~$_[14]~$_[20]~$_[22]~$_[23]~$_[24]~$_[5]~$_[21]|N||\n" ' MRMAP.RRF | lib/mrsat.pl >> attributeIdentity.txt
    
#    2 MAPSUBSETID: Map sub set identifier
#    3 MAPRANK: Order in which mappings in a subset should be applied
#    6 FROMID: Identifier mapped from
#    12 REL: Relationship
#    13 RELA: Relationship attribute
#    14 TOID: Identifier mapped to
#    20 MAPRULE: Machine processable rule for when to apply mapping
#    22 MAPTYPE: Type of mapping
#    23 MAPATN: Row level attribute name associated with this mapping
#    24 MAPATV: Row level attribute value associated with this mapping   
#    5 MAPSID: Source asserted Mapping ID
#    21 MAPRES: Human readable restriction use of mapping
/bin/rm -f xmCuiAui.txt

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
# PREREQUISITE: LVG is installed at LVG_HOME listed below
echo "  Compute lexical class identity for MRCONSO"
setenv LVG_HOME c:/data/lvg2016
/bin/rm -f lexicalClassIdentity.txt
# handle ENG
perl -ne '($d, $language, $d, $id, $d, $d, $d, $d, $d, $d, $d, $d, $d, $d, $string) = split /\|/; $id =~ s/L0*//; print "$id|$language|$string\n" if $language eq "ENG";' MRCONSO.RRF | $LVG_HOME/bin/luiNorm.bat -t:3 | cut -d\| -f 1,2,4 | sed 's/$/\|/' | sort -u -o lexicalClassIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRCONSO.RRF for LUI - ENG"
	exit 1
endif
# verify that we don't have the same norm string for 2 different LUIs - e.g. norm string should be unique in the file
cut -d\| -f 3 lexicalClassIdentity.txt | sort | uniq -d | sed 's/$/\\\|\$/; s/^/\\\|/;' >! x.$$
egrep -f x.$$ lexicalClassIdentity.txt | sort -n | perl -ne 'chop; @_=split/\|/; if ($map{$_[2]}) { $_[2] = "$_[2]$map{$_[2]}";} $map{$_[2]}++; print join "|", @_; print "|\n";' >! y.$$
egrep -v -f x.$$ lexicalClassIdentity.txt | grep -v '289447|carinu pneumocystis|' >> y.$$
/bin/mv -f y.$$ lexicalClassIdentity.txt
/bin/rm -f x.$$
if (`cut -d\| -f 3 lexicalClassIdentity.txt | sort | uniq -d | wc -l` > 0) then
	echo "ERROR problem with lexicalClassIdentity.txt"
	exit 1
endif
if (`cut -d\| -f 1 lexicalClassIdentity.txt  | sort | uniq -d | wc -l` > 0) then
	echo "ERROR problem with lexicalClassIdentity.txt - duplicate id"
	exit 1
endif

# handle non-ENG
perl -ne '($d, $language, $d, $id, $d, $d, $d, $d, $d, $d, $d, $d, $d, $d, $string) = split /\|/; $id =~ s/L0*//; print "$id|$language|$string|\n" if $language ne "ENG";' MRCONSO.RRF | sort -u >> lexicalClassIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRCONSO.RRF for LUI - non ENG"
	exit 1
endif

#
# Relationship Identity
#  id|terminology|terminologyId|type|additionalType|fromId|fromType|fromTerminology|toId|toType|toTerminology|inverseId
#

# make inverseRui.txt
/bin/rm -f relationshipIdentity.txt inverseRui.txt mrrel.txt rel.txt rela.txt
grep inverse MRDOC.RRF  | grep 'REL|' | cut -d\| -f 2,4,5 | sort -t\| -k 1,1 -o rel.txt
grep inverse MRDOC.RRF  | grep 'RELA|' | cut -d\| -f 2,4,5 | sort -t\| -k 1,1 -o rela.txt
lib/inverseRui.pl MRREL.RRF | sort -t\| -k 2,2 -o mrrel.txt
join -t\| -j 2 -o 1.1 2.1 mrrel.txt mrrel.txt | perl -ne 'chop; @_ = split /\|/; print "$_\n" if $_[0] ne $_[1];' | sort -u -o inverseRui.txt
/bin/rm -f mrrel.txt rel.txt rela.txt

#if (`cut -d\| -f 1 inverseRui.txt | sort | uniq -d | wc -l` > 1) then
#	echo "ERROR: duplicate inverse RUIs in MRREL, try using fixMrrel.pl"
#	exit 1
#endif

# verify each RUI is in inverse_ruis

# C0000039|A0016511|AUI|SY|C0000039|A1317687|AUI|permuted_term_of|R28482429||MSH|MSH|||N||
#
echo "  Compute relationship identity for MRREL"
cat MRREL.RRF | lib/mrrel.pl $terminology > relationshipIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRREL.RRF"
	exit 1
endif

if (`perl -ne '@_=split/\|/; print unless $_[11]' relationshipIdentity.txt | wc -l` > 0) then
	echo "ERROR blank inverseRui in relationshipIdentity"
endif


/bin/rm -f inverseRui.txt mrrel.txt rel.txt rela.txt

echo "------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------"
