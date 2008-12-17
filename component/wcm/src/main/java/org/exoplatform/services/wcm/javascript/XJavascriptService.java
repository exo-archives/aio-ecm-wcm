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

import java.util.concurrent.CopyOnWriteArrayList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.exoplatform.services.deployment.ContentInitializerService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.web.application.javascript.JavascriptConfigService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Apr 9, 2008  
 */
public class XJavascriptService implements Startable {

  private static String SHARED_JS_QUERY = "select * from exo:jsFile where jcr:path like '{path}' and exo:active='true' and exo:sharedJS='true' order by exo:priority DESC " ;
  final private String MODULE_NAME = "eXo.WCM.Live".intern();
  final private String PATH = "/javascript/eXo/wcm/live".intern();

  private RepositoryService repositoryService ;
  private JavascriptConfigService jsConfigService ;
  private WCMConfigurationService configurationService;
  private ServletContext sContext ;    
  private CopyOnWriteArrayList<String> javascriptMimeTypes = new CopyOnWriteArrayList<String>();
  
  private Log log = ExoLogger.getLogger("wcm:XJavascriptService");  
  @SuppressWarnings("unused")
  public XJavascriptService(RepositoryService repositoryService,JavascriptConfigService jsConfigService,ServletContext servletContext, 
      WCMConfigurationService configurationService, ContentInitializerService contentInitializerService) {    
    this.repositoryService = repositoryService ;
    this.jsConfigService = jsConfigService ;
    sContext = servletContext ;
    this.configurationService = configurationService;
    javascriptMimeTypes.addIfAbsent("text/javascript");
    javascriptMimeTypes.addIfAbsent("application/x-javascript");
    javascriptMimeTypes.addIfAbsent("text/ecmascript");
  }

  public String getActiveJavaScript(Node home) throws Exception {    
    String jsQuery = "select * from exo:jsFile where jcr:path like '" + home.getPath()+ "/%' and exo:active='true'order by exo:priority DESC " ;
    return getJSDataBySQLQuery(home.getSession(),jsQuery,null);        
  }   

  public void updatePortalJSOnModify(Node jsFile, SessionProvider sessionProvider) throws Exception {    
    String javascript = getJavascriptOfAllPortals(sessionProvider,jsFile.getPath());
    String modifiedJS = jsFile.getNode("jcr:content").getProperty("jcr:data").getString();
    javascript = javascript.concat(modifiedJS);
    addJavascript(javascript);    
  }    

  public void updatePortalJSOnRemove(Node jsFile, SessionProvider sessionProvider) throws Exception {    
    String javascript = getJavascriptOfAllPortals(sessionProvider,jsFile.getPath());
    addJavascript(javascript);    
  }

  private void addJavascript(String jsData) {
    if(jsConfigService.isModuleLoaded(MODULE_NAME)) {      
      jsConfigService.removeExtendedJavascript(MODULE_NAME,PATH,sContext) ;
    }
    jsConfigService.addExtendedJavascript(MODULE_NAME, PATH, sContext, jsData) ;
  }

  private String getJavascriptOfAllPortals(SessionProvider sessionProvider, String exceptPath) throws Exception {
    ManageableRepository manageableRepository = repositoryService.getCurrentRepository();    
    NodeLocation livePortalsLocation = configurationService.getLivePortalsLocation(manageableRepository.getConfiguration().getName());
    String statement = StringUtils.replaceOnce(SHARED_JS_QUERY,"{path}",livePortalsLocation.getPath());    
    Session session = sessionProvider.getSession(livePortalsLocation.getWorkspace(),manageableRepository);
    return getJSDataBySQLQuery(session,statement,exceptPath);        
  }

  private String getJSDataBySQLQuery(Session session, String queryStatement, String exceptPath) throws Exception {
    Session querySession = null;
    QueryManager queryManager = null;
    try {
      if(session.isLive()) {
        queryManager = session.getWorkspace().getQueryManager();
      }else {
        Repository repository = session.getRepository();
        querySession = repository.login(session.getWorkspace().getName());
        queryManager = querySession.getWorkspace().getQueryManager();
      }
      Query query = queryManager.createQuery(queryStatement, Query.SQL) ;
      QueryResult queryResult = query.execute() ;
      StringBuffer buffer = new StringBuffer();
      for(NodeIterator iterator = queryResult.getNodes();iterator.hasNext();) {
        Node jsFile = iterator.nextNode();
        Node jcrContent = jsFile.getNode("jcr:content");
        String mimeType = jcrContent.getProperty("jcr:mimeType").getString();
        if(!javascriptMimeTypes.contains(mimeType)) continue;
        if(jsFile.getPath().equalsIgnoreCase(exceptPath)) continue;
        buffer.append(jcrContent.getProperty("jcr:data").getString()) ;
      }
      return buffer.toString(); 
    }
    finally{
      if(querySession != null)
        querySession.logout();
    }
  }

  public void start() {    
    log.info("Start WCM Javascript service...");
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {                         
      String sharedJS = getJavascriptOfAllPortals(sessionProvider,null) ;
      if(sharedJS != null && sharedJS.length()!= 0) {
        addJavascript(sharedJS); 
      }       
    } catch (Exception e) {      
      log.error("Error when start XJavaScriptService",e);      
    }
    sessionProvider.close();        
  }

  public void stop() {     
  }    
}
