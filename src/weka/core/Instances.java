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
 *    Instances.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core;

import java.io.IOException;
import java.util.Vector;
import java.io.Reader;
import java.io.Serializable;
import java.util.HashSet;

/**
 * Class for handling an ordered set of weighted instances.
 * <p>
 * 
 * Typical usage:
 * <p>
 * 
 * <pre>
 * import weka.core.converters.ConverterUtils.DataSource;
 * ...
 * 
 * // Read all the instances in the file (ARFF, CSV, XRFF, ...)
 * DataSource source = new DataSource(filename);
 * Instances instances = source.getDataSet();
 * 
 * // Make the last attribute be the class
 * instances.setClassIndex(instances.numAttributes() - 1);
 * 
 * // Print header and instances.
 * System.out.println("\nDataset:\n");
 * System.out.println(instances);
 * 
 * ...
 * </pre>
 * <p>
 * 
 * All methods that change a set of instances are safe, ie. a change of a set of
 * instances does not affect any other sets of instances. All methods that
 * change a datasets's attribute information clone the dataset before it is
 * changed.
 * 
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 10497 $
 */
public class Instances implements Serializable {

  /** for serialization */
  static final long serialVersionUID = -19412345060742748L;

  /** The dataset's name. */
  protected/* @spec_public non_null@ */String m_RelationName;

  /** The attribute information. */
  protected/* @spec_public non_null@ */Vector m_Attributes;
  /*
   * public invariant (\forall int i; 0 <= i && i < m_Attributes.size();
   * m_Attributes.elementAt(i) != null);
   */

  /** The instances. */
  protected/* @spec_public non_null@ */Vector m_Instances;

  /** The class attribute's index */
  protected int m_ClassIndex;
  // @ protected invariant classIndex() == m_ClassIndex;

  /**
   * Constructor creating an empty set of instances. Copies references to the
   * header information from the given set of instances. Sets the capacity of
   * the set of instances to 0 if its negative.
   * 
   * @param dataset the instances from which the header information is to be
   *          taken
   * @param capacity the capacity of the new dataset
   */
  public Instances(/* @non_null@ */Instances dataset, int capacity) {
    initialize(dataset, capacity);
  }

  /**
   * initializes with the header information of the given dataset and sets the
   * capacity of the set of instances.
   * 
   * @param dataset the dataset to use as template
   * @param capacity the number of rows to reserve
   */
  protected void initialize(Instances dataset, int capacity) {
    if (capacity < 0) {
      capacity = 0;
    }

    // Strings only have to be "shallow" copied because
    // they can't be modified.
    m_ClassIndex = dataset.m_ClassIndex;
    m_RelationName = dataset.m_RelationName;
    m_Attributes = dataset.m_Attributes;
    m_Instances = new Vector(capacity);
  }

  /**
   * Creates an empty set of instances. Uses the given attribute information.
   * Sets the capacity of the set of instances to 0 if its negative. Given
   * attribute information must not be changed after this constructor has been
   * used.
   * 
   * @param name the name of the relation
   * @param attInfo the attribute information
   * @param capacity the capacity of the set
   */
  public Instances(/* @non_null@ */String name,
  /* @non_null@ */Vector attInfo, int capacity) {

    // check whether the attribute names are unique
    HashSet<String> names = new HashSet<String>();
    StringBuffer nonUniqueNames = new StringBuffer();
    for (int i = 0; i < attInfo.size(); i++) {
      if (names.contains(((Attribute) attInfo.elementAt(i)).name())) {
        nonUniqueNames.append("'" + ((Attribute) attInfo.elementAt(i)).name()
          + "' ");
      }
      names.add(((Attribute) attInfo.elementAt(i)).name());
    }
    if (names.size() != attInfo.size()) {
      throw new IllegalArgumentException("Attribute names are not unique!"
        + " Causes: " + nonUniqueNames.toString());
    }
    names.clear();

    m_RelationName = name;
    m_ClassIndex = -1;
    m_Attributes = attInfo;
    for (int i = 0; i < numAttributes(); i++) {
      attribute(i).setIndex(i);
    }
    m_Instances = new Vector(capacity);
  }

