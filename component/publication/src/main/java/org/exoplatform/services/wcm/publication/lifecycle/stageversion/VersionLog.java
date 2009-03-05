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

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Mar 5, 2009  
 */
public class VersionLog extends VersionData {
  
  private String logDate;
  private String description;
  
  public String getLogDate() {
    return logDate;
  }
  public void setLogDate(String logDate) {
    this.logDate = logDate;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String[] toStringValues() {
    return new String[]{logDate, UUID, state, author, description, startPublicationDate, endPublicationDate };
  }
  
  public static VersionLog toVersionLog(String[] logs) {
    VersionLog versionLog = new VersionLog();
    versionLog.setLogDate(logs[0]);
    versionLog.setUUID(logs[1]);
    versionLog.setState(logs[2]);
    versionLog.setAuthor(logs[3]);
    versionLog.setDescription(logs[4]);
    versionLog.setStartPublicationDate(logs[5]);
    versionLog.setEndPublicationDate(logs[6]);
    return versionLog;
  }
}
