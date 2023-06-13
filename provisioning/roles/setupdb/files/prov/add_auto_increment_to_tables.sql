#This script will add the auto increment to all the appropriate tables

SET foreign_key_checks = 0;

ALTER TABLE FileAttributes
	MODIFY COLUMN PK_FileAttributes_ID INTEGER NOT NULL AUTO_INCREMENT;

ALTER TABLE Analysis
	MODIFY COLUMN PK_Analysis_ID INTEGER NOT NULL AUTO_INCREMENT;

ALTER TABLE Sanitize
	MODIFY COLUMN PK_Sanitize_ID INTEGER NOT NULL AUTO_INCREMENT;

ALTER TABLE UnprocessedFiles
	MODIFY COLUMN PK_UnprocessedFiles_ID INTEGER NOT NULL AUTO_INCREMENT;

SET foreign_key_checks = 1;
