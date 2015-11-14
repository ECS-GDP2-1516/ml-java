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
 *    Utils.java
 *    Copyright (C) 1999-2004 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.text.BreakIterator;
import java.util.Properties;
import java.util.Random;


/**
 * Class implementing some simple utility methods.
 * 
 * @author Eibe Frank
 * @author Yong Wang
 * @author Len Trigg
 * @author Julien Prados
 * @version $Revision: 10570 $
 */
public final class Utils {

  /** The natural logarithm of 2. */
  public static double log2 = Math.log(2);

  /** The small deviation allowed in double comparisons. */
  public static double SMALL = 1e-6;


  /**
   * Removes all occurrences of a string from another string.
   * 
   * @param inString the string to remove substrings from.
   * @param substring the substring to remove.
   * @return the input string with occurrences of substring removed.
   */
  public static String removeSubstring(String inString, String substring) {

    StringBuffer result = new StringBuffer();
    int oldLoc = 0, loc = 0;
    while ((loc = inString.indexOf(substring, oldLoc)) != -1) {
      result.append(inString.substring(oldLoc, loc));
      oldLoc = loc + substring.length();
    }
    result.append(inString.substring(oldLoc));
    return result.toString();
  }

  /**
   * Replaces with a new string, all occurrences of a string from another
   * string.
   * 
   * @param inString the string to replace substrings in.
   * @param subString the substring to replace.
   * @param replaceString the replacement substring
   * @return the input string with occurrences of substring replaced.
   */
  public static String replaceSubstring(String inString, String subString,
    String replaceString) {

    StringBuffer result = new StringBuffer();
    int oldLoc = 0, loc = 0;
    while ((loc = inString.indexOf(subString, oldLoc)) != -1) {
      result.append(inString.substring(oldLoc, loc));
      result.append(replaceString);
      oldLoc = loc + subString.length();
    }
    result.append(inString.substring(oldLoc));
    return result.toString();
  }

  /**
   * Pads a string to a specified length, inserting spaces on the left as
   * required. If the string is too long, characters are removed (from the
   * right).
   * 
   * @param inString the input string
   * @param length the desired length of the output string
   * @return the output string
   */
  public static String padLeft(String inString, int length) {

    return fixStringLength(inString, length, false);
  }

  /**
   * Pads a string to a specified length, inserting spaces on the right as
   * required. If the string is too long, characters are removed (from the
   * right).
   * 
   * @param inString the input string
   * @param length the desired length of the output string
   * @return the output string
   */
  public static String padRight(String inString, int length) {

    return fixStringLength(inString, length, true);
  }

  /**
   * Pads a string to a specified length, inserting spaces as required. If the
   * string is too long, characters are removed (from the right).
   * 
   * @param inString the input string
   * @param length the desired length of the output string
   * @param right true if inserted spaces should be added to the right
   * @return the output string
   */
  private static/* @pure@ */String fixStringLength(String inString, int length,
    boolean right) {

    if (inString.length() < length) {
      while (inString.length() < length) {
        inString = (right ? inString.concat(" ") : " ".concat(inString));
      }
    } else if (inString.length() > length) {
      inString = inString.substring(0, length);
    }
    return inString;
  }

