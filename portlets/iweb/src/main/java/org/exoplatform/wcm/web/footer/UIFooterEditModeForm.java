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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.html.HTMLNode;
import org.exoplatform.services.html.parser.HTMLParser;
import org.exoplatform.services.html.util.HyperLinkUtil;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.services.wcm.webcontent.WebContentSchemaHandler;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong_phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Aug 26, 2008  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIFooterEditModeForm.SaveActionListener.class),
      @EventConfig(listeners = UIFooterEditModeForm.CancelActionListener.class)
    }
)
public class UIFooterEditModeForm extends UIDialogForm {
  
  private final String DEFAULT_HTML = "app:/groovy/footer/resources/footer.html".intern();
  private final String DEFAULT_CSS = "app:/groovy/footer/resources/FooterStylesheet.css".intern();
  
  public void init() throws Exception {
    Node footerFolder = getFooterFolder();
    setRepositoryName(((ManageableRepository) footerFolder.getSession().getRepository()).getConfiguration().getName());
    setWorkspace(footerFolder.getSession().getWorkspace().getName());
    setStoredPath(footerFolder.getPath());
    setContentType("exo:webContent");
    if (footerFolder.hasNode("footer")) {
      setNodePath(footerFolder.getNode("footer").getPath());
      addNew(false);
    } else {
      addNew(true);
    }
  }
  
  public void renderField(String name) throws Exception {
    if (isAddNew()) {
      String jcrPath = ((JcrInputProperty) getInputProperty(name)).getJcrPath();
      if ("/node".equals(jcrPath)) {
        UIFormInputBase<String> formInputBase = findComponentById(name);
        formInputBase.setValue("footer");
      } else if ("/node/default.html/jcr:content/jcr:data".equals(jcrPath)) {
        UIFormInputBase<String> formInputBase = findComponentById(name);
        formInputBase.setValue(loadHtml());
      } else if ("/node/css/default.css/jcr:content/jcr:data".equals(jcrPath)) {
        UIFormInputBase<String> formInputBase = findComponentById(name);
        formInputBase.setValue(loadCss());
      }
    }
    super.renderField(name);
  }
  
