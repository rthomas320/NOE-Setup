#!/bin/bash

DIRTOMONITOR="/home/nifi/data/mockSanitizer1/in"
DIRTOWRITE="/home/nifi/data/mockSanitizer1/out"

while true
do
  ls $DIRTOMONITOR | while read NEWFILE
  do
    CHECK=${#NEWFILE}
    if [ $CHECK -ge 1 ]
    then
      echo "The file '$NEWFILE' was modified and moved to out"
      echo "The file '$NEWFILE' was modified and moved to out" >> /home/nifi/data/mockSanitizer1/sanitzer1.log
      echo 's1' >> $DIRTOMONITOR/$NEWFILE
      echo "Moving '$DIRTOMONITOR'/'$NEWFILE' to '$DIRTOWRITE'/'$NEWFILE'"
      mv $DIRTOMONITOR/$NEWFILE $DIRTOWRITE/$NEWFILE
    fi
  done
  sleep 5
done
