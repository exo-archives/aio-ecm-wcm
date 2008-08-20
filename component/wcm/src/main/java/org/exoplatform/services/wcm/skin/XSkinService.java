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
import org.exoplatform.portal.webui.skin.SkinConfig;
import org.exoplatform.portal.webui.skin.SkinService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS Author : Hoa.Pham hoa.pham@exoplatform.com
 * Apr 9, 2008
 */
public class XSkinService implements Startable {    
  private static String SKIN_PATH_REGEXP = "/portal/css/jcr/(.*)/(.*)/Stylesheet.css";
  private SkinService skinService ;   
  private RepositoryService repositoryService ;
  private LivePortalManagerService livePortalManagerService;  

  /**
   * Instantiates a new extended skin service to manage skin for web content
   * 
   * @param skinService the skin service
   * @param repositoryService the repository service
   * @param portalManagerService the portal manager service
   */
  public XSkinService(SkinService skinService,RepositoryService repositoryService,LivePortalManagerService portalManagerService) {
    this.skinService = skinService ;
    this.repositoryService = repositoryService ;       
    this.livePortalManagerService = portalManagerService;
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
    return queryCSSData(home,cssQuery);
  }

  /**
   * Make shared css.
   * 
   * @param repository the repository
   * @param workspace the workspace
   * @param cssPath the css path
   * @throws Exception the exception
   */
  public void makeSharedCSS(String repository,String workspace,String cssPath) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    ManageableRepository manageableRepository = repositoryService.getRepository(repository) ;
    Session session = sessionProvider.getSession(workspace, manageableRepository) ;
    Node cssNode = (Node)session.getItem(cssPath) ;
    cssNode.setProperty("exo:sharedCSS", true) ;
    cssNode.save();
    sessionProvider.close();
  }

  /**
   * update portal content skin add/edit/modify a css node in a portal
   * 
   * @param repository the repository
   * @param workspace the workspace
   * @param path the path of css node
   * @throws Exception the exception
   */
  public void merge(String repository,String workspace,String path) throws Exception {        
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();    
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(sessionProvider);
    if(path.startsWith(sharedPortal.getPath())) {
      for(Iterator<String> iterator= skinService.getAvailableSkins();iterator.hasNext();) {
        addSharedPortalSkin(sharedPortal,iterator.next()); 
      }
    }else {
      for(String portalPath: livePortalManagerService.getLivePortalsPath()) {
        if(path.startsWith(portalPath)) {
          String portalName = livePortalManagerService.getPortalNameByPath(portalPath);
          Node portal = livePortalManagerService.getLivePortal(portalName,sessionProvider);
          for(Iterator<String> iterator= skinService.getAvailableSkins();iterator.hasNext();) {
            addPortalSkin(portal,iterator.next()); 
          }
        } 
      }
    }    
    sessionProvider.close();
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

  private void addSharedPortalSkin(Node portal,String skinName) throws Exception {    
    String statement= "select * from exo:cssFile where jcr:path like '" 
      + portal.getPath()+"/%' and exo:active='true' and exo:sharedCSS='true' order by exo:priority DESC ";
    String skinCSS = queryCSSData(portal,statement);    
    if(skinCSS == null || skinCSS.length() == 0) return;
    String skinPath = StringUtils.replaceOnce(SKIN_PATH_REGEXP,"(.*)",portal.getName());
    skinPath = StringUtils.replaceOnce(skinPath,"(.*)",skinName);
    skinService.addPortalSkin(portal.getName(),skinName,skinPath,skinCSS);    
  }  

  private void addPortalSkin(Node portal,String skinName) throws Exception {    
    String statement= "select * from exo:cssFile where jcr:path like '" 
      + portal.getPath()+"/%' and exo:active='true' and exo:sharedCSS='true' order by exo:priority DESC ";
    String skinCSS = queryCSSData(portal,statement);    
    if(skinCSS == null || skinCSS.length() == 0) return;
    String skinPath = StringUtils.replaceOnce(SKIN_PATH_REGEXP,"(.*)",portal.getName());
    skinPath = StringUtils.replaceOnce(skinPath,"(.*)",skinName);            
    skinService.addSkin(portal.getName(), skinName, skinPath, skinCSS);   
  }

  private String queryCSSData(Node home,String statement) throws Exception {
    QueryManager manager = home.getSession().getWorkspace().getQueryManager();
    Query query = manager.createQuery(statement,Query.SQL);
    QueryResult queryResult = query.execute();
    StringBuffer buffer = new StringBuffer();
    for(NodeIterator iterator = queryResult.getNodes();iterator.hasNext();) {
      Node cssFile = iterator.nextNode();
      String css = cssFile.getNode("jcr:content").getProperty("jcr:data").getString();      
      buffer.append(css) ;
    }    
    return buffer.toString();
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#start()
   */
  public void start() {    
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    try {
      Node sharedPortal = livePortalManagerService.getLiveSharedPortal(sessionProvider);      
      //Add same portal css for all skins
      for(Iterator<String> iterator= skinService.getAvailableSkins();iterator.hasNext();) {
        String skinName = iterator.next();
        addSharedPortalSkin(sharedPortal,skinName);       
      }
      List<Node> livePortals = livePortalManagerService.getLivePortals(sessionProvider);
      for(Node portal: livePortals) {
        for(Iterator<String> iterator= skinService.getAvailableSkins();iterator.hasNext();) {
          String skinName = iterator.next();                   
          addPortalSkin(portal,skinName);          
        }
      }
    }catch (Exception e) {
    }finally {
      sessionProvider.close();
    }
  }

  /* (non-Javadoc)
   * @see org.picocontainer.Startable#stop()
   */
  public void stop() { }      

}
