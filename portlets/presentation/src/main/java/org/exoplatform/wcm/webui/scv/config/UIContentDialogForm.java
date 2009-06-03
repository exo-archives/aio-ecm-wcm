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
package org.exoplatform.wcm.webui.scv.config;

import java.security.AccessControlException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionException;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.DialogFormActionListeners;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.ecm.webui.utils.LockUtil;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.Constant;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.scv.UISingleContentViewerPortlet;
import org.exoplatform.wcm.webui.scv.config.access.UIPermissionInfo;
import org.exoplatform.wcm.webui.scv.config.access.UIPermissionManager;
import org.exoplatform.wcm.webui.scv.config.social.UISocialInfo;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Jun 9, 2008
 */

@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIContentDialogForm.SaveDraftActionListener.class),
      @EventConfig(listeners = UIContentDialogForm.FastPublishActionListener.class),
      @EventConfig(listeners = UIContentDialogForm.PreferencesActionListener.class),
      @EventConfig(listeners = UIContentDialogForm.CloseActionListener.class),
      @EventConfig(listeners = DialogFormActionListeners.RemoveDataActionListener.class)
    }
)

public class UIContentDialogForm extends UIDialogForm {

  /** The resource resolver. */
  protected JCRResourceResolver resourceResolver;

  /** The stored location. */
  protected NodeLocation storedLocation;

  /** The saved node identifier. */
  public NodeIdentifier savedNodeIdentifier;

  /** The web content. */
  protected Node webContent;

  /** The is edit not integrity. */
  private boolean isEditNotIntegrity;
  
  /** List of actions in this form.*/
  private static final String [] ACTIONS = {"SaveDraft", "FastPublish", "Preferences", "Close"};

  /**
   * Instantiates a new uI content dialog form.
   * 
   * @throws Exception the exception
   */
  public UIContentDialogForm() throws Exception {
    setActions(ACTIONS);
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    PortletPreferences prefs = ((PortletRequestContext)WebuiRequestContext.getCurrentInstance()).getRequest().getPreferences();
    String repositoryName = prefs.getValue(UISingleContentViewerPortlet.REPOSITORY, null);
    String workspace = prefs.getValue(UISingleContentViewerPortlet.WORKSPACE, null);
    String identifier = prefs.getValue(UISingleContentViewerPortlet.IDENTIFIER, null);
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    Session session = provider.getSession(workspace, manageableRepository);
    Node webContentNode = null;
    try {
      webContentNode = session.getNodeByUUID(identifier);
    } catch (Exception e) {
      webContentNode = (Node) session.getItem(identifier);
    }
    setWebContent(webContentNode);
    NodeLocation nodeLocation = new NodeLocation();
    nodeLocation.setRepository(repositoryName);
    nodeLocation.setWorkspace(workspace);
    nodeLocation.setPath(webContentNode.getParent().getPath());
    setStoredLocation(nodeLocation);
    setNodePath(webContentNode.getPath());
    setContentType(webContentNode.getPrimaryNodeType().getName());
    addNew(false);
    resetProperties();
  }

  /**
   * Gets the web content.
   * 
   * @return the web content
   */
  public Node getWebContent () {
    return webContent;
  }

  /**
   * Sets the web content.
   * 
   * @param node the new web content
   */
  protected void setWebContent(Node node) {
    webContent = node;
  }

  /**
   * Sets the stored location.
   * 
   * @param location the new stored location
   */
  public void setStoredLocation(NodeLocation location) {
    storedLocation = location;
    setRepositoryName(location.getRepository());
    setWorkspace(location.getWorkspace());
    setStoredPath(location.getPath());
  }

