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
 *    Attribute.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core;

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

/** 
 * Class for handling an attribute. Once an attribute has been created,
 * it can't be changed. <p>
 *
 * The following attribute types are supported:
 * <ul>
 *    <li> numeric: <br/>
 *         This type of attribute represents a floating-point number.
 *    </li>
 *    <li> nominal: <br/>
 *         This type of attribute represents a fixed set of nominal values.
 *    </li>
 *    <li> string: <br/>
 *         This type of attribute represents a dynamically expanding set of
 *         nominal values. Usually used in text classification.
 *    </li>
 *    <li> date: <br/>
 *         This type of attribute represents a date, internally represented as 
 *         floating-point number storing the milliseconds since January 1, 
 *         1970, 00:00:00 GMT. The string representation of the date must be
 *         <a href="http://www.iso.org/iso/en/prods-services/popstds/datesandtime.html" target="_blank">
 *         ISO-8601</a> compliant, the default is <code>yyyy-MM-dd'T'HH:mm:ss</code>.
 *    </li>
 *    <li> relational: <br/>
 *         This type of attribute can contain other attributes and is, e.g., 
 *         used for representing Multi-Instance data. (Multi-Instance data
 *         consists of a nominal attribute containing the bag-id, then a 
 *         relational attribute with all the attributes of the bag, and 
 *         finally the class attribute.)
 *    </li>
 * </ul>
 * 
 * Typical usage (code from the main() method of this class): <p>
 *
 * <code>
 * ... <br>
 *
 * // Create numeric attributes "length" and "weight" <br>
 * Attribute length = new Attribute("length"); <br>
 * Attribute weight = new Attribute("weight"); <br><br>
 * 
 * // Create vector to hold nominal values "first", "second", "third" <br>
 * Vector my_nominal_values = new Vector(3); <br>
 * my_nominal_values.addElement("first"); <br>
 * my_nominal_values.addElement("second"); <br>
 * my_nominal_values.addElement("third"); <br><br>
 *
 * // Create nominal attribute "position" <br>
 * Attribute position = new Attribute("position", my_nominal_values);<br>
 *
 * ... <br>
 * </code><p>
 *
 * @author Eibe Frank (eibe@cs.waikato.ac.nz)
 * @version $Revision: 9518 $
 */
