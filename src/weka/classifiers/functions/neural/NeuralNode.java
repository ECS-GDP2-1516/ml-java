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

import java.util.Random;

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
  
  /** The best (lowest error) weights. Only used when validation set is used */
  private double[] m_bestWeights;
  
  /** The change in the weights. */
  private double[] m_changeInWeights;
  
  private Random m_random;

  /** Performs the operations for this node. Currently this
   * defines that the node is either a sigmoid or a linear unit. */
  private SigmoidUnit m_methods;

  /** 
   * @param id The string name for this node (used to id this node).
   * @param r A random number generator used to generate initial weights.
   * @param m The methods this node should use to update.
   */
  public NeuralNode(String id, Random r, SigmoidUnit m) {
    super(id);
    m_weights = new double[1];
    m_bestWeights = new double[1];
    m_changeInWeights = new double[1];
    
    m_random = r;
    
    m_weights[0] = m_random.nextDouble() * .1 - .05;
    m_changeInWeights[0] = 0;

    m_methods = m;
  }
  

  /**
   * Call this to get the output value of this unit. 
   * @param calculate True if the value should be calculated if it hasn't been
   * already.
   * @return The output value, or NaN, if the value has not been calculated.
   */
  public double outputValue(boolean calculate) {
    
    if (Double.isNaN(m_unitValue) && calculate) {
      //then calculate the output value;
      m_unitValue = m_methods.outputValue(this);
    }
    
    return m_unitValue;
  }

  
  /**
   * Call this to get the error value of this unit.
   * @param calculate True if the value should be calculated if it hasn't been
   * already.
   * @return The error value, or NaN, if the value has not been calculated.
   */
  public double errorValue(boolean calculate) {

    if (!Double.isNaN(m_unitValue) && Double.isNaN(m_unitError) && calculate) {
      //then calculate the error.
      m_unitError = m_methods.errorValue(this);
    }
    return m_unitError;
  }

  /**
   * Call this to reset the value and error for this unit, ready for the next
   * run. This will also call the reset function of all units that are 
   * connected as inputs to this one.
   * This is also the time that the update for the listeners will be performed.
   */
  public void reset() {
    
    if (!Double.isNaN(m_unitValue) || !Double.isNaN(m_unitError)) {
      m_unitValue = Double.NaN;
      m_unitError = Double.NaN;
      m_weightsUpdated = false;
      for (int noa = 0; noa < m_numInputs; noa++) {
	m_inputList[noa].reset();
      }
    }
  }

  /**
   * call this function to get the weights array.
   * This will also allow the weights to be updated.
   * @return The weights array.
   */
  public double[] getWeights() {
    return m_weights;
  }


}
