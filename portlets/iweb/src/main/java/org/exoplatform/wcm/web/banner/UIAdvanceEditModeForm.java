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

import java.io.InputStream;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong_phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Aug 14, 2008  
 */
@ComponentConfig(
    id = "advanceEdit",
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAdvanceEditModeForm.SaveActionListener.class),
      @EventConfig(listeners = UIAdvanceEditModeForm.CancelActionListener.class)
    }
)
public class UIAdvanceEditModeForm extends UIForm {
  private final String DEFAULT_BANNER = "app:/groovy/banner/webui/UIBannerPortlet.gtmpl".intern();
  
  public UIAdvanceEditModeForm() throws Exception {
    UIFormTextAreaInput formTextAreaInput = new UIFormTextAreaInput("template", "template", loadTemplate());
    formTextAreaInput.setRows(10);
    formTextAreaInput.setColumns(60);
    addChild(formTextAreaInput);
  }
  
  private String loadTemplate() throws Exception {
    String template = null;
    
    String portalName = Util.getUIPortal().getName();
    LivePortalManagerService portalManagerService = getApplicationComponent(LivePortalManagerService.class);
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    Node portalFolder = portalManagerService.getLivePortal(portalName, sessionProvider);
    
    WebSchemaConfigService configService = getApplicationComponent(WebSchemaConfigService.class);
    PortalFolderSchemaHandler portalFolderSchemaHandler = configService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    Node bannerFolder = portalFolderSchemaHandler.getBannerThemes(portalFolder);
    
    if (bannerFolder.hasNode("banner.gtmpl")) {
      template = bannerFolder.getNode("banner.gtmpl").getNode("jcr:content").getProperty("jcr:data").getString();
    } else {
      PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
      InputStream inputStream = portletRequestContext.getApplication().getResourceResolver().getInputStream(DEFAULT_BANNER);
      template = IOUtil.getStreamContentAsString(inputStream);
    }
    sessionProvider.close();
    return template;
  }
  
  public static class SaveActionListener extends EventListener<UIAdvanceEditModeForm> {
    public void execute(Event<UIAdvanceEditModeForm> event) throws Exception {
      UIAdvanceEditModeForm advanceEditModeForm = event.getSource();
      String template = advanceEditModeForm.getUIFormTextAreaInput("template").getValue();
      
      String portalName = Util.getUIPortal().getName();
      LivePortalManagerService portalManagerService = advanceEditModeForm.getApplicationComponent(LivePortalManagerService.class);
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
      Node portalFolder = portalManagerService.getLivePortal(portalName, sessionProvider);
      Session session = portalFolder.getSession();
      String repository = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
      String workspace = session.getWorkspace().getName();
      String uuid = null;
      
      WebSchemaConfigService configService = advanceEditModeForm.getApplicationComponent(WebSchemaConfigService.class);
      PortalFolderSchemaHandler portalFolderSchemaHandler = configService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
      Node bannerFolder = portalFolderSchemaHandler.getBannerThemes(portalFolder);
      
      Node bannerNode = null;
      Node bannerContent = null;
      if (bannerFolder.hasNode("banner.gtmpl")) {
        bannerNode = bannerFolder.getNode("banner.gtmpl");
        bannerContent = bannerNode.getNode("jcr:content");
        bannerContent.setProperty("jcr:data", template);
        bannerContent.setProperty("jcr:lastModified", new Date().getTime()); 
      } else {
        bannerNode = bannerFolder.addNode("banner.gtmpl");
        bannerNode.addMixin("mix:referenceable");
        bannerContent = bannerNode.addNode("jcr:content", "nt:resource");
        bannerContent.setProperty("jcr:encoding", "UTF-8");
        bannerContent.setProperty("jcr:mimeType", "text/html");
        bannerContent.setProperty("jcr:data", template);
        bannerContent.setProperty("jcr:lastModified", new Date().getTime()); 
      }
      
      uuid = bannerNode.getUUID();
      
      portalFolder.getSession().save();
      
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      
      PortletPreferences portletPreferences = context.getRequest().getPreferences();
      portletPreferences.setValue("repository", repository) ;
      portletPreferences.setValue("workspace", workspace) ;
      portletPreferences.setValue("nodeUUID", uuid) ;
      portletPreferences.store();
      
      context.setApplicationMode(PortletMode.VIEW);
    }
  }
  
  public static class CancelActionListener extends EventListener<UIAdvanceEditModeForm> {
    public void execute(Event<UIAdvanceEditModeForm> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.VIEW);
    }
  }
}