  private String loadHtml() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    InputStream inputStream = portletRequestContext.getApplication().getResourceResolver().getInputStream(DEFAULT_HTML);
    String htmlContent = IOUtil.getStreamContentAsString(inputStream);
    return htmlContent;
  }
  
  private String loadCss() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    InputStream inputStream = portletRequestContext.getApplication().getResourceResolver().getInputStream(DEFAULT_CSS);
    String cssContent = IOUtil.getStreamContentAsString(inputStream);
    return cssContent;
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
  
  private Node getFooterFolder() throws Exception {
    String portalName = Util.getUIPortal().getName();
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    Node portalFolder = livePortalManagerService.getLivePortal(portalName, sessionProvider);
    
    WebSchemaConfigService webSchemaConfigService = getApplicationComponent(WebSchemaConfigService.class);
    PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    Node footerFolder = portalFolderSchemaHandler.getFooterThemes(portalFolder);
    
    return footerFolder;
  }
  
  public static class SaveActionListener extends EventListener<UIFooterEditModeForm> {
    private final String SERVLET_HOME = "/iweb";
    
    public void execute(Event<UIFooterEditModeForm> event) throws Exception {
      UIFooterEditModeForm footerEditModeForm = event.getSource();
      footerEditModeForm.init();
      
      List<UIComponent> listComponent = footerEditModeForm.getChildren();
      Map<String, JcrInputProperty> inputProperties = DialogFormUtil.prepareMap(listComponent, footerEditModeForm.getInputProperties());

      String nodeType = null;
      Node homeNode = null;
      if (!footerEditModeForm.isAddNew()) {
        homeNode = footerEditModeForm.getNode().getParent();
        nodeType = footerEditModeForm.getNode().getPrimaryNodeType().getName();
      } else {
        homeNode = footerEditModeForm.getFooterFolder();
        nodeType = "exo:webContent";
      }
      CmsService cmsService = footerEditModeForm.getApplicationComponent(CmsService.class);
      String footerWebContentPath = cmsService.storeNode(nodeType, homeNode, inputProperties, !footerEditModeForm.isEditing(), footerEditModeForm.repositoryName);
      Node footerWebContent = (Node) homeNode.getSession().getItem(footerWebContentPath);
      
      WebSchemaConfigService webSchemaConfigService = footerEditModeForm.getApplicationComponent(WebSchemaConfigService.class);
      WebContentSchemaHandler webContentSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(WebContentSchemaHandler.class);
      Node cssFolder = webContentSchemaHandler.getCSSFolder(footerWebContent);
      Node imageFolder = webContentSchemaHandler.getImagesFolders(footerWebContent);
      
      String htmlContent = footerWebContent.getProperty("default.html/jcr:content/jcr:data").getString();
      List<String> listImageUrl = getListImageUrl(htmlContent);
      for (String imageUrl : listImageUrl) {
        String newImageUrl = saveImage2JCR(imageUrl, imageFolder);
        if (newImageUrl != null) {
          htmlContent = htmlContent.replaceAll(imageUrl, newImageUrl);
        }
      }
      footerWebContent.getNode("default.html/jcr:content").setProperty("jcr:data", htmlContent);
      
      String cssContent = cssFolder.getProperty("default.css/jcr:content/jcr:data").getString();
      listImageUrl = getListImageUrl(cssContent);
      for (String imageUrl : listImageUrl) {
        String newImageUrl = saveImage2JCR(imageUrl, imageFolder);
        if (newImageUrl != null) {
          cssContent = cssContent.replaceAll(imageUrl, "'" + newImageUrl + "'");
        }
      }
      cssFolder.getNode("default.css/jcr:content").setProperty("jcr:data", cssContent);
      
      homeNode.save();
      
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      
      PortletPreferences portletPreferences = context.getRequest().getPreferences();
      portletPreferences.setValue("repository", footerEditModeForm.repositoryName) ;
      portletPreferences.setValue("workspace", footerWebContent.getSession().getWorkspace().getName()) ;
      portletPreferences.setValue("nodeUUID", footerWebContent.getUUID()) ;
      portletPreferences.store();
      event.getRequestContext().setAttribute("nodePath", footerWebContentPath);
      
      context.setApplicationMode(PortletMode.VIEW);
    }
    
    private List<String> getListImageUrl(String content) throws Exception {
      List<String> listImageUrl = new ArrayList<String>();
      
      // Get image url from html
      HTMLNode htmlRootNode = HTMLParser.createDocument(content).getRoot();
      HyperLinkUtil linkUtil = new HyperLinkUtil();
      for (Iterator<String> iterLink = linkUtil.getImageLink(htmlRootNode).iterator(); iterLink.hasNext();) {
        String imageLink = iterLink.next();
        if (imageLink.startsWith(SERVLET_HOME)) {
          listImageUrl.add(imageLink);
        }
      }
      
      // Get image url from css
      Pattern pattern = Pattern.compile("url([^)]+)\\S");
      Matcher matcher = pattern.matcher(content);
      while (matcher.find()) {
        String temp = matcher.group();
        temp = temp.replaceAll("url\\(", "").replaceAll("\\)", "");
        listImageUrl.add(temp);
      }
      
      return listImageUrl;
    }
    
    private String saveImage2JCR(String imagePath, Node imageFolder) throws Exception {
      imagePath = "app:" + imagePath.replaceAll(SERVLET_HOME, "");
      imagePath = imagePath.replaceAll("'", "").replaceAll("\"", "");
      PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
      InputStream inputStream = portletRequestContext.getApplication().getResourceResolver().getInputStream(imagePath);
      if (inputStream != null) {
        String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        
        Node imageNode = imageFolder.addNode(imageName, "nt:file");
        Node imageNodeContent = imageNode.addNode("jcr:content", "nt:resource");
        imageNodeContent.setProperty("jcr:encoding", "UTF-8");
        MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
        String mimeType = mimeTypeResolver.getMimeType(imageName);
        imageNodeContent.setProperty("jcr:mimeType", mimeType);
        imageNodeContent.setProperty("jcr:data", inputStream);
        imageNodeContent.setProperty("jcr:lastModified", new Date().getTime());
        String repositoryName = ((ManageableRepository)(imageFolder.getSession().getRepository())).getConfiguration().getName();
        String workspaceName = imageFolder.getSession().getWorkspace().getName();
        String imageLink = "/portal/rest/jcr/" + repositoryName + "/" + workspaceName + imageNode.getPath();
        return imageLink;
      }
      return null;
    }
  }
  
  public static class CancelActionListener extends EventListener<UIFooterEditModeForm> {
    public void execute(Event<UIFooterEditModeForm> event) throws Exception {
      PortletRequestContext portletRequestContext = (PortletRequestContext) event.getRequestContext();
      portletRequestContext.setApplicationMode(PortletMode.VIEW);
    }
  }
}
