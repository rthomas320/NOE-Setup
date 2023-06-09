select fa.PK_FileAttributes_ID, fa.filename, fa.UUID, fa.MD5, vx.FK_Dynamic_Analysis_ID, sa.FK_SanitizeEngineID, sa.MD5 as sanitized_MD5,
vx.Verdict, vx.Threat_Score, vx.Date_Created from FileAttributes fa inner join VxStream_Results vx on
vx.fK_FileAttributes_ID = fa.PK_FileAttributes_ID left join Dynamic_Analysis_Sanitizations da on da.FK_Dynamic_Analysis_ID = vx.FK_Dynamic_Analysis_ID
left join Sanitize sa on sa.PK_Sanitize_ID = da.FK_Sanitize_ID where PK_FileAttributes_ID in (select FK_FileAttributes_ID from VxStream_Results
where Verdict in ('suspicious','malicious')) order by vx.FK_FileAttributes_ID, vx.Date_Created;
