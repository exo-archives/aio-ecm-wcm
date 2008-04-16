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
package org.exoplatform.services.wcm.skin;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.portal.webui.skin.SkinService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 9, 2008  
 */
public class XSkinService implements Startable {

  private SkinService skinService_ ; 
  private RepositoryService repositoryService_ ;

  public XSkinService(SkinService skinService,RepositoryService repositoryService) {
    this.skinService_ = skinService ;
    this.repositoryService_ = repositoryService ;
  }

  public String getActiveStylesheet(Node home) throws Exception {    
    String cssQuery = "select * from exo:cssFile where jcr:path like '" +home.getPath()+ "/%' and exo:active='true'order by exo:priority DESC " ;   
    QueryManager queryManager = home.getSession().getWorkspace().getQueryManager() ;
    Query query = queryManager.createQuery(cssQuery, Query.SQL) ;
    QueryResult queryResult = query.execute() ;
    StringBuffer buffer = new StringBuffer() ;
    for(NodeIterator iterator = queryResult.getNodes(); iterator.hasNext();) {
      Node cssNode = iterator.nextNode() ;      
      buffer.append(getFileContent(cssNode)) ;       
    }
    return buffer.toString() ;    
  }
  
  public void makeSharedCSS(String repository,String workspace,String cssPath) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    Session session = sessionProvider.getSession(workspace, manageableRepository) ;
    Node cssNode = (Node)session.getItem(cssPath) ;
    cssNode.setProperty("exo:sharedCSS", true) ;
    cssNode.save();
    sessionProvider.close();
  }
  
  public void merge(String repository,String workspace,String cssPath) throws Exception {        
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    String sharedCss = getActiveSharedStylesheet(repository, workspace, sessionProvider);
    skinService_.addSkin("WebContentSkin", "Default", "/portal/css/WebContent/Live/Stylesheet.css",false,sharedCss) ;
    sessionProvider.close();
  }

  private String getActiveSharedStylesheet(String repository,String workspace, SessionProvider sessionProvider) throws Exception{
    String sharedCSSQuery = "select * from exo:cssFile where exo:active='true' and exo:sharedCSS='true' order by exo:priority DESC " ;    
    ManageableRepository manageableRepository = (ManageableRepository)repositoryService_.getRepository(repository) ;
    StringBuffer buffer = new StringBuffer();    
    Session session = sessionProvider.getSession(workspace, manageableRepository) ;
    QueryManager queryManager = session.getWorkspace().getQueryManager() ;
    Query query = queryManager.createQuery(sharedCSSQuery, Query.SQL) ;
    QueryResult queryResult = query.execute() ;
    for(NodeIterator iterator = queryResult.getNodes();iterator.hasNext();) {
      Node cssFile = iterator.nextNode();
      buffer.append(getFileContent(cssFile)) ;
    }        
    return buffer.toString();
  }

  private String getFileContent(Node file) throws Exception {
    Property data = file.getNode("jcr:content").getProperty("jcr:data") ;
    return data.getString() ;
  }

  public void start() {    
    SessionProvider provider = SessionProvider.createSystemProvider();
    try {
      RepositoryEntry repositoryEntry = repositoryService_.getDefaultRepository().getConfiguration() ;      
      String repository = repositoryEntry.getName() ;
      String worksapce = repositoryEntry.getDefaultWorkspaceName() ;      
      String sharedCss = getActiveSharedStylesheet(repository,worksapce, provider) ;      
      skinService_.addSkin("WebContentSkin", "Default", "/portal/css/WebContent/Live/Stylesheet.css",false,sharedCss) ;
    }catch (Exception e) {
    }finally {
      provider.close();
    }
  }

  public void stop() {

  }      

}
