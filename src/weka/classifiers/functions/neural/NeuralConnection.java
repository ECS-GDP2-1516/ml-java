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

  /** The error value for this unit, NaN if not calculated. */
  protected double m_unitError;
  
  /** True if the weights have already been updated. */
  protected boolean m_weightsUpdated;
  
  /**
   * Constructs The unit with the basic connection information prepared for
   * use. 
   * 
   * @param id the unique id of the unit
   */
  public NeuralConnection(String id) {
    
    m_inputList = new NeuralConnection[0];
    m_outputList = new NeuralConnection[0];

    m_numInputs = 0;

    m_unitValue = Double.NaN;
    m_unitError = Double.NaN;

    m_weightsUpdated = false;
  }

  /**
   * Call this to reset the unit for another run.
   * It is expected by that this unit will call the reset functions of all 
   * input units to it. It is also expected that this will not be done
   * if the unit has already been reset (or atleast appears to be).
   */
  public abstract void reset();

  /**
   * Call this to get the output value of this unit. 
   * @param calculate True if the value should be calculated if it hasn't been
   * already.
   * @return The output value, or NaN, if the value has not been calculated.
   */
  public abstract double outputValue(boolean calculate);
}