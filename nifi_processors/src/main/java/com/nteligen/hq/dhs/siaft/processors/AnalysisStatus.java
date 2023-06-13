
package com.nteligen.hq.dhs.siaft.processors;

/**
 * This represents the status of the sanitization process on a file.
 */
public enum AnalysisStatus
{
    /**
     * This represents that the analysis engine process succeeded and a report was generated.
     */
    ANALYSIS_SUCCESS("analyzed"),
    /**
     * This represents that the analysis engnine did not produce an analysis report.
     */
    NOT_PROCESSED("not processed"),
    /**
     * This represents that the nifi analysis process failed.
     */
    ANALYSIS_FAILURE("false");

    private String value;

    AnalysisStatus(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return value;
    }
}
