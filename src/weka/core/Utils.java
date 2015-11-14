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
}
