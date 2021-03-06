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
 *    ArffLoader.java
 *    Copyright (C) 2000 University of Waikato, Hamilton, New Zealand
 *
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;

import weka.core.Attribute;
import java.util.Vector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * <!-- globalinfo-start --> Reads a source that is in arff (attribute relation
 * file format) format.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 11137 $
 * @see Loader
 */
public class ArffLoader
{
  /** The reader for the source file. */
  protected transient Reader m_sourceReader = null;

  /** The parser for the ARFF file */
  protected transient ArffReader m_ArffReader = null;

  /** Holds the determined structure (header) of the data set. */
  private Instances m_structure = null;

  /**
   * Reads data from an ARFF file, either in incremental or batch mode.
   * <p/>
   * 
   * Typical code for batch usage:
   * 
   * <pre>
   * BufferedReader reader =
   *   new BufferedReader(new FileReader(&quot;/some/where/file.arff&quot;));
   * ArffReader arff = new ArffReader(reader);
   * Instances data = arff.getData();
   * data.setClassIndex(data.numAttributes() - 1);
   * </pre>
   * 
   * Typical code for incremental usage:
   * 
   * <pre>
   * BufferedReader reader =
   *   new BufferedReader(new FileReader(&quot;/some/where/file.arff&quot;));
   * ArffReader arff = new ArffReader(reader, 1000);
   * Instances data = arff.getStructure();
   * data.setClassIndex(data.numAttributes() - 1);
   * Instance inst;
   * while ((inst = arff.readInstance(data)) != null) {
   *   data.add(inst);
   * }
   * </pre>
   * 
   * @author Eibe Frank (eibe@cs.waikato.ac.nz)
   * @author Len Trigg (trigg@cs.waikato.ac.nz)
   * @author fracpete (fracpete at waikato dot ac dot nz)
   * @version $Revision: 11137 $
   */
  public static class ArffReader
     {

    /** the tokenizer for reading the stream */
    protected StreamTokenizer m_Tokenizer;

    /** Buffer of values for sparse instance */
    protected double[] m_ValueBuffer;

    /** Buffer of indices for sparse instance */
    protected int[] m_IndicesBuffer;

    /** the actual data */
    protected Instances m_Data;

    /** the number of lines read so far */
    protected int m_Lines;

    /**
     * Reads the data completely from the reader. The data can be accessed via
     * the <code>getData()</code> method.
     * 
     * @param reader the reader to use
     * @throws IOException if something goes wrong
     * @see #getData()
     */
    public ArffReader(Reader reader) throws IOException {
      m_Tokenizer = new StreamTokenizer(reader);
      initTokenizer();

      readHeader(1000);
      initBuffers();

      Instance inst;
      while ((inst = readInstance(m_Data)) != null) {
        m_Data.add(inst);
      }
      ;

      compactify();
    }

    /**
     * Reads only the header and reserves the specified space for instances.
     * Further instances can be read via <code>readInstance()</code>.
     * 
     * @param reader the reader to use
     * @param capacity the capacity of the new dataset
     * @throws IOException if something goes wrong
     * @throws IllegalArgumentException if capacity is negative
     * @see #getStructure()
     * @see #readInstance(Instances)
     */
    public ArffReader(Reader reader, int capacity) throws IOException {
      if (capacity < 0) {
        throw new IllegalArgumentException("Capacity has to be positive!");
      }

      m_Tokenizer = new StreamTokenizer(reader);
      initTokenizer();

      readHeader(capacity);
      initBuffers();
    }

    /**
     * Reads the data without header according to the specified template. The
     * data can be accessed via the <code>getData()</code> method.
     * 
     * @param reader the reader to use
     * @param template the template header
     * @param lines the lines read so far
     * @throws IOException if something goes wrong
     * @see #getData()
     */
    public ArffReader(Reader reader, Instances template, int lines)
      throws IOException {
      this(reader, template, lines, 100);

      Instance inst;
      while ((inst = readInstance(m_Data)) != null) {
        m_Data.add(inst);
      }
      ;

      compactify();
    }

    /**
     * Initializes the reader without reading the header according to the
     * specified template. The data must be read via the
     * <code>readInstance()</code> method.
     * 
     * @param reader the reader to use
     * @param template the template header
     * @param lines the lines read so far
     * @param capacity the capacity of the new dataset
     * @throws IOException if something goes wrong
     * @see #getData()
     */
    public ArffReader(Reader reader, Instances template, int lines, int capacity)
      throws IOException {
      m_Lines = lines;
      m_Tokenizer = new StreamTokenizer(reader);
      initTokenizer();

      m_Data = new Instances(template, capacity);
      initBuffers();
    }

