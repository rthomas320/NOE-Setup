#!/bin/bash

DIRTOMONITOR="/home/nifi/data/mockAnalyzer1/in"
DIRTOWRITE="/home/nifi/data/mockAnalyzer1/out"
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
      RPTTXT='{"file_name":"'$NEWFILE'","results":[{"md5":"'$MD5VAL'","file_type":"'$FILETYPE'","classification":"not malicious"}]}'
      echo $RPTTXT
      echo "Analyzing $NEWFILE"
      echo "Analyzing $NEWFILE" >> /home/nifi/data/mockAnalyzer1/analyzer1.log
      echo $RPTTXT >> $DIRTOMONITOR/$FILEPART$JSONEXT
      echo "The report file $FILEPART$JSONEXT was written to $DIRTOWRITE/$FILEPART$JSONEXT." >> /home/nifi/data/mockAnalyzer1/analyzer1.log
      echo "The report file $FILEPART$JSONEXT was written to $DIRTOWRITE/$FILEPART$JSONEXT."
      echo "Removing '$DIRTOMONITOR'/'$NEWFILE'"
      rm -f $DIRTOMONITOR/$NEWFILE
      mv $DIRTOMONITOR/$FILEPART$JSONEXT $DIRTOWRITE/$FILEPART$JSONEXT
    fi
  done
  sleep 5
done
