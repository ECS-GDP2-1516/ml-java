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
 *    Instance.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core;

import java.io.Serializable;

/**
 * Class for handling an instance. All values (numeric, date, nominal, string
 * or relational) are internally stored as floating-point numbers. If an
 * attribute is nominal (or a string or relational), the stored value is the
 * index of the corresponding nominal (or string or relational) value in the
 * attribute's definition. We have chosen this approach in favor of a more
 * elegant object-oriented approach because it is much faster. <p>
 *
 * Typical usage (code from the main() method of this class): <p>
 *
 * <code>
 * ... <br>
 *      
 * // Create empty instance with three attribute values <br>
 * Instance inst = new Instance(3); <br><br>
 *     
 * // Set instance's values for the attributes "length", "weight", and "position"<br>
 * inst.setValue(length, 5.3); <br>
 * inst.setValue(weight, 300); <br>
 * inst.setValue(position, "first"); <br><br>
 *   
 * // Set instance's dataset to be the dataset "race" <br>
 * inst.setDataset(race); <br><br>
 *   
 * // Print the instance <br>
 * System.out.println("The instance: " + inst); <br>
 *
 * ... <br>
 * </code><p>
 *
 * All methods that change an instance are safe, ie. a change of an
 * instance does not affect any other instances. All methods that
 * change an instance's attribute values clone the attribute value
 * vector before it is changed. If your application heavily modifies
 * instance values, it may be faster to create a new instance from scratch.
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 9140 $ 
 */
