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
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.skin.SkinService;
import org.exoplatform.services.deployment.ContentInitializerService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.picocontainer.Startable;

// TODO: Auto-generated Javadoc
/**
 * Created by The eXo Platform SAS Author : Hoa.Pham hoa.pham@exoplatform.com
 * Apr 9, 2008
 */
public class XSkinService implements Startable {    
  
  /** The SHARE d_ cs s_ query. */
  private static String SHARED_CSS_QUERY = "select * from exo:cssFile where jcr:path like '{path}/%' and exo:active='true' and exo:sharedCSS='true' order by exo:priority DESC ".intern();  
  
  /** The Constant SKIN_PATH_REGEXP. */
  public final static String SKIN_PATH_REGEXP = "/(.*)/css/jcr/(.*)/(.*)/(.*).css".intern();  
  
  /** The Constant SKIN_PATH_PATTERN. */
  private final static String SKIN_PATH_PATTERN = "/{docBase}/css/jcr/(.*)/(.*)/Stylesheet.css".intern();

  /** The log. */
  private static Log log = ExoLogger.getLogger("wcm:XSkinService");           
  
  /** The schema config service. */
  private WebSchemaConfigService schemaConfigService;
  
  /** The configuration service. */
  private WCMConfigurationService configurationService;
  
  /** The skin service. */
  private SkinService skinService ;
  
  /** The servlet context. */
  private ServletContext servletContext;

  /**
   * Instantiates a new extended skin service to manage skin for web content.
   * 
   * @param skinService the skin service
   * @param initializerService the content initializer service. this param makes sure that the service started after the content initializer service is started
   * @param schemaConfigService the schema config service
   * @param configurationService the configuration service
   * @param servletContext the servlet context
   * 
   * @throws Exception the exception
   */
  public XSkinService(SkinService skinService,WebSchemaConfigService schemaConfigService, WCMConfigurationService configurationService, ContentInitializerService initializerService, ServletContext servletContext) throws Exception {
    this.skinService = skinService ;
    this.skinService.addResourceResolver(new WCMSkinResourceResolver(this.skinService));
    this.configurationService = configurationService;
    this.schemaConfigService = schemaConfigService;
    this.servletContext = servletContext;
//    this.cssCache = cacheService.getCacheInstance(this.getClass().getName());
  }

  /**
   * Gets the active stylesheet.
   * 
   * @param home the home
   * 
   * @return the active stylesheet
   * 
   * @throws Exception the exception
   */
  public String getActiveStylesheet(Node home) throws Exception {
    String cssQuery = "select * from exo:cssFile where jcr:path like '" +home.getPath()+ "/%' and exo:active='true'order by exo:priority DESC " ;
    //TODO the jcr can not search on jcr:system for normal workspace. Seem that this is the portal bug
    Session querySession = null;
    String cssData = null;
    try {
      Session currentSession = home.getSession();
      ManageableRepository manageableRepository = (ManageableRepository)currentSession.getRepository();
      String currentWorkspaceName = currentSession.getWorkspace().getName();
      String systemWorkspaceName = manageableRepository.getConfiguration().getSystemWorkspaceName();
      if(home.getPath().startsWith("/jcr:system") && !currentWorkspaceName.equals(systemWorkspaceName)) {
        querySession = manageableRepository.login(systemWorkspaceName);
        cssData = getCSSDataBySQLQuery(querySession,cssQuery,null);
      }else {
        if(currentSession.isLive()) {
          cssData = getCSSDataBySQLQuery(currentSession,cssQuery,null);
        }else {
          querySession = manageableRepository.login(currentWorkspaceName);
          cssData = getCSSDataBySQLQuery(querySession,cssQuery,null);
        }
      }
    }finally {
      if(querySession != null)
        querySession.logout();
    }        
//    cssCache.put(cacheKey,cssData);
    return cssData;
  }  

  /**
   * Update portal skin on modify.
   * 
   * @param cssFile the css file
   * @param portal the portal
   * 
   * @throws Exception the exception
   */
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

  /**
   * Update portal skin on remove.
   * 
   * @param cssFile the css file
   * @param portal the portal
   * 
   * @throws Exception the exception
   */
  public void updatePortalSkinOnRemove(Node cssFile, final Node portal) throws Exception {
    String repository = ((ManageableRepository)portal.getSession().getRepository()).getConfiguration().getName();
    String sharedPortalName = configurationService.getSharedPortalName(repository);
    if(sharedPortalName.equals(portal.getName())) {
      addSharedPortalSkin(portal,SHARED_CSS_QUERY,cssFile.getPath(),null,true);
    }else {            
      addPortalSkin(portal,SHARED_CSS_QUERY,cssFile.getPath(), null, true);
    }    
  }

  /**
   * Adds the portal skin.
   * 
   * @param portal the portal
   * @param preStatement the pre statement
   * @param exceptedPath the excepted path
   * @param appendedCSS the appended css
   * @param allowEmptyCSS the allow empty css
   * 
   * @throws Exception the exception
   */
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

  /**
   * Adds the shared portal skin.
   * 
   * @param portal the portal
   * @param preStatement the pre statement
   * @param exceptedPath the excepted path
   * @param appendedCSS the appended css
   * @param allowEmptyCSS the allow empty css
   * 
   * @throws Exception the exception
   */
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

  /**
   * Gets the cSS data by sql query.
   * 
   * @param session the session
   * @param statement the statement
   * @param exceptedPath the excepted path
   * 
   * @return the cSS data by sql query
   * 
   * @throws Exception the exception
   */
  private String getCSSDataBySQLQuery(Session session, String statement, String exceptedPath) throws Exception {    
    QueryManager queryManager = session.getWorkspace().getQueryManager();      
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

  /**
   * Gets the portal css folder.
   * 
   * @param portal the portal
   * 
   * @return the portal css folder
   * 
   * @throws Exception the exception
   */
  private Node getPortalCSSFolder(Node portal) throws Exception{
    PortalFolderSchemaHandler schemaHandler = schemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    return schemaHandler.getCSSFolder(portal);
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
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

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() { }

}
