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

import java.io.Serializable;
import java.util.ArrayList;

import weka.classifiers.functions.neural.NeuralConnection;
import weka.classifiers.functions.neural.NeuralNode;
import weka.classifiers.rules.ZeroR;
import weka.core.Instance;
import weka.core.Instances;

public class MultilayerPerceptron implements Serializable
  {
  /** for serialization */
  private static final long serialVersionUID = -5990607817048210779L;

  /** 
   * This inner class is used to connect the nodes in the network up to
   * the data that they are classifying, Note that objects of this class are
   * only suitable to go on the attribute side or class side of the network
   * and not both.
   */
  protected class NeuralEnd extends NeuralConnection {
    
    /** for serialization */
    static final long serialVersionUID = 7305185603191183338L;
  
    /** 
     * the value that represents the instance value this node represents. 
     * For an input it is the attribute number, for an output, if nominal
     * it is the class value. 
     */
    public int m_link;
    
    /** True if node is an input, False if it's an output. */
    public boolean m_input;
    
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
	}
      }
      return m_unitValue;
    }
    
    public int getVariable()
    {
    	if (m_input)
    	{
    		return m_link;
    	}
    	else
    	{
    		return super.getVariable();
    	}
    }
  }
  
  
  /** a ZeroR model in case no model can be built from the data 
   * or the network predicts all zeros for the classes */
  public ZeroR m_ZeroR;

  /** The training instances. */
  private Instances m_instances;
  
  /** The current instance running through the network. */
  private Instance m_currentInstance;
  
  /** The ranges for all the attributes. */
  public double[] m_attributeRanges;

  /** The base values for all the attributes. */
  public double[] m_attributeBases;

  /** The output units.(only feeds the errors, does no calcs) */
  public NeuralEnd[] m_outputs;

  /** The number of classes. */
  public int m_numClasses = 0;

  /** This flag states that the user wants the input values normalized. */
  public boolean m_normalizeAttributes;

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
			for (int noa = 0; noa < m_currentInstance.dataset().numAttributes(); noa++)
			{
				if (noa != m_currentInstance.classIndex())
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
		double count = 0;
		
		for (int noa = 0; noa < m_numClasses; noa++)
		{
			theArray[noa] = m_outputs[noa].outputValue(true);
			count        += theArray[noa];
		}
		
		if (count <= 0)
		{
			return m_ZeroR.m_Counts;
		}
		
		return theArray;
	}
	
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
  }
    
  public void export()
  {
	  ArrayList<ArrayList<NeuralConnection>> layers;
	  layers = new ArrayList<ArrayList<NeuralConnection>>();
	  
	  for (NeuralConnection n : m_outputs)
	  {
		  n.generateLayers(0, layers);
	  }
	  
	  ArrayList<NeuralConnection> prev_0, prev_1 = null;
	  prev_0 = layers.get(layers.size() - 1);
	  int varOffset = 0;
	  int varStart  = prev_0.size();
	  int min       = 1;
	  StringBuffer arrs = new StringBuffer();
	  StringBuffer code = new StringBuffer();
	  int dataCount = 0;
	  int r1        = 1;
	  int r2        = 2;
	  
	  for (NeuralConnection n : layers.get(0))
	  {
		  if (n.m_numInputs > 1)
		  {
			  min = 0;
			  break;
		  }
	  }
	  
	  code.append("double* offset=(double*)data;\n");
	  code.append("double* s1;\n");
	  code.append("double* e1;\n");
	  code.append("double* s2=v;\n");
	  code.append("double* e2=s2 + ");
	  code.append(prev_0.size());
	  code.append(";\n\n");
	  
	  for (int l = layers.size() - 2; l >= min; l--)
	  {
		  ArrayList<NeuralConnection> layer = layers.get(l);
		  int firstVar;

		  if (prev_1 == null)
		  {
			  firstVar = varStart;
		  }
		  else
		  {
			  firstVar = prev_1.get(varOffset).getVariable();
		  }
		  
		  code.append("// Layer ");
		  code.append(l);
		  code.append("\n");
		  code.append("s");
		  code.append(r1);
		  code.append("=v + ");
		  code.append(firstVar);
		  code.append(";\n");
		  code.append("e");
		  code.append(r1);
		  code.append("=s");
		  code.append(r1);
		  code.append(" + ");
		  code.append(layer.size());
		  code.append(";\n");
		  code.append("for (double* i = s");
		  code.append(r1);
		  code.append("; i < e");
		  code.append(r1);
		  code.append("; i++){\n");
		  code.append("    *i=*offset++;\n");
		  code.append("    for (double* j=s");
		  code.append(r2);
		  code.append("; j < e");
		  code.append(r2);
		  code.append("; j++) {\n");
		  code.append("        *i+=(*offset++**j) >> 12;\n");
		  code.append("    }\n");
		  code.append("    sigmoid(i);\n");
		  code.append("}\n\n");
		  
		  if (r1 == 1)
		  {
			  r1 = 2;
			  r2 = 1;
		  }
		  else
		  {
			  r1 = 1;
			  r2 = 2;
		  }
		  
		  for (int i = 0; i < layer.size(); i++)
		  {
			  NeuralConnection n = layer.get(i);
			  int var;

			  if (prev_1 == null)
			  {
				  var = varStart++;
			  }
			  else
			  {
				  var = prev_1.get(varOffset++).getVariable();
			  }
			  
			  n.setVariable(var);
			  
			  if (n instanceof NeuralNode)
			  {
				  arrs.append((int)(((NeuralNode)n).m_weights[0] * Math.pow(2, 12)));
				  arrs.append(",");
			  }
			  			  
			  for (int j = 0; j < n.m_numInputs; j++)
			  {				  
				  if (j != 0)
				  {
					  System.out.print("+");
				  }
				  
				  if (n instanceof NeuralNode)
				  {
					  arrs.append((int)(((NeuralNode)n).m_weights[j + 1] * Math.pow(2, 12)));
					  arrs.append(",");
				  }
			  }
			  
			  arrs.append("\n");  
			  
			  if (n instanceof NeuralNode)
			  {
				  dataCount += n.m_numInputs + 1;
			  }
		  }
		  
		  prev_1    = prev_0;
		  prev_0    = layer;
		  varOffset = 0;
	  }
	  
	  arrs.substring(0, arrs.length() - 1);
	  
	  System.out.print("const double* data=new double[");
	  System.out.print(dataCount);
	  System.out.print("]{");
	  System.out.print(arrs.toString());
	  System.out.println("};");
	  
	  System.out.println(code.toString());
  }
}
