#!/bin/csh -f
#
# Make identifier files from RRF data
# 1. attributeIdentity.txt
# 2. relationshipIdentity.txt
# 3. atomIdentity.txt
# ??
#

echo "------------------------------------------"
echo "Starting `/bin/date`"
echo "------------------------------------------"
echo "terminology = $terminology"
set terminology = UMLS

#
# Attribute Identity
#  id|terminologyId|terminology|ownerId|ownerType|ownerQualifier|hashcode
#

# C0000039|A0016515|AT38152019||MSH|Synthetic phospholipid used in liposomes and lipid bilayers to study biological membranes. It is also a major constituent of PULMONARY SURFACTANTS.|N||
# 
echo "  Compute definition identity for MRDEF"
/bin/rm -f attributeIdentity.txt
perl -ne ' BEGIN { use Digest::MD5  qw(md5_hex); } ($d, $ownerId, $id, $terminologyId, $terminology, $value, $d) = split /\|/; $hashcode = md5_hex($value);  $id =~ s/AT0*//; print "$id|DEFINITION|$terminologyId|$terminology|$ownerId|AUI||$hashcode\n";' MRDEF.RRF > attributeIdentity.txt

echo "  Compute attribute identity for MRMAP"


echo "  Compute attribute identity for MRSAT"


#
# Semantic Type Component Identity
#  id|conceptTerminologyId|terminology|semanticType
#
# C0000098|T131|A1.4.1.1.5|Hazardous or Poisonous Substance|AT17620025|256|
# 
echo "  Compute identity for MRSTY"
/bin/rm -f semanticTypeComponentIdentity.txt
perl -ne '($conceptTerminologyId, $d, $d, $semanticType, $id, $d) = split /\|/; $id =~ s/AT0*//; print "$id|$conceptTerminologyId|'$terminology'|$semanticType\n";' MRSTY.RRF > semanticTypeComponentIdentity.txt
  


echo "------------------------------------------"
echo "Finished ...`/bin/date`"
echo "------------------------------------------"
