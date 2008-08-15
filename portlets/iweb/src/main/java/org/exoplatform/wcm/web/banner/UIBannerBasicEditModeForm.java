/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormUploadInput;

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
      @EventConfig(listeners = UIBannerBasicEditModeForm.SaveActionListener.class),
      @EventConfig(listeners = UIBannerBasicEditModeForm.CancelActionListener.class) 
    }
)
public class UIBannerBasicEditModeForm extends UIForm {

  public static final String DEFAULT_TEMPLATE = "app:/groovy/banner/UIBannerTemplate.gtmpl".intern();

  @SuppressWarnings("unchecked")
  public UIBannerBasicEditModeForm() throws Exception {
    setMultiPart(true);
    addUIFormInput(new UIFormUploadInput("logoPath", "logoPath")) ;        
    addUIFormInput(new UIFormStringInput("slogan", "slogan", null));
    UIFormCheckBoxInput checkBoxInput = new UIFormCheckBoxInput("quickEdit", "quickEdit", null );
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    String quickEdit = pContext.getRequest().getPreferences().getValue("quickEdit", "");    
    checkBoxInput.setChecked(Boolean.parseBoolean(quickEdit)) ;    
    addUIFormInput(checkBoxInput) ;         
  }

  public static class SaveActionListener extends EventListener<UIBannerBasicEditModeForm> {
    
    public void execute(Event<UIBannerBasicEditModeForm> event) throws Exception {
      UIBannerBasicEditModeForm bannerBasicEditModeForm = event.getSource();
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      
      String portalName = Util.getUIPortal().getName();
      LivePortalManagerService portalManagerService = bannerBasicEditModeForm.getApplicationComponent(LivePortalManagerService.class);
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
      Node portalFolder = portalManagerService.getLivePortal(portalName, sessionProvider);
      Session session = portalFolder.getSession();
      String repository = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
      String workspace = session.getWorkspace().getName();
      String nodeUUID = null;
   
      WebSchemaConfigService configService = bannerBasicEditModeForm.getApplicationComponent(WebSchemaConfigService.class);
      PortalFolderSchemaHandler portalFolderSchemaHandler = configService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
      Node bannerFolder = portalFolderSchemaHandler.getBannerThemes(portalFolder);
      
      UIFormUploadInput logo = (UIFormUploadInput)bannerBasicEditModeForm.getUIInput("logoPath");
      InputStream logoData = logo.getUploadDataAsStream();      
      if (logoData == null){
        UIApplication uiApp = bannerBasicEditModeForm.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UIBasicEditModeForm.msg.logoPath", null, ApplicationMessage.ERROR)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      String logoPath = "/portal/rest/jcr/" + repository + "/" + workspace; 
      Node logoNode = null;
      Node logoContent = null;
      if(bannerFolder.hasNode("logo.jpg")) {
        logoNode = bannerFolder.getNode("logo.jpg");
        logoContent = logoNode.getNode("jcr:content");
      }else {
        logoNode = bannerFolder.addNode("logo.jpg", "nt:file");
        logoContent = logoNode.addNode("jcr:content", "nt:resource");
      }
      logoContent.setProperty("jcr:encoding", "UTF-8");
      MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
      String logoType = mimeTypeResolver.getMimeType("logo.jpg");
      logoContent.setProperty("jcr:mimeType", logoType);
      logoContent.setProperty("jcr:data", logoData);
      logoContent.setProperty("jcr:lastModified", new Date().getTime());
      logoPath = logoPath.concat(logoNode.getPath());
      
      String slogan = bannerBasicEditModeForm.getUIStringInput("slogan").getValue();
      
      InputStream inputStream = event.getRequestContext().getApplication().getResourceResolver().getInputStream(UIBannerBasicEditModeForm.DEFAULT_TEMPLATE);
      String bannerTemplate = IOUtil.getStreamContentAsString(inputStream);
      
      bannerTemplate = bannerTemplate.replaceAll("\\{logoPath\\}", logoPath);
      bannerTemplate = bannerTemplate.replaceAll("\\{slogan\\}", slogan);
      
      Node bannerNode = null;
      Node bannerContent = null;
      if (bannerFolder.hasNode("banner.gtmpl")) {
        bannerNode = bannerFolder.getNode("banner.gtmpl");
        bannerContent = bannerNode.getNode("jcr:content");
        bannerContent.setProperty("jcr:data", bannerTemplate);
        bannerContent.setProperty("jcr:lastModified", new Date().getTime());
      } else {
        bannerNode = bannerFolder.addNode("banner.gtmpl", "nt:file");
        bannerNode.addMixin("mix:referenceable");
        bannerContent = bannerNode.addNode("jcr:content", "nt:resource");
        bannerContent.setProperty("jcr:encoding", "UTF-8");
        bannerContent.setProperty("jcr:mimeType", "text/html");
        bannerContent.setProperty("jcr:data", bannerTemplate);
        bannerContent.setProperty("jcr:lastModified", new Date().getTime());
      }
      
      nodeUUID = bannerNode.getUUID();
      
      session.save();
      
      PortletPreferences portletPreferences = context.getRequest().getPreferences();
      boolean quickEdit = bannerBasicEditModeForm.getUIFormCheckBoxInput("quickEdit").isChecked();
      portletPreferences.setValue("quickEdit", Boolean.toString(quickEdit));
      portletPreferences.setValue("repository", repository) ;
      portletPreferences.setValue("workspace", workspace) ;
      portletPreferences.setValue("nodeUUID", nodeUUID) ;
      portletPreferences.store();
      
      sessionProvider.close();
      
      context.setApplicationMode(PortletMode.VIEW);
    }
  }

  public static class CancelActionListener extends EventListener<UIBannerBasicEditModeForm> {
    public void execute(Event<UIBannerBasicEditModeForm> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.VIEW);
    }
  }
}