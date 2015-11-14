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
import java.io.Reader;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;

import weka.core.converters.ArffLoader.ArffReader;

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

  /** The filename extension that should be used for arff files */
  public final static String FILE_EXTENSION = ".arff";

  /**
   * The filename extension that should be used for bin. serialized instances
   * files
   */
  public final static String SERIALIZED_OBJ_FILE_EXTENSION = ".bsi";

  /** The keyword used to denote the start of an arff header */
  public final static String ARFF_RELATION = "@relation";

  /** The keyword used to denote the start of the arff data section */
  public final static String ARFF_DATA = "@data";

  /** The dataset's name. */
  protected/* @spec_public non_null@ */String m_RelationName;

  /** The attribute information. */
  protected/* @spec_public non_null@ */FastVector m_Attributes;
  /*
   * public invariant (\forall int i; 0 <= i && i < m_Attributes.size();
   * m_Attributes.elementAt(i) != null);
   */

  /** The instances. */
  protected/* @spec_public non_null@ */FastVector m_Instances;

  /** The class attribute's index */
  protected int m_ClassIndex;
  // @ protected invariant classIndex() == m_ClassIndex;

  /**
   * The lines read so far in case of incremental loading. Since the
   * StreamTokenizer will be re-initialized with every instance that is read, we
   * have to keep track of the number of lines read so far.
   * 
   * @see #readInstance(Reader)
   */
  protected int m_Lines = 0;

  /**
   * Reads an ARFF file from a reader, and assigns a weight of one to each
   * instance. Lets the index of the class attribute be undefined (negative).
   * 
   * @param reader the reader
   * @throws IOException if the ARFF file is not read successfully
   */
  public Instances(/* @non_null@ */Reader reader) throws IOException {
    ArffReader arff = new ArffReader(reader);
    Instances dataset = arff.getData();
    initialize(dataset, dataset.numInstances());
    dataset.copyInstances(0, this, dataset.numInstances());
    compactify();
  }

  /**
   * Reads the header of an ARFF file from a reader and reserves space for the
   * given number of instances. Lets the class index be undefined (negative).
   * 
   * @param reader the reader
   * @param capacity the capacity
   * @throws IllegalArgumentException if the header is not read successfully or
   *           the capacity is negative.
   * @throws IOException if there is a problem with the reader.
   * @deprecated instead of using this method in conjunction with the
   *             <code>readInstance(Reader)</code> method, one should use the
   *             <code>ArffLoader</code> or <code>DataSource</code> class
   *             instead.
   * @see weka.core.converters.ArffLoader
   * @see weka.core.converters.ConverterUtils.DataSource
   */
  // @ requires capacity >= 0;
  // @ ensures classIndex() == -1;
  @Deprecated
  public Instances(/* @non_null@ */Reader reader, int capacity) throws IOException {

    ArffReader arff = new ArffReader(reader, 0);
    Instances header = arff.getStructure();
    initialize(header, capacity);
    m_Lines = arff.getLineNo();
  }

  /**
   * Constructor copying all instances and references to the header information
   * from the given set of instances.
   * 
   * @param dataset the set to be copied
   */
  public Instances(/* @non_null@ */Instances dataset) {

    this(dataset, dataset.numInstances());

    dataset.copyInstances(0, this, dataset.numInstances());
  }

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
    m_Instances = new FastVector(capacity);
  }

  /**
   * Creates a new set of instances by copying a subset of another set.
   * 
   * @param source the set of instances from which a subset is to be created
   * @param first the index of the first instance to be copied
   * @param toCopy the number of instances to be copied
   * @throws IllegalArgumentException if first and toCopy are out of range
   */
  // @ requires 0 <= first;
  // @ requires 0 <= toCopy;
  // @ requires first + toCopy <= source.numInstances();
  public Instances(/* @non_null@ */Instances source, int first, int toCopy) {

    this(source, toCopy);

    if ((first < 0) || ((first + toCopy) > source.numInstances())) {
      throw new IllegalArgumentException("Parameters first and/or toCopy out "
        + "of range");
    }
    source.copyInstances(first, this, toCopy);
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
  /* @non_null@ */FastVector attInfo, int capacity) {

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
    m_Instances = new FastVector(capacity);
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
   * Checks for string attributes in the dataset
   * 
   * @return true if string attributes are present, false otherwise
   */
  public/* @pure@ */boolean checkForStringAttributes() {
    return checkForAttributeType(Attribute.STRING);
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
   * Removes all instances from the set.
   */
  public void delete() {

    m_Instances = new FastVector();
  }

  /**
   * Removes an instance at the given position from the set.
   * 
   * @param index the instance's position (index starts with 0)
   */
  // @ requires 0 <= index && index < numInstances();
  public void delete(int index) {

    m_Instances.removeElementAt(index);
  }

  /**
   * Removes all instances with missing values for a particular attribute from
   * the dataset.
   * 
   * @param attIndex the attribute's index (index starts with 0)
   */
  // @ requires 0 <= attIndex && attIndex < numAttributes();
  public void deleteWithMissing(int attIndex) {

    FastVector newInstances = new FastVector(numInstances());

    for (int i = 0; i < numInstances(); i++) {
      if (!instance(i).isMissing(attIndex)) {
        newInstances.addElement(instance(i));
      }
    }
    m_Instances = newInstances;
  }

  /**
   * Removes all instances with missing values for a particular attribute from
   * the dataset.
   * 
   * @param att the attribute
   */
  public void deleteWithMissing(/* @non_null@ */Attribute att) {

    deleteWithMissing(att.index());
  }

  /**
   * Removes all instances with a missing class value from the dataset.
   * 
   * @throws UnassignedClassException if class is not set
   */
  public void deleteWithMissingClass() {

    if (m_ClassIndex < 0) {
      throw new UnassignedClassException("Class index is negative (not set)!");
    }
    deleteWithMissing(m_ClassIndex);
  }

  /**
   * Returns an enumeration of all the attributes. The class attribute (if set)
   * is skipped by this enumeration.
   * 
   * @return enumeration of all the attributes.
   */
  public/* @non_null pure@ */Enumeration enumerateAttributes() {

    return m_Attributes.elements(m_ClassIndex);
  }

  /**
   * Returns an enumeration of all instances in the dataset.
   * 
   * @return enumeration of all instances in the dataset
   */
  public/* @non_null pure@ */Enumeration enumerateInstances() {

    return m_Instances.elements();
  }

  /**
   * Checks if two headers are equivalent.
   * 
   * @param dataset another dataset
   * @return true if the header of the given dataset is equivalent to this
   *         header
   */
  public/* @pure@ */boolean equalHeaders(Instances dataset) {

    // Check class and all attributes
    if (m_ClassIndex != dataset.m_ClassIndex) {
      return false;
    }
    if (m_Attributes.size() != dataset.m_Attributes.size()) {
      return false;
    }
    for (int i = 0; i < m_Attributes.size(); i++) {
      if (!(attribute(i).equals(dataset.attribute(i)))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the first instance in the set.
   * 
   * @return the first instance in the set
   */
  // @ requires numInstances() > 0;
  public/* @non_null pure@ */Instance firstInstance() {

    return (Instance) m_Instances.firstElement();
  }

  /**
   * Returns a random number generator. The initial seed of the random number
   * generator depends on the given seed and the hash code of a string
   * representation of a instances chosen based on the given seed.
   * 
   * @param seed the given seed
   * @return the random number generator
   */
  public Random getRandomNumberGenerator(long seed) {

    Random r = new Random(seed);
    r.setSeed(instance(r.nextInt(numInstances())).toStringNoWeight().hashCode()
      + seed);
    return r;
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
   * Returns the last instance in the set.
   * 
   * @return the last instance in the set
   */
  // @ requires numInstances() > 0;
  public/* @non_null pure@ */Instance lastInstance() {

    return (Instance) m_Instances.lastElement();
  }

  /**
   * Returns the mean (mode) for a numeric (nominal) attribute as a
   * floating-point value. Returns 0 if the attribute is neither nominal nor
   * numeric. If all values are missing it returns zero.
   * 
   * @param attIndex the attribute's index (index starts with 0)
   * @return the mean or the mode
   */
  public/* @pure@ */double meanOrMode(int attIndex) {

    double result, found;
    int[] counts;

    if (attribute(attIndex).isNumeric()) {
      result = found = 0;
      for (int j = 0; j < numInstances(); j++) {
        if (!instance(j).isMissing(attIndex)) {
          found += instance(j).weight();
          result += instance(j).weight() * instance(j).value(attIndex);
        }
      }
      if (found <= 0) {
        return 0;
      } else {
        return result / found;
      }
    } else if (attribute(attIndex).isNominal()) {
      counts = new int[attribute(attIndex).numValues()];
      for (int j = 0; j < numInstances(); j++) {
        if (!instance(j).isMissing(attIndex)) {
          counts[(int) instance(j).value(attIndex)] += instance(j).weight();
        }
      }
      return Utils.maxIndex(counts);
    } else {
      return 0;
    }
  }

  /**
   * Returns the mean (mode) for a numeric (nominal) attribute as a
   * floating-point value. Returns 0 if the attribute is neither nominal nor
   * numeric. If all values are missing it returns zero.
   * 
   * @param att the attribute
   * @return the mean or the mode
   */
  public/* @pure@ */double meanOrMode(Attribute att) {

    return meanOrMode(att.index());
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
   * Returns the number of class labels.
   * 
   * @return the number of class labels as an integer if the class attribute is
   *         nominal, 1 otherwise.
   * @throws UnassignedClassException if the class is not set
   */
  // @ requires classIndex() >= 0;
  public/* @pure@ */int numClasses() {

    if (m_ClassIndex < 0) {
      throw new UnassignedClassException("Class index is negative (not set)!");
    }
    if (!classAttribute().isNominal()) {
      return 1;
    } else {
      return classAttribute().numValues();
    }
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
   * Shuffles the instances in the set so that they are ordered randomly.
   * 
   * @param random a random number generator
   */
  public void randomize(Random random) {

    for (int j = numInstances() - 1; j > 0; j--) {
      swap(j, random.nextInt(j + 1));
    }
  }

  /**
   * Reads a single instance from the reader and appends it to the dataset.
   * Automatically expands the dataset if it is not large enough to hold the
   * instance. This method does not check for carriage return at the end of the
   * line.
   * 
   * @param reader the reader
   * @return false if end of file has been reached
   * @throws IOException if the information is not read successfully
   * @deprecated instead of using this method in conjunction with the
   *             <code>readInstance(Reader)</code> method, one should use the
   *             <code>ArffLoader</code> or <code>DataSource</code> class
   *             instead.
   * @see weka.core.converters.ArffLoader
   * @see weka.core.converters.ConverterUtils.DataSource
   */
  @Deprecated
  public boolean readInstance(Reader reader) throws IOException {

    ArffReader arff = new ArffReader(reader, this, m_Lines, 1);
    Instance inst = arff.readInstance(arff.getData(), false);
    m_Lines = arff.getLineNo();
    if (inst != null) {
      add(inst);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns the relation's name.
   * 
   * @return the relation's name as a string
   */
  // @ ensures \result == m_RelationName;
  public/* @pure@ */String relationName() {

    return m_RelationName;
  }

  /**
   * Renames an attribute. This change only affects this dataset.
   * 
   * @param att the attribute's index (index starts with 0)
   * @param name the new name
   */
  public void renameAttribute(int att, String name) {
    // name already present?
    for (int i = 0; i < numAttributes(); i++) {
      if (i == att) {
        continue;
      }
      if (attribute(i).name().equals(name)) {
        throw new IllegalArgumentException("Attribute name '" + name
          + "' already present at position #" + i);
      }
    }

    Attribute newAtt = attribute(att).copy(name);
    FastVector newVec = new FastVector(numAttributes());
    for (int i = 0; i < numAttributes(); i++) {
      if (i == att) {
        newVec.addElement(newAtt);
      } else {
        newVec.addElement(attribute(i));
      }
    }
    m_Attributes = newVec;
  }

  /**
   * Renames an attribute. This change only affects this dataset.
   * 
   * @param att the attribute
   * @param name the new name
   */
  public void renameAttribute(Attribute att, String name) {

    renameAttribute(att.index(), name);
  }

  /**
   * Renames the value of a nominal (or string) attribute value. This change
   * only affects this dataset.
   * 
   * @param att the attribute's index (index starts with 0)
   * @param val the value's index (index starts with 0)
   * @param name the new name
   */
  public void renameAttributeValue(int att, int val, String name) {

    Attribute newAtt = (Attribute) attribute(att).copy();
    FastVector newVec = new FastVector(numAttributes());

    newAtt.setValue(val, name);
    for (int i = 0; i < numAttributes(); i++) {
      if (i == att) {
        newVec.addElement(newAtt);
      } else {
        newVec.addElement(attribute(i));
      }
    }
    m_Attributes = newVec;
  }

  /**
   * Renames the value of a nominal (or string) attribute value. This change
   * only affects this dataset.
   * 
   * @param att the attribute
   * @param val the value
   * @param name the new name
   */
  public void renameAttributeValue(Attribute att, String val, String name) {

    int v = att.indexOfValue(val);
    if (v == -1) {
      throw new IllegalArgumentException(val + " not found");
    }
    renameAttributeValue(att.index(), v, name);
  }

  /**
   * Creates a new dataset of the same size using random sampling with
   * replacement.
   * 
   * @param random a random number generator
   * @return the new dataset
   */
  public Instances resample(Random random) {

    Instances newData = new Instances(this, numInstances());
    while (newData.numInstances() < numInstances()) {
      newData.add(instance(random.nextInt(numInstances())));
    }
    return newData;
  }

  /**
   * Creates a new dataset of the same size using random sampling with
   * replacement according to the current instance weights. The weights of the
   * instances in the new dataset are set to one.
   * 
   * @param random a random number generator
   * @return the new dataset
   */
  public Instances resampleWithWeights(Random random) {

    double[] weights = new double[numInstances()];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = instance(i).weight();
    }
    return resampleWithWeights(random, weights);
  }

  /**
   * Creates a new dataset of the same size using random sampling with
   * replacement according to the current instance weights. The weights of the
   * instances in the new dataset are set to one. See also
   * resampleWithWeights(Random, double[], boolean[]).
   * 
   * @param random a random number generator
   * @param sampled an array indicating what has been sampled
   * @return the new dataset
   */
  public Instances resampleWithWeights(Random random, boolean[] sampled) {

    double[] weights = new double[numInstances()];
    for (int i = 0; i < weights.length; i++) {
      weights[i] = instance(i).weight();
    }
    return resampleWithWeights(random, weights, sampled);
  }

  /**
   * Creates a new dataset of the same size using random sampling with
   * replacement according to the given weight vector. See also
   * resampleWithWeights(Random, double[], boolean[]).
   * 
   * @param random a random number generator
   * @param weights the weight vector
   * @return the new dataset
   * @throws IllegalArgumentException if the weights array is of the wrong
   *           length or contains negative weights.
   */
  public Instances resampleWithWeights(Random random, double[] weights) {

    return resampleWithWeights(random, weights, null);
  }

  /**
   * Creates a new dataset of the same size using random sampling with
   * replacement according to the given weight vector. The weights of the
   * instances in the new dataset are set to one. The length of the weight
   * vector has to be the same as the number of instances in the dataset, and
   * all weights have to be positive. Uses Walker's method, see pp. 232 of
   * "Stochastic Simulation" by B.D. Ripley (1987).
   * 
   * @param random a random number generator
   * @param weights the weight vector
   * @param sampled an array indicating what has been sampled, can be null
   * @return the new dataset
   * @throws IllegalArgumentException if the weights array is of the wrong
   *           length or contains negative weights.
   */
  public Instances resampleWithWeights(Random random, double[] weights,
    boolean[] sampled) {

    if (weights.length != numInstances()) {
      throw new IllegalArgumentException("weights.length != numInstances.");
    }

    Instances newData = new Instances(this, numInstances());
    if (numInstances() == 0) {
      return newData;
    }

    // Walker's method, see pp. 232 of "Stochastic Simulation" by B.D. Ripley
    double[] P = new double[weights.length];
    System.arraycopy(weights, 0, P, 0, weights.length);
    Utils.normalize(P);
    double[] Q = new double[weights.length];
    int[] A = new int[weights.length];
    int[] W = new int[weights.length];
    int M = weights.length;
    int NN = -1;
    int NP = M;
    for (int I = 0; I < M; I++) {
      if (P[I] < 0) {
        throw new IllegalArgumentException("Weights have to be positive.");
      }
      Q[I] = M * P[I];
      if (Q[I] < 1.0) {
        W[++NN] = I;
      } else {
        W[--NP] = I;
      }
    }
    if (NN > -1 && NP < M) {
      for (int S = 0; S < M - 1; S++) {
        int I = W[S];
        int J = W[NP];
        A[I] = J;
        Q[J] += Q[I] - 1.0;
        if (Q[J] < 1.0) {
          NP++;
        }
        if (NP >= M) {
          break;
        }
      }
      // A[W[M]] = W[M];
    }

    for (int I = 0; I < M; I++) {
      Q[I] += I;
    }

    for (int i = 0; i < numInstances(); i++) {
      int ALRV;
      double U = M * random.nextDouble();
      int I = (int) U;
      if (U < Q[I]) {
        ALRV = I;
      } else {
        ALRV = A[I];
      }
      newData.add(instance(ALRV));
      if (sampled != null) {
        sampled[ALRV] = true;
      }
      newData.instance(newData.numInstances() - 1).setWeight(1);
    }

    return newData;
  }

  /**
   * Sets the class attribute.
   * 
   * @param att attribute to be the class
   */
  public void setClass(Attribute att) {

    m_ClassIndex = att.index();
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

  /**
   * Sets the relation's name.
   * 
   * @param newName the new relation name.
   */
  public void setRelationName(/* @non_null@ */String newName) {

    m_RelationName = newName;
  }

  /**
   * Computes the sum of all the instances' weights.
   * 
   * @return the sum of all the instances' weights as a double
   */
  public/* @pure@ */double sumOfWeights() {

    double sum = 0;

    for (int i = 0; i < numInstances(); i++) {
      sum += instance(i).weight();
    }
    return sum;
  }

  /**
   * Creates the test set for one fold of a cross-validation on the dataset.
   * 
   * @param numFolds the number of folds in the cross-validation. Must be
   *          greater than 1.
   * @param numFold 0 for the first fold, 1 for the second, ...
   * @return the test set as a set of weighted instances
   * @throws IllegalArgumentException if the number of folds is less than 2 or
   *           greater than the number of instances.
   */
  // @ requires 2 <= numFolds && numFolds < numInstances();
  // @ requires 0 <= numFold && numFold < numFolds;
  public Instances testCV(int numFolds, int numFold) {

    int numInstForFold, first, offset;
    Instances test;

    if (numFolds < 2) {
      throw new IllegalArgumentException("Number of folds must be at least 2!");
    }
    if (numFolds > numInstances()) {
      throw new IllegalArgumentException(
        "Can't have more folds than instances!");
    }
    numInstForFold = numInstances() / numFolds;
    if (numFold < numInstances() % numFolds) {
      numInstForFold++;
      offset = numFold;
    } else {
      offset = numInstances() % numFolds;
    }
    test = new Instances(this, numInstForFold);
    first = numFold * (numInstances() / numFolds) + offset;
    copyInstances(first, test, numInstForFold);
    return test;
  }

  /**
   * Returns the dataset as a string in ARFF format. Strings are quoted if they
   * contain whitespace characters, or if they are a question mark.
   * 
   * @return the dataset in ARFF format as a string
   */
  @Override
  public String toString() {

    StringBuffer text = new StringBuffer();

    text.append(ARFF_RELATION).append(" ").append(Utils.quote(m_RelationName))
      .append("\n\n");
    for (int i = 0; i < numAttributes(); i++) {
      text.append(attribute(i)).append("\n");
    }
    text.append("\n").append(ARFF_DATA).append("\n");

    text.append(stringWithoutHeader());
    return text.toString();
  }

  /**
   * Returns the instances in the dataset as a string in ARFF format. Strings
   * are quoted if they contain whitespace characters, or if they are a question
   * mark.
   * 
   * @return the dataset in ARFF format as a string
   */
  protected String stringWithoutHeader() {

    StringBuffer text = new StringBuffer();

    for (int i = 0; i < numInstances(); i++) {
      text.append(instance(i));
      if (i < numInstances() - 1) {
        text.append('\n');
      }
    }
    return text.toString();
  }

  /**
   * Creates the training set for one fold of a cross-validation on the dataset.
   * 
   * @param numFolds the number of folds in the cross-validation. Must be
   *          greater than 1.
   * @param numFold 0 for the first fold, 1 for the second, ...
   * @return the training set
   * @throws IllegalArgumentException if the number of folds is less than 2 or
   *           greater than the number of instances.
   */
  // @ requires 2 <= numFolds && numFolds < numInstances();
  // @ requires 0 <= numFold && numFold < numFolds;
  public Instances trainCV(int numFolds, int numFold) {

    int numInstForFold, first, offset;
    Instances train;

    if (numFolds < 2) {
      throw new IllegalArgumentException("Number of folds must be at least 2!");
    }
    if (numFolds > numInstances()) {
      throw new IllegalArgumentException(
        "Can't have more folds than instances!");
    }
    numInstForFold = numInstances() / numFolds;
    if (numFold < numInstances() % numFolds) {
      numInstForFold++;
      offset = numFold;
    } else {
      offset = numInstances() % numFolds;
    }
    train = new Instances(this, numInstances() - numInstForFold);
    first = numFold * (numInstances() / numFolds) + offset;
    copyInstances(0, train, first);
    copyInstances(first + numInstForFold, train, numInstances() - first
      - numInstForFold);

    return train;
  }

  /**
   * Creates the training set for one fold of a cross-validation on the dataset.
   * The data is subsequently randomized based on the given random number
   * generator.
   * 
   * @param numFolds the number of folds in the cross-validation. Must be
   *          greater than 1.
   * @param numFold 0 for the first fold, 1 for the second, ...
   * @param random the random number generator
   * @return the training set
   * @throws IllegalArgumentException if the number of folds is less than 2 or
   *           greater than the number of instances.
   */
  // @ requires 2 <= numFolds && numFolds < numInstances();
  // @ requires 0 <= numFold && numFold < numFolds;
  public Instances trainCV(int numFolds, int numFold, Random random) {

    Instances train = trainCV(numFolds, numFold);
    train.randomize(random);
    return train;
  }

  /**
   * Computes the variance for a numeric attribute.
   * 
   * @param attIndex the numeric attribute (index starts with 0)
   * @return the variance if the attribute is numeric
   * @throws IllegalArgumentException if the attribute is not numeric
   */
  public/* @pure@ */double variance(int attIndex) {

    double sum = 0, sumSquared = 0, sumOfWeights = 0;

    if (!attribute(attIndex).isNumeric()) {
      throw new IllegalArgumentException(
        "Can't compute variance because attribute is " + "not numeric!");
    }
    for (int i = 0; i < numInstances(); i++) {
      if (!instance(i).isMissing(attIndex)) {
        sum += instance(i).weight() * instance(i).value(attIndex);
        sumSquared += instance(i).weight() * instance(i).value(attIndex)
          * instance(i).value(attIndex);
        sumOfWeights += instance(i).weight();
      }
    }
    if (sumOfWeights <= 1) {
      return 0;
    }
    double result = (sumSquared - (sum * sum / sumOfWeights))
      / (sumOfWeights - 1);

    // We don't like negative variance
    if (result < 0) {
      return 0;
    } else {
      return result;
    }
  }

  /**
   * Computes the variance for a numeric attribute.
   * 
   * @param att the numeric attribute
   * @return the variance if the attribute is numeric
   * @throws IllegalArgumentException if the attribute is not numeric
   */
  public/* @pure@ */double variance(Attribute att) {

    return variance(att.index());
  }

  /**
   * Gets the value of all instances in this dataset for a particular attribute.
   * Useful in conjunction with Utils.sort to allow iterating through the
   * dataset in sorted order for some attribute.
   * 
   * @param index the index of the attribute.
   * @return an array containing the value of the desired attribute for each
   *         instance in the dataset.
   */
  // @ requires 0 <= index && index < numAttributes();
  public/* @pure@ */double[] attributeToDoubleArray(int index) {

    double[] result = new double[numInstances()];
    for (int i = 0; i < result.length; i++) {
      result[i] = instance(i).value(index);
    }
    return result;
  }

  /**
   * Copies instances from one set to the end of another one.
   * 
   * @param from the position of the first instance to be copied
   * @param dest the destination for the instances
   * @param num the number of instances to be copied
   */
  // @ requires 0 <= from && from <= numInstances() - num;
  // @ requires 0 <= num;
  protected void copyInstances(int from, /* @non_null@ */Instances dest, int num) {

    for (int i = 0; i < num; i++) {
      dest.add(instance(from + i));
    }
  }

  /**
   * Returns string including all instances, their weights and their indices in
   * the original dataset.
   * 
   * @return description of instance and its weight as a string
   */
  protected/* @pure@ */String instancesAndWeights() {

    StringBuffer text = new StringBuffer();

    for (int i = 0; i < numInstances(); i++) {
      text.append(instance(i) + " " + instance(i).weight());
      if (i < numInstances() - 1) {
        text.append("\n");
      }
    }
    return text.toString();
  }

  /**
   * Help function needed for stratification of set.
   * 
   * @param numFolds the number of folds for the stratification
   */
  protected void stratStep(int numFolds) {

    FastVector newVec = new FastVector(m_Instances.capacity());
    int start = 0, j;

    // create stratified batch
    while (newVec.size() < numInstances()) {
      j = start;
      while (j < numInstances()) {
        newVec.addElement(instance(j));
        j = j + numFolds;
      }
      start++;
    }
    m_Instances = newVec;
  }

  /**
   * Swaps two instances in the set.
   * 
   * @param i the first instance's index (index starts with 0)
   * @param j the second instance's index (index starts with 0)
   */
  // @ requires 0 <= i && i < numInstances();
  // @ requires 0 <= j && j < numInstances();
  public void swap(int i, int j) {

    m_Instances.swap(i, j);
  }
}
