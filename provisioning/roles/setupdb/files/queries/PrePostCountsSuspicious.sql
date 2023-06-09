call PrePostIndicatorsSuspicious();

select i.FileType, coalesce(f.FileCount) as FileCount, coalesce(r1.preRLCount, 0) as preRLCount, coalesce(r2.pstRLCount, 0) as pstRLCount,
coalesce(c1.preCYCount, 0) as preCYCount, coalesce(c2.pstCYCount, 0) as pstCYCount, coalesce(g1.preGWCount, 0) as preGWCount, coalesce(g2.pstGWCount, 0) as pstGWCount,
coalesce(n1.preNCount, 0) as preNoThreatCount, coalesce(n2.pstNCount, 0) as pstNoThreatCount, coalesce(s1.preSCount, 0) as preSuspiciousCount, coalesce(s2.pstSCount, 0) as pstSuspiciousCount,
coalesce(m1.preMCount, 0) as preMaliciousCount,  coalesce(m2.pstMCount, 0) as pstMaliciousCount, coalesce(f1.preFCount, 0) as preFailedCount,  coalesce(f2.pstFCount, 0) as pstFailedCount
from (select distinct fa.FileType as FileType from FileAttributes fa group by fa.FileType) i
left join (select fa.FileType, count(*) as FileCount from FileAttributes fa group by fa.FileType) f on f.FileType = i.FileType
left join (select FileType, count(IndicatorID) preRLCount from temp_indicator_data where AnalysisEngine = 1000 and SanitizeEngine is null group by FileType) r1 on r1.FileType = i.FileType
left join (select FileType, count(IndicatorID) pstRLCount from temp_indicator_data where AnalysisEngine = 1000 and SanitizeEngine is not null group by FileType) r2 on r2.FileType = i.FileType
left join (select FileType, count(IndicatorID) preCYCount from temp_indicator_data where AnalysisEngine = 1001 and SanitizeEngine is null group by FileType) c1 on c1.FileType = i.FileType
left join (select FileType, count(IndicatorID) pstCYCount from temp_indicator_data where AnalysisEngine = 1001 and SanitizeEngine is not null group by FileType) c2 on c2.FileType = i.FileType
left join (select FileType, count(IndicatorID) preGWCount from temp_indicator_data where AnalysisEngine = 1003 and SanitizeEngine is null group by FileType) g1 on g1.FileType = i.FileType
left join (select FileType, count(IndicatorID) pstGWCount from temp_indicator_data where AnalysisEngine = 1003 and SanitizeEngine is not null group by FileType) g2 on g2.FileType = i.FileType
left join (select FileType, count(distinct(FileAttributesID)) as preNCount from temp_indicator_data where Verdict = 'no specific threat' and SanitizeEngine is null group by FileType) n1 on n1.FileType = i.FileType
left join (select FileType, count(distinct(FileAttributesID)) as pstNCount from temp_indicator_data where Verdict = 'no specific threat' and SanitizeEngine is not null group by FileType) n2 on n2.FileType = i.FileType
left join (select FileType, count(distinct(FileAttributesID)) as preSCount from temp_indicator_data where Verdict = 'suspicious' and SanitizeEngine is null group by FileType) s1 on s1.FileType = i.FileType
left join (select FileType, count(distinct(FileAttributesID)) as pstSCount from temp_indicator_data where Verdict = 'suspicious' and SanitizeEngine is not null group by FileType) s2 on s2.FileType = i.FileType
left join (select FileType, count(distinct(FileAttributesID)) as preMCount from temp_indicator_data where Verdict = 'malicious' and SanitizeEngine is null group by FileType) m1 on m1.FileType = i.FileType
left join (select FileType, count(distinct(FileAttributesID)) as pstMCount from temp_indicator_data where Verdict = 'malicious' and SanitizeEngine is not null group by FileType) m2 on m2.FileType = i.FileType
left join (select FileType, count(distinct(FileAttributesID)) as preFCount from temp_indicator_data where Verdict is null and SanitizeEngine is null group by FileType) f1 on f1.FileType = i.FileType
left join (select FileType, count(distinct(FileAttributesID)) as pstFCount from temp_indicator_data where Verdict is null and SanitizeEngine is not null group by FileType) f2 on f2.FileType = i.FileType
order by i.FileType;

