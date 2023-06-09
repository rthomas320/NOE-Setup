CREATE TABLE AnalyzeEngines(
PK_AnalyzeEngine_ID INTEGER NOT NULL PRIMARY KEY,
EngineName VARCHAR(40) UNIQUE,
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME);

CREATE TABLE SanitizeEngines (
PK_SanitizeEngine_ID INTEGER NOT NULL PRIMARY KEY,
EngineName VARCHAR(40) UNIQUE,
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME);

CREATE TABLE FileAttributes (
PK_FileAttributes_ID INTEGER NOT NULL PRIMARY KEY,
UUID VARCHAR(40) NOT NULL,
MD5 VARCHAR(33),
FileName VARCHAR(256),
FileType VARCHAR(10),
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME);

CREATE TABLE Analysis (
PK_Analysis_ID INTEGER NOT NULL PRIMARY KEY,
FK_FileAttributes_ID INTEGER NOT NULL,
FK_AnalysisEngineID INTEGER NOT NULL,
FK_SanitizeEngineID INTEGER,
Success BOOLEAN NOT NULL,
Results VARCHAR(2000),
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME,
FOREIGN KEY (FK_FileAttributes_ID) REFERENCES FileAttributes(PK_FileAttributes_ID),
FOREIGN KEY (FK_AnalysisEngineID) REFERENCES AnalyzeEngines(PK_AnalyzeEngine_ID),
FOREIGN KEY (FK_SanitizeEngineID) REFERENCES SanitizeEngines(PK_SanitizeEngine_ID));

CREATE TABLE Sanitize (
PK_Sanitize_ID INTEGER NOT NULL PRIMARY KEY,
FK_FileAttributes_ID INTEGER NOT NULL,
FK_SanitizeEngineID INTEGER,
Results VARCHAR(80),
MD5 VARCHAR(33),
FileType VARCHAR(10),
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME,
FOREIGN KEY (FK_FileAttributes_ID) REFERENCES FileAttributes(PK_FileAttributes_ID),
FOREIGN KEY (FK_SanitizeEngineID) REFERENCES SanitizeEngines(PK_SanitizeEngine_ID));

CREATE TABLE RLCategories (
PK_RLCategory_ID INTEGER NOT NULL PRIMARY KEY,
Category VARCHAR(5),
Prefix VARCHAR(20),
Text VARCHAR(80),
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME);

CREATE TABLE CYCategories (
PK_CYCategory_ID INTEGER NOT NULL PRIMARY KEY,
Indicator VARCHAR(5),
Name VARCHAR(2000),
Category VARCHAR(25),
Description VARCHAR(375),
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME);

CREATE TABLE RLIndicators (
PK_RLIndicators_ID INTEGER NOT NULL PRIMARY KEY,
FK_FileAttributes_ID INTEGER NOT NULL,
FK_RLCategory_ID INTEGER NOT NULL,
FK_Analysis_ID INTEGER NOT NULL,
Priority VARCHAR(2),
Description VARCHAR(300),
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME,
FOREIGN KEY (FK_FileAttributes_ID) REFERENCES FileAttributes(PK_FileAttributes_ID),
FOREIGN KEY (FK_RLCategory_ID) REFERENCES RLCategories(PK_RLCategory_ID),
FOREIGN KEY (FK_Analysis_ID) REFERENCES Analysis(PK_Analysis_ID));

CREATE TABLE CYIndicators (
PK_CYIndicators_ID INTEGER NOT NULL PRIMARY KEY,
FK_FileAttributes_ID INTEGER NOT NULL,
FK_CYCategory_ID INTEGER NOT NULL,
FK_Analysis_ID INTEGER NOT NULL,
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME,
FOREIGN KEY(FK_FileAttributes_ID) REFERENCES FileAttributes(PK_FileAttributes_ID),
FOREIGN KEY (FK_CYCategory_ID) REFERENCES CYCategories(PK_CYCategory_ID),
FOREIGN KEY (FK_Analysis_ID) REFERENCES Analysis(PK_Analysis_ID));

