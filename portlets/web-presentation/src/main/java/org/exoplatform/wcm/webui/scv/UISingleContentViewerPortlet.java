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
package org.exoplatform.wcm.webui.scv;

import java.security.AccessControlException;
import java.util.HashMap;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.apache.commons.logging.Log;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.Constant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.Constant.SITE_MODE;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService;
import org.exoplatform.wcm.webui.WebUIPropertiesConfigService.PopupWindowProperties;
import org.exoplatform.wcm.webui.scv.config.UIPortletConfig;
import org.exoplatform.wcm.webui.scv.config.UIStartEditionInPageWizard;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Jun 9, 2008
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class    
)

public class UISingleContentViewerPortlet extends UIPortletApplication {

  /** The REPOSITORY. */
  public static String REPOSITORY = "repository" ;

  /** The WORKSPACE. */
  public static String WORKSPACE = "workspace" ;

  /** The IDENTIFIER. */
  public static String IDENTIFIER = "nodeIdentifier" ;

  /** The Constant scvLog. */
  public static final Log scvLog = ExoLogger.getLogger("wcm:SingleContentViewer");

  /** The mode_. */
  private PortletMode mode_ = PortletMode.VIEW ;

  /**
   * Instantiates a new uI single content viewer portlet.
   * 
   * @throws Exception the exception
   */
  public UISingleContentViewerPortlet() throws Exception {    
    activateMode(mode_) ;    
  }

  /**
   * Activate mode.
   * 
   * @param mode the mode
   * 
   * @throws Exception the exception
   */
  public void activateMode(PortletMode mode) throws Exception {
    boolean isLiveMode = Utils.isLiveMode();    
    getChildren().clear() ;        
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    //only add popup container in private mode to pass w3c validator
    if(portletRequestContext.getRemoteUser() != null) {
      addChild(UIPopupContainer.class, null, null);
    }
    if(PortletMode.VIEW.equals(mode)) {
      Node orginialNode = null;
      try {
        orginialNode = getReferencedContent();
      } catch(ItemNotFoundException ex) {
        orginialNode = null;
      } catch(RepositoryException rx) {
        orginialNode =  null;
      }
      UIPresentationContainer container = addChild(UIPresentationContainer.class, null, UIPortletApplication.VIEW_MODE);
      String currentState = getRevisionState(orginialNode);
      if(Constant.OBSOLETE_STATE.equals(currentState)) {
        container.setObsoletedContent(true);
      }else {
        container.setObsoletedContent(false);
        UIPresentation livePresentation = container.getChild(UIPresentation.class);
        if(isLiveMode) {
          Node liveRevision = getLiveRevision(orginialNode);
          livePresentation.setOriginalNode(orginialNode);
          livePresentation.setViewNode(liveRevision);      
          container.setDraftRevision(false);       
        }else {        
          if(Constant.DRAFT_STATE.equals(currentState)) {
            livePresentation.setViewNode(orginialNode);          
            livePresentation.setOriginalNode(orginialNode);
            container.setDraftRevision(true);          
          } else if(Constant.LIVE_STATE.equals(currentState)) {
            Node liveRevision = getLiveRevision(orginialNode);
            livePresentation.setOriginalNode(orginialNode);
            livePresentation.setViewNode(liveRevision);
            container.setDraftRevision(false);             
          }
        }  
      }         
    } else if (PortletMode.EDIT.equals(mode)) {
      UIPopupContainer maskPopupContainer = getChild(UIPopupContainer.class);
      UIStartEditionInPageWizard portletEditMode = createUIComponent(UIStartEditionInPageWizard.class,null,null);
      addChild(portletEditMode);
      UIPortletConfig portletConfig = portletEditMode.createUIComponent(UIPortletConfig.class,null,null);      
      portletEditMode.addChild(portletConfig);
      portletConfig.init();
      portletConfig.setRendered(true);      
      WebUIPropertiesConfigService propertiesConfigService = getApplicationComponent(WebUIPropertiesConfigService.class);
      PopupWindowProperties popupProperties = (PopupWindowProperties)propertiesConfigService.getProperties(WebUIPropertiesConfigService.SCV_POPUP_SIZE_EDIT_PORTLET_MODE);
      maskPopupContainer.activate(portletConfig,popupProperties.getWidth(),popupProperties.getHeight());      
      portletRequestContext.addUIComponentToUpdateByAjax(maskPopupContainer);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPortletApplication#processRender(org.exoplatform.webui.application.WebuiApplication, org.exoplatform.webui.application.WebuiRequestContext)
   */
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext pContext = (PortletRequestContext) context ;
    PortletMode newMode = pContext.getApplicationMode() ;
    if(!mode_.equals(newMode)) {
      activateMode(newMode) ;
      mode_ = newMode ;
    }
    super.processRender(app, context) ;
  }

  /**
   * Can edit portlet.
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  public boolean canEditPortlet() throws Exception{
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    String userId = context.getRemoteUser();
    return Utils.canEditCurrentPortal(userId);
  }

  /**
   * Gets the referenced content.
   * 
   * @return the referenced content
   * 
   * @throws Exception the exception
   */
  public Node getReferencedContent() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences preferences = portletRequestContext.getRequest().getPreferences();
    String repository = preferences.getValue(UISingleContentViewerPortlet.REPOSITORY, null);    
    String worksapce = preferences.getValue(UISingleContentViewerPortlet.WORKSPACE, null);
    String nodeIdentifier = preferences.getValue(UISingleContentViewerPortlet.IDENTIFIER, null) ;
    if(repository == null || worksapce == null || nodeIdentifier == null) 
      throw new ItemNotFoundException();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    String userId = Util.getPortalRequestContext().getRemoteUser();
    SessionProvider sessionProvider = null;
    if(userId == null) {
      sessionProvider = SessionProviderFactory.createAnonimProvider();
    }else {
      sessionProvider = SessionProviderFactory.createSessionProvider();
    }
    Session session = sessionProvider.getSession(worksapce, manageableRepository);
    Node content = null;
    try {
      content = session.getNodeByUUID(nodeIdentifier);
    } catch (Exception e) {
      content = (Node) session.getItem(nodeIdentifier);
    }
    return content;
  } 
  
