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

import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.pham@exoplatform.com
 * Oct 7, 2008
 */
public interface SiteSearchService {    
  
  /**
   * Search the content base on query criteria
   * 
   * @param queryCriteria the query criteria
   * @param sessionProvider the session provider
   * 
   * @return the query result
   * 
   * @throws Exception the exception
   */
  public QueryResult searchSiteContents(QueryCriteria queryCriteria, SessionProvider sessionProvider) throws Exception;
  public WCMPaginatedQueryResult searchSiteContents(QueryCriteria queryCriteria, SessionProvider sessionProvider, int pageSize) throws Exception;
}
