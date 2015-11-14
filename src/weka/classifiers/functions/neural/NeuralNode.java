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
 *    NeuralNode.java
 *    Copyright (C) 2000 University of Waikato, Hamilton, New Zealand
 */

package weka.classifiers.functions.neural;

/**
 * This class is used to represent a node in the neuralnet.
 * 
 * @author Malcolm Ware (mfw4@cs.waikato.ac.nz)
 * @version $Revision: 5403 $
 */
public class NeuralNode
  extends NeuralConnection {

/** for serialization */
  private static final long serialVersionUID = -1085750607680839163L;
    
  /** The weights for each of the input connections, and the threshold. */
  private double[] m_weights;

  /**
   * Call this to get the output value of this unit. 
   * @param calculate True if the value should be calculated if it hasn't been
   * already.
   * @return The output value, or NaN, if the value has not been calculated.
   */
  public double outputValue(boolean calculate) {
    
    if (Double.isNaN(m_unitValue) && calculate) {
      //then calculate the output value;
      m_unitValue = outputValue();
    }
    
    return m_unitValue;
  }
  
  	public double outputValue() {

	    double[] weights = m_weights;
	    NeuralConnection[] inputs = m_inputList;
	    double value = weights[0];
	    for (int noa = 0; noa < m_numInputs; noa++) {
	      
	      value += inputs[noa].outputValue(true) 
		* weights[noa+1];
	    }
	     
	    //this I got from the Neural Network faq to combat overflow
	    //pretty simple solution really :)
	    if (value < -45) {
	      value = 0;
	    }
	    else if (value > 45) {
	      value = 1;
	    }
	    else {
	      value = 1 / (1 + Math.exp(-value));
	    }  
	    return value;
	  }
}
