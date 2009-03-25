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

import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.skin.SkinService;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.deployment.ContentInitializerService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS Author : Hoa.Pham hoa.pham@exoplatform.com
 * Apr 9, 2008
 */
public class XSkinService implements Startable {    
  private static String SHARED_CSS_QUERY = "select * from exo:cssFile where jcr:path like '{path}/%' and exo:active='true' and exo:sharedCSS='true' order by exo:priority DESC ".intern();  
  public final static String SKIN_PATH_REGEXP = "/(.*)/css/jcr/(.*)/(.*)/(.*).css".intern();  
  private final static String SKIN_PATH_PATTERN = "/{docBase}/css/jcr/(.*)/(.*)/Stylesheet.css".intern();

  private static Log log = ExoLogger.getLogger("wcm:XSkinService");           
  private WebSchemaConfigService schemaConfigService;
  private WCMConfigurationService configurationService;
  private SkinService skinService ;
  private ServletContext servletContext;
  private ExoCache cssCache;

  /**
   * Instantiates a new extended skin service to manage skin for web content.
   * 
   * @param skinService the skin service
   * @param initializerService the content initializer service. this param makes sure that the service started after the content initializer service is started
   * @param schemaConfigService the schema config service
   * @param configurationService the configuration service
   * @param servletContext the servlet context
   */
  @SuppressWarnings("unused")
  public XSkinService(SkinService skinService,WebSchemaConfigService schemaConfigService, WCMConfigurationService configurationService, ContentInitializerService initializerService, ServletContext servletContext, CacheService cacheService) throws Exception {
    this.skinService = skinService ;
    this.skinService.addResourceResolver(new WCMSkinResourceResolver(this.skinService));
    this.configurationService = configurationService;
    this.schemaConfigService = schemaConfigService;
    this.servletContext = servletContext;
    this.cssCache = cacheService.getCacheInstance(this.getClass().getName());
  }

  /**
   * Gets the active stylesheet.
   * 
   * @param home the home
   * @return the active stylesheet
   * @throws Exception the exception
   */
  public String getActiveStylesheet(Node home) throws Exception {
    /** TODO
     * 
     * This is quick code to improve performance when rendering the web content.
     * In future version, we should find a way use cache data in live mode. In edit mode, we will use raw data 
     * 
     * */
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    boolean useCachedData = false;    
    if(requestContext != null) {
      HttpServletRequest request = requestContext.getRequest();
      HttpSession httpSession = request.getSession();
      Object onEditMode = httpSession.getAttribute("turnOnQuickEdit");
      if(onEditMode == null) {
        useCachedData = true; 
      } else {
        useCachedData = !Boolean.parseBoolean((String)onEditMode);
      }     
    }
    String cacheKey = home.getSession().getWorkspace().getName() + home.getPath();
    if(useCachedData) {
      String cachedCSS = (String)cssCache.get(cacheKey);
      if(cachedCSS != null && cachedCSS.length()>0)
        return cachedCSS;
    }
    String cssQuery = "select * from exo:cssFile where jcr:path like '" +home.getPath()+ "/%' and exo:active='true'order by exo:priority DESC " ;
    String css = getCSSDataBySQLQuery(home.getSession(),cssQuery,null);
    cssCache.put(cacheKey,css);
    return css;
  }  

  public void updatePortalSkinOnModify(final Node cssFile, final Node portal) throws Exception {            
    String modifiedCSS = cssFile.getNode("jcr:content").getProperty("jcr:data").getString();
    String repository = ((ManageableRepository)portal.getSession().getRepository()).getConfiguration().getName();
    String sharedPortalName = configurationService.getSharedPortalName(repository);
    if(sharedPortalName.equals(portal.getName())) {
      addSharedPortalSkin(portal,SHARED_CSS_QUERY,cssFile.getPath(),modifiedCSS,true);
    }else {            
      addPortalSkin(portal,SHARED_CSS_QUERY,cssFile.getPath(),modifiedCSS, true);
    }                  
  }

  public void updatePortalSkinOnRemove(Node cssFile, final Node portal) throws Exception {
    String repository = ((ManageableRepository)portal.getSession().getRepository()).getConfiguration().getName();
    String sharedPortalName = configurationService.getSharedPortalName(repository);
    if(sharedPortalName.equals(portal.getName())) {
      addSharedPortalSkin(portal,SHARED_CSS_QUERY,cssFile.getPath(),null,true);
    }else {            
      addPortalSkin(portal,SHARED_CSS_QUERY,cssFile.getPath(), null, true);
    }    
  }