  /**
   * Rounds a double and converts it into String.
   * 
   * @param value the double value
   * @param afterDecimalPoint the (maximum) number of digits permitted after the
   *          decimal point
   * @return the double as a formatted string
   */
  public static/* @pure@ */String doubleToString(double value,
    int afterDecimalPoint) {

    StringBuffer stringBuffer;
    double temp;
    int dotPosition;
    long precisionValue;

    temp = value * Math.pow(10.0, afterDecimalPoint);
    if (Math.abs(temp) < Long.MAX_VALUE) {
      precisionValue = (temp > 0) ? (long) (temp + 0.5) : -(long) (Math
        .abs(temp) + 0.5);
      if (precisionValue == 0) {
        stringBuffer = new StringBuffer(String.valueOf(0));
      } else {
        stringBuffer = new StringBuffer(String.valueOf(precisionValue));
      }
      if (afterDecimalPoint == 0) {
        return stringBuffer.toString();
      }
      dotPosition = stringBuffer.length() - afterDecimalPoint;
      while (((precisionValue < 0) && (dotPosition < 1)) || (dotPosition < 0)) {
        if (precisionValue < 0) {
          stringBuffer.insert(1, '0');
        } else {
          stringBuffer.insert(0, '0');
        }
        dotPosition++;
      }
      stringBuffer.insert(dotPosition, '.');
      if ((precisionValue < 0) && (stringBuffer.charAt(1) == '.')) {
        stringBuffer.insert(1, '0');
      } else if (stringBuffer.charAt(0) == '.') {
        stringBuffer.insert(0, '0');
      }
      int currentPos = stringBuffer.length() - 1;
      while ((currentPos > dotPosition)
        && (stringBuffer.charAt(currentPos) == '0')) {
        stringBuffer.setCharAt(currentPos--, ' ');
      }
      if (stringBuffer.charAt(currentPos) == '.') {
        stringBuffer.setCharAt(currentPos, ' ');
      }

      return stringBuffer.toString().trim();
    }
    return new String("" + value);
  }

  /**
   * Rounds a double and converts it into a formatted decimal-justified String.
   * Trailing 0's are replaced with spaces.
   * 
   * @param value the double value
   * @param width the width of the string
   * @param afterDecimalPoint the number of digits after the decimal point
   * @return the double as a formatted string
   */
  public static/* @pure@ */String doubleToString(double value, int width,
    int afterDecimalPoint) {

    String tempString = doubleToString(value, afterDecimalPoint);
    char[] result;
    int dotPosition;

    if ((afterDecimalPoint >= width) || (tempString.indexOf('E') != -1)) { // Protects
                                                                           // sci
                                                                           // notation
      return tempString;
    }

    // Initialize result
    result = new char[width];
    for (int i = 0; i < result.length; i++) {
      result[i] = ' ';
    }

    if (afterDecimalPoint > 0) {
      // Get position of decimal point and insert decimal point
      dotPosition = tempString.indexOf('.');
      if (dotPosition == -1) {
        dotPosition = tempString.length();
      } else {
        result[width - afterDecimalPoint - 1] = '.';
      }
    } else {
      dotPosition = tempString.length();
    }

    int offset = width - afterDecimalPoint - dotPosition;
    if (afterDecimalPoint > 0) {
      offset--;
    }

    // Not enough room to decimal align within the supplied width
    if (offset < 0) {
      return tempString;
    }

    // Copy characters before decimal point
    for (int i = 0; i < dotPosition; i++) {
      result[offset + i] = tempString.charAt(i);
    }

    // Copy characters after decimal point
    for (int i = dotPosition + 1; i < tempString.length(); i++) {
      result[offset + i] = tempString.charAt(i);
    }

    return new String(result);
  }

  /**
   * Returns the basic class of an array class (handles multi-dimensional
   * arrays).
   * 
   * @param c the array to inspect
   * @return the class of the innermost elements
   */
  public static Class getArrayClass(Class c) {
    if (c.getComponentType().isArray()) {
      return getArrayClass(c.getComponentType());
    } else {
      return c.getComponentType();
    }
  }

  /**
   * Tests if a is equal to b.
   * 
   * @param a a double
   * @param b a double
   */
  public static/* @pure@ */boolean eq(double a, double b) {

    return (a == b) || (a - b < SMALL) && (b - a < SMALL);
  }