CREATE TABLE GWIndicators (
PK_GWIndicator_ID INTEGER NOT NULL PRIMARY KEY,
FK_FileAttributes_ID INTEGER NOT NULL,
FK_Analysis_ID INTEGER NOT NULL,
TechnicalDescription VARCHAR(2000) NOT NULL,
InstanceCount INTEGER NOT NULL,
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME,
FOREIGN KEY (FK_FileAttributes_ID) REFERENCES FileAttributes(PK_FileAttributes_ID),
FOREIGN KEY (FK_Analysis_ID) REFERENCES Analysis(PK_Analysis_ID));

CREATE TABLE UnprocessedFiles (
PK_UnprocessedFiles_ID INTEGER NOT NULL PRIMARY KEY,
FK_FileAttributes_ID INTEGER NOT NULL,
Unprocessed BOOLEAN,
UnprocessedReason VARCHAR(80),
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME,
FOREIGN KEY(FK_FileAttributes_ID) REFERENCES FileAttributes(PK_FileAttributes_ID)
);

CREATE TABLE Dynamic_Analysis (
PK_Dynamic_Analysis_ID INTEGER NOT NULL PRIMARY KEY,
FK_FileAttributes_ID INTEGER NOT NULL,
FK_AnalysisEngineID INTEGER NOT NULL,
Sanitized BOOLEAN NOT NULL,
Success BOOLEAN NOT NULL,
Failure_Info VARCHAR(10000),
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME,
FOREIGN KEY (FK_FileAttributes_ID) REFERENCES FileAttributes(PK_FileAttributes_ID),
FOREIGN KEY (FK_AnalysisEngineID) REFERENCES AnalyzeEngines(PK_AnalyzeEngine_ID)
);

CREATE TABLE Dynamic_Analysis_Sanitizations (
PK_Dynamic_Analysis_Sanitizations_ID INTEGER NOT NULL PRIMARY KEY,
FK_Dynamic_Analysis_ID INTEGER NOT NULL,
FK_Sanitize_ID INTEGER NOT NULL,
FOREIGN KEY (FK_Sanitize_ID) REFERENCES Sanitize(PK_Sanitize_ID),
FOREIGN KEY (FK_Dynamic_Analysis_ID) REFERENCES Dynamic_Analysis(PK_Dynamic_Analysis_ID)
);

CREATE TABLE VxStream_Results (
PK_VxStream_Results_ID INTEGER NOT NULL PRIMARY KEY,
FK_FileAttributes_ID INTEGER NOT NULL,
FK_Dynamic_Analysis_ID INTEGER NOT NULL,
Verdict VARCHAR(2000),
Threat_Score VARCHAR(2000),
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME,
FOREIGN KEY(FK_FileAttributes_ID) REFERENCES FileAttributes(PK_FileAttributes_ID),
FOREIGN KEY (FK_Dynamic_Analysis_ID) REFERENCES Dynamic_Analysis(PK_Dynamic_Analysis_ID)
);

CREATE TABLE Emails (
PK_Email_ID INTEGER NOT NULL PRIMARY KEY,
UUID VARCHAR(2000),
From_Email_Address VARCHAR(2000),
Recd_By_Email_Address VARCHAR(2000),
Subject VARCHAR(2000),
Deliver_Date DATETIME,
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME
);

CREATE TABLE Recipients (
PK_Recipient_ID INTEGER NOT NULL PRIMARY KEY,
Email_Address VARCHAR(2000),
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME
);

CREATE TABLE Email_Recipients (
PK_Email_Recipient_ID INTEGER NOT NULL PRIMARY KEY,
FK_Email_ID INTEGER NOT NULL,
FK_Recipient_ID INTEGER NOT NULL,
FOREIGN KEY(FK_Email_ID) REFERENCES Emails(PK_Email_ID),
FOREIGN KEY (FK_Recipient_ID) REFERENCES Recipients(PK_Recipient_ID)
);

CREATE TABLE Attachments (
PK_Attachment_ID INTEGER NOT NULL PRIMARY KEY,
FK_Email_ID INTEGER NOT NULL,
MD5 VARCHAR(2000) NOT NULL,
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME,
FOREIGN KEY(FK_Email_ID) REFERENCES Emails(PK_Email_ID)
);
