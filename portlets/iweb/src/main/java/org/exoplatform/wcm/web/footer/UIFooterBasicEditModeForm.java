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
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Author : Do Ngoc Anh *      
 * Email: anh.do@exoplatform.com *
 * May 9, 2008  
 */
@ComponentConfig(
    id = "basicEdit",
    lifecycle = UIFormLifecycle.class,
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIFooterBasicEditModeForm.SaveActionListener.class),
      @EventConfig(listeners = UIFooterBasicEditModeForm.CancelActionListener.class) 
    }
)
public class UIFooterBasicEditModeForm extends UIForm {
  
  public static final String DEFAULT_TEMPLATE = "app:/groovy/footer/UIFooterTemplate.gtmpl".intern();
  
  @SuppressWarnings("unchecked")
  public UIFooterBasicEditModeForm() throws Exception {
    addUIFormInput(new UIFormStringInput("footerText", "footerText", null));
    UIFormCheckBoxInput checkBoxInput = new UIFormCheckBoxInput("quickEdit", "quickEdit", null );
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    String quickEdit = pContext.getRequest().getPreferences().getValue("quickEdit", "");
    checkBoxInput.setChecked(Boolean.parseBoolean(quickEdit)) ;
    addUIFormInput(checkBoxInput) ;
  }

  public void setQuickEditChecked(boolean checked){
    getUIFormCheckBoxInput("quickEdit").setChecked(checked);
  }

  public static class SaveActionListener extends EventListener<UIFooterBasicEditModeForm> {

    public void execute(Event<UIFooterBasicEditModeForm> event) throws Exception {
      UIFooterBasicEditModeForm footerBasicEditModeForm = event.getSource();
      String footerText = footerBasicEditModeForm.getUIStringInput("footerText").getValue();
      boolean quickEdit = footerBasicEditModeForm.getUIFormCheckBoxInput("quickEdit").isChecked();
      
      String portalName = Util.getUIPortal().getName();
      LivePortalManagerService livePortalManagerService = footerBasicEditModeForm.getApplicationComponent(LivePortalManagerService.class);
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
      Node portalFolder = livePortalManagerService.getLivePortal(portalName, sessionProvider);
      Session session = portalFolder.getSession();
      String repository = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
      String workspace = session.getWorkspace().getName();
      String uuid = null;
      
      WebSchemaConfigService webSchemaConfigService = footerBasicEditModeForm.getApplicationComponent(WebSchemaConfigService.class);
      PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
      Node footerFolder = portalFolderSchemaHandler.getFooterThemes(portalFolder);

      InputStream inputStream = event.getRequestContext().getApplication().getResourceResolver().getInputStream(DEFAULT_TEMPLATE);
      String footerTemplate = IOUtil.getStreamContentAsString(inputStream);
      
      footerTemplate = footerTemplate.replaceAll("\\{footerText\\}", footerText);
      
      Node footerNode = null;
      Node footerContent = null;
      if (footerFolder.hasNode("footer.gtmpl")) {
        footerNode = footerFolder.getNode("footer.gtmpl");
        footerContent = footerNode.getNode("jcr:content");
        footerContent.setProperty("jcr:data", footerText);
        footerContent.setProperty("jcr:lastModified", new Date().getTime());
      } else {
        footerNode = footerFolder.addNode("footer.gtmpl", "nt:file");
        footerNode.addMixin("mix:referenceable");
        footerContent = footerNode.addNode("jcr:content", "nt:resource");
        footerContent.setProperty("jcr:encoding", "UTF-8");
        footerContent.setProperty("jcr:mimeType", "text/html");
        footerContent.setProperty("jcr:data", footerTemplate);
        footerContent.setProperty("jcr:lastModified", new Date().getTime());
      }
      
      uuid = footerNode.getUUID();
      
      session.save();
      
      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
      
      portletPreferences.setValue("quickEdit", Boolean.toString(quickEdit));
      portletPreferences.setValue("repository",repository) ;
      portletPreferences.setValue("workspace",workspace) ;
      portletPreferences.setValue("nodeUUID",uuid) ;
      portletPreferences.store();
      
      portletRequestContext.setApplicationMode(PortletMode.VIEW);
    }

  }

  public static class CancelActionListener extends EventListener<UIFooterBasicEditModeForm> {
    public void execute(Event<UIFooterBasicEditModeForm> event) throws Exception {
      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      portletRequestContext.setApplicationMode(PortletMode.VIEW);
    }
  }
  
}
