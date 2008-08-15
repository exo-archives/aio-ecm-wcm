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
 * Aug 15, 2008  
 */
@ComponentConfig (
    id = "advanceEdit",
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIFooterAdvanceEditModeForm.SaveActionListener.class),
      @EventConfig(listeners = UIFooterAdvanceEditModeForm.CancelActionListener.class)
    }
)
public class UIFooterAdvanceEditModeForm extends UIForm {
  
  private final String DEFAULT_FOOTER = "app:/groovy/footer/webui/UIFooterPortlet.gtmpl".intern(); 
  
  public UIFooterAdvanceEditModeForm() throws Exception {
    UIFormTextAreaInput formTextAreaInput = new UIFormTextAreaInput("template", "template", loadTemplate());
    formTextAreaInput.setColumns(60);
    formTextAreaInput.setRows(10);
    addChild(formTextAreaInput);
  }
  
  private String loadTemplate() throws Exception {
    String template = null;
    
    String portalName = Util.getUIPortal().getName();
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider(); 
    Node portalFolder = livePortalManagerService.getLivePortal(portalName, sessionProvider);
    
    WebSchemaConfigService webSchemaConfigService = getApplicationComponent(WebSchemaConfigService.class);
    PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    Node footerFolder = portalFolderSchemaHandler.getFooterThemes(portalFolder);
    
    if (footerFolder.hasNode("footer.gtmpl")) {
      template = footerFolder.getNode("footer.gtmpl").getNode("jcr:content").getProperty("jcr:data").getString();
    } else {
      PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
      InputStream inputStream = portletRequestContext.getApplication().getResourceResolver().getInputStream(DEFAULT_FOOTER);
      template = IOUtil.getStreamContentAsString(inputStream);
    }
    
    sessionProvider.close();
    
    return template;
  }
  
  public static class SaveActionListener extends EventListener<UIFooterAdvanceEditModeForm> {
    public void execute(Event<UIFooterAdvanceEditModeForm> event) throws Exception {
      UIFooterAdvanceEditModeForm footerAdvanceEditModeForm = event.getSource();
      String template = footerAdvanceEditModeForm.getUIFormTextAreaInput("template").getValue();
      
      String portalName = Util.getUIPortal().getName();
      LivePortalManagerService livePortalManagerService = footerAdvanceEditModeForm.getApplicationComponent(LivePortalManagerService.class);
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
      Node portalFolder = livePortalManagerService.getLivePortal(portalName, sessionProvider);
      Session session = portalFolder.getSession();
      String repository = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
      String workspace = session.getWorkspace().getName();
      String uuid = null;
      
      WebSchemaConfigService webSchemaConfigService = footerAdvanceEditModeForm.getApplicationComponent(WebSchemaConfigService.class);
      PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
      Node footerFolder = portalFolderSchemaHandler.getFooterThemes(portalFolder);
      
      Node footerNode = null;
      Node footerContent = null;
      if (footerFolder.hasNode("footer.gtmpl")) {
        footerNode = footerFolder.getNode("footer.gtmpl");
        footerContent = footerNode.getNode("jcr:content");
        footerContent.setProperty("jcr:data", template);
        footerContent.setProperty("jcr:lastModified", new Date().getTime());
      } else {
        footerNode = footerFolder.addNode("footer.gtmpl", "nt:file");
        footerNode.addMixin("mix:referenceable");
        footerContent = footerNode.addNode("jcr:content", "nt:resource");
        footerContent.setProperty("jcr:encoding", "UTF-8");
        footerContent.setProperty("jcr:mimeType", "text/html");
        footerContent.setProperty("jcr:data", template);
        footerContent.setProperty("jcr:lastModified", new Date().getTime());
      }
      
      uuid = footerNode.getUUID();
      
      portalFolder.getSession().save();
      sessionProvider.close();
      
      PortletRequestContext context = (PortletRequestContext)event.getRequestContext();
      PortletPreferences portletPreferences = context.getRequest().getPreferences();
      portletPreferences.setValue("repository", repository);
      portletPreferences.setValue("workspace", workspace);
      portletPreferences.setValue("nodeUUID", uuid);
      portletPreferences.store();
      
      context.setApplicationMode(PortletMode.VIEW);
    }
  }
  
  public static class CancelActionListener extends EventListener<UIFooterAdvanceEditModeForm> {
    public void execute(Event<UIFooterAdvanceEditModeForm> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext)event.getRequestContext();
      context.setApplicationMode(PortletMode.VIEW);
    }
  }
}
