#!/bin/bash
arraytest[0]='test' || (echo 'Failure: arrays not supported in this version of
bash.' && exit 2)

DateVal=$(date +%Y%m%d)
SQLPath=/home/nifi/queries/
ReportPath=/home/nifi/sqlreports/$DateVal/
mkdir -p $ReportPath

XSql=.sql
XTxt=.txt
XCsv=.csv
Files=(SuspiciousFileResults
       )
for File in "${Files[@]}"
do
    echo "Generating report $ReportPath$File$XTxt"
    mysql -u root -pvagrant siaft -t <$SQLPath$File$XSql > $ReportPath$File$XTxt
done
for File in "${Files[@]}"
do
    echo "Generating report $ReportPath$File$XCSV"
    mysql -u root -pvagrant siaft < $SQLPath$File$XSql | tr '\t' ',' > $ReportPath$File$XCsv
done

