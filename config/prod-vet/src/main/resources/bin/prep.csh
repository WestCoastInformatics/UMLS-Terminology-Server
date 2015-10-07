#!/bin/csh -f

set sctDir=/cygdrive/c/data/SNOMED/SnomedCT_RF2Release_INT_20150731/Snapshot
set extDir=/cygdrive/c/data/SNOMED/SnomedCT_Release_VTS1000009_20151001/Snapshot
set outDir=/cygdrive/c/data/SNOMED/SnomedCT_VET_Snapshot

set sctLabel=_INT_
set extLabel=_VTS1000009_
 
set sctVersion=20150731
set extVersion=20151001

echo "-----------------------------------------------"
echo "Starting ...`/bin/date`"
echo "-----------------------------------------------"
echo "  sctDir = $sctDir"
echo "  extDir = $extDir"
echo "  outDir = $outDir"
echo "  sctLabel = $sctLabel"
echo "  extLabel = $extLabel"
echo "  sctVersion = $sctVersion"
echo "  extVersion = $extVersion"
echo ""

# clean and prep
echo "Cleanup and prep output dir"
/bin/rm -rf $outDir/*
/bin/cp -r -f $sctDir/* $outDir
chmod -R 775 $outDir
chmod 664 `find $outDir -name "*txt"`

# rename files
echo "Rename files to match ext"
cd $outDir
foreach f (`find . "*txt"`)
  set f2 = `echo $f | sed "s/$sctLabel/$extLabel/; s/$sctVersion/$extVersion/"`
  echo "  $f"
  echo "    -> $f2"
  /bin/mv -f $f $f2
end

# append ext data
echo "Append ext data"
foreach f (`find $extDir -name "*txt"`)
  echo "  $f"
  set pat = `echo $f | perl -pe 's/.*der2_([^_]*_[^_]*)[\-_].*/$1/; s/.*sct2_([^_]*)_.*/$1/;'`
  set f2 = `find $outDir -name "*_${pat}[-_]*txt"`
  echo "    >> $f2"
  egrep -v '^id' $f >> $f2
end

#
# REVISIT THESE MANUAL FIXES
#

# CODE change to support missing preferred names

# Remove concept refset entries for 900000000000489007 that are descriptions or relationships
#  - attribute value refset for inactive concepts should only reference concepts	
perl -ne '@_ = split/\t/; print unless $_[4] eq "900000000000489007" && $_[5] =~ /\d*1[12]\d$/' Refset/Content/*Attribute*txt >! /tmp/x.$$
/bin/mv -f /tmp/x.$$ Refset/Content/*Attribute*txt

# convert these relationships to "inferred" from "stated"
# isa rels with the following concept ids:
sort -u -o /tmp/x.$$ << EOF
318301000009103
27431000009105
318191000009109
281721000009106
35181000009108
40601000009100
344431000009103
338591000009108
328311000009109
27971000009100
329911000009103
329921000009106
313081000009101
35191000009105
34631000009100
35861000009107
28341000009108
282861000009100
309411000009103
309051000009105
319121000009101
309341000009105
311511000009102
321021000009109
45641000009106
343821000009104
45921000009108
338841000009103
309331000009104
277791000009109
EOF

# load these into a map.
# if it's an "isa" rel (116680003) in the stated rels file
# and sourceConcept id field is in the map, change to inferred
# looks like they are all in the "Relationships" file
# id      effectiveTime   active  moduleId        sourceId        destinationId   relationshipGroup       typeId  characteristicTypeId    modifierId
perl -ne 'BEGIN {open(IN,"/tmp/x.'$$'"); while(<IN>) { chop; $map{$_}=1;} close(IN); } \
  @_ = split /\t/; \
  if ($map{$_[4]} && $_[7] eq "116680003" && $_[8] eq "900000000000010007") { \
    $_[8] = "900000000000011006"; } print join "\t", @_;' Terminology/*_Rel*txt >! /tmp/y.$$
/bin/mv -f /tmp/y.$$ Terminology/*_Rel*txt
    

echo "-----------------------------------------------"
echo "Finished ...`/bin/date`"
echo "-----------------------------------------------"
