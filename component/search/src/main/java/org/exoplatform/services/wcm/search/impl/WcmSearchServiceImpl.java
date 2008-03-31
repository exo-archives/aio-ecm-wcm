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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.SystemIdentity;
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
  private DataStorage dataStorage_ ;
  private UserACL userACL_ ;
  private UserPortalConfigService portalConfigService_ ; 

  public WcmSearchServiceImpl(RepositoryService repositoryService, DataStorage dataStorage, UserACL userACL,
      UserPortalConfigService portalConfigService, InitParams initParams) {
    this.repositoryService = repositoryService ;
    this.dataStorage_ = dataStorage ;
    this.userACL_ = userACL ;
    this.portalConfigService_ = portalConfigService;    
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
      return searchAll(keyword,portalName,repository,worksapce,sessionProvider) ;
    }else if(documentSeach){
      return searchDocument(keyword,portalName,repository,worksapce,sessionProvider) ;
    }
    return searchPortalPage(keyword,portalName,repository,worksapce,sessionProvider);
  }    

  private PageList searchAll(String keyword,String portalName,String repository,String workspace,SessionProvider sessionProvider) throws Exception {
    ManageableRepository manageableRepository = repositoryService.getRepository(repository) ;
    Session session = sessionProvider.getSession(workspace,manageableRepository) ;
    Query query = createQuery(session.getWorkspace().getQueryManager(),keyword,portalName) ;
    QueryResult queryResult = query.execute() ;
    String userId = session.getUserID();    
    if(SystemIdentity.ANONIM.equals(userId)) userId = null ;    
    HashSet<PageNode> pageNodeSet = new HashSet<PageNode>() ;    
    HashSet<Node> nodeSet = new HashSet<Node>() ;
    UserPortalConfig userPortalConfig = portalConfigService_.getUserPortalConfig(portalName,userId);       
    for(NodeIterator iter = queryResult.getNodes();iter.hasNext();) {
      Node document = iter.nextNode() ;
      if(!document.hasProperty("exo:linkedApplications")) continue ;
      boolean found = false ;
      for(Value value: document.getProperty("exo:linkedApplications").getValues()) {
        String applicationId = value.getString();
        for(PageNavigation pageNavigation:userPortalConfig.getNavigations()) {
          List<PageNode> pageNodes = filter(pageNavigation,userId,applicationId) ;
          if(!pageNodes.isEmpty()) {
            found = true ;
            pageNodeSet.addAll(filter(pageNavigation,userId,applicationId)) ;
          }          
        }        
      }
      if(!found) nodeSet.add(document) ;
    }        
    List<Object> resultList = new ArrayList<Object>() ;
    for(Iterator<PageNode> iter = pageNodeSet.iterator();iter.hasNext();) {
      resultList.add(iter.next());
    }        
    for(Iterator<PageNode> iter = pageNodeSet.iterator();iter.hasNext();) {
      resultList.add(iter.next());
    }
    for(Iterator<Node> iterator = nodeSet.iterator();iterator.hasNext();) {
      resultList.add(iterator.next());
    }
    return new ObjectPageList(resultList,10);    
  }

  private PageList searchDocument(String keyword,String portalName,String repository,String workspace, SessionProvider sessionProvider) throws Exception{
    ManageableRepository manageableRepository = repositoryService.getRepository(repository) ;
    Session session = sessionProvider.getSession(workspace,manageableRepository) ;    
    Query query = createQuery(session.getWorkspace().getQueryManager(),keyword,portalName) ;
    QueryResult result = query.execute() ;
    HashSet<Node> hashSet = new HashSet<Node>() ;    
    for(NodeIterator iterator = result.getNodes();iterator.hasNext();) {
      Node node = iterator.nextNode() ;
      hashSet.add(node) ;
    }
    List<Object> document = Arrays.asList(hashSet.toArray()) ;      
    return new ObjectPageList(document,10) ;

  }

  private Query createQuery(QueryManager queryManager,String keyword,String portalName) throws Exception{        
    String queryStatement = "/jcr:root/Web Content/Live/" + portalName + "//*[jcr:contains(.,'"+keyword+"')] order by @exo:dateCreated decending" ;    
    return queryManager.createQuery(queryStatement,Query.XPATH) ;        
  }

  private void processPageNodeRecusive(PageNode root, List<PageNode> allPages) {
    List<PageNode> list = root.getChildren();
    if(list == null || list.size()== 0) { return ; }
    for(PageNode node:list) {
      allPages.add(node);
      processPageNodeRecusive(node,allPages) ;
    }
  }

  private PageList searchPortalPage(String keyword,String portalName,String repository,String workspace, SessionProvider sessionProvider) throws Exception{
    ManageableRepository manageableRepository = repositoryService.getRepository(repository) ;
    Session session = sessionProvider.getSession(workspace,manageableRepository) ;
    Query query = createQuery(session.getWorkspace().getQueryManager(),keyword,portalName) ;
    QueryResult queryResult = query.execute() ;
    String userId = session.getUserID();    
    if(SystemIdentity.ANONIM.equals(userId)) userId = null ;    
    HashSet<PageNode> pageNodeSet = new HashSet<PageNode>() ;    
    UserPortalConfig userPortalConfig = portalConfigService_.getUserPortalConfig(portalName,userId);       
    for(NodeIterator iter = queryResult.getNodes();iter.hasNext();) {
      Node document = iter.nextNode() ;
      if(!document.hasProperty("exo:linkedApplications")) continue ;
      for(Value value: document.getProperty("exo:linkedApplications").getValues()) {
        String applicationId = value.getString();
        for(PageNavigation pageNavigation:userPortalConfig.getNavigations()) {
          pageNodeSet.addAll(filter(pageNavigation,userId,applicationId)) ; 
        }        
      }           
    }        
    List<PageNode> resultList = new ArrayList<PageNode>() ;
    for(Iterator<PageNode> iter = pageNodeSet.iterator();iter.hasNext();) {
      resultList.add(iter.next());
    }    
    return new ObjectPageList(resultList,10);    
  }

  public List<PageNode> filter(PageNavigation nav, String userName,String applicationId) throws Exception {
    List<PageNode> list = new ArrayList<PageNode>() ;    
    for(PageNode node: nav.getNodes()){
      filter(node, userName,applicationId,list);      
    }
    return list;
  }

  public PageNode filter(PageNode node, String userName,String applicationId,List<PageNode> allPageNode) throws Exception {    
    Page page = portalConfigService_.getPage(node.getPageReference(),userName) ;       
    if(!hasApplication(page,applicationId)) return null ;
    allPageNode.add(node) ;
    PageNode copyNode = node.clone();
    copyNode.setChildren(new ArrayList<PageNode>());
    List<PageNode> children = node.getChildren();
    if(children == null) return copyNode;
    for(PageNode child: children){
      PageNode newNode = filter(child, userName,applicationId,allPageNode);
      if(newNode != null ) { 
        allPageNode.add(newNode) ;
        copyNode.getChildren().add(newNode);
      }
    }
    return copyNode;
  }

  private boolean hasApplication(Page page,String applicationId) {
    if(page == null) return false ;
    for(Object object:page.getChildren()) {
      if(object instanceof UIPortlet) {
        UIPortlet portlet = (UIPortlet) object ;
        if(portlet.getId().equalsIgnoreCase(applicationId)) return true;
      }
    }
    return false ;  
  }


}
