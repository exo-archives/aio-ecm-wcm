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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion.config;

import java.util.Calendar;


/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Mar 4, 2009  
 */
public class VersionData {
  protected String UUID;  
  protected String versionName;
  protected String state;
  protected String author;
  protected Calendar startPublicationDate;
  protected Calendar endPublicationDate;

  public VersionData(String uuid, String state, String author, Calendar startPublicationDate, Calendar endPublicationDate) {
    this.UUID = uuid;
    this.state = state;
    this.author = author;
    this.startPublicationDate = startPublicationDate;
    this.endPublicationDate = endPublicationDate;
  }
  
  public VersionData(String uuid, String state, String author) {
    this.UUID = uuid;
    this.state = state;
    this.author = author;
    this.startPublicationDate = null;
    this.endPublicationDate = null;
  }
  
  public String getVersionName() {
    return versionName;
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }

  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }        

  public String getAuthor() {
    return author;
  }
  public void setAuthor(String author) {
    this.author = author;
  }    

  public Calendar getStartPublicationDate() {
    return startPublicationDate;
  }
  public void setStartPublicationDate(Calendar startPublicationDate) {
    this.startPublicationDate = startPublicationDate;
  }
  public Calendar getEndPublicationDate() {
    return endPublicationDate;
  }
  public void setEndPublicationDate(Calendar endPublicationDate) {
    this.endPublicationDate = endPublicationDate;
  }

  public String[] toStringValues() {
    return new String[] { versionName, state, author} ;
  }
  
  public String toStringValue() {
    StringBuilder builder = new StringBuilder();
    builder.append(UUID).append(",").append(state).append(",").append(author);           
    return builder.toString();
  }
  public static VersionData toVersionData(String s) {
    String[] info = s.split(",");
    return new VersionData(info[0],info[1],info[2]);
  }

  public String getUUID() {
    return UUID;
  }

  public void setUUID(String uuid) {
    UUID = uuid;
  }  
  
}