public class Instance
  implements Serializable {
  
  /** for serialization */
  static final long serialVersionUID = 1482635194499365122L;
  
  /** Constant representing a missing value. */
  protected static final double MISSING_VALUE = Double.NaN;

  /** 
   * The dataset the instance has access to.  Null if the instance
   * doesn't have access to any dataset.  Only if an instance has
   * access to a dataset, it knows about the actual attribute types.  
   */
  protected /*@spec_public@*/ Instances m_Dataset;

  /** The instance's attribute values. */
  protected /*@spec_public non_null@*/ double[] m_AttValues;

  /** The instance's weight. */
  protected double m_Weight;

  /**
   * Constructor that copies the attribute values and the weight from
   * the given instance. Reference to the dataset is set to null.
   * (ie. the instance doesn't have access to information about the
   * attribute types)
   *
   * @param instance the instance from which the attribute
   * values and the weight are to be copied 
   */
  //@ ensures m_Dataset == null;
  public Instance(/*@non_null@*/ Instance instance) {
    
    m_AttValues = instance.m_AttValues;
    m_Weight = instance.m_Weight;
    m_Dataset = null;
  }

  /**
   * Constructor that inititalizes instance variable with given
   * values. Reference to the dataset is set to null. (ie. the instance
   * doesn't have access to information about the attribute types)
   *
   * @param weight the instance's weight
   * @param attValues a vector of attribute values 
   */
  //@ ensures m_Dataset == null;
  public Instance(double weight,  /*@non_null@*/ double[]attValues){
    
    m_AttValues = attValues;
    m_Weight = weight;
    m_Dataset = null;
  }

  /**
   * Constructor of an instance that sets weight to one, all values to
   * be missing, and the reference to the dataset to null. (ie. the instance
   * doesn't have access to information about the attribute types)
   *
   * @param numAttributes the size of the instance 
   */
  //@ requires numAttributes > 0;    // Or maybe == 0 is okay too?
  //@ ensures m_Dataset == null;
  public Instance(int numAttributes) {
    
    m_AttValues = new double[numAttributes];
    for (int i = 0; i < m_AttValues.length; i++) {
      m_AttValues[i] = MISSING_VALUE;
    }
    m_Weight = 1;
    m_Dataset = null;
  }

  /**
   * Returns the attribute with the given index.
   *
   * @param index the attribute's index
   * @return the attribute at the given position
   * @throws UnassignedDatasetException if instance doesn't have access to a
   * dataset
   */ 
  //@ requires m_Dataset != null;
  public /*@pure@*/ Attribute attribute(int index) {
   
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.attribute(index);
  }

  /**
   * Returns class attribute.
   *
   * @return the class attribute
   * @throws UnassignedDatasetException if the class is not set or the
   * instance doesn't have access to a dataset
   */
  //@ requires m_Dataset != null;
  public /*@pure@*/ Attribute classAttribute() {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.classAttribute();
  }

  /**
   * Returns the class attribute's index.
   *
   * @return the class index as an integer 
   * @throws UnassignedDatasetException if instance doesn't have access to a dataset 
   */
  //@ requires m_Dataset != null;
  //@ ensures  \result == m_Dataset.classIndex();
  public /*@pure@*/ int classIndex() {
    
    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    }
    return m_Dataset.classIndex();
  }

  /**
   * Produces a shallow copy of this instance. The copy has
   * access to the same dataset. (if you want to make a copy
   * that doesn't have access to the dataset, use 
   * <code>new Instance(instance)</code>
   *
   * @return the shallow copy
   */
  //@ also ensures \result != null;
  //@ also ensures \result instanceof Instance;
  //@ also ensures ((Instance)\result).m_Dataset == m_Dataset;
  public /*@pure@*/ Object copy() {

    Instance result = new Instance(this);
    result.m_Dataset = m_Dataset;
    return result;
  }

  /**
   * Returns the dataset this instance has access to. (ie. obtains
   * information about attribute types from) Null if the instance
   * doesn't have access to a dataset.
   *
   * @return the dataset the instance has accesss to
   */
  //@ ensures \result == m_Dataset;
  public /*@pure@*/ Instances dataset() {

    return m_Dataset;
  }

  /**
   * Tests if a specific value is "missing".
   *
   * @param attIndex the attribute's index
   * @return true if the value is "missing"
   */
  public /*@pure@*/ boolean isMissing(int attIndex) {

    if (Double.isNaN(m_AttValues[attIndex])) {
      return true;
    }
    return false;
  }

  /**
   * Returns the double that codes "missing".
   *
   * @return the double that codes "missing"
   */
  public /*@pure@*/ static double missingValue() {

    return MISSING_VALUE;
  }
  
  /**
   * Sets the reference to the dataset. Does not check if the instance
   * is compatible with the dataset. Note: the dataset does not know
   * about this instance. If the structure of the dataset's header
   * gets changed, this instance will not be adjusted automatically.
   *
   * @param instances the reference to the dataset 
   */
  public final void setDataset(Instances instances) {
    
    m_Dataset = instances;
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
    
    freshAttributeVector();
    m_AttValues[attIndex] = value;
  }

  /** 
   * Returns the relational value of a relational attribute.
   *
   * @param attIndex the attribute's index
   * @return the corresponding relation as an Instances object
   * @throws IllegalArgumentException if the attribute is not a
   * relation-valued attribute
   * @throws UnassignedDatasetException if the instance doesn't belong
   * to a dataset.
   */
  //@ requires m_Dataset != null;
  public final /*@pure@*/ Instances relationalValue(int attIndex) {

    if (m_Dataset == null) {
      throw new UnassignedDatasetException("Instance doesn't have access to a dataset!");
    } 
    return relationalValue(m_Dataset.attribute(attIndex));
  }

  /** 
   * Returns the relational value of a relational attribute.
   *
   * @param att the attribute
   * @return the corresponding relation as an Instances object
   * @throws IllegalArgumentException if the attribute is not a
   * relation-valued attribute
   * @throws UnassignedDatasetException if the instance doesn't belong
   * to a dataset.
   */
  public final /*@pure@*/ Instances relationalValue(Attribute att) {

    int attIndex = att.index();
    if (att.isRelationValued()) {
      if (isMissing(attIndex)) {
        return null;
      }
      return att.relation((int) value(attIndex));
    } else {
      throw new IllegalArgumentException("Attribute isn't relation-valued!");
    }
  }
  
  /**
   * Returns the values of each attribute as an array of doubles.
   *
   * @return an array containing all the instance attribute values
   */
  public double[] toDoubleArray() {

    double[] newValues = new double[m_AttValues.length];
    System.arraycopy(m_AttValues, 0, newValues, 0, 
		     m_AttValues.length);
    return newValues;
  }
  
  /**
   * Returns an instance's attribute value in internal format.
   *
   * @param attIndex the attribute's index
   * @return the specified value as a double (If the corresponding
   * attribute is nominal (or a string) then it returns the value's index as a 
   * double).
   */
  public /*@pure@*/ double value(int attIndex) {

    return m_AttValues[attIndex];
  }
  
 
  /**
   * Private constructor for subclasses. Does nothing.
   */
  protected Instance() {
  }

  /**
   * Clones the attribute vector of the instance and
   * overwrites it with the clone.
   */
  private void freshAttributeVector() {

    m_AttValues = toDoubleArray();
  }
}