  /**
   * Quotes a string if it contains special characters.
   * 
   * The following rules are applied:
   * 
   * A character is backquoted version of it is one of <tt>" ' % \ \n \r \t</tt>
   * .
   * 
   * A string is enclosed within single quotes if a character has been
   * backquoted using the previous rule above or contains <tt>{ }</tt> or is
   * exactly equal to the strings <tt>, ? space or ""</tt> (empty string).
   * 
   * A quoted question mark distinguishes it from the missing value which is
   * represented as an unquoted question mark in arff files.
   * 
   * @param string the string to be quoted
   * @return the string (possibly quoted)
   * @see #unquote(String)
   */
  public static/* @pure@ */String quote(String string) {
    boolean quote = false;

    // backquote the following characters
    if ((string.indexOf('\n') != -1) || (string.indexOf('\r') != -1)
      || (string.indexOf('\'') != -1) || (string.indexOf('"') != -1)
      || (string.indexOf('\\') != -1) || (string.indexOf('\t') != -1)
      || (string.indexOf('%') != -1) || (string.indexOf('\u001E') != -1)) {
      string = backQuoteChars(string);
      quote = true;
    }

    // Enclose the string in 's if the string contains a recently added
    // backquote or contains one of the following characters.
    if ((quote == true) || (string.indexOf('{') != -1)
      || (string.indexOf('}') != -1) || (string.indexOf(',') != -1)
      || (string.equals("?")) || (string.indexOf(' ') != -1)
      || (string.equals(""))) {
      string = ("'".concat(string)).concat("'");
    }

    return string;
  }

  /**
   * Converts carriage returns and new lines in a string into \r and \n.
   * Backquotes the following characters: ` " \ \t and %
   * 
   * @param string the string
   * @return the converted string
   * @see #unbackQuoteChars(String)
   */
  public static/* @pure@ */String backQuoteChars(String string) {

    int index;
    StringBuffer newStringBuffer;

    // replace each of the following characters with the backquoted version
    char charsFind[] = { '\\', '\'', '\t', '\n', '\r', '"', '%', '\u001E' };
    String charsReplace[] = { "\\\\", "\\'", "\\t", "\\n", "\\r", "\\\"",
      "\\%", "\\u001E" };
    for (int i = 0; i < charsFind.length; i++) {
      if (string.indexOf(charsFind[i]) != -1) {
        newStringBuffer = new StringBuffer();
        while ((index = string.indexOf(charsFind[i])) != -1) {
          if (index > 0) {
            newStringBuffer.append(string.substring(0, index));
          }
          newStringBuffer.append(charsReplace[i]);
          if ((index + 1) < string.length()) {
            string = string.substring(index + 1);
          } else {
            string = "";
          }
        }
        newStringBuffer.append(string);
        string = newStringBuffer.toString();
      }
    }

    return string;
  }

  /**
   * The inverse operation of backQuoteChars(). Converts back-quoted carriage
   * returns and new lines in a string to the corresponding character ('\r' and
   * '\n'). Also "un"-back-quotes the following characters: ` " \ \t and %
   * 
   * @param string the string
   * @return the converted string
   * @see #backQuoteChars(String)
   */
  public static String unbackQuoteChars(String string) {

    int index;
    StringBuffer newStringBuffer;

    // replace each of the following characters with the backquoted version
    String charsFind[] = { "\\\\", "\\'", "\\t", "\\n", "\\r", "\\\"", "\\%",
      "\\u001E" };
    char charsReplace[] = { '\\', '\'', '\t', '\n', '\r', '"', '%', '\u001E' };
    int pos[] = new int[charsFind.length];
    int curPos;

    String str = new String(string);
    newStringBuffer = new StringBuffer();
    while (str.length() > 0) {
      // get positions and closest character to replace
      curPos = str.length();
      index = -1;
      for (int i = 0; i < pos.length; i++) {
        pos[i] = str.indexOf(charsFind[i]);
        if ((pos[i] > -1) && (pos[i] < curPos)) {
          index = i;
          curPos = pos[i];
        }
      }

      // replace character if found, otherwise finished
      if (index == -1) {
        newStringBuffer.append(str);
        str = "";
      } else {
        newStringBuffer.append(str.substring(0, pos[index]));
        newStringBuffer.append(charsReplace[index]);
        str = str.substring(pos[index] + charsFind[index].length());
      }
    }

    return newStringBuffer.toString();
  }

