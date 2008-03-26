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

package org.exoplatform.services.wcm.search.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.search.WcmSearchService;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Mar 19, 2008  
 */
public class WcmSearchServiceImpl implements WcmSearchService {
  private RepositoryService repositoryService ;
  private String defaultRepository ;
  private String defaultWorksapce ;
  private boolean useCachedResult ;

  public WcmSearchServiceImpl(RepositoryService repositoryService, InitParams initParams) {
    this.repositoryService = repositoryService ;
    PropertiesParam serviceParams = initParams.getPropertiesParam("service-params") ;
    defaultRepository = serviceParams.getProperty("defaultRepository") ;
    defaultWorksapce = serviceParams.getProperty("defaultWorksapce") ;
    useCachedResult = Boolean.parseBoolean(serviceParams.getProperty("useCachedResult")) ;
  }

  public PageList searchWebContent(String keyword, String portalName, boolean documentSearch,
      boolean pageSearch, SessionProvider sessionProvider) throws Exception {
    return searchWebContent(keyword,portalName,defaultRepository,defaultWorksapce,
        documentSearch,pageSearch,sessionProvider);
  }

  public PageList searchWebContent(String keyword, String portalName, String repository,
      String worksapce, boolean documentSeach, boolean pageSearch, SessionProvider sessionProvider) throws Exception {
    
    if(documentSeach && pageSearch) {

    }else if(documentSeach){      
      HashSet hashSet = new HashSet() ;
      QueryResult queryResult = searchDocument(keyword,portalName,repository,worksapce,sessionProvider) ;
      for(NodeIterator iterator = queryResult.getNodes();iterator.hasNext();) {
        Node node = iterator.nextNode() ;
        hashSet.add(node) ;
      }
      List<Object> document = Arrays.asList(hashSet.toArray()) ;      
      return new ObjectPageList(document,10) ;
    }
    return searchPortalPage(keyword,portalName,repository,worksapce,sessionProvider);
  }    

  private QueryResult searchDocument(String keyword,String portalName,String repository,String workspace, SessionProvider sessionProvider) throws Exception{
    ManageableRepository manageableRepository = repositoryService.getRepository(repository) ;
    Session session = sessionProvider.getSession(workspace,manageableRepository) ;
    QueryManager queryManager = session.getWorkspace().getQueryManager() ;
    String portalPath = "/Web Content/Live/"+portalName ;
    String queryStatement = "/jcr:root" + portalPath + "/element(*,'"+keyword+"') oreder by exo:dateCreated DESC" ;
    Query query = queryManager.createQuery(queryStatement,Query.XPATH) ;
    QueryResult result = query.execute() ;
    return result;    
  }

  private PageList searchPortalPage(String keyword,String portalName,String repository,String workspace, SessionProvider sessionProvider) throws Exception{
    
    return null;    
  }
}