public class Attribute
  implements Serializable {

  /** for serialization */
  static final long serialVersionUID = -742180568732916383L;
  
  /** Constant set for numeric attributes. */
  public static final int NUMERIC = 0;

  /** Constant set for nominal attributes. */
  public static final int NOMINAL = 1;

  /** Constant set for symbolic attributes. */
  public static final int ORDERING_SYMBOLIC = 0;

  /** Constant set for ordered attributes. */
  public static final int ORDERING_ORDERED  = 1;

  /** Constant set for modulo-ordered attributes. */
  public static final int ORDERING_MODULO   = 2;
  
  /** The attribute's name. */
  private /*@ spec_public non_null @*/ String m_Name;

  /** The attribute's type. */
  private /*@ spec_public @*/ int m_Type;
  /*@ invariant m_Type == NUMERIC || 
                m_Type == NOMINAL;
  */

  /** The attribute's values (if nominal or string). */
  private /*@ spec_public @*/ Vector m_Values;

  /** Mapping of values to indices (if nominal or string). */
  private Hashtable m_Hashtable;

  /** The attribute's index. */
  private /*@ spec_public @*/ int m_Index;

  /** The attribute's metadata. */
  private ProtectedProperties m_Metadata;

  /** The attribute's ordering. */
  private int m_Ordering;

  /** Whether the attribute is regular. */
  private boolean m_IsRegular;

  /** Whether the attribute is averagable. */
  private boolean m_IsAveragable;

  /** Whether the attribute has a zeropoint. */
  private boolean m_HasZeropoint;

  /** The attribute's weight. */
  private double m_Weight;

  /** The attribute's lower numeric bound. */
  private double m_LowerBound;

  /** Whether the lower bound is open. */
  private boolean m_LowerBoundIsOpen;

  /** The attribute's upper numeric bound. */
  private double m_UpperBound;

  /** Whether the upper bound is open */
  private boolean m_UpperBoundIsOpen;

  /**
   * Constructor for a numeric attribute.
   *
   * @param attributeName the name for the attribute
   */
  //@ requires attributeName != null;
  //@ ensures  m_Name == attributeName;
  public Attribute(String attributeName) {

    this(attributeName, new ProtectedProperties(new Properties()));
  }

  /**
   * Constructor for a numeric attribute, where metadata is supplied.
   *
   * @param attributeName the name for the attribute
   * @param metadata the attribute's properties
   */
  //@ requires attributeName != null;
  //@ requires metadata != null;
  //@ ensures  m_Name == attributeName;
  public Attribute(String attributeName, ProtectedProperties metadata) {

    m_Name = attributeName;
    m_Index = -1;
    m_Values = null;
    m_Hashtable = null;
    m_Type = NUMERIC;
    setMetadata(metadata);
  }

  /**
   * Constructor for nominal attributes and string attributes.
   * If a null vector of attribute values is passed to the method,
   * the attribute is assumed to be a string.
   *
   * @param attributeName the name for the attribute
   * @param attributeValues a vector of strings denoting the 
   * attribute values. Null if the attribute is a string attribute.
   */
  //@ requires attributeName != null;
  //@ ensures  m_Name == attributeName;
  public Attribute(String attributeName, 
		   Vector attributeValues) {

    this(attributeName, attributeValues,
	 new ProtectedProperties(new Properties()));
  }

  /**
   * Constructor for nominal attributes and string attributes, where
   * metadata is supplied. If a null vector of attribute values is passed
   * to the method, the attribute is assumed to be a string.
   *
   * @param attributeName the name for the attribute
   * @param attributeValues a vector of strings denoting the 
   * attribute values. Null if the attribute is a string attribute.
   * @param metadata the attribute's properties
   */
  //@ requires attributeName != null;
  //@ requires metadata != null;
  /*@ ensures  m_Name == attributeName;
      ensures  m_Index == -1;
      ensures  attributeValues == null && m_Type == STRING
            || attributeValues != null && m_Type == NOMINAL 
                  && m_Values.size() == attributeValues.size();
      signals (IllegalArgumentException ex) 
                 (* if duplicate strings in attributeValues *);
  */
  public Attribute(String attributeName, 
		   Vector attributeValues,
		   ProtectedProperties metadata) {

    m_Name = attributeName;
    m_Index = -1;
    if (attributeValues == null) {} else {
      m_Values = new Vector(attributeValues.size());
      m_Hashtable = new Hashtable(attributeValues.size());
      for (int i = 0; i < attributeValues.size(); i++) {
	Object store = attributeValues.elementAt(i);

	if (m_Hashtable.containsKey(store)) {
	  throw new IllegalArgumentException("A nominal attribute (" +
					     attributeName + ") cannot"
					     + " have duplicate labels (" + store + ").");
	}
	m_Values.addElement(store);
	m_Hashtable.put(store, new Integer(i));
      }
      m_Type = NOMINAL;
    }
    setMetadata(metadata);
  }

  /**
   * Produces a shallow copy of this attribute.
   *
   * @return a copy of this attribute with the same index
   */
  //@ also ensures \result instanceof Attribute;
  public /*@ pure non_null @*/ Object copy() {

    Attribute copy = new Attribute(m_Name);

    copy.m_Index = m_Index;
    copy.m_Type = m_Type;
    copy.m_Values = m_Values;
    copy.m_Hashtable = m_Hashtable;
    copy.setMetadata(m_Metadata);
 
    return copy;
  }

  /**
   * Returns an enumeration of all the attribute's values if the
   * attribute is nominal, string, or relation-valued, null otherwise.
   *
   * @return enumeration of all the attribute's values
   */
  public final /*@ pure @*/ Enumeration enumerateValues() {

    if (isNominal()) {
      final Enumeration ee = m_Values.elements();
      return new Enumeration () {
          public boolean hasMoreElements() {
            return ee.hasMoreElements();
          }
          public Object nextElement() {
            return ee.nextElement();
          }
        };
    }
    return null;
  }

  /**
   * Returns the index of this attribute.
   *
   * @return the index of this attribute
   */
  //@ ensures \result == m_Index;
  public final /*@ pure @*/ int index() {

    return m_Index;
  }

  /**
   * Returns the index of a given attribute value. (The index of
   * the first occurence of this value.)
   *
   * @param value the value for which the index is to be returned
   * @return the index of the given attribute value if attribute
   * is nominal or a string, -1 if it is not or the value 
   * can't be found
   */
  public final int indexOfValue(String value) {

    if (!isNominal())
      return -1;

    Integer val = (Integer)m_Hashtable.get(value);
    if (val == null) return -1;
    else return val.intValue();
  }

  /**
   * Test if the attribute is nominal.
   *
   * @return true if the attribute is nominal
   */
  //@ ensures \result <==> (m_Type == NOMINAL);
  public final /*@ pure @*/ boolean isNominal() {

    return (m_Type == NOMINAL);
  }

  /**
   * Tests if the attribute is numeric.
   *
   * @return true if the attribute is numeric
   */
  //@ ensures \result <==> ((m_Type == NUMERIC) || (m_Type == DATE));
  public final /*@ pure @*/ boolean isNumeric() {

    return ((m_Type == NUMERIC));
  }

  /**
   * Returns the attribute's name.
   *
   * @return the attribute's name as a string
   */
  //@ ensures \result == m_Name;
  public final /*@ pure @*/ String name() {

    return m_Name;
  }
  
  /**
   * Returns the number of attribute values. Returns 0 for 
   * attributes that are not either nominal, string, or
   * relation-valued.
   *
   * @return the number of attribute values
   */
  public final /*@ pure @*/ int numValues() {

    if (!isNominal()) {
      return 0;
    } else {
      return m_Values.size();
    }
  }

  /**
   * Returns the attribute's type as an integer.
   *
   * @return the attribute's type.
   */
  //@ ensures \result == m_Type;
  public final /*@ pure @*/ int type() {

    return m_Type;
  }
 

  /**
   * Returns a value of a nominal or string attribute.  Returns an
   * empty string if the attribute is neither a string nor a nominal
   * attribute.
   *
   * @param valIndex the value's index
   * @return the attribute's value as a string
   */
  public final /*@ non_null pure @*/ String value(int valIndex) {
    
    if (!isNominal()) {
      return "";
    } else {
      Object val = m_Values.elementAt(valIndex);
      
      return (String) val;
    }
  }

  /**
   * Constructor for a numeric attribute with a particular index.
   *
   * @param attributeName the name for the attribute
   * @param index the attribute's index
   */
  //@ requires attributeName != null;
  //@ requires index >= 0;
  //@ ensures  m_Name == attributeName;
  //@ ensures  m_Index == index;
  public Attribute(String attributeName, int index) {

    this(attributeName);
    m_Index = index;
  }

  /**
   * Constructor for nominal attributes and string attributes with
   * a particular index.
   * If a null vector of attribute values is passed to the method,
   * the attribute is assumed to be a string.
   *
   * @param attributeName the name for the attribute
   * @param attributeValues a vector of strings denoting the attribute values.
   * Null if the attribute is a string attribute.
   * @param index the attribute's index
   */
  //@ requires attributeName != null;
  //@ requires index >= 0;
  //@ ensures  m_Name == attributeName;
  //@ ensures  m_Index == index;
  public Attribute(String attributeName, Vector attributeValues, 
	    int index) {

    this(attributeName, attributeValues);
    m_Index = index;
  }

  /**
   * Adds an attribute value. Creates a fresh list of attribute
   * values before adding it.
   *
   * @param value the attribute value
   */
  final void addValue(String value) {

    m_Values = (Vector)m_Values.clone();
    m_Hashtable = (Hashtable)m_Hashtable.clone();
    forceAddValue(value);
  }

  /**
   * Produces a shallow copy of this attribute with a new name.
   *
   * @param newName the name of the new attribute
   * @return a copy of this attribute with the same index
   */
  //@ requires newName != null;
  //@ ensures \result.m_Name  == newName;
  //@ ensures \result.m_Index == m_Index;
  //@ ensures \result.m_Type  == m_Type;
  public final /*@ pure non_null @*/ Attribute copy(String newName) {

    Attribute copy = new Attribute(newName);

    copy.m_Index = m_Index;
    copy.m_Type = m_Type;
    copy.m_Values = m_Values;
    copy.m_Hashtable = m_Hashtable;
    copy.setMetadata(m_Metadata);
 
    return copy;
  }

  /**
   * Adds an attribute value.
   *
   * @param value the attribute value
   */
  //@ requires value != null;
  //@ ensures  m_Values.size() == \old(m_Values.size()) + 1;
  final void forceAddValue(String value) {

    m_Values.addElement(value);
    m_Hashtable.put(value, new Integer(m_Values.size() - 1));
  }

  /**
   * Sets the index of this attribute.
   *
   * @param index the index of this attribute
   */
  //@ requires 0 <= index;
  //@ assignable m_Index;
  //@ ensures m_Index == index;
  final void setIndex(int index) {

    m_Index = index;
  }

  /**
   * Sets a value of a nominal attribute or string attribute.
   * Creates a fresh list of attribute values before it is set.
   *
   * @param index the value's index
   * @param string the value
   * @throws IllegalArgumentException if the attribute is not nominal or 
   * string.
   */
  //@ requires string != null;
  //@ requires isNominal() || isString();
  //@ requires 0 <= index && index < m_Values.size();
  final void setValue(int index, String string) {
    
    switch (m_Type) {
    case NOMINAL:
      m_Values = (Vector)m_Values.clone();
      m_Hashtable = (Hashtable)m_Hashtable.clone();
      m_Hashtable.remove(m_Values.elementAt(index));
      m_Values.setElementAt(string, index);
      m_Hashtable.put(string, new Integer(index));
      break;
    default:
      throw new IllegalArgumentException("Can only set values for nominal"
                                         + " or string attributes!");
    }
  }

  /**
   * Returns the properties supplied for this attribute.
   *
   * @return metadata for this attribute
   */  
  public final /*@ pure @*/ ProtectedProperties getMetadata() {

    return m_Metadata;
  }

  /**
   * Returns the ordering of the attribute. One of the following:
   * 
   * ORDERING_SYMBOLIC - attribute values should be treated as symbols.
   * ORDERING_ORDERED  - attribute values have a global ordering.
   * ORDERING_MODULO   - attribute values have an ordering which wraps.
   *
   * @return the ordering type of the attribute
   */
  public final /*@ pure @*/ int ordering() {

    return m_Ordering;
  }

  /**
   * Returns whether the attribute values are equally spaced.
   *
   * @return whether the attribute is regular or not
   */
  public final /*@ pure @*/ boolean isRegular() {

    return m_IsRegular;
  }

  /**
   * Returns whether the attribute can be averaged meaningfully.
   *
   * @return whether the attribute can be averaged or not
   */
  public final /*@ pure @*/ boolean isAveragable() {

    return m_IsAveragable;
  }

  /**
   * Returns whether the attribute has a zeropoint and may be
   * added meaningfully.
   *
   * @return whether the attribute has a zeropoint or not
   */
  public final /*@ pure @*/ boolean hasZeropoint() {

    return m_HasZeropoint;
  }

  /**
   * Returns the attribute's weight.
   *
   * @return the attribute's weight as a double
   */
  public final /*@ pure @*/ double weight() {

    return m_Weight;
  }

  /**
   * Sets the new attribute's weight
   * 
   * @param value	the new weight
   */
  public void setWeight(double value) {
    Properties	props;
    Enumeration names;
    String	name;
    
    m_Weight = value;

    // generate new metadata object
    props = new Properties();
    names = m_Metadata.propertyNames();
    while (names.hasMoreElements()) {
      name = (String) names.nextElement();
      if (!name.equals("weight"))
	props.setProperty(name, m_Metadata.getProperty(name));
    }
    props.setProperty("weight", "" + m_Weight);
    m_Metadata = new ProtectedProperties(props);
  }
  
  /**
   * Returns the lower bound of a numeric attribute.
   *
   * @return the lower bound of the specified numeric range
   */
  public final /*@ pure @*/ double getLowerNumericBound() {

    return m_LowerBound;
  }

  /**
   * Returns whether the lower numeric bound of the attribute is open.
   *
   * @return whether the lower numeric bound is open or not (closed)
   */
  public final /*@ pure @*/ boolean lowerNumericBoundIsOpen() {

    return m_LowerBoundIsOpen;
  }

  /**
   * Returns the upper bound of a numeric attribute.
   *
   * @return the upper bound of the specified numeric range
   */
  public final /*@ pure @*/ double getUpperNumericBound() {

    return m_UpperBound;
  }

  /**
   * Returns whether the upper numeric bound of the attribute is open.
   *
   * @return whether the upper numeric bound is open or not (closed)
   */
  public final /*@ pure @*/ boolean upperNumericBoundIsOpen() {

    return m_UpperBoundIsOpen;
  }

  /**
   * Sets the metadata for the attribute. Processes the strings stored in the
   * metadata of the attribute so that the properties can be set up for the
   * easy-access metadata methods. Any strings sought that are omitted will
   * cause default values to be set.
   * 
   * The following properties are recognised:
   * ordering, averageable, zeropoint, regular, weight, and range.
   *
   * All other properties can be queried and handled appropriately by classes
   * calling the getMetadata() method.
   *
   * @param metadata the metadata
   * @throws IllegalArgumentException if the properties are not consistent
   */
  //@ requires metadata != null;
  private void setMetadata(ProtectedProperties metadata) {
    
    m_Metadata = metadata;

    {

      // get ordering
      String orderString = m_Metadata.getProperty("ordering","");
      
      // numeric ordered attributes are averagable and zeropoint by default
      String def;
      if (m_Type == NUMERIC
	  && orderString.compareTo("modulo") != 0
	  && orderString.compareTo("symbolic") != 0)
	def = "true";
      else def = "false";
      
      // determine boolean states
      m_IsAveragable =
	(m_Metadata.getProperty("averageable",def).compareTo("true") == 0);
      m_HasZeropoint =
	(m_Metadata.getProperty("zeropoint",def).compareTo("true") == 0);
      // averagable or zeropoint implies regular
      if (m_IsAveragable || m_HasZeropoint) def = "true";
      m_IsRegular =
	(m_Metadata.getProperty("regular",def).compareTo("true") == 0);
      
      // determine ordering
      if (orderString.compareTo("symbolic") == 0)
	m_Ordering = ORDERING_SYMBOLIC;
      else if (orderString.compareTo("ordered") == 0)
	m_Ordering = ORDERING_ORDERED;
      else if (orderString.compareTo("modulo") == 0)
	m_Ordering = ORDERING_MODULO;
      else {
	if (m_Type == NUMERIC || m_IsAveragable || m_HasZeropoint)
	  m_Ordering = ORDERING_ORDERED;
	else m_Ordering = ORDERING_SYMBOLIC;
      }
    }

    // consistency checks
    if (m_IsAveragable && !m_IsRegular)
      throw new IllegalArgumentException("An averagable attribute must be"
					 + " regular");
    if (m_HasZeropoint && !m_IsRegular)
      throw new IllegalArgumentException("A zeropoint attribute must be"
					 + " regular");
    if (m_IsRegular && m_Ordering == ORDERING_SYMBOLIC)
      throw new IllegalArgumentException("A symbolic attribute cannot be"
					 + " regular");
    if (m_IsAveragable && m_Ordering != ORDERING_ORDERED)
      throw new IllegalArgumentException("An averagable attribute must be"
					 + " ordered");
    if (m_HasZeropoint && m_Ordering != ORDERING_ORDERED)
      throw new IllegalArgumentException("A zeropoint attribute must be"
					 + " ordered");

    // determine weight
    m_Weight = 1.0;
    String weightString = m_Metadata.getProperty("weight");
    if (weightString != null) {
      try{
	m_Weight = Double.valueOf(weightString).doubleValue();
      } catch (NumberFormatException e) {
	// Check if value is really a number
	throw new IllegalArgumentException("Not a valid attribute weight: '" 
					   + weightString + "'");
      }
    }

    // determine numeric range
    if (m_Type == NUMERIC) setNumericRange(m_Metadata.getProperty("range"));
  }

  /**
   * Sets the numeric range based on a string. If the string is null the range
   * will default to [-inf,+inf]. A square brace represents a closed interval, a
   * curved brace represents an open interval, and 'inf' represents infinity.
   * Examples of valid range strings: "[-inf,20)","(-13.5,-5.2)","(5,inf]"
   *
   * @param rangeString the string to parse as the attribute's numeric range
   * @throws IllegalArgumentException if the range is not valid
   */
  //@ requires rangeString != null;
  private void setNumericRange(String rangeString)
  {
    // set defaults
    m_LowerBound = Double.NEGATIVE_INFINITY;
    m_LowerBoundIsOpen = false;
    m_UpperBound = Double.POSITIVE_INFINITY;
    m_UpperBoundIsOpen = false;

    if (rangeString == null) return;

    // set up a tokenzier to parse the string
    StreamTokenizer tokenizer =
      new StreamTokenizer(new StringReader(rangeString));
    tokenizer.resetSyntax();         
    tokenizer.whitespaceChars(0, ' ');    
    tokenizer.wordChars(' '+1,'\u00FF');
    tokenizer.ordinaryChar('[');
    tokenizer.ordinaryChar('(');
    tokenizer.ordinaryChar(',');
    tokenizer.ordinaryChar(']');
    tokenizer.ordinaryChar(')');

    try {

      // get opening brace
      tokenizer.nextToken();
    
      if (tokenizer.ttype == '[') m_LowerBoundIsOpen = false;
      else if (tokenizer.ttype == '(') m_LowerBoundIsOpen = true;
      else throw new IllegalArgumentException("Expected opening brace on range,"
					      + " found: "
					      + tokenizer.toString());

      // get lower bound
      tokenizer.nextToken();
      if (tokenizer.ttype != tokenizer.TT_WORD)
	throw new IllegalArgumentException("Expected lower bound in range,"
					   + " found: "
					   + tokenizer.toString());
      if (tokenizer.sval.compareToIgnoreCase("-inf") == 0)
	m_LowerBound = Double.NEGATIVE_INFINITY;
      else if (tokenizer.sval.compareToIgnoreCase("+inf") == 0)
	m_LowerBound = Double.POSITIVE_INFINITY;
      else if (tokenizer.sval.compareToIgnoreCase("inf") == 0)
	m_LowerBound = Double.NEGATIVE_INFINITY;
      else try {
	m_LowerBound = Double.valueOf(tokenizer.sval).doubleValue();
      } catch (NumberFormatException e) {
	throw new IllegalArgumentException("Expected lower bound in range,"
					   + " found: '" + tokenizer.sval + "'");
      }

      // get separating comma
      if (tokenizer.nextToken() != ',')
	throw new IllegalArgumentException("Expected comma in range,"
					   + " found: "
					   + tokenizer.toString());

      // get upper bound
      tokenizer.nextToken();
      if (tokenizer.ttype != tokenizer.TT_WORD)
	throw new IllegalArgumentException("Expected upper bound in range,"
					   + " found: "
					   + tokenizer.toString());
      if (tokenizer.sval.compareToIgnoreCase("-inf") == 0)
	m_UpperBound = Double.NEGATIVE_INFINITY;
      else if (tokenizer.sval.compareToIgnoreCase("+inf") == 0)
	m_UpperBound = Double.POSITIVE_INFINITY;
      else if (tokenizer.sval.compareToIgnoreCase("inf") == 0)
	m_UpperBound = Double.POSITIVE_INFINITY;
      else try {
	m_UpperBound = Double.valueOf(tokenizer.sval).doubleValue();
      } catch (NumberFormatException e) {
	throw new IllegalArgumentException("Expected upper bound in range,"
					   + " found: '" + tokenizer.sval + "'");
      }

      // get closing brace
      tokenizer.nextToken();
    
      if (tokenizer.ttype == ']') m_UpperBoundIsOpen = false;
      else if (tokenizer.ttype == ')') m_UpperBoundIsOpen = true;
      else throw new IllegalArgumentException("Expected closing brace on range,"
					      + " found: "
					      + tokenizer.toString());

      // check for rubbish on end
      if (tokenizer.nextToken() != tokenizer.TT_EOF)
	throw new IllegalArgumentException("Expected end of range string,"
					   + " found: "
					   + tokenizer.toString());

    } catch (IOException e) {
      throw new IllegalArgumentException("IOException reading attribute range"
					 + " string: " + e.getMessage());
    }

    if (m_UpperBound < m_LowerBound)
      throw new IllegalArgumentException("Upper bound (" + m_UpperBound
					 + ") on numeric range is"
					 + " less than lower bound ("
					 + m_LowerBound + ")!");
  }
}
  
