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

  //bitwise flags for the types of unit.

  /** This unit is not connected to any others. */
  public static final int UNCONNECTED = 0;
  
  /** This unit is a pure input unit. */
  public static final int PURE_INPUT = 1;
  
  /** This unit is a pure output unit. */
  public static final int PURE_OUTPUT = 2;
  
  /** This unit is an input unit. */
  public static final int INPUT = 4;
  
  /** This unit is an output unit. */
  public static final int OUTPUT = 8;
  
  /** This flag is set once the unit has a connection. */
  public static final int CONNECTED = 16;



  /////The difference between pure and not is that pure is used to feed 
  /////the neural network the attribute values and the errors on the outputs
  /////Beyond that they do no calculations, and have certain restrictions
  /////on the connections they can make.



  /** The list of inputs to this unit. */
  protected NeuralConnection[] m_inputList;

  /** The list of outputs from this unit. */
  protected NeuralConnection[] m_outputList;

  /** The numbering for the connections at the other end of the input lines. */
  protected int[] m_inputNums;
  
  /** The numbering for the connections at the other end of the out lines. */
  protected int[] m_outputNums;

  /** The number of inputs. */
  protected int m_numInputs;

  /** The number of outputs. */
  protected int m_numOutputs;

  /** The output value for this unit, NaN if not calculated. */
  protected double m_unitValue;

  /** The error value for this unit, NaN if not calculated. */
  protected double m_unitError;
  
  /** True if the weights have already been updated. */
  protected boolean m_weightsUpdated;
  
  /** The string that uniquely (provided naming is done properly) identifies
   * this unit. */
  protected String m_id;

  /** The type of unit this is. */
  protected int m_type;

  /** The x coord of this unit purely for displaying purposes. */
  protected double m_x;
  
  /** The y coord of this unit purely for displaying purposes. */
  protected double m_y;
  
  /**
   * Constructs The unit with the basic connection information prepared for
   * use. 
   * 
   * @param id the unique id of the unit
   */
  public NeuralConnection(String id) {
    
    m_id = id;
    m_inputList = new NeuralConnection[0];
    m_outputList = new NeuralConnection[0];
    m_inputNums = new int[0];
    m_outputNums = new int[0];

    m_numInputs = 0;
    m_numOutputs = 0;

    m_unitValue = Double.NaN;
    m_unitError = Double.NaN;

    m_weightsUpdated = false;
    m_x = 0;
    m_y = 0;
    m_type = UNCONNECTED;
  }

  /**
   * @return The type of this unit.
   */
  public int getType() {
    return m_type;
  }

  /**
   * @param t The new type of this unit.
   */
  public void setType(int t) {
    m_type = t;
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

  /**
   * Call this to get the weight value on a particular connection.
   * @param n The connection number to get the weight for, -1 if The threshold
   * weight should be returned.
   * @return This function will default to return 1. If overridden, it should
   * return the value for the specified connection or if -1 then it should 
   * return the threshold value. If no value exists for the specified 
   * connection, NaN will be returned.
   */
  public double weightValue(int n) {
    return 1;
  }

  /**
   * Use this to get easy access to the inputs.
   * It is not advised to change the entries in this list
   * (use the connecting and disconnecting functions to do that)
   * @return The inputs list.
   */
  public NeuralConnection[] getInputs() {
    return m_inputList;
  }

  /**
   * Use this to get easy access to the outputs.
   * It is not advised to change the entries in this list
   * (use the connecting and disconnecting functions to do that)
   * @return The outputs list.
   */
  public NeuralConnection[] getOutputs() {
    return m_outputList;
  }

  /**
   * Use this to get easy access to the output numbers.
   * It is not advised to change the entries in this list
   * (use the connecting and disconnecting functions to do that)
   * @return The outputs list.
   */
  public int[] getOutputNums() {
    return m_outputNums;
  }
  
  /**
   * @return The number of input connections.
   */
  public int getNumInputs() {
    return m_numInputs;
  }

  /**
   * @return The number of output connections.
   */
  public int getNumOutputs() {
    return m_numOutputs;
  }
}