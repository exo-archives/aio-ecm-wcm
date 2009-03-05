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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion;

import java.util.Calendar;

import org.exoplatform.commons.utils.ISO8601;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Mar 5, 2009  
 */
public class VersionLog extends VersionData {
  private Calendar logDate;
  private String description;
  
  public VersionLog(String uuid, String state, String author, Calendar logDate, String description) {
    super(uuid, state, author, null,null);
    this.logDate = logDate;
    this.description = description;
  }    
  
  public Calendar getLogDate() {
    return logDate;
  }
  public void setLogDate(Calendar logDate) {
    this.logDate = logDate;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String[] toStringValues() {
    return new String[]{UUID, state, author, ISO8601.format(logDate), description};
  }
  
  public static VersionLog toVersionLog(String[] logs) {
    return new VersionLog(logs[0],logs[1],logs[2],ISO8601.parse(logs[3]),logs[4]);
  }
  
}
