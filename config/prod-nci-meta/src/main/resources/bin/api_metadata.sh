#!/bin/bash
#
# Script to retrieve metadata from the API upon request to support inversions
#
while [[ "$#" -gt 0 ]]; do case $1 in
  --vsab) vsab="$2"; shift;;
  --quiet) quiet=1;;
  *) arr=( "${arr[@]}" "$1" );;
esac; shift; done

if [ ${#arr[@]} -ne 1 ]; then
  echo "Usage: $0 <mode> [--vsab <vsab>] [--quiet]"
  echo "  e.g. $0 max_src_atom_id --vsab NCI_2020_02D"
  echo "  e.g. $0 min_src_atom_id --vsab NCI_2020_02D"
  echo "  e.g. $0 rela_inverse"
  echo "  e.g. $0 expanded_form"
  echo "  e.g. $0 tty_class"
  echo "  e.g. $0 current_sources"
  exit 1
fi

mode=${arr[0]}

# import URL into environment from config
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
adminUser=`grep 'admin.user' $DIR/../config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
adminPwd=`grep 'admin.password' $DIR/../config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
#url=`grep 'base.url' $DIR/../config/config.properties | perl -ne '@_ = split/=/; print $_[1];'`
url=https://meme-edit.nci.nih.gov/ncim-server-rest

if [[ $quiet != 1 ]]; then
echo "-----------------------------------------------------"
echo "Starting ...$(/bin/date)"
echo "-----------------------------------------------------"
echo "mode = $mode"
echo "vsab = $vsab"
echo "url = $url"
echo "adminUser = $adminUser"
echo ""
fi

# Login
token=`curl -H "Content-type: text/plain" -X POST -d "$adminPwd" $url/security/authenticate/$adminUser 2>/dev/null | perl -pe 's/.*"authToken":"([^"]*).*/$1/;'`

# Perform call based on mode
if [[ $mode == "max_src_atom_id" ]] || [[ $mode == "min_src_atom_id" ]]; then
  curl -v -w "\n%{http_code}" -G "$url/inversion/range/1/$vsab" -H "Authorization: $token" 2> /dev/null > /tmp/x.$$
elif [[ $mode == "rela_inverse" ]] || [[ $mode == "expanded_form" ]] || [[ $mode == "tty_class" ]]; then
  curl -v -w "\n%{http_code}" -G "$url/metadata/all/NCIMTH/latest" -H "Authorization: $token" 2> /dev/null > /tmp/x.$$
elif [[ $mode == "current_sources" ]]; then
  curl -v -w "\n%{http_code}" -G "$url/metadata/terminology/current" -H "Authorization: $token" 2> /dev/null > /tmp/x.$$
else
    echo "ERROR: invalid mode = $mode"
    exit 1
fi

if [ $? -ne 0 ]; then
    perl -pe 's/200$//' /tmp/x.$$ | sed 's/^/    /'
    echo "ERROR: GET call failed"
    exit 1
fi

# check status
status=`tail -1 /tmp/x.$$`
if [ $status -ne 200 ]; then
    perl -pe 's/200$//' /tmp/x.$$ | sed 's/^/    /'
    echo "ERROR: GET returned $status, expected 200"
    exit 1
fi

# get output
# Perform call based on mode
if [[ $mode == "max_src_atom_id" ]]; then
    head -1 /tmp/x.$$ | python -c 'import sys, json; print json.load(sys.stdin)["sourceIdRanges"][0]["endSourceId"]'
elif [[ $mode == "min_src_atom_id" ]]; then
    head -1 /tmp/x.$$ | python -c 'import sys, json; print json.load(sys.stdin)["sourceIdRanges"][0]["beginSourceId"]'

elif [[ $mode == "rela_inverse" ]]; then
    cat > x.$$.py <<EOF
import sys, json
for x in json.load(sys.stdin)["keyValuePairLists"]:
  if x["name"] == "Additional_Relationship_Types":
    print json.dumps(x["keyValuePairs"])
EOF
    head -1 /tmp/x.$$ | python x.$$.py | python -m json.tool |\
    grep '"key"' | perl -pe 's/ +"key": "//; s/",?$//' > y.$$.txt
    for rela in `cat y.$$.txt`; do
        inverse=`curl --silent -G "$url/metadata/additionalRelationshipType/$rela/NCIMTH/latest" -H "Authorization: $token" | python -m json.tool | grep '"inverseAbbreviation"' | perl -pe 's/ *"inverseAbbreviation": "//; s/",?$//'`
        echo "$rela|$inverse"
done
    /bin/rm -f x.$$.py y.$$.txt

elif [[ $mode == "expanded_form" ]]; then
    cat > x.$$.py <<EOF
import sys, json
for x in json.load(sys.stdin)["keyValuePairLists"]:
  if x["name"] == "Term_Types":
    name = "TTY"
  if x["name"] == "Attribute_Names":
    name = "ATN"
  if x["name"] == "Additional_Relationship_Types":
    name = "RELA"
  for pair in x["keyValuePairs"]:
    s = name + "|" + pair["key"] + "|expanded_form|" + pair["value"]
    print(s)
EOF
    head -1 /tmp/x.$$ | python x.$$.py
    /bin/rm -f x.$$.py

elif [[ $mode == "tty_class" ]]; then
    cat > x.$$.py <<EOF
import sys, json
for x in json.load(sys.stdin)["keyValuePairLists"]:
  if x["name"] == "Term_Types":
    print json.dumps(x["keyValuePairs"])
EOF
    head -1 /tmp/x.$$ | python x.$$.py | python -m json.tool |\
    grep '"key"' | perl -pe 's/ +"key": "//; s/",?$//' > y.$$.txt
    for tty in `cat y.$$.txt`; do
        curl --silent -G "$url/metadata/termType/$tty/NCIMTH/latest" -H "Authorization: $token" | python -m json.tool | egrep '"(code|name)VariantType"' | perl -pe 's/ *"(code|name)VariantType": "//; s/",?$//' | perl -pe "s/^/TTY|$tty|tty_class|/"
done
    /bin/rm -f x.$$.py y.$$.txt

elif [[ $mode == "current_sources" ]]; then

    head -1 /tmp/x.$$ | python -m json.tool | egrep '"(terminology|version)"' |\
      perl -ne 'chop; s/ *"(terminology|version)": "//; s/",?$//; if ($x) { print "$x|".$x."_$_\n"; $x= ""; } else {$x = $_; }' |\
      perl -pe 's/_latest//;'

fi

# Uncomment this for testing
# cat /tmp/x.$$

# Cleanup
/bin/rm -f /tmp/x.$$

if [[ $quiet != 1 ]]; then
echo "-----------------------------------------------------"
echo "Finished ...`/bin/date`"
echo "-----------------------------------------------------"
fi
