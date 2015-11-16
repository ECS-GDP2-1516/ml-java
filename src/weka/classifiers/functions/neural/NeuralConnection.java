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
 *    NeuralConnection.java
 *    Copyright (C) 2000 University of Waikato, Hamilton, New Zealand
 */

package weka.classifiers.functions.neural;

import java.io.Serializable;

/** 
 * Abstract unit in a NeuralNetwork.
 *
 * @author Malcolm Ware (mfw4@cs.waikato.ac.nz)
 * @version $Revision: 5403 $
 */
public abstract class NeuralConnection
  implements Serializable {

  /** for serialization */
  private static final long serialVersionUID = -286208828571059163L;

  /////The difference between pure and not is that pure is used to feed 
  /////the neural network the attribute values and the errors on the outputs
  /////Beyond that they do no calculations, and have certain restrictions
  /////on the connections they can make.

  /** The list of inputs to this unit. */
  protected NeuralConnection[] m_inputList;

  /** The list of outputs from this unit. */
  protected NeuralConnection[] m_outputList;

  /** The number of inputs. */
  protected int m_numInputs;

  /** The output value for this unit, NaN if not calculated. */
  protected double m_unitValue;

  /**
   * Call this to reset the value and error for this unit, ready for the next
   * run. This will also call the reset function of all units that are 
   * connected as inputs to this one.
   * This is also the time that the update for the listeners will be performed.
   */
  public void reset() {
    
    if (!Double.isNaN(m_unitValue)) {
      m_unitValue = Double.NaN;
      for (int noa = 0; noa < m_numInputs; noa++) {
    	  m_inputList[noa].reset();
      }
    }
  }

  /**
   * Call this to get the output value of this unit. 
   * @param calculate True if the value should be calculated if it hasn't been
   * already.
   * @return The output value, or NaN, if the value has not been calculated.
   */
  public abstract double outputValue(boolean calculate);
}