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
 *    NominalToBinary.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */


package weka.filters.unsupervised.attribute;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Range;
import weka.core.SparseInstance;
import weka.filters.Filter;

/** 
 <!-- globalinfo-start -->
 * Converts all nominal attributes into binary numeric attributes. An attribute with k values is transformed into k binary attributes if the class is nominal (using the one-attribute-per-value approach). Binary attributes are left binary, if option '-A' is not given.If the class is numeric, you might want to use the supervised version of this filter.
 * <p/>
 <!-- globalinfo-end -->
 * 
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -N
 *  Sets if binary attributes are to be coded as nominal ones.</pre>
 * 
 * <pre> -A
 *  For each nominal value a new attribute is created, 
 *  not only if there are more than 2 values.</pre>
 * 
 * <pre> -R &lt;col1,col2-col4,...&gt;
 *  Specifies list of columns to act on. First and last are 
 *  valid indexes.
 *  (default: first-last)</pre>
 * 
 * <pre> -V
 *  Invert matching sense of column indexes.</pre>
 * 
 <!-- options-end -->
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz) 
 * @version $Revision: 9819 $ 
 */
public class NominalToBinary 
  extends Filter 
   {
  
  /** for serialization */
  static final long serialVersionUID = -1130642825710549138L;

  /** Stores which columns to act on */
  protected Range m_Columns = new Range();

  /** Are all values transformed into new attributes? */
  private boolean m_TransformAll = false;
  
  /** Whether we need to transform at all */
  private boolean m_needToTransform = false;

  /** Constructor - initialises the filter */
  public NominalToBinary() {

  }

  /**
   * Input an instance for filtering. Filter requires all
   * training instances be read before producing output.
   *
   * @param instance the input instance
   * @return true if the filtered instance may now be
   * collected with output().
   * @throws IllegalStateException if no input format has been set
   */
  public boolean input(Instance instance) {

    if (m_InputFormat == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }

    convertInstance(instance);
    return true;
  }

  /**
   * Convert a single instance over if the class is nominal. The converted 
   * instance is added to the end of the output queue.
   *
   * @param instance the instance to convert
   */
  private void convertInstance(Instance instance) {
    
    if (!m_needToTransform) {
      push(instance);
      return;
    }

    double [] vals = new double [m_OutputFormat.numAttributes()];
    int attSoFar = 0;

    for(int j = 0; j < m_InputFormat.numAttributes(); j++) {
      Attribute att = m_InputFormat.attribute(j);
      if (!att.isNominal() || (j == m_InputFormat.classIndex()) ||
	  !m_Columns.isInRange(j)) {
	vals[attSoFar] = instance.value(j);
	attSoFar++;
      } else {
	if ( (att.numValues() <= 2) && (!m_TransformAll) ) {
	  vals[attSoFar] = instance.value(j);
	  attSoFar++;
	} else {
	  if (instance.isMissing(j)) {
	    for (int k = 0; k < att.numValues(); k++) {
              vals[attSoFar + k] = instance.value(j);
	    }
	  } else {
	    for (int k = 0; k < att.numValues(); k++) {
	      if (k == (int)instance.value(j)) {
                vals[attSoFar + k] = 1;
	      } else {
                vals[attSoFar + k] = 0;
	      }
	    }
	  }
	  attSoFar += att.numValues();
	}
      }
    }
    Instance inst = null;System.out.println(instance instanceof SparseInstance);
    if (instance instanceof SparseInstance) {
      inst = new SparseInstance(instance.weight(), vals);
    } else {
      inst = new Instance(instance.weight(), vals);
    }
    inst.setDataset(getOutputFormat());
    copyValues(inst, false, instance.dataset(), getOutputFormat());
    inst.setDataset(getOutputFormat());
    push(inst);
  }
}
