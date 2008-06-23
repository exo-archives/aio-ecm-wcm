/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.connector.fckeditor;

/*
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 *          anh.do@exoplatform.com
 * Jun 19, 2008  
 */
public class ErrorMessage {
  
  public String getErrorMessage(int errorNumber) throws Exception {
    String message = "";
    switch (errorNumber) {
    case 0:
      message = "No Errors Found. The folder has been created.";
      break;
    case 101:
      message = "Folder already exists.";
      break;
    case 102:
      message = "Invalid folder name.";
      break;
    case 103:
      message = "You have no permissions to create the folder.";
      break;    
    default:
      message = "Unknown error creating folder.";
      break;
    }
    return message;
  } 
  
}
