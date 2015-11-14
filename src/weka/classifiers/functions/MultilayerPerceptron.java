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
 *    MultilayerPerceptron.java
 *    Copyright (C) 2000 University of Waikato, Hamilton, New Zealand
 */

package weka.classifiers.functions;

import weka.classifiers.Classifier;
import weka.classifiers.functions.neural.NeuralConnection;
import weka.core.Instance;
import weka.core.Instances;

/** 
 <!-- globalinfo-start -->
 * A Classifier that uses backpropagation to classify instances.<br/>
 * This network can be built by hand, created by an algorithm or both. The network can also be monitored and modified during training time. The nodes in this network are all sigmoid (except for when the class is numeric in which case the the output nodes become unthresholded linear units).
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -L &lt;learning rate&gt;
 *  Learning Rate for the backpropagation algorithm.
 *  (Value should be between 0 - 1, Default = 0.3).</pre>
 * 
 * <pre> -M &lt;momentum&gt;
 *  Momentum Rate for the backpropagation algorithm.
 *  (Value should be between 0 - 1, Default = 0.2).</pre>
 * 
 * <pre> -N &lt;number of epochs&gt;
 *  Number of epochs to train through.
 *  (Default = 500).</pre>
 * 
 * <pre> -V &lt;percentage size of validation set&gt;
 *  Percentage size of validation set to use to terminate
 *  training (if this is non zero it can pre-empt num of epochs.
 *  (Value should be between 0 - 100, Default = 0).</pre>
 * 
 * <pre> -S &lt;seed&gt;
 *  The value used to seed the random number generator
 *  (Value should be &gt;= 0 and and a long, Default = 0).</pre>
 * 
 * <pre> -E &lt;threshold for number of consequetive errors&gt;
 *  The consequetive number of errors allowed for validation
 *  testing before the netwrok terminates.
 *  (Value should be &gt; 0, Default = 20).</pre>
 * 
 * <pre> -G
 *  GUI will be opened.
 *  (Use this to bring up a GUI).</pre>
 * 
 * <pre> -A
 *  Autocreation of the network connections will NOT be done.
 *  (This will be ignored if -G is NOT set)</pre>
 * 
 * <pre> -B
 *  A NominalToBinary filter will NOT automatically be used.
 *  (Set this to not use a NominalToBinary filter).</pre>
 * 
 * <pre> -H &lt;comma seperated numbers for nodes on each layer&gt;
 *  The hidden layers to be created for the network.
 *  (Value should be a list of comma separated Natural 
 *  numbers or the letters 'a' = (attribs + classes) / 2, 
 *  'i' = attribs, 'o' = classes, 't' = attribs .+ classes)
 *  for wildcard values, Default = a).</pre>
 * 
 * <pre> -C
 *  Normalizing a numeric class will NOT be done.
 *  (Set this to not normalize the class if it's numeric).</pre>
 * 
 * <pre> -I
 *  Normalizing the attributes will NOT be done.
 *  (Set this to not normalize the attributes).</pre>
 * 
 * <pre> -R
 *  Reseting the network will NOT be allowed.
 *  (Set this to not allow the network to reset).</pre>
 * 
 * <pre> -D
 *  Learning rate decay will occur.
 *  (Set this to cause the learning rate to decay).</pre>
 * 
 <!-- options-end -->
 *
 * @author Malcolm Ware (mfw4@cs.waikato.ac.nz)
 * @version $Revision: 10073 $
 */