  /**
   * Adds one instance to the end of the set. Shallow copies instance before it
   * is added. Increases the size of the dataset if it is not large enough. Does
   * not check if the instance is compatible with the dataset. Note: String or
   * relational values are not transferred.
   * 
   * @param instance the instance to be added
   */
  public void add(/* @non_null@ */Instance instance) {

    Instance newInstance = (Instance) instance.copy();

    newInstance.setDataset(this);
    m_Instances.addElement(newInstance);
  }

  /**
   * Returns an attribute.
   * 
   * @param index the attribute's index (index starts with 0)
   * @return the attribute at the given position
   */
  // @ requires 0 <= index;
  // @ requires index < m_Attributes.size();
  // @ ensures \result != null;
  public/* @pure@ */Attribute attribute(int index) {

    return (Attribute) m_Attributes.elementAt(index);
  }

  /**
   * Returns an attribute given its name. If there is more than one attribute
   * with the same name, it returns the first one. Returns null if the attribute
   * can't be found.
   * 
   * @param name the attribute's name
   * @return the attribute with the given name, null if the attribute can't be
   *         found
   */
  public/* @pure@ */Attribute attribute(String name) {

    for (int i = 0; i < numAttributes(); i++) {
      if (attribute(i).name().equals(name)) {
        return attribute(i);
      }
    }
    return null;
  }

  /**
   * Checks for attributes of the given type in the dataset
   * 
   * @param attType the attribute type to look for
   * @return true if attributes of the given type are present
   */
  public boolean checkForAttributeType(int attType) {

    int i = 0;

    while (i < m_Attributes.size()) {
      if (attribute(i++).type() == attType) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the class attribute.
   * 
   * @return the class attribute
   * @throws UnassignedClassException if the class is not set
   */
  // @ requires classIndex() >= 0;
  public/* @pure@ */Attribute classAttribute() {

    if (m_ClassIndex < 0) {
      throw new UnassignedClassException("Class index is negative (not set)!");
    }
    return attribute(m_ClassIndex);
  }

  /**
   * Returns the class attribute's index. Returns negative number if it's
   * undefined.
   * 
   * @return the class index as an integer
   */
  // ensures \result == m_ClassIndex;
  public/* @pure@ */int classIndex() {

    return m_ClassIndex;
  }

  /**
   * Compactifies the set of instances. Decreases the capacity of the set so
   * that it matches the number of instances in the set.
   */
  public void compactify() {

    m_Instances.trimToSize();
  }

  /**
   * Returns the instance at the given position.
   * 
   * @param index the instance's index (index starts with 0)
   * @return the instance at the given position
   */
  // @ requires 0 <= index;
  // @ requires index < numInstances();
  public/* @non_null pure@ */Instance instance(int index) {

    return (Instance) m_Instances.elementAt(index);
  }

  /**
   * Returns the number of attributes.
   * 
   * @return the number of attributes as an integer
   */
  // @ ensures \result == m_Attributes.size();
  public/* @pure@ */int numAttributes() {

    return m_Attributes.size();
  }

  /**
   * Returns the number of instances in the dataset.
   * 
   * @return the number of instances in the dataset as an integer
   */
  // @ ensures \result == m_Instances.size();
  public/* @pure@ */int numInstances() {

    return m_Instances.size();
  }

  /**
   * Sets the class index of the set. If the class index is negative there is
   * assumed to be no class. (ie. it is undefined)
   * 
   * @param classIndex the new class index (index starts with 0)
   * @throws IllegalArgumentException if the class index is too big or < 0
   */
  public void setClassIndex(int classIndex) {

    if (classIndex >= numAttributes()) {
      throw new IllegalArgumentException("Invalid class index: " + classIndex);
    }
    m_ClassIndex = classIndex;
  }
}
