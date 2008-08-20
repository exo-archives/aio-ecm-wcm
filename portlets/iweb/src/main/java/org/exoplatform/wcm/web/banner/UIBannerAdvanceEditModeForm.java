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
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormTabPane;
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
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UIBannerAdvanceEditModeForm.SaveActionListener.class),
      @EventConfig(listeners = UIBannerAdvanceEditModeForm.CancelActionListener.class)
    }
)
public class UIBannerAdvanceEditModeForm extends UIFormTabPane {
  
  private final String DEFAULT_HTML = "app:/groovy/banner/resources/banner.html".intern();
  private final String DEFAULT_CSS = "app:/groovy/banner/resources/BannerStylesheet.css".intern();
  private final String DEFAULT_LOGIN = "app:/groovy/banner/resources/LoginFragment.gtmpl".intern();
  
  public UIBannerAdvanceEditModeForm() throws Exception {
    super("UIBannerAdvanceEditModeForm");
    
    UIFormTextAreaInput htmlTextAreaInput = new UIFormTextAreaInput("htmlTemplate", "htmlTemplate", loadHtml());
    htmlTextAreaInput.setRows(10);
    htmlTextAreaInput.setColumns(60);
    addChild(htmlTextAreaInput);
    
    UIFormTextAreaInput cssTextAreaInput = new UIFormTextAreaInput("cssTemplate", "cssTemplate", loadStyle());
    cssTextAreaInput.setRows(10);
    cssTextAreaInput.setColumns(60);
    addChild(cssTextAreaInput);
    
    setSelectedTab(htmlTextAreaInput.getId());
  }
  
  private Node getBannerWebContent() throws Exception {
    PortletRequestContext portletRequestContext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
    String repository = portletPreferences.getValue("repository", null);
    String workspace = portletPreferences.getValue("workspace", null);
    String nodeUUID = portletPreferences.getValue("nodeUUID", null);
    
    if (repository != null && workspace != null & nodeUUID != null) {
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      return session.getNodeByUUID(nodeUUID);
    }
    
    return null;
  }
  
  private String loadHtml() throws Exception {
    Node bannerWebContent = getBannerWebContent();
    if (bannerWebContent != null && bannerWebContent.hasNode("default.html")) {
      return bannerWebContent.getNode("default.html").getNode("jcr:content").getProperty("jcr:data").getString();
    } else {
      PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
      InputStream inputStream = portletRequestContext.getApplication().getResourceResolver().getInputStream(DEFAULT_HTML);
      return IOUtil.getStreamContentAsString(inputStream);
    }
  }
  
  private String loadStyle() throws Exception {
    Node bannerWebContent = getBannerWebContent();
    if (bannerWebContent != null && bannerWebContent.hasNode("css") && bannerWebContent.getNode("css").hasNode("default.css")) {
      return bannerWebContent.getNode("css").getNode("default.css").getNode("jcr:content").getProperty("jcr:data").getString();
    } else {
      PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
      InputStream inputStream = portletRequestContext.getApplication().getResourceResolver().getInputStream(DEFAULT_CSS);
      return IOUtil.getStreamContentAsString(inputStream);
    }
  }
  
