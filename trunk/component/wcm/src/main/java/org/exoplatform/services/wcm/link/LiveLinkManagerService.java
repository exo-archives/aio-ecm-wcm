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

import java.util.List;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong_phan@exoplatform.com
 * Aug 6, 2008  
 */
public interface LiveLinkManagerService {
  
  public void validateLink() throws Exception;
  public void validateLink(String portalName) throws Exception;
  
  public List<LinkBean> getBrokenLinks(String portalName) throws Exception;
  public List<String> getBrokenLinks(Node webContent) throws Exception ;
  
  public List<LinkBean> getUncheckedLinks(String portalName) throws Exception;
  public List<String> getUncheckedLinks(Node webContent) throws Exception ;
  
  public List<LinkBean> getActiveLinks(String portalName) throws Exception;
  public List<String> getActiveLinks(Node webContent) throws Exception ;
}
