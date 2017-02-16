#!/bin/tcsh -f
#
# This script is used to build the configuration
# files used by MetamorphoSys.
#

# set MEME_HOME 
set rootdir = `dirname $0`
set abs_rootdir = `cd $rootdir && pwd`
setenv MEME_HOME $abs_rootdir:h

echo "--------------------------------------------------------"
echo "Starting `/bin/date`"
echo "--------------------------------------------------------"
echo "MEME_HOME = $MEME_HOME"

echo "Collect settings..."
set host = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$1"'`
set port = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$2"'`
set db = `grep 'javax.persistence.jdbc.url' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; $_[1] =~ /jdbc:mysql:\/\/(.*):(\d*)\/(.*)\?/; print "$3"'`
set user = `grep 'javax.persistence.jdbc.user' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set pwd = `grep 'javax.persistence.jdbc.password' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set mysql = "mysql -h$host -P$port -u$user -p$pwd $db"
set url = `grep 'base.url' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set adminUser = `grep 'admin.user' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set adminPwd = `grep 'admin.password' $MEME_HOME/config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
set enabled = `echo "select if(automationsEnabled,'true','false') from projects;" | $mysql | tail -1`
set projectId = `echo "select id from projects;" | $mysql | tail -1`

echo "project: $projectId"
echo "enabled: $enabled"

if ($#argv == 3) then
    setenv META_RELEASE $1
    set NET_DIR=$2
    setenv MMSYS_DIR $3
else
    echo "ERROR: Wrong number of parameters"
    echo "ERROR: $usage"
    exit 1
endif

if (!(-e $META_RELEASE/MRSAB.RRF)) then
    echo "ERROR: $META_RELEASE/MRSAB.RRF does not exist."
    exit 1
endif

if (!(-e $META_RELEASE/MRRANK.RRF)) then
    echo "$META_RELEASE/MRRANK.RRF does not exist."
    exit 1
endif

if (!(-e $META_RELEASE/MRCONSO.RRF)) then
    echo "$META_RELEASE/MRCONSO.RRF does not exist."
    exit 1
endif

if (!(-e $META_RELEASE/MRDOC.RRF)) then
    echo "$META_RELEASE/MRDOC.RRF does not exist."
    exit 1
endif

if (!(-e $META_RELEASE/MRSTY.RRF)) then
    echo "$META_RELEASE/MRSTY.RRF does not exist."
    exit 1
endif

if (!(-e $META_RELEASE/MRREL.RRF)) then
    echo "$META_RELEASE/MRREL.RRF does not exist."
    exit 1
endif

if (!(-e $META_RELEASE/MRSAT.RRF)) then
    echo "$META_RELEASE/MRSAT.RRF does not exist."
    exit 1
endif

if (!(-e $META_RELEASE/MRDEF.RRF)) then
    echo "$META_RELEASE/MRDEF.RRF does not exist."
    exit 1
endif

if (!(-e $MMSYS_DIR/release.dat)) then
    echo "$MMSYS_DIR/release.dat does not exist."
    exit 1
endif

echo "META_RELEASE:          $META_RELEASE"
echo "NET_DIR:               $NET_DIR"
echo "MMSYS_DIR:             $MMSYS_DIR"

echo ""
echo "    Note: This script assumes that the restriction level "
echo "          and suppressibility flags for the sources and "
echo "          termgroups have already been verified.  Additionally"
echo "          it assumes that the source list in MRSAB.RRF is correct"
echo "          and that MRSAB.RRF conforms to the spec"
echo ""

echo "    Get data from release.dat... `/bin/date`"

foreach f ("`grep = $MMSYS_DIR/release.dat`")
   set var = `echo $f | sed 's/=.*//' | sed 's/\./_/g' | sed 's/description/des/g'`
   set val = `echo $f | sed 's/.*=//'`
   eval setenv $var "'"$val"'"
end

setenv OUTPUT_DIR $MMSYS_DIR/config/$umls_release_name
echo "      OUTPUT_DIR:          $OUTPUT_DIR"
echo ""


echo "    Get previous release version  ... `/bin/date`"

awk -F\| '{print $2}' $META_RELEASE/MRCUI.RRF >! test.out
set prev_release = `/bin/sort -u test.out | tail -1`
echo "      previous release: $prev_release"


echo "    Get data for sources and termgroups  ... `/bin/date`"
#
# sources property has the following fields:
#   RSAB|SON|VSAB|SF|RL|LAT|CFR|IMETA
#
$PATH_TO_PERL -ne 'split /\|/; print "$_[3]|$_[4]|$_[2]|$_[5]|$_[13]|$_[19]|$_[15]|$_[9]\n" if $_[22] eq "Y";' \
  $META_RELEASE/MRSAB.RRF | sort -t\| -k 1,1 -u >! $OUTPUT_DIR/source_info.dat

#
# Get data for precedence and suppressed_termgroups properties
# RANK|SAB|TTY|SUPPRESS
#
sort -t\| -k 1,1 -o $OUTPUT_DIR/source_info.dat{,}
$sed 's/^.//' $META_RELEASE/MRRANK.RRF | $sed 's/\|$//' |\
  sort -t\| -k 2,2 |\
  join -t\| -j1 2 -j2 1 -o 1.1 2.1 1.3 1.4 - $OUTPUT_DIR/source_info.dat >! $OUTPUT_DIR/termgroup_info.dat

#
# Sort source/termgroup data
#
sort -t\| -k 1,1 -o $OUTPUT_DIR/source_info.dat{,}
sort -t\| -k 2,2 -o $OUTPUT_DIR/termgroup_info.dat{,}

#
# Join sources/termgroups to obtain the following fields:
#  VSAB|TTY|RANK|RSAB|SUPPRESS
# Sort by rank (in reverse order) for precedence property
#
join -t\| -1 1 -2 2 -o 1.3 2.3 2.1 1.1 2.4 \
  $OUTPUT_DIR/source_info.dat $OUTPUT_DIR/termgroup_info.dat |\
  sort -t\| -k 3,3r -o $OUTPUT_DIR/join1.dat

#
# Get precedence property
#
awk -F\| '{print $4 "|" $2}' $OUTPUT_DIR/join1.dat >! $OUTPUT_DIR/precedence.dat

#
# Get suppressed termgroups by
# keeping rows with suppressible='Y'
#
/bin/cat $OUTPUT_DIR/join1.dat | \
  awk -F\| '{if($5=="Y") {print $4 "|" $2}}' >! $OUTPUT_DIR/suppr_tg.dat

#
# Get sources to remove list by finding
# rows that do not have a restriction level of 0
#
echo "    Get sources to remove ... `/bin/date`"
$PATH_TO_PERL -ne 'chop; split /\|/; print "$_[0]|$_[3]\n" if $_[4] ne "0";' \
  $OUTPUT_DIR/source_info.dat >! $OUTPUT_DIR/sources_to_remove.dat

#
# Get data for languages property from language table
# Fields: LAT|LANGUAGE
#
echo "    Get languages ... `/bin/date`"
awk -F\| '{if ($1 == "LAT") {print $2"|"$4}} ' $META_RELEASE/MRDOC.RRF | sort -u |\
  awk -F\| '{print $1 }' >! $OUTPUT_DIR/lat.dat

#
# Get data for sty property from sty table
# Fields: UI|STY|STN
#
echo "    Get STYs ... `/bin/date`"
awk -F\| '{print $2"|"$4"|"$3} ' $META_RELEASE/MRSTY.RRF | sort -u >! $OUTPUT_DIR/stys.dat

#
# Get data for rel property from rel table
# Fields: RSAB|REL|RELA
#
echo "    Get SAB/REL/RELA ... `/bin/date`"
$PATH_TO_PERL -ne 'split /\|/; next if $map{"$_[10]$_[3]$_[7]"}; \
     $map{"$_[10]$_[3]$_[7]"} = 1; \
     print "$_[10]|$_[3]|$_[7]\n"' $META_RELEASE/MRREL.RRF |\
   sort -u >! $OUTPUT_DIR/rel_types.dat

#
# Get data for attributes property from attributes table
# Fields: RSAB|ATN
# Use MEMBERSTATUS instead of SUBSETMEMBER
#
echo "    Get SAB/ATN ... `/bin/date`"
$PATH_TO_PERL -ne 'chop; split /\|/; \
   if ($_[8] eq "SUBSETMEMBER") { \
     next if $map{"MEMBERSTATUS$_[9]"}; \
     $map{"MEMBERSTATUS$_[9]"} = 1; \
     print "$_[9]|MEMBERSTATUS\n"; \
   } elsif ($_[8] eq "CV_MEMBER")  { \
     @fields = split(/~/, $_[10]); \
     $atn = $fields[1]; \
     next if $map{"$atn$_[9]"}; \
     $map{"$atn$_[9]"} = 1; \
     print "$_[9]|$atn\n"; \
   } elsif ($_[8] ne "DA" && $_[8] ne "MR" && $_[8] ne "ST")  { \
     next if $map{"$_[8]$_[9]"}; \
     $map{"$_[8]$_[9]"} = 1;; \
     print "$_[9]|$_[8]\n"; } ' $META_RELEASE/MRSAT.RRF |\
   sort -u >! $OUTPUT_DIR/att_types.dat

#
# Build RELA to SNOMEDCT RELATIONTYPE map
#
echo "    Get SNOMEDCT RELA mappings ... `/bin/date`"
$PATH_TO_PERL -ne 'chop; split /\|/; print "$_[3]|$_[1]\n" if $_[2] eq "snomedct_rela_mapping"' $META_RELEASE/MRDOC.RRF >! $OUTPUT_DIR/snomed_rela_map.dat

#
#  Get data for mrpluscolsfiles.dat
#
echo "    Get mrpluscolsfiles.dat ... `/bin/date`"
$PATH_TO_PERL -ne 'chop; split /\|/; print "$_[0]|MRFILES|$_[2]|$_[1]|\n"' $META_RELEASE/MRFILES.RRF >! $OUTPUT_DIR/mrpluscolsfiles.dat
$PATH_TO_PERL -ne 'chop; split /\|/; print "$_[0]|MRCOLS|$_[7]|$_[1]|\n"' $META_RELEASE/MRCOLS.RRF >> $OUTPUT_DIR/mrpluscolsfiles.dat
/bin/sort -u -o $OUTPUT_DIR/mrpluscolsfiles.dat $OUTPUT_DIR/mrpluscolsfiles.dat

#
# Get max field length. This is based on the max number of
# characters in an ATV in MRSAT that has multi-byte UTF-8
# characters but no more than 4000 bytes of data.
#
echo "    Get max field length ... `/bin/date`"
$PATH_TO_PERL -e 'open (F,"$ENV{META_RELEASE}/MRSAT.RRF"); \
   while (<F>) {split /\|/; if($_[10] =~ /[\200-\377]/) { \
     $_[10] = substr($_[10],0,4000); \
     print (join "|", @_) if length($_[10])>3999; } \
   } close(F);' |\
  $PATH_TO_PERL -e 'binmode(STDIN,":utf8"); \
  while (<>) {split /\|/; print length($_[10]),"\n";}' |\
  sort -n | tail -1 >! /tmp/mfl.$$
$PATH_TO_PERL -e 'open (F,"$ENV{META_RELEASE}/MRDEF.RRF"); \
   while (<F>) {split /\|/; if($_[5] =~ /[\200-\377]/) { \
     $_[5] = substr($_[5],0,4000); \
     print (join "|", @_) if length($_[5])>3999; } \
   } close(F);' |\
  $PATH_TO_PERL -e 'binmode(STDIN,":utf8"); \
  while (<>) {split /\|/; print length($_[5]),"\n";}' |\
  sort -n | tail -1 >> /tmp/mfl.$$
set max_field_length=`/bin/sort -n /tmp/mfl.$$ | head -1`
if ($max_field_length == "") set max_field_length=4000
/bin/rm -f /tmp/mfl.$$

#
# Get character map for unicode filter
#
echo "    Get char map ... `/bin/date`"
    $PATH_TO_PERL -e '\
      my $IN; my %chars = (); \
        open($IN,"<:encoding(utf8)","$ENV{META_RELEASE}/MRCONSO.RRF"); \
        binmode(STDOUT,":utf8"); \
        while (<$IN>) { \
          split /\|/; \
           if ($_[1] !~ /(JPN|KOR)/ && $_[14] =~ /[^\x00-\x7F]/) { \
            my $ch; \
            foreach $ch (split (//,$_[14])) { \
              if (ord($ch)>127) { $chars{$ch}=1;} \
            } \
        } } \
      foreach $key (keys %chars) { \
        print "$key\n"; \
      } \
      close($IN); ' >! $OUTPUT_DIR/chars.tmp.dat

    $PATH_TO_PERL -e '\
      my $IN; my %chars = (); \
        open($IN,"<:encoding(utf8)","$ENV{META_RELEASE}/MRSAT.RRF"); \
        binmode(STDOUT,":utf8"); \
        while (<$IN>) { \
          split /\|/; \
           if ($_[9] !~ /(JPN|KOR)/ && $_[10] =~ /[^\x00-\x7F]/) { \
            my $ch; \
            foreach $ch (split (//,$_[10])) { \
              if (ord($ch)>127) { $chars{$ch}=1;} \
            } \
        } } \
      foreach $key (keys %chars) { \
        print "$key\n"; \
      } \
      close($IN); ' >> $OUTPUT_DIR/chars.tmp.dat

    $PATH_TO_PERL -e '\
      my $IN; my %chars = (); \
        open($IN,"<:encoding(utf8)","$ENV{META_RELEASE}/MRDEF.RRF"); \
        binmode(STDOUT,":utf8"); \
        while (<$IN>) { \
          split /\|/; \
           if ($_[4] !~ /(JPN|KOR)/ && $_[5] =~ /[^\x00-\x7F]/) { \
            my $ch; \
            foreach $ch (split (//,$_[5])) { \
              if (ord($ch)>127) { $chars{$ch}=1;} \
            } \
        } } \
      foreach $key (keys %chars) { \
        print "$key\n"; \
      } \
      close($IN); ' >> $OUTPUT_DIR/chars.tmp.dat
      cat $OUTPUT_DIR/chars.tmp.dat | $LVG_HOME/bin/lvg -f:q7:q8 |\
      cut -d\| -f 1,2 | $PATH_TO_PERL -pe 's/\|/\:/' >! $OUTPUT_DIR/chars.dat
          /bin/rm -f $OUTPUT_DIR/chars.tmp.dat

#
# Write all prop files: user.{a,b,c,d}.prop
#
foreach f (a b c d)
echo "    Write user.$f.prop ... `/bin/date`"
if ($f == a) set name="Level 0"
if ($f == a) set description="Exclude all non-level 0 sources"
if ($f == b) set name="Level 0 + SNOMEDCT"
if ($f == b) set description="Exclude all non-level 0 sources except SNOMEDCT"
if ($f == c) set name="SNOMEDCT + SCTUSX"
if ($f == c) set description="Include only SNOMEDCT and US Extension to SNOMEDCT"
if ($f == d) set name="Active Subset"
if ($f == d) set description="Include only active UMLS sources"
    #
    # Write all data to configuration file
    #
    echo "    Write configuration file ... `/bin/date`"
/bin/rm -f $OUTPUT_DIR/user.$f.prop

    cat <<EOF >! $OUTPUT_DIR/user.$f.prop
# Configuration Properties File
# `date`
#
# Directories
#

meta_source_uri=../
umls_source_uri=../
meta_destination_uri=
umls_destination_uri=

release_version=$umls_release_name


#
# List of selected sources
#
EOF

if ($f == "c") then
   echo "gov.nih.nlm.umls.mmsys.filter.SourceListFilter.selected_sources=SNOMEDCT|SNOMEDCT;SCTUSX|SNOMEDCT" >> $OUTPUT_DIR/user.$f.prop
else if ($f == "d") then
   echo "gov.nih.nlm.umls.mmsys.filter.SourceListFilter.selected_sources=AIR|AIR;AOD|AOD;BI|BI;CCPSS|CCPSS;COSTAR|COSTAR;CPTSP|CPT;CST|CST;DDB|DDB;DMDUMD|UMD;DSM3R|DSM3R;DXP|DXP;HLREL|HLREL;ICPC|ICPC;JABL|JABL;LCH|LCH;MCM|MCM;MTHMST|MTHMST;NCISEER|NCISEER;PCDS|PCDS;PPAC|PPAC;QMR|QMR;RAM|RAM;RCD|RCD;SNM|SNM;SNMI|SNMI;ULT|ULT;WHO|WHO;ICPCBAQ|ICPC;ICPCDAN|ICPC;ICPCDUT|ICPC;ICPCFIN|ICPC;ICPCFRE|ICPC;ICPCGER|ICPC;ICPCHEB|ICPC;ICPCHUN|ICPC;ICPCITA|ICPC;ICPCNOR|ICPC;ICPCPOR|ICPC;ICPCSPA|ICPC;ICPCSWE|ICPC;MTHMSTFRE|MTHMST;MTHMSTITA|MTHMST;RCDAE|RCD;RCDSA|RCD;RCDSY|RCD;WHOFRE|WHO;WHOGER|WHO;WHOPOR|WHO;WHOSPA|WHO" >> $OUTPUT_DIR/user.$f.prop
else
    $PATH_TO_PERL -e '\
        open(SAB, "$ENV{OUTPUT_DIR}/sources_to_remove.dat"); \
        binmode(SAB,":utf8"); \
        $remove_line = "gov.nih.nlm.umls.mmsys.filter.SourceListFilter.selected_sources="; \
        while ($line = <SAB>) { \
          if ($ARGV[0] eq "b" && ($line =~ /SNOMEDCT/ || $line =~ /MTHSCT/)) { next; } \
          chop($line); $remove_line .= "$line;"; } \
        $remove_line =~ s/;$//; \
        print "$remove_line\n";' $f >> $OUTPUT_DIR/user.$f.prop
endif

cat <<EOF >> $OUTPUT_DIR/user.$f.prop

#
# Precedence
# fields are: RSAB|TTY
#
EOF

    $PATH_TO_PERL -e '\
        open(PREC, "$ENV{OUTPUT_DIR}/precedence.dat"); \
        binmode(PREC,":utf8"); \
        $prec_line = "gov.nih.nlm.umls.mmsys.filter.PrecedenceFilter.precedence="; \
        while ($line = <PREC>) { \
            chop($line); $prec_line .= "$line;"; } \
        $prec_line =~ s/;$//; \
        print "$prec_line\n";' >> $OUTPUT_DIR/user.$f.prop

    cat <<EOF >> $OUTPUT_DIR/user.$f.prop

#
# SAB|TTY that are considered suppressible
# fields are: RSAB|TTY
#
EOF

    $PATH_TO_PERL -e ' \
        open(PREC, "$ENV{OUTPUT_DIR}/suppr_tg.dat"); \
        binmode(PREC,":utf8"); \
        $suptgs_line = "gov.nih.nlm.umls.mmsys.filter.SuppressibleFilter.suppressed_sabttys="; \
        while ($line = <PREC>) { \
            chop($line); $suptgs_line .= "$line;"; } \
        $suptgs_line =~ s/;$//; \
        print "$suptgs_line\n";' >> $OUTPUT_DIR/user.$f.prop

    cat <<EOF >> $OUTPUT_DIR/user.$f.prop

#
# Languages
#
EOF

        $PATH_TO_PERL -e '\
        open(PREC, "$ENV{OUTPUT_DIR}/lat.dat"); \
        binmode(PREC,":utf8"); \
        $lprop="gov.nih.nlm.umls.mmsys.filter.LanguagesFilter.selected_languages=";  \
        while ($line = <PREC>) { \
            chop($line); $lprop .= "$line;" unless $line eq "ENG";  } \
        $lprop =~ s/;$//; \
        print "$lprop\n";' >> $OUTPUT_DIR/user.$f.prop

    cat <<EOF >> $OUTPUT_DIR/user.$f.prop


#
# Semantic Types
#
EOF

    $PATH_TO_PERL -e '\
        open(PREC, "$ENV{OUTPUT_DIR}/stys.dat"); \
        binmode(PREC,":utf8"); \
        $stys="gov.nih.nlm.umls.mmsys.filter.SemanticTypesFilter.selected_semantic_types="; \
        $stys =~ s/;$//;  \
        print "$stys\n";' >> $OUTPUT_DIR/user.$f.prop

    cat <<EOF >> $OUTPUT_DIR/user.$f.prop

#
# Relation Types
# fields are: RSAB|TYPE
#
gov.nih.nlm.umls.mmsys.filter.RelationsFilter.selected_relation_types=
gov.nih.nlm.umls.mmsys.filter.RelationsFilter.remove_selected_rels=true

#
# Attributes Types
# fields are: SAB|ATN
#
gov.nih.nlm.umls.mmsys.filter.AttributesFilter.selected_attribute_types=
gov.nih.nlm.umls.mmsys.filter.AttributesFilter.remove_selected_attributes=true

#
# Content View Information
#
gov.nih.nlm.umls.mmsys.filter.ContentViewFilter.selected_views=

#
# Unicode Filter Info
#
gov.nih.nlm.umls.mmsys.filter.UnicodeFilter.convert_unicode_char=true
EOF


    $PATH_TO_PERL -e '\
    open(IN, "$ENV{OUTPUT_DIR}/chars.dat"); \
    binmode(IN,":utf8"); \
    binmode(STDOUT,":utf8"); \
    $lprop="gov.nih.nlm.umls.mmsys.filter.UnicodeFilter.char_map=";  \
    while ($line = <IN>) { \
        chop($line); split /\|/; \
        $lprop .= "$line;";  } \
    $lprop =~ s/;$//; \
    print "$lprop\n";' >! char_map.prop

    cat >! X.java << EOF
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.StringTokenizer;
public class X {
    public static void main(String[] s) {
        try {
            FileInputStream fin = new FileInputStream(s[0]);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(fin, "UTF-8"));
            String line = null;
            Properties p = new Properties();
            while ((line = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line,"=");
                p.put(st.nextToken(),st.nextToken());
            }
            in.close();
            p.store(new FileOutputStream(s[1]),"no comments");
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
EOF
    setenv CLASSPATH .
    $JAVA_HOME/bin/javac X.java
    if ($status != 0) then
        echo "Javac of X.java failed"
        exit 1
    endif
    $JAVA_HOME/bin/java X char_map.prop x.prop
    if ($status != 0) then
        echo "Conversion program failed"
        exit 1
    endif
    grep char_map= x.prop  >> $OUTPUT_DIR/user.$f.prop
    /bin/rm -rf X.java X.class char_map.prop x.prop

        if ($f == "c") then
                set rss = false
        else
                set rss = true
        endif
    cat <<EOF >> $OUTPUT_DIR/user.$f.prop

#
# Default Subset Configuration Information
#
name=$name
description=$description

#
# Input Stream Configuration
#
mmsys_input_stream=gov.nih.nlm.umls.mmsys.io.NLMFileMetamorphoSysInputStream
gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysInputStream.enable_efficient=true
gov.nih.nlm.umls.mmsys.io.NLMFileMetamorphoSysInputStream.enable_efficient=true

#
# Output Steam Configuration
#
mmsys_output_stream=gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream

gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream.database=
gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream.versioned_output=false
gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream.max_field_length=$max_field_length
gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream.truncate=false
gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream.remove_utf8=false
gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream.remove_mth_only=false
gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream.calculate_md5s=false
gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream.add_unicode_bom=false
gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream.character_encoding=UTF-8
gov.nih.nlm.umls.mmsys.io.RRFMetamorphoSysOutputStream.build_indexes=true

gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream.database=
gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream.versioned_output=false
gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream.max_field_length=$max_field_length
gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream.truncate=false
gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream.remove_utf8=false
gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream.remove_mth_only=false
gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream.calculate_md5s=false
gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream.add_unicode_bom=false
gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream.character_encoding=UTF-8
gov.nih.nlm.umls.mmsys.io.ORFMetamorphoSysOutputStream.build_indexes=true

#
# Advanced Options Properties
#
gov.nih.nlm.umls.mmsys.filter.SourceListFilter.enforce_family_selection=true
gov.nih.nlm.umls.mmsys.filter.SourceListFilter.enforce_dep_source_selection=true
gov.nih.nlm.umls.mmsys.filter.SourceListFilter.remove_selected_sources=$rss
gov.nih.nlm.umls.mmsys.filter.SourceListFilter.base_url=http://www.nlm.nih.gov/research/umls/sourcereleasedocs/
gov.nih.nlm.umls.mmsys.filter.SourceListFilter.ip_associations=
automatic_selection=false
gov.nih.nlm.umls.mmsys.filter.SuppressibleFilter.remove_source_tty_suppressible_data=false
gov.nih.nlm.umls.mmsys.filter.SuppressibleFilter.remove_editor_suppressible_data=false
gov.nih.nlm.umls.mmsys.filter.SuppressibleFilter.remove_obsolete_data=false

gov.nih.nlm.umls.mmsys.filter.PrecedenceFilter.cut_mode=false
gov.nih.nlm.umls.mmsys.filter.SemanticTypesFilter.any_sty=true
gov.nih.nlm.umls.mmsys.filter.SemanticTypesFilter.remove_selected_stys=true
gov.nih.nlm.umls.mmsys.filter.SourceTermTypesFilter.remove_selected_sabtty=true
gov.nih.nlm.umls.mmsys.filter.SourceTermTypesFilter.selected_sabttys=

active_filters=gov.nih.nlm.umls.mmsys.filter.SourceListFilter;gov.nih.nlm.umls.mmsys.filter.PrecedenceFilter;gov.nih.nlm.umls.mmsys.filter.SuppressibleFilter

EOF

end

echo "    Write umls.prop ... `/bin/date`"
/bin/rm -f $OUTPUT_DIR/umls.prop

    cat <<EOF >> $OUTPUT_DIR/umls.prop
#
# Configuration Properties File
# `date`
#
EOF

    cat <<EOF >> $OUTPUT_DIR/umls.prop

#
# SAB/ATN combinations
# fields are: RSAB|ATN
#
EOF

    $PATH_TO_PERL -e '\
    open(SABATN, "$ENV{OUTPUT_DIR}/att_types.dat"); \
    binmode(SABATN,":utf8"); \
    $prop_line = "sabatns="; \
    while ($line = <SABATN>) { \
        chop($line); $prop_line .= "$line;"; } \
    $prop_line =~ s/;$//; \
    print "$prop_line\n";' >> $OUTPUT_DIR/umls.prop

    cat <<EOF >> $OUTPUT_DIR/umls.prop

#
# SAB/REL/RELA combinations
# fields are: RSAB|REL|RELA
#
EOF

    $PATH_TO_PERL -e '\
    open(SABRR, "$ENV{OUTPUT_DIR}/rel_types.dat"); \
    binmode(SABRR,":utf8"); \
    $prop_line = "sabrelrelas="; \
    while ($line = <SABRR>) { \
        chop($line); $prop_line .= "$line;"; } \
    $prop_line =~ s/;$//; \
    print "$prop_line\n";' >> $OUTPUT_DIR/umls.prop

    $PATH_TO_PERL -ne 'split /\|/; print "$_[11]\n" if $_[9] ne "";' $META_RELEASE/MRCONSO.RRF |\
      /bin/sort -u | $PATH_TO_PERL -ne 'chomp; print "$_;";' >! x$$.tmp
    set scui_sources=`cat x$$.tmp | $PATH_TO_PERL -pe 's/;$//'`
    /bin/rm -f x$$.tmp

    $PATH_TO_PERL -ne 'split /\|/; print "$_[11]\n" if $_[10] ne "";' $META_RELEASE/MRCONSO.RRF |\
      /bin/sort -u | $PATH_TO_PERL -ne 'chomp; print "$_;";' >! x$$.tmp
    set sdui_sources=`cat x$$.tmp | $PATH_TO_PERL -pe 's/;$//'`
    /bin/rm -f x$$.tmp

    cat <<EOF >> $OUTPUT_DIR/umls.prop
#
# Indicates which sources have SCUI clusters
#
scui_sources=$scui_sources

#
# Indicates which sources have SDUI clusters
#
sdui_sources=$sdui_sources

#
# Release Information
#
release_version=$umls_release_name
release_description=$umls_release_des
release_date=$umls_release_date
previous_version=$prev_release



#
# Content Views
#
EOF

#
$PATH_TO_PERL -ne 'split /\|/; print if $_[9] eq "MTH" && ($_[8] eq "CV_CODE" || $_[8] eq "CV_DESCRIPTION");' \
   $META_RELEASE/MRSAT.RRF >&! $META_RELEASE/cvfmrsat.dat
set id=0
set cvfs=""
foreach cui (`/bin/cut -d\| -f 1 $META_RELEASE/cvfmrsat.dat | /bin/sort -u`)
    set name=`$MEME_HOME/bin/look.pl $cui $META_RELEASE/MRCONSO.RRF | grep "|CV|" | /bin/cut -d\| -f 15`
    set description=`grep CV_DESCRIPTION $META_RELEASE/cvfmrsat.dat | grep "^$cui" | /bin/cut -d\| -f 11`
    set code=`grep CV_CODE $META_RELEASE/cvfmrsat.dat | grep "^$cui" | /bin/cut -d\| -f 11`
    set includeObsolete=`grep CV_INCLUDE_OBSOLETE $META_RELEASE/cvfmrsat.dat | grep "^$cui" | /bin/cut -d\| -f 11`
    if ($includeObsolete == "") then
      set includeObsolete = "Y"
    endif
    set append_value=($cui"|"$name"|"$description"|"$code"|"$includeObsolete";")
    set cvfs="${cvfs}${append_value}"
end
/bin/rm -f $META_RELEASE/cvfmrsat.dat
echo "$cvfs" >> $OUTPUT_DIR/cvfs.dat

    $PATH_TO_PERL -e '\
        open(PREC, "$ENV{OUTPUT_DIR}/cvfs.dat"); \
        binmode(PREC,":utf8"); \
        $lprop = "content_views="; \
        while ($line = <PREC>) { \
            chop($line);  \
        $lprop .= "$line"  } \
        print "$lprop\n";' >> $OUTPUT_DIR/umls.prop

    cat <<EOF >> $OUTPUT_DIR/umls.prop

#
# Subsets
#
EOF

    $PATH_TO_PERL -ne 'BEGIN { print "subsets="; } split /\|/; \
        print "$_[0]|$_[13]|$_[11]|$_[14]|$_[14];" if $_[12] eq "SB"' $META_RELEASE/MRCONSO.RRF \
        >> $OUTPUT_DIR/umls.prop

    cat <<EOF >> $OUTPUT_DIR/umls.prop

#
# Valid/Active filters
#
valid_filters=gov.nih.nlm.umls.mmsys.filter.SourceListFilter;gov.nih.nlm.umls.mmsys.filter.PrecedenceFilter;gov.nih.nlm.umls.mmsys.filter.SuppressibleFilter;gov.nih.nlm.umls.mmsys.filter.AttributesFilter;gov.nih.nlm.umls.mmsys.filter.LanguagesFilter;gov.nih.nlm.umls.mmsys.filter.RelationsFilter;gov.nih.nlm.umls.mmsys.filter.SemanticTypesFilter;gov.nih.nlm.umls.mmsys.filter.ContentViewFilter;gov.nih.nlm.umls.mmsys.filter.SourceTermTypeFilter

EOF


echo "    Copying MR*RRF files .... `/bin/date`"
/bin/cp $META_RELEASE/MRRANK.RRF $OUTPUT_DIR
/bin/cp $META_RELEASE/MRSAB.RRF $OUTPUT_DIR
/bin/cp $META_RELEASE/MRDOC.RRF $OUTPUT_DIR
if (`grep -c MEMBERSTATUS $OUTPUT_DIR/MRDOC.RRF` == 0) then
    echo "ATN|MEMBERSTATUS|expanded_form|Member Status|" >> $OUTPUT_DIR/MRDOC.RRF
    /bin/sort -u -o $OUTPUT_DIR/MRDOC.RRF{,}
endif

/bin/rm -f $OUTPUT_DIR/MRSTY.RRF.gz
/bin/cp $META_RELEASE/MRSTY.RRF $OUTPUT_DIR
gzip -9 $OUTPUT_DIR/MRSTY.RRF &
/bin/cp $NET_DIR/SRDEF $OUTPUT_DIR
/bin/cp $NET_DIR/SRSTR $OUTPUT_DIR

# This is now done by package-release.pl after nlm.build.date is added
#/bin/cp $MMSYS_DIR/release.dat $OUTPUT_DIR

echo "    Prep DAMRST.txt.gz .... `/bin/date`"
$PATH_TO_PERL -ne 'split /\|/; print "$_[0]|$_[8]|$_[6]|\n" if $_[8] eq "DA" || $_[8] eq "MR" || $_[8] eq "ST"' $META_RELEASE/MRSAT.RRF | gzip -9 >&!  $OUTPUT_DIR/DAMRST.txt.gz

echo "    Cleanup ... `/bin/date`"
/bin/rm -f $OUTPUT_DIR/join1.dat $OUTPUT_DIR/termgroup_info.dat $OUTPUT_DIR/sr.dat
/bin/rm -f $OUTPUT_DIR/mrsab.dat $OUTPUT_DIR/source_info.dat $OUTPUT_DIR/chars.dat
/bin/rm -f $OUTPUT_DIR/stys.dat $OUTPUT_DIR/suppr_tg.dat $OUTPUT_DIR/precedence.dat
/bin/rm -f $OUTPUT_DIR/att_types.dat $OUTPUT_DIR/rel_types.dat
/bin/rm -f $OUTPUT_DIR/sources_to_remove.dat $OUTPUT_DIR/lat.dat $OUTPUT_DIR/cvfs.dat

wait

echo "-----------------------------------------------------"
echo "Finished $0 ... `/bin/date`"
echo "-----------------------------------------------------"
