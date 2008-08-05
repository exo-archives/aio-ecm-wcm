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
  
  private String strUrl;
  private String strStatus;
  
  public String getStatus() {
    return strStatus;
  }
  public void setStatus(String strStatus) {
    this.strStatus = strStatus;
  }
  public String getUrl() {
    return strUrl;
  }
  public void setUrl(String strUrl) {
    this.strUrl = strUrl;
  }
  
//  public String toString() {
//    return STATUS + strStatus + SEPARATOR + URL + strUrl;
//  }
//  
//  public LinkBean parse(String s) {
//    String[] strLinkValues = s.split(SEPARATOR);
//    LinkBean linkValue = new LinkBean();
//    linkValue.setStatus(STATUS + strLinkValues[0]);
//    linkValue.setUrl(URL + strLinkValues[1]);
//    return linkValue;
//  }
}
