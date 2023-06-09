-- MySQL dump 10.14  Distrib 5.5.60-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: siaft
-- ------------------------------------------------------
-- Server version	5.5.60-MariaDB
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping routines for database 'siaft'
--
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`vagrant`@`localhost` PROCEDURE `CylanceIndicators`()
begin
  declare _FileAttributesID int(11);
  declare _FName varchar(80) default null;
  declare _UUID varchar(40) default null;
  declare _MD5 varchar(33) default null;
  declare _AnalysisEngineID int(11);
  declare _DynamicAnalysisID int(11);
  declare _SanitizeEngineID int(11);
  declare _SanitizedMD5 varchar(33) default null;
  declare _Verdict varchar(20) default null;
  declare _Score varchar(3) default null;
  declare _DateStamp varchar(20);
  declare _Category varchar(10) default null;
  declare _CatType varchar(40) default null;
  declare _Description varchar(305) default null;
  declare _LastAttributeID int(11);
  declare _BeginDate varchar(20);
  declare _EndDate varchar(20);
  declare done int default 0;
  
  declare cur1 cursor for select FK_FileAttributes_ID, FK_Dynamic_Analysis_ID, Verdict, Threat_Score, Date_Created
  from VxStream_Results where FK_FileAttributes_ID in (select FK_FileAttributes_ID from VxStream_Results
  where Verdict in ('suspicious','malicious')) order by FK_FileAttributes_ID, Date_Created;
  declare continue handler for NOT FOUND SET done = 1;
  
  drop table if exists t_VxStream_Results;
  create table t_VxStream_Results(
  FK_FileAttributes_ID int(11),
  FK_Dynamic_Analysis_ID int(11),
  Verdict varchar(20),
  Threat_Score varchar(3),
  MinDate datetime,
  MaxDate dateTime);
  
  drop table if exists temp_data;
  create table temp_data (
  FileAttributesID int(11),
  FileName varchar(80),
  UUID varchar(40),
  MD5 varchar(33),
  DynamicAnalysisID int(11),
  AnalysisEngine int(11),
  SanitizeEngine int(11),
  SanitizeMD5 varchar(33),
  Verdict varchar(20),
  Score varchar(3),
  Category varchar(10),
  CatType varchar(40),
  Description varchar(305),
  DateStamp datetime);
  
  open cur1;
  
  set _LastAttributeID = 'none';
  first: loop
     fetch cur1 into _FileAttributesID, _DynamicAnalysisID, _Verdict, _Score, _DateStamp;
    if done = 1 then leave first; end if;
    if(_LastAttributeID <> _FileAttributesID) then
      set _LastAttributeID = _FileAttributesID;
      set _BeginDate = '2000-01-01 00:00:00';
      set _EndDate = _DateStamp;
	else
      set _BeginDate = _EndDate;
      set _EndDate = _DateStamp;
	end if;
    insert into t_VxStream_Results values(_FileAttributesID, _DynamicAnalysisID, _Verdict, _Score, _BeginDate, _EndDate);
  end loop first;
  close cur1;

  begin 
    declare done1 int default 0;
    declare cur2 cursor for select tv.FK_FileAttributes_ID, fa.FileName, fa.UUID, fa.MD5, tv.FK_Dynamic_Analysis_ID, an.FK_AnalysisEngineID,
    sa.FK_SanitizeEngineID, sa.MD5, tv.verdict, tv.Threat_Score, ca.Category, ca.Name, ca.Description, tv.MaxDate as Date_Created
    from t_VxStream_Results tv inner join FileAttributes fa on fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    left join Dynamic_Analysis_Sanitizations ds on ds.FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Sanitize sa on sa.PK_Sanitize_ID = ds.FK_Sanitize_ID
    left join Dynamic_Analysis da on da.PK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Analysis an on an.FK_FileAttributes_ID = tv.FK_FileAttributes_ID and an.FK_AnalysisEngineID = 1001 
      and an.Date_Created > tv.MinDate and an.Date_Created < tv.MaxDate
    left join CYIndicators cy on cy.FK_Analysis_ID = an.PK_Analysis_ID
    left join CYCategories ca on ca.PK_CYCategory_ID = cy.FK_CYCategory_ID
    where fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    order by tv.FK_FileAttributes_ID, tv.FK_Dynamic_Analysis_ID, ca.PK_CYCategory_ID;
	declare continue handler for NOT FOUND SET done1 = 1;
    open cur2;
	second: loop
      fetch cur2 into _FileAttributesID, _FName, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _Category, _CatType, _Description, _DateStamp;
      if done1 = 1 then leave second; end if;
      insert into temp_data values(_FileAttributesID, _FName, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _Category, _CatType, _Description, _DateStamp);
    end loop second;
    close cur2;
  end;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`vagrant`@`localhost` PROCEDURE `GlasswallIndicators`()
