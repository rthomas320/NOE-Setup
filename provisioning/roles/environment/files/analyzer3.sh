#!/bin/bash

DIRTOMONITOR="/home/nifi/data/mockAnalyzer3/in"
DIRTOWRITE="/home/nifi/data/mockAnalyzer3/out"
JSONEXT=".json"

while true
do
  ls $DIRTOMONITOR | while read NEWFILE
  do
    CHECK=${#NEWFILE}
    if [ $CHECK -ge 1 ]
    then
      echo "The file '$NEWFILE' was found"
      MD5VAL="$( cut -d ' ' -f 1 <( cat $DIRTOMONITOR/$NEWFILE | md5sum - ))"
      FILETYPE="${NEWFILE##*.}"
      FILEPART="${NEWFILE%%.*}"
      echo "File name is '$FILEPART'"
      echo "Extension is '$JSONEXT'"
      echo '{"file_name":"'$NEWFILE'","results":[{"md5":"'$MD5VAL'","file_type":"'$FILETYPE'","classification":"not malicious"}]}'

      echo '{"file_name":"'$NEWFILE'","results":[{"md5":"'$MD5VAL'","file_type":"'$FILETYPE'","classification":"not malicious"}]}' >> $DIRTOWRITE/$FILEPART$JSONEXT
      echo "The report file '$FILEPART''$JSONEXT' was created." >> /home/nifi/data/mockAnalyzer3/analyzer3.log
      echo "Removing DIRTOMONITOR'/'$NEWFILE'"
      rm -f $DIRTOMONITOR/$NEWFILE
    fi
  done
  sleep 5
done