  /**
   * Tests if a is smaller than b.
   * 
   * @param a a double
   * @param b a double
   */
  public static/* @pure@ */boolean sm(double a, double b) {

    return (b - a > SMALL);
  }

  /**
   * Tests if a is greater than b.
   * 
   * @param a a double
   * @param b a double
   */
  public static/* @pure@ */boolean gr(double a, double b) {

    return (a - b > SMALL);
  }

  /**
   * Returns the kth-smallest value in the array
   * 
   * @param array the array of double
   * @param k the value of k
   * @return the kth-smallest value
   */
  public static double kthSmallestValue(double[] array, int k) {

    int[] index = initialIndex(array.length);
    return array[index[select(array, index, 0, array.length - 1, k)]];
  }

  /**
   * Returns the logarithm of a for base 2.
   * 
   * @param a a double
   * @return the logarithm for base 2
   */
  public static/* @pure@ */double log2(double a) {

    return Math.log(a) / log2;
  }

  /**
   * Returns index of maximum element in a given array of doubles. First maximum
   * is returned.
   * 
   * @param doubles the array of doubles
   * @return the index of the maximum element
   */
  public static/* @pure@ */int maxIndex(double[] doubles) {

    double maximum = 0;
    int maxIndex = 0;

    for (int i = 0; i < doubles.length; i++) {
      if ((i == 0) || (doubles[i] > maximum)) {
        maxIndex = i;
        maximum = doubles[i];
      }
    }

    return maxIndex;
  }

  /**
   * Returns index of maximum element in a given array of integers. First
   * maximum is returned.
   * 
   * @param ints the array of integers
   * @return the index of the maximum element
   */
  public static/* @pure@ */int maxIndex(int[] ints) {

    int maximum = 0;
    int maxIndex = 0;

    for (int i = 0; i < ints.length; i++) {
      if ((i == 0) || (ints[i] > maximum)) {
        maxIndex = i;
        maximum = ints[i];
      }
    }

    return maxIndex;
  }

  /**
   * Normalizes the doubles in the array by their sum.
   * 
   * @param doubles the array of double
   * @exception IllegalArgumentException if sum is Zero or NaN
   */
  public static void normalize(double[] doubles) {

    double sum = 0;
    for (double d : doubles) {
      sum += d;
    }
    normalize(doubles, sum);
  }

  /**
   * Normalizes the doubles in the array using the given value.
   * 
   * @param doubles the array of double
   * @param sum the value by which the doubles are to be normalized
   * @exception IllegalArgumentException if sum is zero or NaN
   */
  public static void normalize(double[] doubles, double sum) {

    if (Double.isNaN(sum)) {
      throw new IllegalArgumentException("Can't normalize array. Sum is NaN.");
    }
    if (sum == 0) {
      // Maybe this should just be a return.
      throw new IllegalArgumentException("Can't normalize array. Sum is zero.");
    }
    for (int i = 0; i < doubles.length; i++) {
      doubles[i] /= sum;
    }
  }
  
  /**
   * Replaces all "missing values" in the given array of double values with
   * MAX_VALUE.
   * 
   * @param array the array to be modified.
   */
  public static void replaceMissingWithMAX_VALUE(double[] array) {

    for (int i = 0; i < array.length; i++) {
      if (Instance.isMissingValue(array[i])) {
        array[i] = Double.MAX_VALUE;
      }
    }
  }

