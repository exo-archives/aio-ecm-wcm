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
package org.exoplatform.services.wcm.search;

import java.util.Calendar;


/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Oct 7, 2008  
 */
/*
 * This is query criteria for SiteSearch service. Base on search criteria, SiteSearch service 
 * can easy create query statement to search.
 * */
public class QueryCriteria {  
  private String siteName;
  private String[] categories;
  private String[] tags;
  private Calendar startPublicationDate;
  private Calendar endPublicationDate;
  private String[] authors;  
  private String[] contentTypes;
  private String[] mimeTypes;
  private Calendar createdDate;
  private Calendar lastModifiedDate;
  private boolean searchWebpage = true;
  private boolean searchDocument = true;
  private String keyword;  
  public String getSiteName() { return siteName; }
  public void setSiteName(String siteName) { this.siteName = siteName; }
  
  public String[] getCategories() { return categories; }
  public void setCategories(String[] categories) { this.categories = categories; }
  
  public String[] getTags() { return tags; }
  public void setTags(String[] tags) { this.tags = tags; }

  public Calendar getStartPublicationDate() { return startPublicationDate; }

  public void setStartPublicationDate(Calendar startPublicationDate) { 
    this.startPublicationDate = startPublicationDate; 
  }

  public Calendar getEndPublicationDate() {
    return endPublicationDate;
  }

  public void setEndPublicationDate(Calendar endPublicationDate) {
    this.endPublicationDate = endPublicationDate;
  }

  public String[] getAuthors() { return authors; }
  public void setAuthors(String[] authors) { this.authors = authors; }
  public String[] getContentTypes() { return contentTypes; }
  public void setContentTypes(String[] contentTypes) { this.contentTypes = contentTypes; }

  public String[] getMimeTypes() { return mimeTypes; }
  public void setMimeTypes(String[] mimeTypes) { this.mimeTypes = mimeTypes; }

  public Calendar getCreatedDate() { return createdDate; }
  public void setCreatedDate(Calendar createdDate) { this.createdDate = createdDate; }

  public Calendar getLastModifiedDate() { return lastModifiedDate; }
  public void setLastModifiedDate(Calendar lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }
  
  public String getKeyword() { return this.keyword; }
  public void setKeyword(String s) { this.keyword = s; }
  
  public boolean isSearchWebpage() { return searchWebpage;}
  public void setSearchWebpage(boolean searchWebpage) { 
    this.searchWebpage = searchWebpage;
  }
  
  public boolean isSearchDocument() { return searchDocument;}
  public void setSearchDocument(boolean searchDocument) {
    this.searchDocument = searchDocument;
  }
}
