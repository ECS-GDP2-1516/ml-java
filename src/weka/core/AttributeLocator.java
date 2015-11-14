/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * StringLocator.java
 * Copyright (C) 2005-2012 University of Waikato, Hamilton, New Zealand
 */

package weka.core;

import java.io.Serializable;
import java.util.Vector;
import java.util.BitSet;

/**
 * This class locates and records the indices of a certain type of attributes, 
 * recursively in case of Relational attributes.
 * 
 * @author fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 8034 $
 * @see Attribute#RELATIONAL
 */
public class AttributeLocator 
  implements Serializable, Comparable<AttributeLocator> {
  
  /** for serialization */
  private static final long serialVersionUID = -2932848827681070345L;

  /** the attribute indices that may be inspected */
  protected int[] m_AllowedIndices = null;
  
  /** contains the attribute locations, either true or false Boolean objects */
  protected Vector<Boolean> m_Attributes = null;
  
  /** contains the attribute locations, either true or false (efficient replacement) */
  protected BitSet m_AttributesEfficient = null;
  
  /** contains the locator locations, either null or a AttributeLocator reference */
  protected Vector<AttributeLocator> m_Locators = null;

  /** the type of the attribute */
  protected int m_Type = -1;
  
  /** the referenced data */
  protected Instances m_Data = null;

  /** the indices */
  protected int[] m_Indices = null;

  /** the indices of locator objects */
  protected int[] m_LocatorIndices = null;
  
  /**
   * returns the type of attribute that is located
   * 
   * @return		the type of attribute
   */
  public int getType() {
    return m_Type;
  }
  
  /**
   * returns the indices that are allowed to check for the attribute type
   * 
   * @return 		the indices that are checked for the attribute type
   */
  public int[] getAllowedIndices() {
    return m_AllowedIndices;
  }
  
  /**
   * returns the underlying data
   * 
   * @return      the underlying Instances object
   */
  public Instances getData() {
    return m_Data;
  }
  


  /**
   * returns actual index in the Instances object.
   * 
   * @param index	the index in the m_AllowedIndices array
   * @return		the actual index in the instances object
   */
  public int getActualIndex(int index) {
    return m_AllowedIndices[index];
  }
  
  /**
   * Returns the indices of the attributes. These indices are referring
   * to the m_AllowedIndices array, not the actual indices in the Instances
   * object.
   * 
   * @return	the indices of the attributes
   * @see	#getActualIndex(int)
   */
  public int[] getAttributeIndices() {
    return m_Indices;
  }
  
  /**
   * Returns the indices of the AttributeLocator objects.  These indices are 
   * referring to the m_AllowedIndices array, not the actual indices in the 
   * Instances object.
   * 
   * @return	the indices of the AttributeLocator objects
   * @see	#getActualIndex(int)
   */
  public int[] getLocatorIndices() {
    return m_LocatorIndices;
  }
  
  /**
   * Returns the AttributeLocator at the given index. This index refers to
   * the index of the m_AllowedIndices array, not the actual Instances object.
   * 
   * @param index   the index of the locator to retrieve
   * @return        the AttributeLocator at the given index
   */
  public AttributeLocator getLocator(int index) {
    return (AttributeLocator) m_Locators.get(index);
  }
  
  /**
   * Compares this object with the specified object for order. Returns a 
   * negative integer, zero, or a positive integer as this object is less 
   * than, equal to, or greater than the specified object. Only type and
   * indices are checked.
   * 
   * @param o		the object to compare with
   * @return		-1 if less than, 0 if equal, +1 if greater than the 
   * 			given object
   */
  public int compareTo(AttributeLocator o) {
    int		result;
    int		i;
    
    result = 0;
    
    // 1. check type
    if (this.getType() < o.getType()) {
      result = -1;
    }
    else if (this.getType() > o.getType()) {
      result = 1;
    }
    else {
      // 2. check indices
      if (this.getAllowedIndices().length < o.getAllowedIndices().length) {
	result = -1;
      }
      else if (this.getAllowedIndices().length > o.getAllowedIndices().length) {
	result = 1;
      }
      else {
	for (i = 0; i < this.getAllowedIndices().length; i++) {
	  if (this.getAllowedIndices()[i] < o.getAllowedIndices()[i]) {
	    result = -1;
	    break;
	  }
	  else if (this.getAllowedIndices()[i] > o.getAllowedIndices()[i]) {
	    result = 1;
	    break;
	  }
	  else {
	    result = 0;
	  }
	}
      }
    }
    
    return result;
  }
}