  /**
   * Sorts a given array of doubles in ascending order and returns an array of
   * integers with the positions of the elements of the original array in the
   * sorted array. NOTE THESE CHANGES: the sort is no longer stable and it
   * doesn't use safe floating-point comparisons anymore. Occurrences of
   * Double.NaN are treated as Double.MAX_VALUE.
   * 
   * @param array this array is not changed by the method!
   * @return an array of integers with the positions in the sorted array.
   */
  public static/* @pure@ */int[] sort(/* @non_null@ */double[] array) {

    int[] index = initialIndex(array.length);
    if (array.length > 1) {
      array = array.clone();
      replaceMissingWithMAX_VALUE(array);
      quickSort(array, index, 0, array.length - 1);
    }
    return index;
  }

  /**
   * Sorts a given array of doubles in ascending order and returns an array of
   * integers with the positions of the elements of the original array in the
   * sorted array. Missing values in the given array are replaced by
   * Double.MAX_VALUE, so the array is modified in that case!
   * 
   * @param array the array to be sorted, which is modified if it has missing
   *          values
   * @return an array of integers with the positions in the sorted array.
   */
  public static/* @pure@ */int[] sortWithNoMissingValues(
  /* @non_null@ */double[] array) {

    int[] index = initialIndex(array.length);
    if (array.length > 1) {
      quickSort(array, index, 0, array.length - 1);
    }
    return index;
  }

  /**
   * Initial index, filled with values from 0 to size - 1.
   */
  private static int[] initialIndex(int size) {

    int[] index = new int[size];
    for (int i = 0; i < size; i++) {
      index[i] = i;
    }
    return index;
  }

  /**
   * Sorts left, right, and center elements only, returns resulting center as
   * pivot.
   */
  private static int sortLeftRightAndCenter(double[] array, int[] index, int l,
    int r) {

    int c = (l + r) / 2;
    conditionalSwap(array, index, l, c);
    conditionalSwap(array, index, l, r);
    conditionalSwap(array, index, c, r);
    return c;
  }

  /**
   * Swaps two elements in the given integer array.
   */
  private static void swap(int[] index, int l, int r) {

    int help = index[l];
    index[l] = index[r];
    index[r] = help;
  }

  /**
   * Conditional swap for quick sort.
   */
  private static void conditionalSwap(double[] array, int[] index, int left,
    int right) {

    if (array[index[left]] > array[index[right]]) {
      int help = index[left];
      index[left] = index[right];
      index[right] = help;
    }
  }

  /**
   * Partitions the instances around a pivot. Used by quicksort and
   * kthSmallestValue.
   * 
   * @param array the array of doubles to be sorted
   * @param index the index into the array of doubles
   * @param l the first index of the subset
   * @param r the last index of the subset
   * 
   * @return the index of the middle element
   */
  private static int partition(double[] array, int[] index, int l, int r,
    double pivot) {

    r--;
    while (true) {
      while ((array[index[++l]] < pivot)) {
        ;
      }
      while ((array[index[--r]] > pivot)) {
        ;
      }
      if (l >= r) {
        return l;
      }
      swap(index, l, r);
    }
  }

  /**
   * Partitions the instances around a pivot. Used by quicksort and
   * kthSmallestValue.
   * 
   * @param array the array of integers to be sorted
   * @param index the index into the array of integers
   * @param l the first index of the subset
   * @param r the last index of the subset
   * 
   * @return the index of the middle element
   */
  private static int partition(int[] array, int[] index, int l, int r) {

    double pivot = array[index[(l + r) / 2]];
    int help;

    while (l < r) {
      while ((array[index[l]] < pivot) && (l < r)) {
        l++;
      }
      while ((array[index[r]] > pivot) && (l < r)) {
        r--;
      }
      if (l < r) {
        help = index[l];
        index[l] = index[r];
        index[r] = help;
        l++;
        r--;
      }
    }
    if ((l == r) && (array[index[r]] > pivot)) {
      r--;
    }

    return r;
  }

