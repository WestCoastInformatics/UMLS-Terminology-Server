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
perl -ne ' BEGIN { use Digest::MD5  qw(md5_hex); } ($d, $componentId, $id, $terminologyId, $terminology, $value, $d) = split /\|/; $hashcode = md5_hex($value);  $id =~ s/AT0*//; print "$id|$terminologyId|$terminology|$componentId|ATOM|$terminology|DEFINITION|$hashcode\n";' MRDEF.RRF > attributeIdentity.txt
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
perl -ne '($conceptTerminologyId, $d, $d, $semanticType, $id, $d) = split /\|/; $id =~ s/AT0*//; print "$id|$conceptTerminologyId|'$terminology'|$semanticType\n";' MRSTY.RRF > semanticTypeComponentIdentity.txt
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
perl -ne '($d, $language, $d, $d, $d, $stringClassId, $d, $id, $terminologyId, $conceptId, $descriptorId, $terminology, $termType, $code, $name) = split /\|/; $id =~ s/A0*//; print "$id|$stringClassId|$terminology|$terminologyId|$termType|$code|$conceptId|$descriptorId\n";' MRCONSO.RRF > atomIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRCONSO.RRF"
	exit 1
endif

# String class identity
#  id|language|string
#
echo "  Compute string identity for MRCONSO"
/bin/rm -f stringClassIdentity.txt
perl -ne '($d, $language, $d, $d, $d, $id, $d, $d, $d, $d, $d, $d, $d, $d, $string) = split /\|/; $id =~ s/S0*//; print "$id|$language|$string\n";' MRCONSO.RRF | sort -u -o stringIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRCONSO.RRF"
	exit 1
endif

# Lexical class identity
#  id|language|string
#
echo "  Compute string identity for MRCONSO"
/bin/rm -f lexicalClassIdentity.txt
perl -ne '($d, $d, $d, $d, $d, $id, $d, $d, $d, $d, $d, $d, $d, $d, $string) = split /\|/; $id =~ s/S0*//; print "$id|$language|$string\n";' MRCONSO.RRF | sort -u -o stringIdentity.txt
if ($status != 0) then
	echo "ERROR handling MRCONSO.RRF"
	exit 1
endif




echo "------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------"