begin
  declare _FileAttributesID int(11);
  declare _FName varchar(80) default null;
  declare _UUID varchar(40) default null;
  declare _MD5 varchar(33) default null;
  declare _AnalysisEngineID int(11);
  declare _DynamicAnalysisID int(11);
  declare _SanitizeEngineID int(11);
  declare _SanitizedMD5 varchar(33) default null;
  declare _Verdict varchar(20) default null;
  declare _Score varchar(3) default null;
  declare _DateStamp varchar(20);
  declare _Category varchar(10) default null;
  declare _CatType varchar(40) default null;
  declare _Description varchar(305) default null;
  declare _InstanceCount int(11);
  declare _LastAttributeID int(11);
  declare _BeginDate varchar(20);
  declare _EndDate varchar(20);
  declare done int default 0;
  
  declare cur1 cursor for select FK_FileAttributes_ID, FK_Dynamic_Analysis_ID, Verdict, Threat_Score, Date_Created
  from VxStream_Results where FK_FileAttributes_ID in (select FK_FileAttributes_ID from VxStream_Results
  where Verdict in ('suspicious','malicious')) order by FK_FileAttributes_ID, Date_Created;
  declare continue handler for NOT FOUND SET done = 1;
  
  drop table if exists t_VxStream_Results;
  create table t_VxStream_Results(
  FK_FileAttributes_ID int(11),
  FK_Dynamic_Analysis_ID int(11),
  Verdict varchar(20),
  Threat_Score varchar(3),
  MinDate datetime,
  MaxDate dateTime);
  
  drop table if exists temp_data;
  create table temp_data (
  FileAttributesID int(11),
  FileName varchar(80),
  UUID varchar(40),
  MD5 varchar(33),
  DynamicAnalysisID int(11),
  AnalysisEngine int(11),
  SanitizeEngine int(11),
  SanitizeMD5 varchar(33),
  Verdict varchar(20),
  Score varchar(3),
  Description varchar(305),
  InstanceCount int(11),
  DateStamp datetime);
  
  open cur1;
  
  set _LastAttributeID = 'none';
  first: loop
     fetch cur1 into _FileAttributesID, _DynamicAnalysisID, _Verdict, _Score, _DateStamp;
    if done = 1 then leave first; end if;
    if(_LastAttributeID <> _FileAttributesID) then
      set _LastAttributeID = _FileAttributesID;
      set _BeginDate = '2000-01-01 00:00:00';
      set _EndDate = _DateStamp;
	else
      set _BeginDate = _EndDate;
      set _EndDate = _DateStamp;
	end if;
    insert into t_VxStream_Results values(_FileAttributesID, _DynamicAnalysisID, _Verdict, _Score, _BeginDate, _EndDate);
  end loop first;
  close cur1;

  begin 
    declare done1 int default 0;
    declare cur2 cursor for select tv.FK_FileAttributes_ID, fa.FileName, fa.UUID, fa.MD5, tv.FK_Dynamic_Analysis_ID, an.FK_AnalysisEngineID,
    sa.FK_SanitizeEngineID, sa.MD5, tv.verdict, tv.Threat_Score, gw.TechnicalDescription, gw.InstanceCount, tv.MaxDate as Date_Created
    from t_VxStream_Results tv inner join FileAttributes fa on fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    left join Dynamic_Analysis_Sanitizations ds on ds.FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Sanitize sa on sa.PK_Sanitize_ID = ds.FK_Sanitize_ID
    left join Dynamic_Analysis da on da.PK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Analysis an on an.FK_FileAttributes_ID = tv.FK_FileAttributes_ID and an.FK_AnalysisEngineID = 1003 
      and an.Date_Created > tv.MinDate and an.Date_Created < tv.MaxDate
    left join GWIndicators gw on gw.FK_Analysis_ID = an.PK_Analysis_ID
    where fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    order by tv.FK_FileAttributes_ID, tv.FK_Dynamic_Analysis_ID, gw.PK_GWIndicator_ID;
	declare continue handler for NOT FOUND SET done1 = 1;
    open cur2;
	second: loop
      fetch cur2 into _FileAttributesID, _FName, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _Description, _InstanceCount, _DateStamp;
      if done1 = 1 then leave second; end if;
      insert into temp_data values(_FileAttributesID, _FName, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _Description, _InstanceCount, _DateStamp);
    end loop second;
    close cur2;
  end;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`vagrant`@`localhost` PROCEDURE `PrePostIndicatorsAll`()
