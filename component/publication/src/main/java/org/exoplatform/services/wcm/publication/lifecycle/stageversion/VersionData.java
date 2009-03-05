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
 * Mar 4, 2009  
 */
public class VersionData {
  protected String UUID;
  protected String state;
  protected String author;
  protected String startPublicationDate;
  protected String endPublicationDate;
  
  public String getUUID() {
    return UUID;
  }
  public void setUUID(String uuid) {
    this.UUID = uuid;
  }
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }
  
  public String getStartPublicationDate() {
    return startPublicationDate;
  }
  public void setStartPublicationDate(String startPublicationDate) {
    this.startPublicationDate = startPublicationDate;
  }
  public String getEndPublicationDate() {
    return endPublicationDate;
  }
  public void setEndPublicationDate(String endPublicationDate) {
    this.endPublicationDate = endPublicationDate;
  }
  
  public String[] toStringArray() {
    return new String[] { UUID, state, author, startPublicationDate, endPublicationDate} ;
  }
  
  public String getAuthor() {
    return author;
  }
  public void setAuthor(String author) {
    this.author = author;
  }
  
  public static VersionData parse(String[] versionDataString) {
    VersionData versionData = new VersionData();
    versionData.setUUID(versionDataString[0]);
    versionData.setState(versionDataString[1]);
    versionData.setAuthor(versionDataString[2]);
    versionData.setStartPublicationDate(versionDataString[3]);
    versionData.setEndPublicationDate(versionDataString[4]);
    return versionData;
  }
  
}