  private String loadLogin() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    InputStream inputStream = portletRequestContext.getApplication().getResourceResolver().getInputStream(DEFAULT_LOGIN);
    return IOUtil.getStreamContentAsString(inputStream);
  }
  
  public static class SaveActionListener extends EventListener<UIBannerAdvanceEditModeForm> {
    
    public void execute(Event<UIBannerAdvanceEditModeForm> event) throws Exception {
      UIBannerAdvanceEditModeForm bannerAdvanceEditModeForm = event.getSource();
      String htmlContent = bannerAdvanceEditModeForm.getUIFormTextAreaInput("htmlTemplate").getValue();
      String cssContent = bannerAdvanceEditModeForm.getUIFormTextAreaInput("cssTemplate").getValue();
      String loginContent = bannerAdvanceEditModeForm.loadLogin();
      
      String portalName = Util.getUIPortal().getName();
      LivePortalManagerService livePortalManagerService = bannerAdvanceEditModeForm.getApplicationComponent(LivePortalManagerService.class);
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
      Node portalFolder = livePortalManagerService.getLivePortal(portalName, sessionProvider);

      WebSchemaConfigService webSchemaConfigService = bannerAdvanceEditModeForm.getApplicationComponent(WebSchemaConfigService.class);
      PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
      Node bannerFolder = portalFolderSchemaHandler.getBannerThemes(portalFolder);
      System.out.println(bannerFolder.getPath());
      
      Node bannerWebContent = null;
      if (bannerFolder.hasNode("banner")) {
        bannerWebContent = bannerFolder.getNode("banner");
      } else {
        bannerWebContent = bannerFolder.addNode("banner", "exo:webContent");
      }
      
      Session session = bannerWebContent.getSession();
      String repository = ((ManageableRepository) session.getRepository()).getConfiguration().getName();
      String workspace = session.getWorkspace().getName();
      String nodeUUID = null;
      
      if (bannerWebContent.hasNode("default.html")) {
        Node defaultHTML = bannerWebContent.getNode("default.html");
        Node defaultHTMLContent = defaultHTML.getNode("jcr:content");
        defaultHTMLContent.setProperty("jcr:data", htmlContent);
        defaultHTMLContent.setProperty("jcr:lastModified", new Date().getTime());
      } else {
        Node defaultHTML = bannerWebContent.addNode("default.html", "nt:file");
        defaultHTML.addMixin("exo:htmlFile");
        Node defaultHTMLContent = defaultHTML.addNode("jcr:content", "nt:resource");
        defaultHTMLContent.setProperty("jcr:encoding", "UTF-8");
        defaultHTMLContent.setProperty("jcr:mimeType", "text/html");
        defaultHTMLContent.setProperty("jcr:data", htmlContent);
        defaultHTMLContent.setProperty("jcr:lastModified", new Date().getTime()); 
      }
      
      if (bannerWebContent.hasNode("css")) {
        Node CSSFolder = bannerWebContent.getNode("css");
        if (CSSFolder.hasNode("default.css")) {
          Node defaultCSS = CSSFolder.getNode("default.css"); 
          Node defaultCSSContent = defaultCSS.getNode("jcr:content");
          defaultCSSContent.setProperty("jcr:data", cssContent);
          defaultCSSContent.setProperty("jcr:lastModified", new Date().getTime()); 
        } else {
          Node defaultCSS = CSSFolder.addNode("default.css", "nt:file"); 
          Node defaultCSSContent = defaultCSS.addNode("jcr:content", "nt:resource");
          defaultCSSContent.setProperty("jcr:encoding", "UTF-8");
          defaultCSSContent.setProperty("jcr:mimeType", "text/css");
          defaultCSSContent.setProperty("jcr:data", cssContent);
          defaultCSSContent.setProperty("jcr:lastModified", new Date().getTime()); 
        }
      } else {
        Node CSSFolder = bannerWebContent.addNode("css", "exo:cssFolder");
        Node defaultCSS = CSSFolder.addNode("default.css", "nt:file"); 
        Node defaultCSSContent = defaultCSS.addNode("jcr:content", "nt:resource");
        defaultCSSContent.setProperty("jcr:encoding", "UTF-8");
        defaultCSSContent.setProperty("jcr:mimeType", "text/css");
        defaultCSSContent.setProperty("jcr:data", cssContent);
        defaultCSSContent.setProperty("jcr:lastModified", new Date().getTime()); 
      }
      
      if (bannerWebContent.hasNode("documents")) {
        Node documentFolder = bannerWebContent.getNode("documents");
        if (documentFolder.hasNode("access.gtmpl")) {
          Node accessGTMPL = documentFolder.getNode("access.gtmpl"); 
          Node accessGTMPLContent = accessGTMPL.getNode("jcr:content");
          accessGTMPLContent.setProperty("jcr:data", loginContent);
          accessGTMPLContent.setProperty("jcr:lastModified", new Date().getTime()); 
        } else {
          Node accessGTMPL = documentFolder.addNode("access.gtmpl", "nt:file"); 
          Node accessGTMPLContent = accessGTMPL.addNode("jcr:content", "nt:resource");
          accessGTMPLContent.setProperty("jcr:encoding", "UTF-8");
          accessGTMPLContent.setProperty("jcr:mimeType", "text/plain");
          accessGTMPLContent.setProperty("jcr:data", loginContent);
          accessGTMPLContent.setProperty("jcr:lastModified", new Date().getTime()); 
        }
      } else {
        Node documentFolder = bannerWebContent.addNode("documents", "nt:unstructured");
        documentFolder.addMixin("exo:documentFolder");
        Node accessGTMPL = documentFolder.addNode("access.gtmpl", "nt:file"); 
        Node accessGTMPLContent = accessGTMPL.addNode("jcr:content", "nt:resource");
        accessGTMPLContent.setProperty("jcr:encoding", "UTF-8");
        accessGTMPLContent.setProperty("jcr:mimeType", "text/plain");
        accessGTMPLContent.setProperty("jcr:data", loginContent);
        accessGTMPLContent.setProperty("jcr:lastModified", new Date().getTime()); 
      }
      
      nodeUUID = bannerWebContent.getUUID();
      
      session.save();
      
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      
      PortletPreferences portletPreferences = context.getRequest().getPreferences();
      portletPreferences.setValue("repository", repository) ;
      portletPreferences.setValue("workspace", workspace) ;
      portletPreferences.setValue("nodeUUID", nodeUUID) ;
      portletPreferences.store();

      context.setApplicationMode(PortletMode.VIEW);
    }
  }
  
  public static class CancelActionListener extends EventListener<UIBannerAdvanceEditModeForm> {
    public void execute(Event<UIBannerAdvanceEditModeForm> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.VIEW);
    }
  }
}