BEGIN
  declare _FileAttributesID int(11);
  declare _FName varchar(80) default null;
  declare _FType varchar(5) default null;
  declare _UUID varchar(40) default null;
  declare _MD5 varchar(33) default null;
  declare _AnalysisEngineID int(11);
  declare _DynamicAnalysisID int(11);
  declare _SanitizeEngineID int(11);
  declare _SanitizedMD5 varchar(33) default null;
  declare _Verdict varchar(20) default null;
  declare _Score varchar(3) default null;
  declare _DateStamp varchar(20);
  declare _IndicatorID int(11);
  declare _LastAttributeID int(11);
  declare _BeginDate varchar(20);
  declare _EndDate varchar(20);
  declare done int default 0;
  
  declare cur1 cursor for select FK_FileAttributes_ID, FK_Dynamic_Analysis_ID, Verdict, Threat_Score, Date_Created
  from VxStream_Results where FK_FileAttributes_ID in (select distinct(FK_FileAttributes_ID) from VxStream_Results)
  order by FK_FileAttributes_ID, FK_Dynamic_Analysis_ID, Date_Created;
  declare continue handler for NOT FOUND SET done = 1;
  
  drop table if exists t_VxStream_Results;
  create table t_VxStream_Results(
  FK_FileAttributes_ID int(11),
  FK_Dynamic_Analysis_ID int(11),
  Verdict varchar(20),
  Threat_Score varchar(3),
  MinDate datetime,
  MaxDate dateTime);
  
  drop table if exists temp_indicator_data;
  create table temp_indicator_data (
  FileAttributesID int(11),
  FileName varchar(80),
  FileType varchar(5),
  UUID varchar(40),
  MD5 varchar(33),
  DynamicAnalysisID int(11),
  AnalysisEngine int(11),
  SanitizeEngine int(11),
  SanitizeMD5 varchar(33),
  Verdict varchar(20),
  Score varchar(3),
  IndicatorID int(11),
  DateStamp datetime);
  
  open cur1;
  
  set _LastAttributeID = 'none';
  first: loop
     fetch cur1 into _FileAttributesID, _DynamicAnalysisID, _Verdict, _Score, _DateStamp;
    if done = 1 then leave first; end if;
    if(_LastAttributeID <> _FileAttributesID) then
      set _LastAttributeID = _FileAttributesID;
      set _BeginDate = '2000-01-01 00:00:00';
      set _EndDate = _DateStamp;
	else
      set _BeginDate = _EndDate;
      set _EndDate = _DateStamp;
	end if;
    insert into t_VxStream_Results values(_FileAttributesID, _DynamicAnalysisID, _Verdict, _Score, _BeginDate, _EndDate);
  end loop first;
  close cur1;

  begin 
    declare done1 int default 0;
    declare cur2 cursor for select tv.FK_FileAttributes_ID, fa.FileName, fa.FileType, fa.UUID, fa.MD5, tv.FK_Dynamic_Analysis_ID, an.FK_AnalysisEngineID,
    sa.FK_SanitizeEngineID, sa.MD5, tv.verdict, tv.Threat_Score, cy.PK_CYIndicators_ID as IndicatorID, tv.MaxDate as Date_Analyzed
    from t_VxStream_Results tv inner join FileAttributes fa on fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    left join Dynamic_Analysis_Sanitizations ds on ds.FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID  and ds.FK_Sanitize_ID = 
    (select min(FK_Sanitize_ID) from Dynamic_Analysis_Sanitizations where FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID)
    left join Sanitize sa on sa.PK_Sanitize_ID = ds.FK_Sanitize_ID
    left join Dynamic_Analysis da on da.PK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Analysis an on an.FK_FileAttributes_ID = tv.FK_FileAttributes_ID and an.FK_AnalysisEngineID = 1001
      and an.Date_Created > tv.MinDate and an.Date_Created < tv.MaxDate
    left join CYIndicators cy on cy.FK_Analysis_ID = an.PK_Analysis_ID
    where fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    order by tv.FK_FileAttributes_ID, tv.FK_Dynamic_Analysis_ID;
	declare continue handler for NOT FOUND SET done1 = 1;
    open cur2;
	second: loop
      fetch cur2 into _FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp;
      if done1 = 1 then leave second; end if;
      insert into temp_indicator_data values(_FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp);
    end loop second;
    close cur2;
  end;
  begin 
    declare done2 int default 0;
    declare cur3 cursor for select tv.FK_FileAttributes_ID, fa.FileName, fa.FileType, fa.UUID, fa.MD5, tv.FK_Dynamic_Analysis_ID, an.FK_AnalysisEngineID,
    sa.FK_SanitizeEngineID, sa.MD5, tv.verdict, tv.Threat_Score, rl.PK_RLIndicators_ID as IndicatorID, tv.MaxDate as Date_Analyzed
    from t_VxStream_Results tv inner join FileAttributes fa on fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    left join Dynamic_Analysis_Sanitizations ds on ds.FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID and ds.FK_Sanitize_ID = 
    (select min(FK_Sanitize_ID) from Dynamic_Analysis_Sanitizations where FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID)
    left join Sanitize sa on sa.PK_Sanitize_ID = ds.FK_Sanitize_ID
    left join Dynamic_Analysis da on da.PK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Analysis an on an.FK_FileAttributes_ID = tv.FK_FileAttributes_ID and an.FK_AnalysisEngineID = 1000
      and an.Date_Created > tv.MinDate and an.Date_Created < tv.MaxDate
    left join RLIndicators rl on rl.FK_Analysis_ID = an.PK_Analysis_ID
    where fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    order by tv.FK_FileAttributes_ID, tv.FK_Dynamic_Analysis_ID;
	declare continue handler for NOT FOUND SET done2 = 1;
    open cur3;
	second: loop
      fetch cur3 into _FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp;
      if done2 = 1 then leave second; end if;
      insert into temp_indicator_data values(_FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp);
    end loop second;
    close cur3;
  end;
  begin 
    declare done3 int default 0;
    declare cur4 cursor for select tv.FK_FileAttributes_ID, fa.FileName, fa.FileType, fa.UUID, fa.MD5, tv.FK_Dynamic_Analysis_ID, an.FK_AnalysisEngineID,
    sa.FK_SanitizeEngineID, sa.MD5, tv.verdict, tv.Threat_Score, gw.PK_GWIndicator_ID as IndicatorID, tv.MaxDate as Date_Analyzed
    from t_VxStream_Results tv inner join FileAttributes fa on fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    left join Dynamic_Analysis_Sanitizations ds on ds.FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID  and ds.FK_Sanitize_ID = 
    (select min(FK_Sanitize_ID) from Dynamic_Analysis_Sanitizations where FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID)
    left join Sanitize sa on sa.PK_Sanitize_ID = ds.FK_Sanitize_ID
    left join Dynamic_Analysis da on da.PK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Analysis an on an.FK_FileAttributes_ID = tv.FK_FileAttributes_ID and an.FK_AnalysisEngineID = 1003
      and an.Date_Created > tv.MinDate and an.Date_Created < tv.MaxDate
    left join GWIndicators gw on gw.FK_Analysis_ID = an.PK_Analysis_ID
    where fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    order by tv.FK_FileAttributes_ID, tv.FK_Dynamic_Analysis_ID;
	declare continue handler for NOT FOUND SET done3 = 1;
    open cur4;
	second: loop
      fetch cur4 into _FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp;
      if done3 = 1 then leave second; end if;
      insert into temp_indicator_data values(_FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp);
    end loop second;
    close cur4;
  end;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`vagrant`@`localhost` PROCEDURE `PrePostIndicatorsSuspicious`()
