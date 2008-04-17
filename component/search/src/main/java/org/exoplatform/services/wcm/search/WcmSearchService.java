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

import javax.jcr.Node;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Mar 19, 2008  
 */
public interface WcmSearchService {      
  public PageList searchWebContent(String keyword, String portalName,String repository,String worksapce, boolean documentSeach, boolean pageSearch,SessionProvider sessionProvider) throws Exception ;  
  public PageList searchWebContent(String keyword,String portalName, boolean documentSearch,boolean pageSearch,SessionProvider sessionProvider) throws Exception ;
  public void updatePagesCache() throws Exception ;
  public Node getAssociatedDocument(PageNode pageNode,SessionProvider sessionProvider) throws Exception ;
}
