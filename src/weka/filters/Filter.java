/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    Filter.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.Queue;
import weka.core.RelationalLocator;
import weka.core.StringLocator;
import java.io.Serializable;

/** 
 * An abstract class for instance filters: objects that take instances
 * as input, carry out some transformation on the instance and then
 * output the instance. The method implementations in this class
 * assume that most of the work will be done in the methods overridden
 * by subclasses.<p>
 *
 * A simple example of filter use. This example doesn't remove
 * instances from the output queue until all instances have been
 * input, so has higher memory consumption than an approach that
 * uses output instances as they are made available:<p>
 *
 * <code> <pre>
 *  Filter filter = ..some type of filter..
 *  Instances instances = ..some instances..
 *  for (int i = 0; i < data.numInstances(); i++) {
 *    filter.input(data.instance(i));
 *  }
 *  filter.batchFinished();
 *  Instances newData = filter.outputFormat();
 *  Instance processed;
 *  while ((processed = filter.output()) != null) {
 *    newData.add(processed);
 *  }
 *  ..do something with newData..
 * </pre> </code>
 *
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version $Revision: 7880 $
 */
public abstract class Filter
  implements Serializable {

  /** for serialization */
  private static final long serialVersionUID = -8835063755891851218L;

  /** The output format for instances */
  protected Instances m_OutputFormat = null;

  /** The output instance queue */
  private Queue m_OutputQueue = null;

  /** Indices of string attributes in the output format */
  protected StringLocator m_OutputStringAtts = null;

  /** Indices of string attributes in the input format */
  protected StringLocator m_InputStringAtts = null;

  /** Indices of relational attributes in the output format */
  protected RelationalLocator m_OutputRelAtts = null;

  /** Indices of relational attributes in the input format */
  protected RelationalLocator m_InputRelAtts = null;

  /** The input format for instances */
  protected Instances m_InputFormat = null;

  /** Record whether the filter is at the start of a batch */
  protected boolean m_NewBatch = true;

  /** True if the first batch has been done */
  protected boolean m_FirstBatchDone = false;

  /**
   * Adds an output instance to the queue. The derived class should use this
   * method for each output instance it makes available. 
   *
   * @param instance the instance to be added to the queue.
   */
  protected void push(Instance instance) {

    if (instance != null) {
      if (instance.dataset() != null)
	copyValues(instance, false);
      instance.setDataset(m_OutputFormat);
      m_OutputQueue.push(instance);
    }
  }

  /**
   * Clears the output queue.
   */
  protected void resetQueue() {

    m_OutputQueue = new Queue();
  }

  /**
   * Adds the supplied input instance to the inputformat dataset for
   * later processing.  Use this method rather than
   * getInputFormat().add(instance). Or else. Note that the provided
   * instance gets copied when buffered. 
   *
   * @param instance the <code>Instance</code> to buffer.  
   */
  protected void bufferInput(Instance instance) {

    if (instance != null) {
      copyValues(instance, true);
      m_InputFormat.add(instance);
    }
  }

  
  /**
   * Copies string/relational values contained in the instance copied to a new
   * dataset. The Instance must already be assigned to a dataset. This
   * dataset and the destination dataset must have the same structure.
   *
   * @param instance		the Instance containing the string/relational 
   * 				values to copy.
   * @param isInput		if true the input format and input attribute 
   * 				locators are used otherwise the output format 
   * 				and output locators
   */
  protected void copyValues(Instance instance, boolean isInput) {

    RelationalLocator.copyRelationalValues(
	instance, 
	(isInput) ? m_InputFormat : m_OutputFormat, 
	(isInput) ? m_InputRelAtts : m_OutputRelAtts);

    StringLocator.copyStringValues(
	instance, 
	(isInput) ? m_InputFormat : m_OutputFormat, 
	(isInput) ? m_InputStringAtts : m_OutputStringAtts);
  }

  /**
   * Takes string/relational values referenced by an Instance and copies them 
   * from a source dataset to a destination dataset. The instance references are
   * updated to be valid for the destination dataset. The instance may have the 
   * structure (i.e. number and attribute position) of either dataset (this
   * affects where references are obtained from). Only works if the number
   * of string/relational attributes is the same in both indices (implicitly 
   * these string/relational attributes should be semantically same but just 
   * with shifted positions).
   *
   * @param instance 		the instance containing references to strings/
   * 				relational values in the source dataset that 
   * 				will have references updated to be valid for 
   * 				the destination dataset.
   * @param instSrcCompat 	true if the instance structure is the same as 
   * 				the source, or false if it is the same as the 
   * 				destination (i.e. which of the string/relational 
   * 				attribute indices contains the correct locations 
   * 				for this instance).
   * @param srcDataset 		the dataset for which the current instance 
   * 				string/relational value references are valid 
   * 				(after any position mapping if needed)
   * @param destDataset 	the dataset for which the current instance 
   * 				string/relational value references need to be 
   * 				inserted (after any position mapping if needed)
   */
  protected void copyValues(Instance instance, boolean instSrcCompat,
                         Instances srcDataset, Instances destDataset) {

    RelationalLocator.copyRelationalValues(
	instance, instSrcCompat, 
	srcDataset, m_InputRelAtts,
	destDataset, m_OutputRelAtts);

    StringLocator.copyStringValues(
	instance, instSrcCompat, 
	srcDataset, m_InputStringAtts,
	getOutputFormat(), m_OutputStringAtts);
  }

  /**
   * This will remove all buffered instances from the inputformat dataset.
   * Use this method rather than getInputFormat().delete();
   */
  protected void flushInput() {

    if (    (m_InputStringAtts.getAttributeIndices().length > 0) 
	 || (m_InputRelAtts.getAttributeIndices().length > 0) ) {
      m_InputFormat = m_InputFormat.stringFreeStructure();
      m_InputStringAtts = new StringLocator(m_InputFormat, m_InputStringAtts.getAllowedIndices());
      m_InputRelAtts = new RelationalLocator(m_InputFormat, m_InputRelAtts.getAllowedIndices());
    } else {
      // This more efficient than new Instances(m_InputFormat, 0);
      m_InputFormat.delete();
    }
  }

  /**
   * Gets the format of the output instances. This should only be called
   * after input() or batchFinished() has returned true. The relation
   * name of the output instances should be changed to reflect the
   * action of the filter (eg: add the filter name and options).
   *
   * @return an Instances object containing the output instance
   * structure only.
   * @throws NullPointerException if no input structure has been
   * defined (or the output format hasn't been determined yet) 
   */
  public Instances getOutputFormat() {

    if (m_OutputFormat == null) {
      throw new NullPointerException("No output format defined.");
    }
    return new Instances(m_OutputFormat, 0);
  }

  /**
   * Input an instance for filtering. Ordinarily the instance is
   * processed and made available for output immediately. Some filters
   * require all instances be read before producing output, in which
   * case output instances should be collected after calling
   * batchFinished(). If the input marks the start of a new batch, the
   * output queue is cleared. This default implementation assumes all
   * instance conversion will occur when batchFinished() is called.
   *
   * @param instance the input instance
   * @return true if the filtered instance may now be
   * collected with output().
   * @throws NullPointerException if the input format has not been
   * defined.
   * @throws Exception if the input instance was not of the correct 
   * format or if there was a problem with the filtering.  
   */
  public boolean input(Instance instance) throws Exception {

    if (m_InputFormat == null) {
      throw new NullPointerException("No input instance format defined");
    }
    if (m_NewBatch) {
      m_OutputQueue = new Queue();
      m_NewBatch = false;
    }
    bufferInput(instance);
    return false;
  }

  /**
   * Output an instance after filtering and remove from the output queue.
   *
   * @return the instance that has most recently been filtered (or null if
   * the queue is empty).
   * @throws NullPointerException if no output structure has been defined
   */
  public Instance output() {

    if (m_OutputFormat == null) {
      throw new NullPointerException("No output instance format defined");
    }
    if (m_OutputQueue.empty()) {
      return null;
    }
    Instance result = (Instance)m_OutputQueue.pop();

    return result;
  }
}