BEGIN
  declare _FileAttributesID int(11);
  declare _FName varchar(80) default null;
  declare _FType varchar(5) default null;
  declare _UUID varchar(40) default null;
  declare _MD5 varchar(33) default null;
  declare _AnalysisEngineID int(11);
  declare _DynamicAnalysisID int(11);
  declare _SanitizeEngineID int(11);
  declare _SanitizedMD5 varchar(33) default null;
  declare _Verdict varchar(20) default null;
  declare _Score varchar(3) default null;
  declare _DateStamp varchar(20);
  declare _IndicatorID int(11);
  declare _LastAttributeID int(11);
  declare _BeginDate varchar(20);
  declare _EndDate varchar(20);
  declare done int default 0;
  
  declare cur1 cursor for select FK_FileAttributes_ID, FK_Dynamic_Analysis_ID, Verdict, Threat_Score, Date_Created
  from VxStream_Results where FK_FileAttributes_ID in (select FK_FileAttributes_ID from VxStream_Results 
  where Verdict in ('suspicious','malicious')) order by FK_FileAttributes_ID, FK_Dynamic_Analysis_ID, Date_Created;
  declare continue handler for NOT FOUND SET done = 1;
  
  drop table if exists t_VxStream_Results;
  create table t_VxStream_Results(
  FK_FileAttributes_ID int(11),
  FK_Dynamic_Analysis_ID int(11),
  Verdict varchar(20),
  Threat_Score varchar(3),
  MinDate datetime,
  MaxDate dateTime);
  
  drop table if exists temp_indicator_data;
  create table temp_indicator_data (
  FileAttributesID int(11),
  FileName varchar(80),
  FileType varchar(5),
  UUID varchar(40),
  MD5 varchar(33),
  DynamicAnalysisID int(11),
  AnalysisEngine int(11),
  SanitizeEngine int(11),
  SanitizeMD5 varchar(33),
  Verdict varchar(20),
  Score varchar(3),
  IndicatorID int(11),
  DateStamp datetime);
  
  open cur1;
  
  set _LastAttributeID = 'none';
  first: loop
     fetch cur1 into _FileAttributesID, _DynamicAnalysisID, _Verdict, _Score, _DateStamp;
    if done = 1 then leave first; end if;
    if(_LastAttributeID <> _FileAttributesID) then
      set _LastAttributeID = _FileAttributesID;
      set _BeginDate = '2000-01-01 00:00:00';
      set _EndDate = _DateStamp;
	else
      set _BeginDate = _EndDate;
      set _EndDate = _DateStamp;
	end if;
    insert into t_VxStream_Results values(_FileAttributesID, _DynamicAnalysisID, _Verdict, _Score, _BeginDate, _EndDate);
  end loop first;
  close cur1;

  begin 
    declare done1 int default 0;
    declare cur2 cursor for select tv.FK_FileAttributes_ID, fa.FileName, fa.FileType, fa.UUID, fa.MD5, tv.FK_Dynamic_Analysis_ID, an.FK_AnalysisEngineID,
    sa.FK_SanitizeEngineID, sa.MD5, tv.verdict, tv.Threat_Score, cy.PK_CYIndicators_ID as IndicatorID, tv.MaxDate as Date_Analyzed
    from t_VxStream_Results tv inner join FileAttributes fa on fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    left join Dynamic_Analysis_Sanitizations ds on ds.FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID  and ds.FK_Sanitize_ID = 
    (select min(FK_Sanitize_ID) from Dynamic_Analysis_Sanitizations where FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID)
    left join Sanitize sa on sa.PK_Sanitize_ID = ds.FK_Sanitize_ID
    left join Dynamic_Analysis da on da.PK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Analysis an on an.FK_FileAttributes_ID = tv.FK_FileAttributes_ID and an.FK_AnalysisEngineID = 1001
      and an.Date_Created > tv.MinDate and an.Date_Created < tv.MaxDate
    left join CYIndicators cy on cy.FK_Analysis_ID = an.PK_Analysis_ID
    where fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    order by tv.FK_FileAttributes_ID, tv.FK_Dynamic_Analysis_ID;
	declare continue handler for NOT FOUND SET done1 = 1;
    open cur2;
	second: loop
      fetch cur2 into _FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp;
      if done1 = 1 then leave second; end if;
      insert into temp_indicator_data values(_FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp);
    end loop second;
    close cur2;
  end;
  begin 
    declare done2 int default 0;
    declare cur3 cursor for select tv.FK_FileAttributes_ID, fa.FileName, fa.FileType, fa.UUID, fa.MD5, tv.FK_Dynamic_Analysis_ID, an.FK_AnalysisEngineID,
    sa.FK_SanitizeEngineID, sa.MD5, tv.verdict, tv.Threat_Score, rl.PK_RLIndicators_ID as IndicatorID, tv.MaxDate as Date_Analyzed
    from t_VxStream_Results tv inner join FileAttributes fa on fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    left join Dynamic_Analysis_Sanitizations ds on ds.FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID and ds.FK_Sanitize_ID = 
    (select min(FK_Sanitize_ID) from Dynamic_Analysis_Sanitizations where FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID)
    left join Sanitize sa on sa.PK_Sanitize_ID = ds.FK_Sanitize_ID
    left join Dynamic_Analysis da on da.PK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Analysis an on an.FK_FileAttributes_ID = tv.FK_FileAttributes_ID and an.FK_AnalysisEngineID = 1000
      and an.Date_Created > tv.MinDate and an.Date_Created < tv.MaxDate
    left join RLIndicators rl on rl.FK_Analysis_ID = an.PK_Analysis_ID
    where fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    order by tv.FK_FileAttributes_ID, tv.FK_Dynamic_Analysis_ID;
	declare continue handler for NOT FOUND SET done2 = 1;
    open cur3;
	second: loop
      fetch cur3 into _FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp;
      if done2 = 1 then leave second; end if;
      insert into temp_indicator_data values(_FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp);
    end loop second;
    close cur3;
  end;
  begin 
    declare done3 int default 0;
    declare cur4 cursor for select tv.FK_FileAttributes_ID, fa.FileName, fa.FileType, fa.UUID, fa.MD5, tv.FK_Dynamic_Analysis_ID, an.FK_AnalysisEngineID,
    sa.FK_SanitizeEngineID, sa.MD5, tv.verdict, tv.Threat_Score, gw.PK_GWIndicator_ID as IndicatorID, tv.MaxDate as Date_Analyzed
    from t_VxStream_Results tv inner join FileAttributes fa on fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    left join Dynamic_Analysis_Sanitizations ds on ds.FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID  and ds.FK_Sanitize_ID = 
    (select min(FK_Sanitize_ID) from Dynamic_Analysis_Sanitizations where FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID)
    left join Sanitize sa on sa.PK_Sanitize_ID = ds.FK_Sanitize_ID
    left join Dynamic_Analysis da on da.PK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Analysis an on an.FK_FileAttributes_ID = tv.FK_FileAttributes_ID and an.FK_AnalysisEngineID = 1003
      and an.Date_Created > tv.MinDate and an.Date_Created < tv.MaxDate
    left join GWIndicators gw on gw.FK_Analysis_ID = an.PK_Analysis_ID
    where fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    order by tv.FK_FileAttributes_ID, tv.FK_Dynamic_Analysis_ID;
	declare continue handler for NOT FOUND SET done3 = 1;
    open cur4;
	second: loop
      fetch cur4 into _FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp;
      if done3 = 1 then leave second; end if;
      insert into temp_indicator_data values(_FileAttributesID, _FName, _FType, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _IndicatorID, _DateStamp);
    end loop second;
    close cur4;
  end;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