  /**
   * Implements quicksort with median-of-three method and explicit sort for
   * problems of size three or less.
   * 
   * @param array the array of doubles to be sorted
   * @param index the index into the array of doubles
   * @param left the first index of the subset to be sorted
   * @param right the last index of the subset to be sorted
   */
  // @ requires 0 <= first && first <= right && right < array.length;
  // @ requires (\forall int i; 0 <= i && i < index.length; 0 <= index[i] &&
  // index[i] < array.length);
  // @ requires array != index;
  // assignable index;
  private static void quickSort(/* @non_null@ */double[] array, /* @non_null@ */
    int[] index, int left, int right) {

    int diff = right - left;

    switch (diff) {
    case 0:

      // No need to do anything
      return;
    case 1:

      // Swap two elements if necessary
      conditionalSwap(array, index, left, right);
      return;
    case 2:

      // Just need to sort three elements
      conditionalSwap(array, index, left, left + 1);
      conditionalSwap(array, index, left, right);
      conditionalSwap(array, index, left + 1, right);
      return;
    default:

      // Establish pivot
      int pivotLocation = sortLeftRightAndCenter(array, index, left, right);

      // Move pivot to the right, partition, and restore pivot
      swap(index, pivotLocation, right - 1);
      int center = partition(array, index, left, right, array[index[right - 1]]);
      swap(index, center, right - 1);

      // Sort recursively
      quickSort(array, index, left, center - 1);
      quickSort(array, index, center + 1, right);
    }
  }

  /**
   * Implements quicksort according to Manber's "Introduction to Algorithms".
   * 
   * @param array the array of integers to be sorted
   * @param index the index into the array of integers
   * @param left the first index of the subset to be sorted
   * @param right the last index of the subset to be sorted
   */
  // @ requires 0 <= first && first <= right && right < array.length;
  // @ requires (\forall int i; 0 <= i && i < index.length; 0 <= index[i] &&
  // index[i] < array.length);
  // @ requires array != index;
  // assignable index;
  private static void quickSort(/* @non_null@ */int[] array, /* @non_null@ */
    int[] index, int left, int right) {

    if (left < right) {
      int middle = partition(array, index, left, right);
      quickSort(array, index, left, middle);
      quickSort(array, index, middle + 1, right);
    }
  }

  /**
   * Implements computation of the kth-smallest element according to Manber's
   * "Introduction to Algorithms".
   * 
   * @param array the array of double
   * @param index the index into the array of doubles
   * @param left the first index of the subset
   * @param right the last index of the subset
   * @param k the value of k
   * 
   * @return the index of the kth-smallest element
   */
  // @ requires 0 <= first && first <= right && right < array.length;
  private static int select(/* @non_null@ */double[] array, /* @non_null@ */
    int[] index, int left, int right, int k) {

    int diff = right - left;
    switch (diff) {
    case 0:

      // Nothing to be done
      return left;
    case 1:

      // Swap two elements if necessary
      conditionalSwap(array, index, left, right);
      return left + k - 1;
    case 2:

      // Just need to sort three elements
      conditionalSwap(array, index, left, left + 1);
      conditionalSwap(array, index, left, right);
      conditionalSwap(array, index, left + 1, right);
      return left + k - 1;
    default:

      // Establish pivot
      int pivotLocation = sortLeftRightAndCenter(array, index, left, right);

      // Move pivot to the right, partition, and restore pivot
      swap(index, pivotLocation, right - 1);
      int center = partition(array, index, left, right, array[index[right - 1]]);
      swap(index, center, right - 1);

      // Proceed recursively
      if ((center - left + 1) >= k) {
        return select(array, index, left, center, k);
      } else {
        return select(array, index, center + 1, right, k - (center - left + 1));
      }
    }
  }