  /**
   * Gets the saved node identifier.
   * 
   * @return the saved node identifier
   */
  public NodeIdentifier getSavedNodeIdentifier() {
    return savedNodeIdentifier;
  }  

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.form.UIDialogForm#getTemplate()
   */
  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser();
    try{
      return templateService.getTemplatePathByUser(true, contentType, userName, repositoryName);
    } catch(Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class);
      Object[] arg = {contentType};
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.not-support", arg, ApplicationMessage.ERROR));
      return null;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#getTemplateResourceResolver(org.exoplatform.webui.application.WebuiRequestContext, java.lang.String)
   */
  @SuppressWarnings("unused")
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
      if (resourceResolver == null) {
        RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
        ManageableRepository manageableRepository = repositoryService.getRepository(this.repositoryName);
        String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
        resourceResolver = new JCRResourceResolver(this.repositoryName, workspace, TemplateService.EXO_TEMPLATE_FILE_PROP);
      }
    }catch(Exception e) {
      if(UISingleContentViewerPortlet.scvLog.isDebugEnabled()) {
        UISingleContentViewerPortlet.scvLog.debug(e);
      }
    }
    return resourceResolver;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.form.UIDialogForm#onchange(org.exoplatform.webui.event.Event)
   */
  public void onchange(Event event) throws Exception {

  }

  /**
   * Gets the parent node.
   * 
   * @return the parent node
   * 
   * @throws Exception the exception
   */
  private Node getParentNode() throws Exception {
    String repository = storedLocation.getRepository();
    String path = storedLocation.getPath();
    String workspace = storedLocation.getWorkspace();
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    Session session = provider.getSession(workspace, manageableRepository);
    Node parentNode = (Node) session.getItem(path);
    return parentNode;
  }

  /**
   * Node is locked.
   * 
   * @param node the node
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  private boolean nodeIsLocked(Node node) throws Exception {
    if(!node.isLocked()) return false;        
    String lockToken = LockUtil.getLockToken(node);
    if(lockToken != null) {
      node.getSession().addLockToken(lockToken);
      return false;
    }                
    return true;
  }

  /**
   * The listener interface for receiving closeAction events.
   * The class that is interested in processing a cancelAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addCloseActionListener<code> method. When
   * the cancelAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CloseActionEvent
   */
  static public class CloseActionListener extends EventListener<UIContentDialogForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIContentDialogForm> event) throws Exception {
      UIContentDialogForm uiContentDialogForm = event.getSource();
      UIQuickCreationWizard uiQuickCreationWizard = uiContentDialogForm.getAncestorOfType(UIQuickCreationWizard.class);      
      if (uiContentDialogForm.isEditNotIntegrity()) {
        UIPortletConfig uiPortletConfig = uiQuickCreationWizard.getAncestorOfType(UIPortletConfig.class);
        uiPortletConfig.getChildren().clear();
        uiPortletConfig.addUIWelcomeScreen();
        return;
      }
      UIPortletConfig uiPortletConfig = uiContentDialogForm.getAncestorOfType(UIPortletConfig.class);      
      UINameWebContentForm uiNameWebContentForm = uiQuickCreationWizard.getChild(UINameWebContentForm.class);
      if(uiPortletConfig.isNewConfig()) {
        String portalName = Util.getUIPortal().getName();
        LivePortalManagerService livePortalManagerService = uiContentDialogForm.getApplicationComponent(LivePortalManagerService.class);
        SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
        Node portalNode = livePortalManagerService.getLivePortal(portalName, sessionProvider);
        WebSchemaConfigService webSchemaConfigService = uiNameWebContentForm.getApplicationComponent(WebSchemaConfigService.class);
        PortalFolderSchemaHandler handler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
        Node webContentStorage = handler.getWebContentStorage(portalNode);
        String webContentName = ((UIFormStringInput)uiNameWebContentForm.getChildById(UINameWebContentForm.NAME_WEBCONTENT)).getValue();
        Node webContentNode = webContentStorage.getNode(webContentName);
        Session session = webContentNode.getSession();
        webContentNode.remove();
        session.save();
        uiNameWebContentForm.reset();
      }
      
//      uiNameWebContentForm.back();
      uiContentDialogForm.reset();
//      uiQuickCreationWizard.viewStep(1);
//      UIPortletConfig portletConfig = uiQuickCreationWizard.getAncestorOfType(UIPortletConfig.class);
//      portletConfig.showPopup(event.getRequestContext());
      UIPortletConfig portletConfig = uiQuickCreationWizard.getAncestorOfType(UIPortletConfig.class);
      uiPortletConfig.closePopupAndUpdateUI(event.getRequestContext(),true);

    }
  }

  /**
   * The listener interface for receiving preferencesAction events.
   * The class that is interested in processing a preferencesAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addPreferencesActionListener<code> method. When
   * the PreferencesAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CancelActionEvent
   */
  static public class PreferencesActionListener extends EventListener<UIContentDialogForm> {
	  
	  /* (non-Javadoc)
	   * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
	   */
	  public void execute(Event<UIContentDialogForm> event) throws Exception {
	      UIContentDialogForm dialogForm = event.getSource();
	      UIQuickCreationWizard uiQuickWizard = dialogForm.getAncestorOfType(UIQuickCreationWizard.class);
	      UISocialInfo uiSocialInfo = uiQuickWizard.getChild(UISocialInfo.class);
	      uiSocialInfo.update();
	      uiQuickWizard.viewStep(3);
	      UIPortletConfig portletConfig = uiQuickWizard.getAncestorOfType(UIPortletConfig.class);
	      portletConfig.showPopup(event.getRequestContext());
	  }
  }
  
  /**
   * The listener interface for receiving saveAction events.
   * The class that is interested in processing a saveAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSaveDraftActionListener<code> method. When
   * the saveAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SaveDraftActionEvent
   */
  public static class SaveDraftActionListener extends EventListener<UIContentDialogForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIContentDialogForm> event) throws Exception {
      UIContentDialogForm dialogForm = event.getSource();
      UIApplication uiApplication = dialogForm.getAncestorOfType(UIApplication.class);
      PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
      PortletPreferences prefs = pContext.getRequest().getPreferences();
      String repositoryName = prefs.getValue(UISingleContentViewerPortlet.REPOSITORY, null);
      boolean isCheckOut = true;
      if (repositoryName != null) {
        String workspaceName = prefs.getValue(UISingleContentViewerPortlet.WORKSPACE, null);
        String identifier = prefs.getValue(UISingleContentViewerPortlet.IDENTIFIER, null);
        RepositoryService repositoryService = dialogForm.getApplicationComponent(RepositoryService.class);
        ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
        Session session = SessionProviderFactory.createSystemProvider().getSession(workspaceName, manageableRepository);
        Node webContentNode = null;
        try {
          webContentNode = session.getNodeByUUID(identifier);
        } catch (Exception e) {
          webContentNode = (Node) session.getItem(identifier);
        }
        if (dialogForm.nodeIsLocked(webContentNode)) {
          Object[] objs = { webContentNode.getPath() };
          uiApplication.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", objs));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
          return;
        }
        if (!webContentNode.isCheckedOut()) {
          isCheckOut = false;
          webContentNode.checkout();
        }
      }
      String nodeType;
      Node homeNode;
      List inputs = dialogForm.getChildren();
      Map inputProperties = DialogFormUtil.prepareMap(inputs, dialogForm.getInputProperties());
      if (dialogForm.isAddNew()) {
        homeNode = dialogForm.getParentNode();
        nodeType = dialogForm.contentType;
      } else {
        homeNode = dialogForm.getNode().getParent();
        nodeType = dialogForm.getNode().getPrimaryNodeType().getName();
      }
      Node newNode = null;
      try{
        CmsService cmsService = dialogForm.getApplicationComponent(CmsService.class);
        String addedPath = cmsService.storeNode(nodeType, homeNode, inputProperties, dialogForm.isAddNew, dialogForm.repositoryName);
        try{
          homeNode.save();
          newNode = (Node) homeNode.getSession().getItem(addedPath);
          event.getRequestContext().setAttribute("nodePath",newNode.getPath());
        }catch(Exception e) {
          if(UISingleContentViewerPortlet.scvLog.isDebugEnabled()) {
            UISingleContentViewerPortlet.scvLog.debug(e);
          }
        } 
      }catch(AccessControlException ace) {
        if(UISingleContentViewerPortlet.scvLog.isDebugEnabled()) {
          UISingleContentViewerPortlet.scvLog.debug(ace);
        }
      }catch(VersionException ve) {
        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      }catch(ItemNotFoundException item) {
        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.item-not-found", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      }catch(RepositoryException repo) {
        String key = "UIDocumentForm.msg.repository-exception";
        if (ItemExistsException.class.isInstance(repo)) key = "UIDocumentForm.msg.not-allowed-same-name-sibling";
        uiApplication.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      }catch(NumberFormatException nfe) {
        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.numberformat-exception", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      }catch(Exception e) {
        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.cannot-save", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      }
      dialogForm.savedNodeIdentifier = NodeIdentifier.make(newNode);
      dialogForm.setWebContent(newNode);

      if (!isCheckOut) {
        newNode.checkin();
      }
//      UIQuickCreationWizard uiQuickWizard = dialogForm.getAncestorOfType(UIQuickCreationWizard.class);
//      UISocialInfo uiSocialInfo = uiQuickWizard.getChild(UISocialInfo.class);
//      UIPermissionManager uiPermissionManager = uiSocialInfo.getChild(UIPermissionManager.class);
//      uiPermissionManager.getChild(UIPermissionInfo.class).updateGrid();
//      uiQuickWizard.viewStep(3);
//      UIPortletConfig portletConfig = uiQuickWizard.getAncestorOfType(UIPortletConfig.class);
//      portletConfig.showPopup(event.getRequestContext());
      
      UIPortletConfig uiPortletConfig = dialogForm.getAncestorOfType(UIPortletConfig.class);
      uiPortletConfig.closePopupAndUpdateUI(event.getRequestContext(),true);      
    }
  }
  
  /**
   * The listener interface for receiving fastPublishAction events.
   * The class that is interested in processing a cancelAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addFastPublishActionListener<code> method. When
   * the cancelAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see FastPublishActionEvent
   */
  public static class FastPublishActionListener extends EventListener<UIContentDialogForm> {
	  @Override
	public void execute(Event<UIContentDialogForm> event) throws Exception {
		  UIContentDialogForm dialogForm = event.getSource();
	      UIApplication uiApplication = dialogForm.getAncestorOfType(UIApplication.class);
	      PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
	      PortletPreferences prefs = pContext.getRequest().getPreferences();
	      String repositoryName = prefs.getValue(UISingleContentViewerPortlet.REPOSITORY, null);
	      boolean isCheckOut = true;
	      if (repositoryName != null) {
	        String workspaceName = prefs.getValue(UISingleContentViewerPortlet.WORKSPACE, null);
	        String identifier = prefs.getValue(UISingleContentViewerPortlet.IDENTIFIER, null);
	        RepositoryService repositoryService = dialogForm.getApplicationComponent(RepositoryService.class);
	        ManageableRepository manageableRepository = repositoryService.getRepository(repositoryName);
	        Session session = SessionProviderFactory.createSystemProvider().getSession(workspaceName, manageableRepository);
	        Node webContentNode = null;
	        try {
	          webContentNode = session.getNodeByUUID(identifier);
	        } catch (Exception e) {
	          webContentNode = (Node) session.getItem(identifier);
	        }
	        if (dialogForm.nodeIsLocked(webContentNode)) {
	          Object[] objs = { webContentNode.getPath() };
	          uiApplication.addMessage(new ApplicationMessage("UIPopupMenu.msg.node-locked", objs));
	          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
	          return;
	        }
	        if (!webContentNode.isCheckedOut()) {
	          isCheckOut = false;
	          webContentNode.checkout();
	        }
	      }
	      String nodeType;
	      Node homeNode;
	      List inputs = dialogForm.getChildren();
	      Map inputProperties = DialogFormUtil.prepareMap(inputs, dialogForm.getInputProperties());
	      if (dialogForm.isAddNew()) {
	        homeNode = dialogForm.getParentNode();
	        nodeType = dialogForm.contentType;
	      } else {
	        homeNode = dialogForm.getNode().getParent();
	        nodeType = dialogForm.getNode().getPrimaryNodeType().getName();
	      }
	      Node newNode = null;
	      try{
	        CmsService cmsService = dialogForm.getApplicationComponent(CmsService.class);
	        String addedPath = cmsService.storeNode(nodeType, homeNode, inputProperties, dialogForm.isAddNew, dialogForm.repositoryName);
	        try{
	          homeNode.save();
	          newNode = (Node) homeNode.getSession().getItem(addedPath);
	          event.getRequestContext().setAttribute("nodePath",newNode.getPath());
	        }catch(Exception e) {
	          if(UISingleContentViewerPortlet.scvLog.isDebugEnabled()) {
	            UISingleContentViewerPortlet.scvLog.debug(e);
	          }
	        } 
	      }catch(AccessControlException ace) {
//	      throw new AccessDeniedException(ace.getMessage());
	        if(UISingleContentViewerPortlet.scvLog.isDebugEnabled()) {
	          UISingleContentViewerPortlet.scvLog.debug(ace);
	        }
	      }catch(VersionException ve) {
	        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.in-versioning", null, ApplicationMessage.WARNING));
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
	      }catch(ItemNotFoundException item) {
	        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.item-not-found", null, ApplicationMessage.WARNING));
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
	      }catch(RepositoryException repo) {
	        String key = "UIDocumentForm.msg.repository-exception";
	        if (ItemExistsException.class.isInstance(repo)) key = "UIDocumentForm.msg.not-allowed-same-name-sibling";
	        uiApplication.addMessage(new ApplicationMessage(key, null, ApplicationMessage.WARNING));
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
	      }catch(NumberFormatException nfe) {
	        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.numberformat-exception", null, ApplicationMessage.WARNING));
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
	      }catch(Exception e) {
	        uiApplication.addMessage(new ApplicationMessage("UIDocumentForm.msg.cannot-save", null, ApplicationMessage.WARNING));
	        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
	      }
	      dialogForm.savedNodeIdentifier = NodeIdentifier.make(newNode);
	      dialogForm.setWebContent(newNode);

	      if (!isCheckOut) {
	        newNode.checkin();
	      }
	      PublicationService publicationService = dialogForm.getApplicationComponent(PublicationService.class);
	      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(Constant.LIFECYCLE_NAME);
	      HashMap<String, String> context = new HashMap<String, String>();
	      if(newNode != null) {
	    	  context.put(Constant.CURRENT_REVISION_NAME, newNode.getName());
	      }
	      publicationPlugin.changeState(newNode, Constant.LIVE_STATE, context);
//	      UIQuickCreationWizard uiQuickWizard = dialogForm.getAncestorOfType(UIQuickCreationWizard.class);
//	      UISocialInfo uiSocialInfo = uiQuickWizard.getChild(UISocialInfo.class);
//	      UIPermissionManager uiPermissionManager = uiSocialInfo.getChild(UIPermissionManager.class);
//	      uiPermissionManager.getChild(UIPermissionInfo.class).updateGrid();
//	      uiQuickWizard.viewStep(3);
//	      UIPortletConfig portletConfig = uiQuickWizard.getAncestorOfType(UIPortletConfig.class);
//	      portletConfig.showPopup(event.getRequestContext());
	      
	      UIPortletConfig uiPortletConfig = dialogForm.getAncestorOfType(UIPortletConfig.class);
	      uiPortletConfig.closePopupAndUpdateUI(event.getRequestContext(),true);

	}
  }

  /**
   * Checks if is edits the not integrity.
   * 
   * @return true, if is edits the not integrity
   */
  public boolean isEditNotIntegrity() {
    return isEditNotIntegrity;
  }

  /**
   * Sets the edits the not integrity.
   * 
   * @param isEditNotIntegrity the new edits the not integrity
   */
  public void setEditNotIntegrity(boolean isEditNotIntegrity) {
    this.isEditNotIntegrity = isEditNotIntegrity;
  }
}