CREATE DEFINER=`vagrant`@`localhost` PROCEDURE `ReversingLabsIndicators`()
begin
  declare _FileAttributesID int(11);
  declare _FName varchar(80) default null;
  declare _UUID varchar(40) default null;
  declare _MD5 varchar(33) default null;
  declare _AnalysisEngineID int(11);
  declare _DynamicAnalysisID int(11);
  declare _SanitizeEngineID int(11);
  declare _SanitizedMD5 varchar(33) default null;
  declare _Verdict varchar(20) default null;
  declare _Score varchar(3) default null;
  declare _DateStamp varchar(20);
  declare _Category varchar(10) default null;
  declare _CatType varchar(40) default null;
  declare _Description varchar(305) default null;
  declare _LastAttributeID int(11);
  declare _BeginDate varchar(20);
  declare _EndDate varchar(20);
  declare done int default 0;
  
  declare cur1 cursor for select FK_FileAttributes_ID, FK_Dynamic_Analysis_ID, Verdict, Threat_Score, Date_Created
  from VxStream_Results where FK_FileAttributes_ID in (select FK_FileAttributes_ID from VxStream_Results
  where Verdict in ('suspicious','malicious')) order by FK_FileAttributes_ID, Date_Created;
  declare continue handler for NOT FOUND SET done = 1;
  
  drop table if exists t_VxStream_Results;
  create table t_VxStream_Results(
  FK_FileAttributes_ID int(11),
  FK_Dynamic_Analysis_ID int(11),
  Verdict varchar(20),
  Threat_Score varchar(3),
  MinDate datetime,
  MaxDate dateTime);
  
  drop table if exists temp_data;
  create table temp_data (
  FileAttributesID int(11),
  FileName varchar(80),
  UUID varchar(40),
  MD5 varchar(33),
  DynamicAnalysisID int(11),
  AnalysisEngine int(11),
  SanitizeEngine int(11),
  SanitizeMD5 varchar(33),
  Verdict varchar(20),
  Score varchar(3),
  Category varchar(10),
  CatType varchar(40),
  Description varchar(305),
  DateStamp datetime);
  
  open cur1;
  
  set _LastAttributeID = 'none';
  first: loop
     fetch cur1 into _FileAttributesID, _DynamicAnalysisID, _Verdict, _Score, _DateStamp;
    if done = 1 then leave first; end if;
    if(_LastAttributeID <> _FileAttributesID) then
      set _LastAttributeID = _FileAttributesID;
      set _BeginDate = '2000-01-01 00:00:00';
      set _EndDate = _DateStamp;
	else
      set _BeginDate = _EndDate;
      set _EndDate = _DateStamp;
	end if;
    insert into t_VxStream_Results values(_FileAttributesID, _DynamicAnalysisID, _Verdict, _Score, _BeginDate, _EndDate);
  end loop first;
  close cur1;

  begin 
    declare done1 int default 0;
    declare cur2 cursor for select tv.FK_FileAttributes_ID, fa.FileName, fa.UUID, fa.MD5, tv.FK_Dynamic_Analysis_ID, an.FK_AnalysisEngineID,
    sa.FK_SanitizeEngineID, sa.MD5, tv.verdict, tv.Threat_Score, rc.Category, rc.Prefix, rc.Text, tv.MaxDate as Date_Created
    from t_VxStream_Results tv inner join FileAttributes fa on fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    left join Dynamic_Analysis_Sanitizations ds on ds.FK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Sanitize sa on sa.PK_Sanitize_ID = ds.FK_Sanitize_ID
    left join Dynamic_Analysis da on da.PK_Dynamic_Analysis_ID = tv.FK_Dynamic_Analysis_ID
    left join Analysis an on an.FK_FileAttributes_ID = tv.FK_FileAttributes_ID and an.FK_AnalysisEngineID = 1000 
      and an.Date_Created > tv.MinDate and an.Date_Created < tv.MaxDate
    left join RLIndicators rl on rl.FK_Analysis_ID = an.PK_Analysis_ID
    left join RLCategories rc on rc.PK_RLCategory_ID = rl.FK_RLCategory_ID
    where fa.PK_FileAttributes_ID = tv.FK_FileAttributes_ID
    order by tv.FK_FileAttributes_ID, tv.FK_Dynamic_Analysis_ID, rc.PK_RLCategory_ID;
	declare continue handler for NOT FOUND SET done1 = 1;
    open cur2;
	second: loop
      fetch cur2 into _FileAttributesID, _FName, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _Category, _CatType, _Description, _DateStamp;
      if done1 = 1 then leave second; end if;
      insert into temp_data values(_FileAttributesID, _FName, _UUID, _MD5, _DynamicAnalysisID, _AnalysisEngineID, _SanitizeEngineID, _SanitizedMD5, _Verdict, _Score, _Category, _CatType, _Description, _DateStamp);
    end loop second;
    close cur2;
  end;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-05-06 13:10:19