  /**
   * Converts a File's absolute path to a path relative to the user (ie start)
   * directory. Includes an additional workaround for Cygwin, which doesn't like
   * upper case drive letters.
   * 
   * @param absolute the File to convert to relative path
   * @return a File with a path that is relative to the user's directory
   * @exception Exception if the path cannot be constructed
   */
  public static File convertToRelativePath(File absolute) throws Exception {
    File result;
    String fileStr;

    result = null;

    // if we're running windows, it could be Cygwin
    if (File.separator.equals("\\")) {
      // Cygwin doesn't like upper case drives -> try lower case drive
      try {
        fileStr = absolute.getPath();
        fileStr = fileStr.substring(0, 1).toLowerCase() + fileStr.substring(1);
        result = createRelativePath(new File(fileStr));
      } catch (Exception e) {
        // no luck with Cygwin workaround, convert it like it is
        result = createRelativePath(absolute);
      }
    } else {
      result = createRelativePath(absolute);
    }

    return result;
  }

  /**
   * Converts a File's absolute path to a path relative to the user (ie start)
   * directory.
   * 
   * @param absolute the File to convert to relative path
   * @return a File with a path that is relative to the user's directory
   * @exception Exception if the path cannot be constructed
   */
  protected static File createRelativePath(File absolute) throws Exception {
    File userDir = new File(System.getProperty("user.dir"));
    String userPath = userDir.getAbsolutePath() + File.separator;
    String targetPath = (new File(absolute.getParent())).getPath()
      + File.separator;
    String fileName = absolute.getName();
    StringBuffer relativePath = new StringBuffer();
    // relativePath.append("."+File.separator);
    // System.err.println("User dir "+userPath);
    // System.err.println("Target path "+targetPath);

    // file is in user dir (or subdir)
    int subdir = targetPath.indexOf(userPath);
    if (subdir == 0) {
      if (userPath.length() == targetPath.length()) {
        relativePath.append(fileName);
      } else {
        int ll = userPath.length();
        relativePath.append(targetPath.substring(ll));
        relativePath.append(fileName);
      }
    } else {
      int sepCount = 0;
      String temp = new String(userPath);
      while (temp.indexOf(File.separator) != -1) {
        int ind = temp.indexOf(File.separator);
        sepCount++;
        temp = temp.substring(ind + 1, temp.length());
      }

      String targetTemp = new String(targetPath);
      String userTemp = new String(userPath);
      int tcount = 0;
      while (targetTemp.indexOf(File.separator) != -1) {
        int ind = targetTemp.indexOf(File.separator);
        int ind2 = userTemp.indexOf(File.separator);
        String tpart = targetTemp.substring(0, ind + 1);
        String upart = userTemp.substring(0, ind2 + 1);
        if (tpart.compareTo(upart) != 0) {
          if (tcount == 0) {
            tcount = -1;
          }
          break;
        }
        tcount++;
        targetTemp = targetTemp.substring(ind + 1, targetTemp.length());
        userTemp = userTemp.substring(ind2 + 1, userTemp.length());
      }
      if (tcount == -1) {
        // then target file is probably on another drive (under windows)
        throw new Exception("Can't construct a path to file relative to user "
          + "dir.");
      }
      if (targetTemp.indexOf(File.separator) == -1) {
        targetTemp = "";
      }
      for (int i = 0; i < sepCount - tcount; i++) {
        relativePath.append(".." + File.separator);
      }
      relativePath.append(targetTemp + fileName);
    }
    // System.err.println("new path : "+relativePath.toString());
    return new File(relativePath.toString());
  }

  /**
   * Implements computation of the kth-smallest element according to Manber's
   * "Introduction to Algorithms".
   * 
   * @param array the array of integers
   * @param index the index into the array of integers
   * @param left the first index of the subset
   * @param right the last index of the subset
   * @param k the value of k
   * 
   * @return the index of the kth-smallest element
   */
  // @ requires 0 <= first && first <= right && right < array.length;
  private static int select(/* @non_null@ */int[] array, /* @non_null@ */
    int[] index, int left, int right, int k) {

    if (left == right) {
      return left;
    } else {
      int middle = partition(array, index, left, right);
      if ((middle - left + 1) >= k) {
        return select(array, index, left, middle, k);
      } else {
        return select(array, index, middle + 1, right, k - (middle - left + 1));
      }
    }
  }
}
