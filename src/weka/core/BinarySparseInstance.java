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
 *    BinarySparseInstance.java
 *    Copyright (C) 2002 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core;

import java.util.Enumeration;

/**
 * Class for storing a binary-data-only instance as a sparse vector. A
 * sparse instance only requires storage for those attribute values
 * that are non-zero.  Since the objective is to reduce storage
 * requirements for datasets with large numbers of default values,
 * this also includes nominal attributes -- the first nominal value
 * (i.e. that which has index 0) will not require explicit storage, so
 * rearrange your nominal attribute value orderings if
 * necessary. Missing values are not supported, and will be treated as 
 * 1 (true).
 *
 * @version $Revision: 1.13 $
 */
public class BinarySparseInstance
  extends SparseInstance {

  /** for serialization */
  private static final long serialVersionUID = -5297388762342528737L;

  /**
   * Constructor that generates a sparse instance from the given
   * instance. Reference to the dataset is set to null.
   * (ie. the instance doesn't have access to information about the
   * attribute types)
   *
   * @param instance the instance from which the attribute values
   * and the weight are to be copied
   */
  public BinarySparseInstance(Instance instance) {
    
    m_Weight = instance.m_Weight;
    m_Dataset = null;
    m_NumAttributes = instance.numAttributes();
    if (instance instanceof SparseInstance) {
      m_AttValues = null;
      m_Indices = ((SparseInstance)instance).m_Indices;
    } else {
      int[] tempIndices = new int[instance.numAttributes()];
      int vals = 0;
      for (int i = 0; i < instance.numAttributes(); i++) {
	if (instance.value(i) != 0) {
	  tempIndices[vals] = i;
	  vals++;
	}
      }
      m_AttValues = null;
      m_Indices = new int[vals];
      System.arraycopy(tempIndices, 0, m_Indices, 0, vals);
    }
  }
  
  /**
   * Constructor that copies the info from the given instance. 
   * Reference to the dataset is set to null.
   * (ie. the instance doesn't have access to information about the
   * attribute types)
   *
   * @param instance the instance from which the attribute
   * info is to be copied 
   */
  public BinarySparseInstance(SparseInstance instance) {
    
    m_AttValues = null;
    m_Indices = instance.m_Indices;
    m_Weight = instance.m_Weight;
    m_NumAttributes = instance.m_NumAttributes;
    m_Dataset = null;
  }

  /**
   * Constructor that generates a sparse instance from the given
   * parameters. Reference to the dataset is set to null.
   * (ie. the instance doesn't have access to information about the
   * attribute types)
   *
   * @param weight the instance's weight
   * @param attValues a vector of attribute values 
   */
  public BinarySparseInstance(double weight, double[] attValues) {
    
    m_Weight = weight;
    m_Dataset = null;
    m_NumAttributes = attValues.length;
    int[] tempIndices = new int[m_NumAttributes];
    int vals = 0;
    for (int i = 0; i < m_NumAttributes; i++) {
      if (attValues[i] != 0) {
	tempIndices[vals] = i;
	vals++;
      }
    }
    m_AttValues = null;
    m_Indices = new int[vals];
    System.arraycopy(tempIndices, 0, m_Indices, 0, vals);
  }
  
  /**
   * Constructor that inititalizes instance variable with given
   * values. Reference to the dataset is set to null. (ie. the instance
   * doesn't have access to information about the attribute types)
   *
   * @param weight the instance's weight
   * @param indices the indices of the given values in the full vector
   * @param maxNumValues the maximium number of values that can be stored
   */
  public BinarySparseInstance(double weight,
                              int[] indices, int maxNumValues) {
    
    m_AttValues = null;
    m_Indices = indices;
    m_Weight = weight;
    m_NumAttributes = maxNumValues;
    m_Dataset = null;
  }

  /**
   * Constructor of an instance that sets weight to one, all values to
   * 1, and the reference to the dataset to null. (ie. the instance
   * doesn't have access to information about the attribute types)
   *
   * @param numAttributes the size of the instance 
   */
  public BinarySparseInstance(int numAttributes) {
    
    m_AttValues = null;
    m_NumAttributes = numAttributes;
    m_Indices = new int[numAttributes];
    for (int i = 0; i < m_Indices.length; i++) {
      m_Indices[i] = i;
    }
    m_Weight = 1;
    m_Dataset = null;
  }

  /**
   * Produces a shallow copy of this instance. The copy doesn't have
   * access to a dataset.
   *
   * @return the shallow copy
   */
  public Object copy() {

    return new BinarySparseInstance(this);
  }

  /**
   * Merges this instance with the given instance and returns
   * the result. Dataset is set to null.
   *
   * @param inst the instance to be merged with this one
   * @return the merged instances
   */
  public Instance mergeInstance(Instance inst) {

    int [] indices = new int [numValues() + inst.numValues()];

    int m = 0;
    for (int j = 0; j < numValues(); j++) {
      indices[m++] = index(j);
    }
    for (int j = 0; j < inst.numValues(); j++) {
      if (inst.valueSparse(j) != 0) {
        indices[m++] = numAttributes() + inst.index(j);
      }
    }

    if (m != indices.length) {
      // Need to truncate
      int [] newInd = new int [m];
      System.arraycopy(indices, 0, newInd, 0, m);
      indices = newInd;
    }
    return new BinarySparseInstance(1.0, indices, numAttributes() +
                                    inst.numAttributes());
  }

  /** 
   * Does nothing, since we don't support missing values.
   *
   * @param array containing the means and modes
   */
  public void replaceMissingValues(double[] array) {
	 
    // Does nothing, since we don't store missing values.
  }

  /**
   * Sets a specific value in the instance to the given value 
   * (internal floating-point format). Performs a deep copy
   * of the vector of attribute values before the value is set.
   *
   * @param attIndex the attribute's index 
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).  
   */
  public void setValue(int attIndex, double value) {

    int index = locateIndex(attIndex);
    
    if ((index >= 0) && (m_Indices[index] == attIndex)) {
      if (value == 0) {
	int[] tempIndices = new int[m_Indices.length - 1];
	System.arraycopy(m_Indices, 0, tempIndices, 0, index);
	System.arraycopy(m_Indices, index + 1, tempIndices, index, 
			 m_Indices.length - index - 1);
	m_Indices = tempIndices;
      }
    } else {
      if (value != 0) {
	int[] tempIndices = new int[m_Indices.length + 1];
	System.arraycopy(m_Indices, 0, tempIndices, 0, index + 1);
	tempIndices[index + 1] = attIndex;
	System.arraycopy(m_Indices, index + 1, tempIndices, index + 2, 
			 m_Indices.length - index - 1);
	m_Indices = tempIndices;
      }
    }
  }

  /**
   * Sets a specific value in the instance to the given value 
   * (internal floating-point format). Performs a deep copy
   * of the vector of attribute values before the value is set.
   *
   * @param indexOfIndex the index of the attribute's index 
   * @param value the new attribute value (If the corresponding
   * attribute is nominal (or a string) then this is the new value's
   * index as a double).  
   */
  public void setValueSparse(int indexOfIndex, double value) {

    if (value == 0) {
      int[] tempIndices = new int[m_Indices.length - 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, indexOfIndex);
      System.arraycopy(m_Indices, indexOfIndex + 1, tempIndices, indexOfIndex, 
		       m_Indices.length - indexOfIndex - 1);
      m_Indices = tempIndices;
    }
  }

  /**
   * Returns the values of each attribute as an array of doubles.
   *
   * @return an array containing all the instance attribute values
   */
  public double[] toDoubleArray() {

    double[] newValues = new double[m_NumAttributes];
    for (int i = 0; i < m_Indices.length; i++) {
      newValues[m_Indices[i]] = 1.0;
    }
    return newValues;
  }

  /**
   * Returns the description of one instance in sparse format. 
   * If the instance doesn't have access to a dataset, it returns the 
   * internal floating-point values. Quotes string values that contain 
   * whitespace characters.
   *
   * @return the instance's description as a string
   */
  public String toString() {

    StringBuffer text = new StringBuffer();
    
    text.append('{');
    for (int i = 0; i < m_Indices.length; i++) {
      if (i > 0) {
        text.append(",");
      }
      if (m_Dataset == null) {
        text.append(m_Indices[i] + " 1");
      } else {
        if (m_Dataset.attribute(m_Indices[i]).isNominal() || 
            m_Dataset.attribute(m_Indices[i]).isString()) {
          text.append(m_Indices[i] + " " +
                      Utils.quote(m_Dataset.attribute(m_Indices[i]).
                                  value(1)));
        } else {
          text.append(m_Indices[i] + " 1");
        }
      }
    }
    text.append('}');
    if (m_Weight != 1.0) {
      text.append(",{" + Utils.doubleToString(m_Weight, 6) + "}");
    }
    return text.toString();
  }

  /**
   * Returns an instance's attribute value in internal format.
   *
   * @param attIndex the attribute's index
   * @return the specified value as a double (If the corresponding
   * attribute is nominal (or a string) then it returns the value's index as a 
   * double).
   */
  public double value(int attIndex) {

    int index = locateIndex(attIndex);
    if ((index >= 0) && (m_Indices[index] == attIndex)) {
      return 1.0;
    } else {
      return 0.0;
    }
  }  

  /**
   * Returns an instance's attribute value in internal format.
   * Does exactly the same thing as value() if applied to an Instance.
   *
   * @param indexOfIndex the index of the attribute's index
   * @return the specified value as a double (If the corresponding
   * attribute is nominal (or a string) then it returns the value's index as a 
   * double).
   */
  public final double valueSparse(int indexOfIndex) {

    int index = m_Indices[indexOfIndex]; // Throws if out of bounds
    return 1;
  }  

  /**
   * Deletes an attribute at the given position (0 to 
   * numAttributes() - 1).
   *
   * @param position the attribute's position
   */
  void forceDeleteAttributeAt(int position) {

    int index = locateIndex(position);

    m_NumAttributes--;
    if ((index >= 0) && (m_Indices[index] == position)) {
      int[] tempIndices = new int[m_Indices.length - 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index);
      for (int i = index; i < m_Indices.length - 1; i++) {
	tempIndices[i] = m_Indices[i + 1] - 1;
      }
      m_Indices = tempIndices;
    } else {
      int[] tempIndices = new int[m_Indices.length];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index + 1);
      for (int i = index + 1; i < m_Indices.length - 1; i++) {
	tempIndices[i] = m_Indices[i] - 1;
      }
      m_Indices = tempIndices;
    }
  }

  /**
   * Inserts an attribute at the given position
   * (0 to numAttributes()) and sets its value to 1. 
   *
   * @param position the attribute's position
   */
  void forceInsertAttributeAt(int position)  {

    int index = locateIndex(position);

    m_NumAttributes++;
    if ((index >= 0) && (m_Indices[index] == position)) {
      int[] tempIndices = new int[m_Indices.length + 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index);
      tempIndices[index] = position;
      for (int i = index; i < m_Indices.length; i++) {
	tempIndices[i + 1] = m_Indices[i] + 1;
      }
      m_Indices = tempIndices;
    } else {
      int[] tempIndices = new int[m_Indices.length + 1];
      System.arraycopy(m_Indices, 0, tempIndices, 0, index + 1);
      tempIndices[index + 1] = position;
      for (int i = index + 1; i < m_Indices.length; i++) {
	tempIndices[i + 1] = m_Indices[i] + 1;
      }
      m_Indices = tempIndices;
    }
  }
}
