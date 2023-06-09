package com.nteligen.hq.dhs.siaft.processors;

import java.lang.Iterable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class Pipeline implements Iterable<String>
{
  private List<String> pipeline;

  /**
   * Converts a pipeline string to a list of stages. Splits stages based on a
   * comma delimiter and trims whitespace.
   * @param pipelineStr A comma delimited string representation of the pipeline.
   */
  public Pipeline(String pipelineStr)
  {
    String[] stages = pipelineStr.split(",");
    Arrays.parallelSetAll(stages, (index) -> stages[index].trim());
    pipeline = new ArrayList<String>(Arrays.asList(stages));
  }

  @Override
  public Iterator<String> iterator()
  {
    return pipeline.iterator();
  }

  /**
   * Pops a String indicating the next stage of the pipeline. This removes the
   * String from the pipeline list in the process.
   * @return String representing the next stage in the pipeline.
   */
  public String pop()
  {
    Iterator<String> iter = pipeline.iterator();
    String front = iter.next();
    iter.remove();
    return front;
  }

  @Override
  public String toString()
  {
    return String.join(",", pipeline);
  }
}