    /**
     * initializes the buffers for sparse instances to be read
     * 
     * @see #m_ValueBuffer
     * @see #m_IndicesBuffer
     */
    protected void initBuffers() {
      m_ValueBuffer = new double[m_Data.numAttributes()];
      m_IndicesBuffer = new int[m_Data.numAttributes()];
    }

    /**
     * compactifies the data
     */
    protected void compactify() {
      if (m_Data != null) {
        m_Data.compactify();
      }
    }

    /**
     * Throws error message with line number and last token read.
     * 
     * @param msg the error message to be thrown
     * @throws IOException containing the error message
     */
    protected void errorMessage(String msg) throws IOException {
      String str = msg + ", read " + m_Tokenizer.toString();
      if (m_Lines > 0) {
        int line = Integer.parseInt(str.replaceAll(".* line ", ""));
        str = str.replaceAll(" line .*", " line " + (m_Lines + line - 1));
      }
      throw new IOException(str);
    }

    /**
     * returns the current line number
     * 
     * @return the current line number
     */
    public int getLineNo() {
      return m_Lines + m_Tokenizer.lineno();
    }

    /**
     * Gets next token, skipping empty lines.
     * 
     * @throws IOException if reading the next token fails
     */
    protected void getFirstToken() throws IOException {
      while (m_Tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
      }
      ;

      if ((m_Tokenizer.ttype == '\'') ||
        (m_Tokenizer.ttype == '"')) {
        m_Tokenizer.ttype = StreamTokenizer.TT_WORD;
      } else if ((m_Tokenizer.ttype == StreamTokenizer.TT_WORD) &&
        (m_Tokenizer.sval.equals("?"))) {
        m_Tokenizer.ttype = '?';
      }
    }

    /**
     * Gets index, checking for a premature and of line.
     * 
     * @throws IOException if it finds a premature end of line
     */
    protected void getIndex() throws IOException {
      if (m_Tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
        errorMessage("premature end of line");
      }
      if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
        errorMessage("premature end of file");
      }
    }

    /**
     * Gets token and checks if its end of line.
     * 
     * @param endOfFileOk whether EOF is OK
     * @throws IOException if it doesn't find an end of line
     */
    protected void getLastToken(boolean endOfFileOk) throws IOException {
      if ((m_Tokenizer.nextToken() != StreamTokenizer.TT_EOL) &&
        ((m_Tokenizer.ttype != StreamTokenizer.TT_EOF) || !endOfFileOk)) {
        errorMessage("end of line expected");
      }
    }

    /**
     * Gets the value of an instance's weight (if one exists)
     * 
     * @return the value of the instance's weight, or NaN if no weight has been
     *         supplied in the file
     */
    protected double getInstanceWeight() throws IOException {
      double weight = Double.NaN;
      m_Tokenizer.nextToken();
      if (m_Tokenizer.ttype == StreamTokenizer.TT_EOL ||
        m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
        return weight;
      }
      // see if we can read an instance weight
      // m_Tokenizer.pushBack();
      if (m_Tokenizer.ttype == '{') {
        m_Tokenizer.nextToken();
        String weightS = m_Tokenizer.sval;
        // try to parse weight as a double
        try {
          weight = Double.parseDouble(weightS);
        } catch (NumberFormatException e) {
          // quietly ignore
          return weight;
        }
        // see if we have the closing brace
        m_Tokenizer.nextToken();
        if (m_Tokenizer.ttype != '}') {
          errorMessage("Problem reading instance weight");
        }
      }
      return weight;
    }