  private Node getLiveRevision(Node content) throws Exception {
    if (content == null) return null;
    HashMap<String,Object> context = new HashMap<String, Object>();    
    context.put(Constant.RUNTIME_MODE, SITE_MODE.LIVE);    
    PublicationService pubService = getApplicationComponent(PublicationService.class);
    String lifecycleName = pubService.getNodeLifecycleName(content);
    PublicationPlugin pubPlugin = pubService.getPublicationPlugins().get(lifecycleName);
    return pubPlugin.getNodeView(content, context);
  }    

  private String getRevisionState(Node content) throws Exception {
    String currentState = null;
    try {
      currentState = content.getProperty("publication:currentState").getString();
    } catch (Exception e) {
    } 
    if(Constant.DRAFT_STATE.equals(currentState))
      return Constant.DRAFT_STATE;
    if(Constant.ENROLLED_STATE.equals(currentState)) {
      String liveRevision = null;
      try {
        liveRevision = content.getProperty("publication:liveRevision").getString();
      } catch (Exception e) {       
      }
      if(liveRevision != null && liveRevision.length()>0) 
        return Constant.LIVE_STATE;
      return Constant.OBSOLETE_STATE;
    }
    return null;
  }

  /**
   * Can edit content.
   * 
   * @param content the content
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  public boolean canEditContent(final Node content) throws Exception {
    if (content == null || !content.isNodeType("exo:webContent")) return false;
    try {
      ((ExtendedNode)content).checkPermission(PermissionType.ADD_NODE);
      ((ExtendedNode)content).checkPermission(PermissionType.REMOVE);
      ((ExtendedNode)content).checkPermission(PermissionType.SET_PROPERTY);
    } catch (AccessControlException e) {
      return false;
    }
    return true;
  }

  //TODO: the overridden method will not render UIPopupMessgae in live move to save render time
  //In future, this should be supported in portal framework
  @Override
  public void renderChildren() throws Exception {
    if(Utils.isLiveMode()) {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      renderChildren(context) ; 
    }else {
      super.renderChildren();
    }    
  }
}
