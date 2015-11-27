#!/bin/csh -f

#
# Takes a Full directory and makes a Snapshot from it
#

#
# Set environment (if configured)
#


#
# Parse arguments
#
set usage = "Usage: $0 [-[-]help] <full dir>"
if ($#argv == 0) then
    echo "Error: Bad argument"
    echo "$usage"
    exit 1
endif

set i=1
while ($i <= $#argv)
    switch ($argv[$i])
        case '-*help':
            cat << EOF
    This script makes a Snapshot directory from the Full.

    $usage

Options:
    --help                    : display this help

EOF
            exit 0

        default :
            set arg_count=1
            set all_args=`expr $i + $arg_count - 1`
            if ($all_args != $#argv) then
                echo "Error: Incorrect number of arguments: $all_args, $#argv"
                echo "Usage: $usage"
                exit 1
            endif
            set fullDir = $argv[$i]
    endsw
    set i=`expr $i + 1`
end

echo "----------------------------------------------------------------------"
echo "Starting ... `/bin/date`"
echo "----------------------------------------------------------------------"
echo "Full dir:      $fullDir"
echo ""

if (-e "Snapshot") then
    echo "ERROR: there is already a Snapshot directory, please clean it up first"
    exit 1
endif

if (! -e $fullDir) then
    echo "ERROR: $fullDir does not exist"
    exit 1
endif

if (! -e "$fullDir/Refset") then
    echo "ERROR: $fullDir does not have the right format (no Refset subdir)"
    exit 1
endif

if (! -e "$fullDir/Terminology") then
    echo "ERROR: $fullDir does not have the right format (no Terminology subdir)"
    exit 1
endif

#
# Make snapshot
#
echo "    Make Snapshot ...`/bin/date`"
/bin/mkdir Snapshot

foreach f (`find $fullDir/Refset $fullDir/Terminology -name "*txt"`)
    echo $f
    set file_path = `echo $f | perl -pe 's/.*\/(Refset\/.*)/$1/; s/.*\/(Terminology\/.*)/$1/; s/Full/Snapshot/'`
    /bin/mkdir -p Snapshot/$file_path
    /bin/rmdir Snapshot/$file_path
    /usr/bin/head -1 $f >! Snapshot/$file_path
    perl -e ' \
    open($IN,$ARGV[0]) || die "Could not open $ARGV[0]: $! $?\n"; \
    while (<$IN>) { \
        ($id, $et, @x) = split /\t/; \
        next if $id =~ /^id/; \
        if (! $max{$id} || $max{$id} le $et) { \
            $max{$id} = $et; $lines{$id} = $_; \
        } \
    } \
    close($IN); \
    foreach $line (keys %lines) { \
        print $lines{$line}; \
    } ' $f | sort -u >> Snapshot/$file_path
    if ($status != 0) then
    echo "ERROR writing Snapshot/$file_path"
    exit 1
    endif
end

#
# cleanup
#
/bin/rm -f concepts.$$.txt

echo "----------------------------------------------------------------------"
echo "Finished ... `/bin/date`"
echo "----------------------------------------------------------------------"
