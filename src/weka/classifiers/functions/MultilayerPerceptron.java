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
  
  private ArrayList<NeuralConnection> visited;
  
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
	  
	  for (NeuralConnection n : layers.get(0))
	  {
		  if (n.m_numInputs > 1)
		  {
			  min = 0;
			  break;
		  }
	  }
	  
	  for (int l = layers.size() - 2; l >= min; l--)
	  {
		  ArrayList<NeuralConnection> layer = layers.get(l);
		  
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
			  System.out.print("v[" + var + "]=");
			  
			  if (n instanceof NeuralNode)
			  {
				  System.out.print(((NeuralNode)n).m_weights[0] + "+");
			  }
			  			  
			  for (int j = 0; j < n.m_numInputs; j++)
			  {
				  NeuralConnection in = n.m_inputList[j];
				  
				  if (j != 0)
				  {
					  System.out.print("+");
				  }
				  
				  if (n instanceof NeuralNode)
				  {
					  System.out.print(((NeuralNode)n).m_weights[j + 1] + "*");
				  }
				  
				  System.out.print("v[" + in.getVariable() + "]");
			  }
			  
			  System.out.println(";");
			  
			  if (n instanceof NeuralNode)
			  {
				  System.out.println("sigmoid(&v[" + var + "]);");
			  }
		  }
		  
		  prev_1    = prev_0;
		  prev_0    = layer;
		  varOffset = 0;
	  }
  }
  
  public void _export()
  {
	  
	  
  	System.out.println("Classes: " + m_numClasses);
  	System.out.println("m_normalizeAttributes: " + m_normalizeAttributes);
  	
  	System.out.println("ZeroR");
  	for (int i = 0; i < m_numClasses; i++)
  	{
  		System.out.println("  " + m_ZeroR.m_Counts[i]);
  	}
  	
  	System.out.print("attributeBases {");
  	for (int i = 0; i < m_attributeBases.length; i++)
  	{
  		System.out.print(m_attributeBases[i] + ",");
  	}
  	System.out.println("}");
  	
  	System.out.print("attributeRanges {");
  	for (int i = 0; i < m_attributeRanges.length; i++)
  	{
  		System.out.print(m_attributeRanges[i] + ",");
  	}
  	System.out.println("}");
  	  	
  	System.out.println();
  	System.out.println("NETWORK");
  	System.out.println();
  	
  	visited = new ArrayList<NeuralConnection>();
  	loop(this.m_outputs, this.m_numClasses);
  	
  	for (int i = 0; i < visited.size(); i++)
  	{
  		NeuralConnection item = visited.get(i);
  		
  		if (item.m_numInputs != 0)
  		{
  			System.out.print("n" + i + "->link(new NeuralConnection*[" + item.m_numInputs + "] {");
  			
  			for (int j = 0; j < item.m_numInputs - 1; j++)
			{
  				System.out.print("n" + visited.indexOf(item.m_inputList[j]) + ",");
  			}
  			
  			System.out.print("n" + visited.indexOf(item.m_inputList[item.m_numInputs - 1]));
  			
  			System.out.println("});");
  		}
  	}
  	
  }
  
  	private void loop(NeuralConnection[] items, int size)
	{
  		for (int i = 0; i < size; i++)
	  	{
	  		if (visited.contains(items[i])) continue;
	  		
  			System.out.print("NeuralConnection* n" + visited.size() + "=new ");
  			visited.add(items[i]);
  			
  			if (items[i] instanceof NeuralNode)
  			{
  				double[] weights = ((NeuralNode)items[i]).m_weights;
  				
  				System.out.print("NeuralNode(" + weights[0] + "," + items[i].m_numInputs + "," + "new double[" + items[i].m_numInputs + "]{");
  				
  				for (int j = 1; j < items[i].m_numInputs; j++)
  				{
  					System.out.print(weights[j] + ",");
  				}
  				
  				System.out.println(weights[items[i].m_numInputs] + "});");
  				loop(items[i].m_inputList, items[i].m_numInputs);
  			}
  			else
  			{
  				NeuralEnd end = (NeuralEnd)items[i];
  				
  				if (end.m_input)
  				{
  					System.out.println("NeuralInput(" + end.m_link + ");");
  				}
  				else
  				{
  					System.out.println("NeuralOutput(" + items[i].m_numInputs + ");");
  					loop(items[i].m_inputList, items[i].m_numInputs);
  				}
  			}
	  	}
	}
}
