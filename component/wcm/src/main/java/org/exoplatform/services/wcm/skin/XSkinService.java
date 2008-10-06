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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.skin.SkinConfig;
import org.exoplatform.portal.webui.skin.SkinService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS Author : Hoa.Pham hoa.pham@exoplatform.com
 * Apr 9, 2008
 */
public class XSkinService implements Startable {    
  private static String SHARED_CSS_QUERY = "select * from exo:cssFile where jcr:path like '{path}/%' and exo:active='true' and exo:sharedCSS='true' order by exo:priority DESC ".intern();  
  private static String SKIN_PATH_REGEXP = "/portal/css/jcr/(.*)/(.*)/Stylesheet.css".intern();
  
  private static Log log = ExoLogger.getLogger("wcm:XSkinService");           
  private WebSchemaConfigService schemaConfigService;
  private WCMConfigurationService configurationService;
  private SkinService skinService ;
  /**
   * Instantiates a new extended skin service to manage skin for web content
   * 
   * @param skinService the skin service
   * @param repositoryService the repository service
   * @param portalManagerService the portal manager service
   */
  public XSkinService(SkinService skinService,WebSchemaConfigService schemaConfigService, WCMConfigurationService configurationService) {
    this.skinService = skinService ;
    this.configurationService = configurationService;
    this.schemaConfigService = schemaConfigService;
  }

  /**
   * Gets the active stylesheet.
   * 
   * @param home the home
   * @return the active stylesheet
   * @throws Exception the exception
   */
  public String getActiveStylesheet(Node home) throws Exception {    
    String cssQuery = "select * from exo:cssFile where jcr:path like '" +home.getPath()+ "/%' and exo:active='true'order by exo:priority DESC " ;
    return getCSSDataBySQLQuery(home.getSession(),cssQuery,null);
  }

  public String getPortalSkin(String requestURI) {    
    if(!requestURI.matches(SKIN_PATH_REGEXP)) return null;       
    String[] elements = requestURI.split("/");
    String portalName = elements[4];
    String skinName = elements[5];
    String skinModule = portalName;
    //get css for shared portal if the portalName is shared Portal
    for(SkinConfig skinConfig: skinService.getPortalSkins(skinName)) {
      if(skinConfig.getModule().equals(skinModule)) {
        return skinService.getMergedCSS(skinConfig.getCSSPath());
      }
    }
    //get merged css for portal
    SkinConfig skinConfig = skinService.getSkin(portalName,skinName);
    if(skinConfig != null) {
      return skinService.getMergedCSS(skinConfig.getCSSPath());
    }
    return null;    
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
    String skinPath = StringUtils.replaceOnce(SKIN_PATH_REGEXP,"(.*)",portal.getName());
    for(Iterator<String> iterator= skinService.getAvailableSkins();iterator.hasNext();) {
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
    String skinPath = StringUtils.replaceOnce(SKIN_PATH_REGEXP,"(.*)",portal.getName());
    for(Iterator<String> iterator= skinService.getAvailableSkins();iterator.hasNext();) {
      String skinName = iterator.next();
      skinPath = StringUtils.replaceOnce(skinPath,"(.*)",skinName);
      skinService.addPortalSkin(portal.getName(),skinName, skinPath, cssData);
    }         
  }

  private String getCSSDataBySQLQuery(Session session, String statement, String exceptedPath) throws Exception {
    QueryManager manager = session.getWorkspace().getQueryManager();
    Query query = manager.createQuery(statement,Query.SQL);
    QueryResult queryResult = query.execute();    
    StringBuffer buffer = new StringBuffer();

    for(NodeIterator iterator = queryResult.getNodes();iterator.hasNext();) {
      Node cssFile = iterator.nextNode();
      if(cssFile.getPath().equals(exceptedPath)) continue;
      String css = cssFile.getNode("jcr:content").getProperty("jcr:data").getString();      
      buffer.append(css) ;
    }    
    return buffer.toString();
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
