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

# convert all relationships to "inferred" from "stated"
perl -ne ' \
  @_ = split /\t/; \
  if ($_[8] eq "900000000000010007") { \
    $_[8] = "900000000000011006"; } print join "\t", @_;' Terminology/*_Rel*txt >! /tmp/y.$$
/bin/mv -f /tmp/y.$$ Terminology/*_Rel*txt
    

echo "-----------------------------------------------"
echo "Finished ...`/bin/date`"
echo "-----------------------------------------------"
