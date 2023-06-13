package com.nteligen.hq.dhs.siaft.processors;

import com.nteligen.hq.dhs.siaft.exceptions.ReportProcessingException;

public interface ReportParser
{
  public abstract void persistIndicators(String report, long fileAttributeId, long analysisId)
      throws ReportProcessingException;
}