public class MultilayerPerceptron 
  extends Classifier 
  {
	
  /** for serialization */
  private static final long serialVersionUID = -5990607817048210779L;
  

  /** 
   * This inner class is used to connect the nodes in the network up to
   * the data that they are classifying, Note that objects of this class are
   * only suitable to go on the attribute side or class side of the network
   * and not both.
   */
  protected class NeuralEnd 
    extends NeuralConnection {
    
    /** for serialization */
    static final long serialVersionUID = 7305185603191183338L;
  
    /** 
     * the value that represents the instance value this node represents. 
     * For an input it is the attribute number, for an output, if nominal
     * it is the class value. 
     */
    private int m_link;
    
    /** True if node is an input, False if it's an output. */
    private boolean m_input;
    
    /**
     * Call this to get the output value of this unit. 
     * @param calculate True if the value should be calculated if it hasn't 
     * been already.
     * @return The output value, or NaN, if the value has not been calculated.
     */
    public double outputValue(boolean calculate) {
     
      if (Double.isNaN(m_unitValue) && calculate) {
	if (m_input) {
	  if (m_currentInstance.isMissing(m_link)) {
	    m_unitValue = 0;
	  }
	  else {
	    
	    m_unitValue = m_currentInstance.value(m_link);
	  }
	}
	else {
	  //node is an output.
	  m_unitValue = 0;
	  for (int noa = 0; noa < m_numInputs; noa++) {
	    m_unitValue += m_inputList[noa].outputValue(true);
	   
	  }
	  if (m_numeric && m_normalizeClass) {
	    //then scale the value;
	    //this scales linearly from between -1 and 1
	    m_unitValue = m_unitValue * 
	      m_attributeRanges[m_instances.classIndex()] + 
	      m_attributeBases[m_instances.classIndex()];
	  }
	}
      }
      return m_unitValue;
      
      
    }
    
    /**
     * Call this to reset the value and error for this unit, ready for the next
     * run. This will also call the reset function of all units that are 
     * connected as inputs to this one.
     * This is also the time that the update for the listeners will be 
     * performed.
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
   
  }
  
  
  /** a ZeroR model in case no model can be built from the data 
   * or the network predicts all zeros for the classes */
  private Classifier m_ZeroR;

  /** Whether to use the default ZeroR model */
  private boolean m_useDefaultModel = false;
    
  /** The training instances. */
  private Instances m_instances;
  
  /** The current instance running through the network. */
  private Instance m_currentInstance;
  
  /** A flag to say that it's a numeric class. */
  private boolean m_numeric;

  /** The ranges for all the attributes. */
  private double[] m_attributeRanges;

  /** The base values for all the attributes. */
  private double[] m_attributeBases;

  /** The output units.(only feeds the errors, does no calcs) */
  private NeuralEnd[] m_outputs;

  /** The number of classes. */
  private int m_numClasses = 0;

  /** A flag to state that a nominal to binary filter should be used. */
  private boolean m_useNomToBin;

  /** This flag states that the user wants the input values normalized. */
  private boolean m_normalizeAttributes;

  /** This flag states that the user wants the class to be normalized while
   * processing in the network is done. (the final answer will be in the
   * original range regardless). This option will only be used when the class
   * is numeric. */
  private boolean m_normalizeClass;
  
  /**
   * this will reset all the nodes in the network.
   */
  private void resetNetwork() {
    for (int noc = 0; noc < m_numClasses; noc++) {
      m_outputs[noc].reset();
    }
  }

  /**
   * Call this function to predict the class of an instance once a 
   * classification model has been built with the buildClassifier call.
   * @param i The instance to classify.
   * @return A double array filled with the probabilities of each class type.
   * @throws Exception if can't classify instance.
   */
	public double[] distributionForInstance(Instance i) throws Exception
	{
		// Make a copy of the instance so that it isn't modified
		m_currentInstance = (Instance)i.copy();
		
		if (m_normalizeAttributes)
		{
			for (int noa = 0; noa < m_instances.numAttributes(); noa++)
			{
				if (noa != m_instances.classIndex())
				{
					if (m_attributeRanges[noa] != 0)
					{
						m_currentInstance.setValue
						(
							noa,
							(m_currentInstance.value(noa) - m_attributeBases[noa]) / m_attributeRanges[noa]
						);
					}
					else
					{
						m_currentInstance.setValue
						(
							noa, m_currentInstance.value(noa) - m_attributeBases[noa]
						);
					}
				}
			}
		}
		
		resetNetwork();
		
		//since all the output values are needed.
		//They are calculated manually here and the values collected.
		double[] theArray = new double[m_numClasses];
		
		for (int noa = 0; noa < m_numClasses; noa++) {
			theArray[noa] = m_outputs[noa].outputValue(true);
		}
		
		//now normalize the array
		double count = 0;
		for (int noa = 0; noa < m_numClasses; noa++)
		{
			count += theArray[noa];
		}
		if (count <= 0)
		{
			return m_ZeroR.distributionForInstance(i);
		}
		for (int noa = 0; noa < m_numClasses; noa++) {
			theArray[noa] /= count;
		}
		return theArray;
	}
}
