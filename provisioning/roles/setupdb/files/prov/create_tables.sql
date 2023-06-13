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

CREATE TABLE UnprocessedFiles (
PK_UnprocessedFiles_ID INTEGER NOT NULL PRIMARY KEY,
FK_FileAttributes_ID INTEGER NOT NULL,
Unprocessed BOOLEAN,
UnprocessedReason VARCHAR(80),
Date_Created DATETIME NOT NULL,
Date_Updated DATETIME,
FOREIGN KEY(FK_FileAttributes_ID) REFERENCES FileAttributes(PK_FileAttributes_ID)
);
