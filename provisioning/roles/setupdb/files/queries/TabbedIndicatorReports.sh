#!/bin/bash
arraytest[0]='test' || (echo 'Failure: arrays not supported in this version of
bash.' && exit 2)

DateVal=$(date +%Y%m%d)
SQLPath=/home/nifi/queries/
ReportPath=/home/nifi/sqlreports/$DateVal/
mkdir -p $ReportPath

XSql=.sql
XTxt=.txt
Files=(CylanceIndicators
       ReversingLabsIndicators
       GlasswallIndicators
       PrePostCountsAll
       PrePostCountsSuspicious
       FileCounts
       )
for File in "${Files[@]}"
do
    echo "Generating report $ReportPath$File$XTxt"
    mysql -u root -pvagrant siaft -t <$SQLPath$File$XSql > $ReportPath$File$XTxt
done
