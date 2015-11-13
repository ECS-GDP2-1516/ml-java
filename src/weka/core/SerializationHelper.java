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
 * SerializationHelper.java
 * Copyright (C) 2007 University of Waikato, Hamilton, New Zealand
 */

package weka.core;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * A helper class for determining serialVersionUIDs and checking whether
 * classes contain one and/or need one. One can also serialize and deserialize
 * objects to and fro files or streams.
 * 
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 8597 $
 */
public class SerializationHelper {

  /**
   * deserializes from the given stream and returns the object from it.
   * 
   * @param stream	the stream to deserialize from
   * @return		the deserialized object
   * @throws Exception	if deserialization fails
   */
  public static Object read(String filename) throws Exception {
    
	  InputStream stream = new FileInputStream(filename);
	  ObjectInputStream 	ois;
    Object		result;
    
    if (!(stream instanceof BufferedInputStream))
      stream = new BufferedInputStream(stream);
    
    ois    = new ObjectInputStream(stream);
    result = ois.readObject();
    ois.close();
    
    return result;
  }
}
