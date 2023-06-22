#!/bin/bash
cp sanitizer*.sh /home/nifi/data/
cp analyzer*.sh /home/nifi/data/
mkdir /home/nifi/data/hold
cp *.txt /home/nifi/data/hold/
mkdir -p /home/nifi/data/mockAnalyzer1/in
mkdir -p /home/nifi/data/mockAnalyzer1/out
mkdir -p /home/nifi/data/mockAnalyzer2/in
mkdir -p /home/nifi/data/mockAnalyzer2/out
mkdir -p /home/nifi/data/mockAnalyzer3/in
mkdir -p /home/nifi/data/mockAnalyzer3/out
mkdir -p /home/nifi/data/mockSanitizer1/in
mkdir -p /home/nifi/data/mockSanitizer1/out
mkdir -p /home/nifi/data/mockSanitizer2/in
mkdir -p /home/nifi/data/mockSanitizer2/out
mkdir -p /home/nifi/data/mockSanitizer3/in
mkdir -p /home/nifi/data/mockSanitizer3/out
chown -R nifi.nifi /home/nifi/data/*

