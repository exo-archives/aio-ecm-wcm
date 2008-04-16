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
package org.exoplatform.services.wcm.javascript;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletContext;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 9, 2008  
 */
public class XJavascriptService implements Startable {

  private RepositoryService repositoryService_ ;
  private JavascriptConfigService jsConfigService_ ;
  private ServletContext sContext_ ;  
  final private String MODULE_NAME = "eXo.WCM.Live".intern();
  final private String PATH = "/javascript/eXo/wcm/Live".intern();  

  public XJavascriptService(RepositoryService repositoryService,JavascriptConfigService jsConfigService,ServletContext servletContext) {    
    this.repositoryService_ = repositoryService ;
    this.jsConfigService_ = jsConfigService ;
    sContext_ = servletContext ;
  }

  public String getActiveJavaScript(Node home) throws Exception {    
    String jsQuery = "select * from exo:jsFile where jcr:path like '" +home.getPath()+ "/%' and exo:active='true'order by exo:priority DESC " ;
    QueryManager queryManager = home.getSession().getWorkspace().getQueryManager() ;
    Query query = queryManager.createQuery(jsQuery, Query.SQL) ;
    QueryResult queryResult = query.execute() ;
    StringBuffer buffer = new StringBuffer() ;
    for(NodeIterator iterator = queryResult.getNodes(); iterator.hasNext();) {
      Node cssNode = iterator.nextNode() ;      
      buffer.append(getFileContent(cssNode)) ;       
    }
    return buffer.toString() ;    
  }
  
  public void makeSharedJavascript(String repository,String workspace,String javascriptPath) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;
    Session session = sessionProvider.getSession(workspace, manageableRepository) ;
    Node jsNode = (Node)session.getItem(javascriptPath) ;
    jsNode.setProperty("exo:sharedJS", true) ;
    jsNode.save();
    sessionProvider.close();
  }
  
  public void mergeJavascript(String repository,String workspace,String jsPath) throws Exception {    
    if(jsConfigService_.isModuleLoaded(MODULE_NAME)) {      
      jsConfigService_.removeJavaScript(MODULE_NAME,PATH,sContext_) ;
    }
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    String javascript = getActiveSharedJavaScript(repository, workspace, sessionProvider) ;
    jsConfigService_.addJavascript(MODULE_NAME, PATH, sContext_, javascript) ;
    sessionProvider.close();
  }

  private String getActiveSharedJavaScript(String repository,String workspace,SessionProvider sessionProvider) throws Exception{
    String sharedJSQuery = "select * from exo:jsFile where exo:active='true' and exo:sharedJS='true' order by exo:priority DESC " ;
    ManageableRepository manageableRepository = (ManageableRepository)repositoryService_.getRepository(repository) ;
    StringBuffer buffer = new StringBuffer();    
    Session session = sessionProvider.getSession(workspace, manageableRepository) ;
    QueryManager queryManager = session.getWorkspace().getQueryManager() ;
    Query query = queryManager.createQuery(sharedJSQuery, Query.SQL) ;
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
      String repository = repositoryService_.getDefaultRepository().getConfiguration().getName() ;
      String workspace = repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName();      
      String sharedJS = getActiveSharedJavaScript(repository, workspace, provider) ;
      if(sharedJS != null && sharedJS.length()!= 0) {
        jsConfigService_.addJavascript(MODULE_NAME, PATH,sContext_,sharedJS) ; 
      }                  
    } catch (Exception e) {

    }finally {
      provider.close();
    }     
  }

  public void stop() {     
  }    
}
