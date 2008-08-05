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
package org.exoplatform.services.wcm.link;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong_phan@exoplatform.com
 * Sep 4, 2008  
 */
public class LinkBean {
  
  static final public String SEPARATOR = "@";
  static final public String STATUS = "status=";
  static final public String URL = "url=";
  
  static final public String STATUS_UNCHECKED = "unchecked";
  static final public String STATUS_ACTIVE = "active";
  static final public String STATUS_BROKEN = "broken";
  
  private String link;
  private String url;
  private String status;
  
  public LinkBean() {
    super();
  }
  
  public LinkBean(String link) {
    this.link = link;
  }
  
  public LinkBean(String url, String status) {
    this.url = url;
    this.status = status;
  }
  
  public String getStatus() {
    return status;
  }
  
  public void setStatus(String status) {
    this.status = status;
  }
  
  public String getUrl() {
    return url;
  }
  
  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getLinkUrl() {
    // link pattern: "status=xxx@url=http://xxx.com
    String[] links = link.split(SEPARATOR);
    return links[1].replaceAll(URL, "");
  }
  
  public String toPattern() {
    return STATUS + status + SEPARATOR + URL + url;
  }
  
}
