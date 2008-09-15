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
package org.exoplatform.wcm.web.banner;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
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
 * Aug 21, 2008  
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIBannerEditModeForm.SaveActionListener.class),
      @EventConfig(listeners = UIBannerEditModeForm.CancelActionListener.class)
    }
)
public class UIBannerEditModeForm extends UIDialogForm {

  private static PortletPreferences portletPreferences = ((PortletRequestContext) WebuiRequestContext.getCurrentInstance()).getRequest().getPreferences();
  
  public void init() throws Exception {
    Node bannerFolder = getSharedBannerFolder();
    setRepositoryName(((ManageableRepository)(bannerFolder.getSession().getRepository())).getConfiguration().getName());
    setWorkspace(bannerFolder.getSession().getWorkspace().getName());
    setStoredPath(bannerFolder.getPath());    
    setContentType("exo:webContent");
    setNodePath(bannerFolder.getSession().getNodeByUUID(portletPreferences.getValue("nodeUUID", null)).getPath());
    addNew(false);
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext webuiRequestContext, String template) {    
    try {
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(this.repositoryName);
      String workspaceName = manageableRepository.getConfiguration().getSystemWorkspaceName();
      return new JCRResourceResolver(this.repositoryName, workspaceName, TemplateService.EXO_TEMPLATE_FILE_PROP);
    } catch(Exception e){}
    return super.getTemplateResourceResolver(webuiRequestContext, template);
  }

  private Node getSharedBannerFolder() throws Exception {
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    Node sharedPortalFolder = livePortalManagerService.getLiveSharedPortal(sessionProvider);

    WebSchemaConfigService webSchemaConfigService = getApplicationComponent(WebSchemaConfigService.class);
    PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    Node sharedBannerFolder = portalFolderSchemaHandler.getBannerThemes(sharedPortalFolder);
    
    return sharedBannerFolder;
  }
  
  public static class SaveActionListener extends EventListener<UIBannerEditModeForm> {
    public void execute(Event<UIBannerEditModeForm> event) throws Exception {
      UIBannerEditModeForm bannerEditModeForm = event.getSource();
      bannerEditModeForm.init();
      
      List<UIComponent> listComponent = bannerEditModeForm.getChildren();
      Map<String, JcrInputProperty> inputProperties = DialogFormUtil.prepareMap(listComponent, bannerEditModeForm.getInputProperties());

      Node homeNode = bannerEditModeForm.getNode().getParent();
      String nodeType = bannerEditModeForm.getNode().getPrimaryNodeType().getName();
      
      CmsService cmsService = bannerEditModeForm.getApplicationComponent(CmsService.class);
      String bannerWebContentPath = cmsService.storeNode(nodeType, homeNode, inputProperties, !bannerEditModeForm.isEditing(), bannerEditModeForm.repositoryName);
      Node bannerWebContent = (Node) homeNode.getSession().getItem(bannerWebContentPath);
      
      homeNode.save();
      
      portletPreferences.setValue("repository", bannerEditModeForm.repositoryName) ;
      portletPreferences.setValue("workspace", bannerWebContent.getSession().getWorkspace().getName()) ;
      portletPreferences.setValue("nodeUUID", bannerWebContent.getUUID()) ;
      portletPreferences.store();
      
      event.getRequestContext().setAttribute("nodePath", bannerWebContentPath);
      
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.VIEW);
    }
  }

  public static class CancelActionListener extends EventListener<UIBannerEditModeForm> {
    public void execute(Event<UIBannerEditModeForm> event) throws Exception {
      PortletRequestContext portletRequestContext = (PortletRequestContext)event.getRequestContext();
      portletRequestContext.setApplicationMode(PortletMode.VIEW);
    }
  }

}
