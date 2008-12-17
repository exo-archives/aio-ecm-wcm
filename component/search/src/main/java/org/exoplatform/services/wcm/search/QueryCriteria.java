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
 * hoa.pham@exoplatform.com
 * Oct 7, 2008
 */
/*
 * This is query criteria for SiteSearch service. Base on search criteria, SiteSearch service 
 * can easy create query statement to search.
 * */
public class QueryCriteria {  
  
  /** The site name. */
  private String siteName;
  
  /** The categories. */
  private String[] categories;
  
  /** The tags. */
  private String[] tags;
  
  /** The start publication date. */
  private Calendar startPublicationDate;
  
  /** The end publication date. */
  private Calendar endPublicationDate;
  
  /** The authors. */
  private String[] authors;  
  
  /** The content types. */
  private String[] contentTypes;
  
  /** The mime types. */
  private String[] mimeTypes;
  
  /** The created date. */
  private Calendar createdDate;
  
  /** The last modified date. */
  private Calendar lastModifiedDate;
  
  /** The search webpage. */
  private boolean searchWebpage = true;
  
  /** The search document. */
  private boolean searchDocument = true;
  
  /** The keyword. */
  private String keyword;
  
  /**
   * Gets the site name.
   * 
   * @return the site name
   */
  public String getSiteName() { return siteName; }
  
  /**
   * Sets the site name.
   * 
   * @param siteName the new site name
   */
  public void setSiteName(String siteName) { this.siteName = siteName; }
  
  /**
   * Gets the categories.
   * 
   * @return the categories
   */
  public String[] getCategories() { return categories; }
  
  /**
   * Sets the categories.
   * 
   * @param categories the new categories
   */
  public void setCategories(String[] categories) { this.categories = categories; }
  
  /**
   * Gets the tags.
   * 
   * @return the tags
   */
  public String[] getTags() { return tags; }
  
  /**
   * Sets the tags.
   * 
   * @param tags the new tags
   */
  public void setTags(String[] tags) { this.tags = tags; }

  /**
   * Gets the start publication date.
   * 
   * @return the start publication date
   */
  public Calendar getStartPublicationDate() { return startPublicationDate; }

  /**
   * Sets the start publication date.
   * 
   * @param startPublicationDate the new start publication date
   */
  public void setStartPublicationDate(Calendar startPublicationDate) { 
    this.startPublicationDate = startPublicationDate; 
  }

  /**
   * Gets the end publication date.
   * 
   * @return the end publication date
   */
  public Calendar getEndPublicationDate() {
    return endPublicationDate;
  }

  /**
   * Sets the end publication date.
   * 
   * @param endPublicationDate the new end publication date
   */
  public void setEndPublicationDate(Calendar endPublicationDate) {
    this.endPublicationDate = endPublicationDate;
  }

  /**
   * Gets the authors.
   * 
   * @return the authors
   */
  public String[] getAuthors() { return authors; }
  
  /**
   * Sets the authors.
   * 
   * @param authors the new authors
   */
  public void setAuthors(String[] authors) { this.authors = authors; }
  
  /**
   * Gets the content types.
   * 
   * @return the content types
   */
  public String[] getContentTypes() { return contentTypes; }
  
  /**
   * Sets the content types.
   * 
   * @param contentTypes the new content types
   */
  public void setContentTypes(String[] contentTypes) { this.contentTypes = contentTypes; }

  /**
   * Gets the mime types.
   * 
   * @return the mime types
   */
  public String[] getMimeTypes() { return mimeTypes; }
  
  /**
   * Sets the mime types.
   * 
   * @param mimeTypes the new mime types
   */
  public void setMimeTypes(String[] mimeTypes) { this.mimeTypes = mimeTypes; }

  /**
   * Gets the created date.
   * 
   * @return the created date
   */
  public Calendar getCreatedDate() { return createdDate; }
  
  /**
   * Sets the created date.
   * 
   * @param createdDate the new created date
   */
  public void setCreatedDate(Calendar createdDate) { this.createdDate = createdDate; }

  /**
   * Gets the last modified date.
   * 
   * @return the last modified date
   */
  public Calendar getLastModifiedDate() { return lastModifiedDate; }
  
  /**
   * Sets the last modified date.
   * 
   * @param lastModifiedDate the new last modified date
   */
  public void setLastModifiedDate(Calendar lastModifiedDate) {
    this.lastModifiedDate = lastModifiedDate;
  }
  
  /**
   * Gets the keyword.
   * 
   * @return the keyword
   */
  public String getKeyword() { return this.keyword; }
  
  /**
   * Sets the keyword.
   * 
   * @param s the new keyword
   */
  public void setKeyword(String s) { this.keyword = s; }
  
  /**
   * Checks if is search webpage.
   * 
   * @return true, if is search webpage
   */
  public boolean isSearchWebpage() { return searchWebpage;}
  
  /**
   * Sets the search webpage.
   * 
   * @param searchWebpage the new search webpage
   */
  public void setSearchWebpage(boolean searchWebpage) { 
    this.searchWebpage = searchWebpage;
  }
  
  /**
   * Checks if is search document.
   * 
   * @return true, if is search document
   */
  public boolean isSearchDocument() { return searchDocument;}
  
  /**
   * Sets the search document.
   * 
   * @param searchDocument the new search document
   */
  public void setSearchDocument(boolean searchDocument) {
    this.searchDocument = searchDocument;
  }
}