    /**
     * Gets next token, checking for a premature and of line.
     * 
     * @throws IOException if it finds a premature end of line
     */
    protected void getNextToken() throws IOException {
      if (m_Tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
        errorMessage("premature end of line");
      }
      if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
        errorMessage("premature end of file");
      } else if ((m_Tokenizer.ttype == '\'') ||
        (m_Tokenizer.ttype == '"')) {
        m_Tokenizer.ttype = StreamTokenizer.TT_WORD;
      } else if ((m_Tokenizer.ttype == StreamTokenizer.TT_WORD) &&
        (m_Tokenizer.sval.equals("?"))) {
        m_Tokenizer.ttype = '?';
      }
    }

    /**
     * Initializes the StreamTokenizer used for reading the ARFF file.
     */
    protected void initTokenizer() {
      m_Tokenizer.resetSyntax();
      m_Tokenizer.whitespaceChars(0, ' ');
      m_Tokenizer.wordChars(' ' + 1, '\u00FF');
      m_Tokenizer.whitespaceChars(',', ',');
      m_Tokenizer.commentChar('%');
      m_Tokenizer.quoteChar('"');
      m_Tokenizer.quoteChar('\'');
      m_Tokenizer.ordinaryChar('{');
      m_Tokenizer.ordinaryChar('}');
      m_Tokenizer.eolIsSignificant(true);
    }

    /**
     * Reads a single instance using the tokenizer and returns it.
     * 
     * @param structure the dataset header information, will get updated in case
     *          of string or relational attributes
     * @return null if end of file has been reached
     * @throws IOException if the information is not read successfully
     */
    public Instance readInstance(Instances structure) throws IOException {
      return readInstance(structure, true);
    }

    /**
     * Reads a single instance using the tokenizer and returns it.
     * 
     * @param structure the dataset header information, will get updated in case
     *          of string or relational attributes
     * @param flag if method should test for carriage return after each instance
     * @return null if end of file has been reached
     * @throws IOException if the information is not read successfully
     */
    public Instance readInstance(Instances structure, boolean flag)
      throws IOException {
      return getInstance(structure, flag);
    }

    /**
     * Reads a single instance using the tokenizer and returns it.
     * 
     * @param structure the dataset header information, will get updated in case
     *          of string or relational attributes
     * @param flag if method should test for carriage return after each instance
     * @return null if end of file has been reached
     * @throws IOException if the information is not read successfully
     */
    protected Instance getInstance(Instances structure, boolean flag)
      throws IOException {
      m_Data = structure;

      // Check if any attributes have been declared.
      if (m_Data.numAttributes() == 0) {
        errorMessage("no header information available");
      }

      // Check if end of file reached.
      getFirstToken();
      if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
        return null;
      }

      // Parse instance
      if (m_Tokenizer.ttype == '{') {
    	  throw new Error("Sparse Instances have been deleted!");
      } else {
        return getInstanceFull(flag);
      }
    }

    /**
     * Reads a single instance using the tokenizer and returns it.
     * 
     * @param flag if method should test for carriage return after each instance
     * @return null if end of file has been reached
     * @throws IOException if the information is not read successfully
     */
    protected Instance getInstanceFull(boolean flag) throws IOException {
      double[] instance = new double[m_Data.numAttributes()];
      int index;

      // Get values for all attributes.
      for (int i = 0; i < m_Data.numAttributes(); i++) {
        // Get next token
        if (i > 0) {
          getNextToken();
        }

        // Check if value is missing.
        if (m_Tokenizer.ttype == '?') {
          instance[i] = Instance.missingValue();
        } else {

          // Check if token is valid.
          if (m_Tokenizer.ttype != StreamTokenizer.TT_WORD) {
            errorMessage("not a valid value");
          }
                    
          switch (m_Data.attribute(i).type()) {
          case Attribute.NOMINAL:
            // Check if value appears in header.
            index = m_Data.attribute(i).indexOfValue(m_Tokenizer.sval);
            if (index == -1) {
              errorMessage("nominal value not declared in header");
            }
            instance[i] = index;
            break;
          case Attribute.NUMERIC:
            // Check if value is really a number.
            try {
              instance[i] = Double.valueOf(m_Tokenizer.sval).
                doubleValue();
            } catch (NumberFormatException e) {
              errorMessage("number expected");
            }
            break;
          default:
            errorMessage("unknown attribute type in column " + i);
          }
        }
      }

      double weight = 1.0;
      if (flag) {
        // check for an instance weight
        weight = getInstanceWeight();
        if (!Double.isNaN(weight)) {
          getLastToken(true);
        } else {
          weight = 1.0;
        }
      }

      // Add instance to dataset
      Instance inst = new Instance(weight, instance);
      inst.setDataset(m_Data);

      return inst;
    }

    /**
     * Reads and stores header of an ARFF file.
     * 
     * @param capacity the number of instances to reserve in the data structure
     * @throws IOException if the information is not read successfully
     */
    protected void readHeader(int capacity) throws IOException {
      m_Lines = 0;
      String relationName = "";

      // Get name of relation.
      getFirstToken();
      if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
        errorMessage("premature end of file");
      }
      if ("@relation".equalsIgnoreCase(m_Tokenizer.sval)) {
        getNextToken();
        relationName = m_Tokenizer.sval;
        getLastToken(false);
      } else {
        errorMessage("keyword " + "@relation" + " expected");
      }

      // Create vectors to hold information temporarily.
      Vector attributes = new Vector();

      // Get attribute declarations.
      getFirstToken();
      if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
        errorMessage("premature end of file");
      }

      while ("@attribute".equalsIgnoreCase(m_Tokenizer.sval)) {
        attributes = parseAttribute(attributes);
      }

      // Check if data part follows. We can't easily check for EOL.
      if (!"@data".equalsIgnoreCase(m_Tokenizer.sval)) {
        errorMessage("keyword " + "@data" + " expected");
      }

      // Check if any attributes have been declared.
      if (attributes.size() == 0) {
        errorMessage("no attributes declared");
      }

      m_Data = new Instances(relationName, attributes, capacity);
    }

    /**
     * Parses the attribute declaration.
     * 
     * @param attributes the current attributes vector
     * @return the new attributes vector
     * @throws IOException if the information is not read successfully
     */
    protected Vector parseAttribute(Vector attributes)
      throws IOException {
      String attributeName;
      Vector attributeValues;

      // Get attribute name.
      getNextToken();
      attributeName = m_Tokenizer.sval;
      getNextToken();

      // Check if attribute is nominal.
      if (m_Tokenizer.ttype == StreamTokenizer.TT_WORD) {
        // Attribute is real, integer, or string.
        if (m_Tokenizer.sval.equalsIgnoreCase("numeric")) {
          attributes
            .addElement(new Attribute(attributeName, attributes.size()));
          readTillEOL();
        } else {
          errorMessage("no valid attribute type or invalid " +
            "enumeration");
        }
      } else {

        // Attribute is nominal.
        attributeValues = new Vector();
        m_Tokenizer.pushBack();

        // Get values for nominal attribute.
        if (m_Tokenizer.nextToken() != '{') {
          errorMessage("{ expected at beginning of enumeration");
        }
        while (m_Tokenizer.nextToken() != '}') {
          if (m_Tokenizer.ttype == StreamTokenizer.TT_EOL) {
            errorMessage("} expected at end of enumeration");
          } else {
            attributeValues.addElement(m_Tokenizer.sval);
          }
        }
        attributes.
          addElement(new Attribute(attributeName, attributeValues,
            attributes.size()));
      }
      getLastToken(false);
      getFirstToken();
      if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
        errorMessage("premature end of file");
      }

      return attributes;
    }

    /**
     * Reads and skips all tokens before next end of line token.
     * 
     * @throws IOException in case something goes wrong
     */
    protected void readTillEOL() throws IOException {
      while (m_Tokenizer.nextToken() != StreamTokenizer.TT_EOL) {
      }
      ;

      m_Tokenizer.pushBack();
    }

    /**
     * Returns the header format
     * 
     * @return the header format
     */
    public Instances getStructure() {
      return new Instances(m_Data, 0);
    }

    /**
     * Returns the data that was read
     * 
     * @return the data
     */
    public Instances getData() {
      return m_Data;
    }
  }
  
  /**
   * Resets the Loader object and sets the source of the data set to be 
   * the supplied File object.
   *
   * @param file 		the source file.
   * @throws IOException 	if an error occurs
   */
  public void setSource(File file) throws IOException {
    m_structure = null;
    
      // set the source only if the file exists
      if (file.exists()) {
    	  m_sourceReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
      }
      else
      {
    	  throw new IOException("No File");
      }
  }

  /**
   * Determines and returns (if possible) the structure (internally the header)
   * of the data set as an empty set of instances.
   * 
   * @return the structure of the data set as an empty set of Instances
   * @throws IOException if an error occurs
   */
  public Instances getStructure() throws IOException {

    if (m_structure == null) {
      if (m_sourceReader == null) {
        throw new IOException("No source has been specified");
      }
      try {
        m_ArffReader = new ArffReader(m_sourceReader, 1);
        m_structure = m_ArffReader.getStructure();
      } catch (Exception ex) {
        throw new IOException("Unable to determine structure as arff (Reason: "
          + ex.toString() + ").");
      }
    }

    return new Instances(m_structure, 0);
  }

  /**
   * Return the full data set. If the structure hasn't yet been determined by a
   * call to getStructure then method should do so before processing the rest of
   * the data set.
   * 
   * @return the structure of the data set as an empty set of Instances
   * @throws IOException if there is no source or parsing fails
   */
  
  public Instances getDataSet() throws IOException {

    Instances insts = null;
    try {
      if (m_sourceReader == null) {
        throw new IOException("No source has been specified");
      }

      if (m_structure == null) {
        getStructure();
      }

      // Read all instances
      Instance inst;
      insts = new Instances(m_structure, 0);
      while ((inst = m_ArffReader.readInstance(m_structure)) != null) {
        insts.add(inst);
      }

      // Instances readIn = new Instances(m_structure);
    } finally {
      if (m_sourceReader != null) {
        // close the stream
        m_sourceReader.close();
      }
    }

    return insts;
  }
}