  private void addPortalSkin(Node portal,String preStatement, String exceptedPath,String appendedCSS, boolean allowEmptyCSS) throws Exception {
    Node cssFolder = getPortalCSSFolder(portal);
    String statement = StringUtils.replaceOnce(preStatement,"{path}",cssFolder.getPath());
    String cssData = getCSSDataBySQLQuery(portal.getSession(),statement, exceptedPath);
    if(appendedCSS != null) {
      if(cssData == null) cssData = appendedCSS;
      else cssData = cssData.concat(appendedCSS);
    }
    if(!allowEmptyCSS) {
      if(cssData == null || cssData.length() == 0)        
        return;
    }
    String skinPath = StringUtils.replaceOnce(SKIN_PATH_PATTERN,"(.*)",portal.getName()).replaceFirst("\\{docBase\\}", servletContext.getServletContextName());    
    for(Iterator<String> iterator= skinService.getAvailableSkinNames().iterator();iterator.hasNext();) {
      String skinName = iterator.next();
      skinPath = StringUtils.replaceOnce(skinPath,"(.*)",skinName);
      skinService.addSkin(portal.getName(), skinName, skinPath, cssData);
    }       
  }  

  private void addSharedPortalSkin(Node portal,String preStatement, String exceptedPath, String appendedCSS, boolean allowEmptyCSS) throws Exception {
    Node cssFolder = getPortalCSSFolder(portal);
    String statement = StringUtils.replaceOnce(preStatement,"{path}",cssFolder.getPath());
    String cssData = getCSSDataBySQLQuery(portal.getSession(),statement, exceptedPath);
    if(appendedCSS != null) {
      if(cssData == null) cssData = appendedCSS;
      else cssData = cssData.concat(appendedCSS);
    } 
    if(!allowEmptyCSS) {
      if(cssData == null || cssData.length() == 0)        
        return;
    }
    String skinPath = StringUtils.replaceOnce(SKIN_PATH_PATTERN,"(.*)",portal.getName()).replaceFirst("\\{docBase\\}", servletContext.getServletContextName());
    for(Iterator<String> iterator= skinService.getAvailableSkinNames().iterator();iterator.hasNext();) {
      String skinName = iterator.next();
      skinPath = StringUtils.replaceOnce(skinPath,"(.*)",skinName);
      skinService.addPortalSkin(portal.getName(),skinName, skinPath, cssData);
    }         
  }

  private String getCSSDataBySQLQuery(Session session, String statement, String exceptedPath) throws Exception {
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
      Query query = queryManager.createQuery(statement,Query.SQL);
      QueryResult queryResult = query.execute();    
      StringBuffer buffer = new StringBuffer();
      for(NodeIterator iterator = queryResult.getNodes();iterator.hasNext();) {
        Node cssFile = iterator.nextNode();
        if(cssFile.getPath().equals(exceptedPath)) continue;
        Node jcrContent = cssFile.getNode("jcr:content");
        String mimeType = jcrContent.getProperty("jcr:mimeType").getString();
        if(!"text/css".equals(mimeType)) continue;
        String css = jcrContent.getProperty("jcr:data").getString();      
        buffer.append(css) ;
      }    
      return buffer.toString(); 
    }
    finally{
      if(querySession != null)
        querySession.logout();
    }
  }  

  private Node getPortalCSSFolder(Node portal) throws Exception{
    PortalFolderSchemaHandler schemaHandler = schemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    return schemaHandler.getCSSFolder(portal);
  }

  public void start() {  
    log.info("Start WCMSkinService...");
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();    
    try {      
      LivePortalManagerService livePortalManagerService = 
        (LivePortalManagerService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(LivePortalManagerService.class);
      Node sharedPortal = livePortalManagerService.getLiveSharedPortal(sessionProvider);
      addSharedPortalSkin(sharedPortal,SHARED_CSS_QUERY,null,null,false);
      List<Node> livePortals = livePortalManagerService.getLivePortals(sessionProvider);
      for(Node portal: livePortals) {
        addPortalSkin(portal,SHARED_CSS_QUERY,null, null, false);
      }
    }catch (Exception e) {
      if(log.isErrorEnabled()) {
        log.error("Exception when start XSkinService",e);
      }
    }finally {
      sessionProvider.close();
    }
  }

  public void stop() { }

}
