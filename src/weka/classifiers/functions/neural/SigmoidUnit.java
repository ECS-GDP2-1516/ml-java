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
 *    SigmoidUnit.java
 *    Copyright (C) 2001 University of Waikato, Hamilton, New Zealand
 */

package weka.classifiers.functions.neural;

import java.io.Serializable;

/**
 * This can be used by the 
 * neuralnode to perform all it's computations (as a sigmoid unit).
 *
 * @author Malcolm Ware (mfw4@cs.waikato.ac.nz)
 * @version $Revision: 1.7 $
 */
public class SigmoidUnit implements Serializable
   {

  /** for serialization */
  private static final long serialVersionUID = -5162958458177475652L;
  
  /**
   * This function calculates what the output value should be.
   * @param node The node to calculate the value for.
   * @return The value.
   */
  public double outputValue(NeuralNode node) {
    double[] weights = node.getWeights();
    NeuralConnection[] inputs = node.getInputs();
    double value = weights[0];
    for (int noa = 0; noa < node.getNumInputs(); noa++) {
      
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
  
  /**
   * This function calculates what the error value should be.
   * @param node The node to calculate the error for.
   * @return The error.
   */
  public double errorValue(NeuralNode node) {
    //then calculate the error.
    
    NeuralConnection[] outputs = node.getOutputs();
    int[] oNums = node.getOutputNums();
    double error = 0;
    
    for (int noa = 0; noa < node.getNumOutputs(); noa++) {
      error += outputs[noa].errorValue(true) 
	* outputs[noa].weightValue(oNums[noa]);
    }
    double value = node.outputValue(false);
    error *= value * (1 - value);
    
    return error;
  }
}
