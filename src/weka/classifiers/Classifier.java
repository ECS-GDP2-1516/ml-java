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
 *    Classifier.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.classifiers;

import java.io.Serializable;
import weka.core.Attribute;
import weka.core.Instance;

/**
 * Abstract classifier. All schemes for numeric or nominal prediction in Weka
 * extend this class. Note that a classifier MUST either implement
 * distributionForInstance() or classifyInstance().
 * 
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version $Revision: 10485 $
 */
public abstract class Classifier implements Cloneable, Serializable
   {

  /** for serialization */
  private static final long serialVersionUID = 6502780192411755341L;

  /** Whether the classifier is run in debug mode. */
  protected boolean m_Debug = false;
  
  abstract   public double[] distributionForInstance(Instance i) throws Exception;

  /**
   * Classifies the given test instance. The instance has to belong to a dataset
   * when it's being classified. Note that a classifier MUST implement either
   * this or distributionForInstance().
   * 
   * @param instance the instance to be classified
   * @return the predicted most likely class for the instance or
   *         Instance.missingValue() if no prediction is made
   * @exception Exception if an error occurred during the prediction
   */
  public double classifyInstance(Instance instance) throws Exception {

    double[] dist = distributionForInstance(instance);
    if (dist == null) {
      throw new Exception("Null distribution predicted");
    }
    switch (instance.classAttribute().type()) {
    case Attribute.NOMINAL:
      double max = 0;
      int maxIndex = 0;

      for (int i = 0; i < dist.length; i++) {
        if (dist[i] > max) {
          maxIndex = i;
          max = dist[i];
        }
      }
      if (max > 0) {
        return maxIndex;
      } else {
        return Instance.missingValue();
      }
    case Attribute.NUMERIC:
    case Attribute.DATE:
      return dist[0];
    default:
      return Instance.missingValue();
    }
  }
}
