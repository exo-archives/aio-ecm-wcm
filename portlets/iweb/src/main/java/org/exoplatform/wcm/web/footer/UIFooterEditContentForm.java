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
package org.exoplatform.wcm.web.footer;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong_phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Aug 26, 2008  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIFooterEditContentForm.SaveActionListener.class),
      @EventConfig(listeners = UIFooterEditContentForm.CancelActionListener.class)
    }
)
public class UIFooterEditContentForm extends UIDialogForm {
  
  private static PortletPreferences portletPreferences = ((PortletRequestContext) WebuiRequestContext.getCurrentInstance()).getRequest().getPreferences();
  
  public void init() throws Exception {
    Node footerFolder = getSharedFooterFolder();
    setRepositoryName(((ManageableRepository) footerFolder.getSession().getRepository()).getConfiguration().getName());
    setWorkspace(footerFolder.getSession().getWorkspace().getName());
    setStoredPath(footerFolder.getPath());
    setContentType("exo:webContent");
    
    Session session = footerFolder.getSession();
    String nodeIdentifier = portletPreferences.getValue("nodeIdentifier", null);
    Node content = null;
    try {
      content = session.getNodeByUUID(nodeIdentifier);
    } catch (Exception e) {
      content = (Node) session.getItem(nodeIdentifier);
    }
    setNodePath(content.getPath());
    
    addNew(false);
  }
  
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext webuiRequestContext, String template) {
    try {
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(this.repositoryName);
      String workspaceName = manageableRepository.getConfiguration().getSystemWorkspaceName();
      return new JCRResourceResolver(this.repositoryName, workspaceName, TemplateService.EXO_TEMPLATE_FILE_PROP);
    } catch(Exception e) {}
    return super.getTemplateResourceResolver(webuiRequestContext, template);
  }
  
  private Node getSharedFooterFolder() throws Exception {
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    Node sharedPortalFolder = livePortalManagerService.getLiveSharedPortal(sessionProvider);
    
    WebSchemaConfigService webSchemaConfigService = getApplicationComponent(WebSchemaConfigService.class);
    PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    Node sharedFooterFolder = portalFolderSchemaHandler.getFooterThemes(sharedPortalFolder);
    
    return sharedFooterFolder;
  }
  
  public static class SaveActionListener extends EventListener<UIFooterEditContentForm> {
    public void execute(Event<UIFooterEditContentForm> event) throws Exception {
      UIFooterEditContentForm footerEditModeForm = event.getSource();
      footerEditModeForm.init();
      
      List<UIComponent> listComponent = footerEditModeForm.getChildren();
      Map<String, JcrInputProperty> inputProperties = DialogFormUtil.prepareMap(listComponent, footerEditModeForm.getInputProperties());

      Node homeNode = footerEditModeForm.getNode().getParent();
      String nodeType = footerEditModeForm.getNode().getPrimaryNodeType().getName();
      
      CmsService cmsService = footerEditModeForm.getApplicationComponent(CmsService.class);
      String footerWebContentPath = cmsService.storeNode(nodeType, homeNode, inputProperties, !footerEditModeForm.isEditing(), footerEditModeForm.repositoryName);
      Node footerWebContent = (Node) homeNode.getSession().getItem(footerWebContentPath);
      
      homeNode.save();
      
      portletPreferences.setValue("repository", footerEditModeForm.repositoryName) ;
      portletPreferences.setValue("workspace", footerWebContent.getSession().getWorkspace().getName()) ;
      portletPreferences.setValue("nodeIdentifier", footerWebContent.getUUID()) ;
      portletPreferences.store();
      
      event.getRequestContext().setAttribute("nodePath", footerWebContentPath);
      
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.VIEW);
    }
    
  }
  
  public static class CancelActionListener extends EventListener<UIFooterEditContentForm> {
    public void execute(Event<UIFooterEditContentForm> event) throws Exception {
      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      portletRequestContext.setApplicationMode(PortletMode.VIEW);
    }
  }
}
