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
import javax.servlet.ServletContext;

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
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

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

  private Node bannerFolder;
  private final String DEFAULT_HTML = "app:/groovy/banner/resources/banner.html".intern();
  private final String DEFAULT_CSS = "app:/groovy/banner/resources/BannerStylesheet.css".intern();

  public UIBannerEditModeForm() throws Exception {
    super();
  }

  public void init() throws Exception {
    String portalName = Util.getUIPortal().getName();
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    Node portalFolder = livePortalManagerService.getLivePortal(portalName, sessionProvider);

    WebSchemaConfigService webSchemaConfigService = getApplicationComponent(WebSchemaConfigService.class);
    PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    bannerFolder = portalFolderSchemaHandler.getBannerThemes(portalFolder);

    setRepositoryName(((ManageableRepository)(bannerFolder.getSession().getRepository())).getConfiguration().getName());
    setWorkspace(bannerFolder.getSession().getWorkspace().getName());
    setStoredPath(bannerFolder.getPath());    
    setContentType("exo:webContent");
    if(bannerFolder.hasNode("banner")) {
      setNodePath(bannerFolder.getNode("banner").getPath());
      addNew(false);
    } else {
      addNew(true);
    }    
  }

  public void renderField(String name) throws Exception {
    if (isAddNew()) {
      String jcrPath = ((JcrInputProperty)getInputProperties().get(name)).getJcrPath();
      if ("/node".equals(jcrPath)) {
        UIFormStringInput formTextField = findComponentById(name);
        formTextField.setValue("banner");
      } else if("/node/default.html/jcr:content/jcr:data".equals(jcrPath)) {
        UIFormWYSIWYGInput formWYSIWYGInput = findComponentById(name);
        formWYSIWYGInput.setValue(loadHtml());
      } else if("/node/css/default.css/jcr:content/jcr:data".equals(jcrPath)){
        UIFormTextAreaInput formTextAreaInput = findComponentById(name);
        formTextAreaInput.setValue(loadStyle());
      }       
    }
    super.renderField(name);
  }

  private String loadHtml() throws Exception {    
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    InputStream inputStream = portletRequestContext.getApplication().getResourceResolver().getInputStream(DEFAULT_HTML);
    return IOUtil.getStreamContentAsString(inputStream);    
  }

  private String loadStyle() throws Exception {    
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    InputStream inputStream = portletRequestContext.getApplication().getResourceResolver().getInputStream(DEFAULT_CSS);
    return IOUtil.getStreamContentAsString(inputStream);    
  }

  //TODO should use the method on UIDialogForm
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext webuiRequestContext, String template) {    
    try {
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getRepository(this.repositoryName);
      String workspaceName = manageableRepository.getConfiguration().getSystemWorkspaceName();
      return new JCRResourceResolver(this.repositoryName, workspaceName, TemplateService.EXO_TEMPLATE_FILE_PROP);
    } catch(Exception e){}
    return super.getTemplateResourceResolver(webuiRequestContext, template);
  }

  public static class SaveActionListener extends EventListener<UIBannerEditModeForm> {
    private final String DEFAULT_LOGIN = "app:/groovy/banner/resources/LoginFragment.gtmpl".intern();
    private final String SERVLET_HOME = "/iweb";
    
    public void execute(Event<UIBannerEditModeForm> event) throws Exception {
      UIBannerEditModeForm bannerEditModeForm = event.getSource();
      bannerEditModeForm.init();
      
      List<UIComponent> listComponent = bannerEditModeForm.getChildren();
      Map<String, JcrInputProperty> inputProperties = DialogFormUtil.prepareMap(listComponent, bannerEditModeForm.getInputProperties());

      String nodeType = null;
      Node homeNode = null;
      if (!bannerEditModeForm.isAddNew()) {
        homeNode = bannerEditModeForm.getNode().getParent();
        nodeType = bannerEditModeForm.getNode().getPrimaryNodeType().getName();
      } else {
        homeNode = bannerEditModeForm.bannerFolder;
        nodeType = "exo:webContent";
      }
      CmsService cmsService = bannerEditModeForm.getApplicationComponent(CmsService.class);
      String bannerWebContentPath = cmsService.storeNode(nodeType, homeNode, inputProperties, !bannerEditModeForm.isEditing(), bannerEditModeForm.repositoryName);
      Node bannerWebContent = (Node) homeNode.getSession().getItem(bannerWebContentPath);

      WebSchemaConfigService webSchemaConfigService = bannerEditModeForm.getApplicationComponent(WebSchemaConfigService.class);
      WebContentSchemaHandler webContentSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(WebContentSchemaHandler.class);
      Node cssFolder = webContentSchemaHandler.getCSSFolder(bannerWebContent);
      Node documentFolder = webContentSchemaHandler.getDocumentFolder(bannerWebContent);
      Node imageFolder = webContentSchemaHandler.getImagesFolders(bannerWebContent);
      
      Node accessGTMPL = null;
      if (documentFolder.hasNode("access.gtmpl")) {
        accessGTMPL = documentFolder.getNode("access.gtmpl");
        Node accessGTMPLContent = accessGTMPL.getNode("jcr:content");
        accessGTMPLContent.setProperty("jcr:data", loadLogin());
        accessGTMPLContent.setProperty("jcr:lastModified", new Date().getTime()); 
      }
      else {
        accessGTMPL = documentFolder.addNode("access.gtmpl", "nt:file");
        Node accessGTMPLContent = accessGTMPL.addNode("jcr:content", "nt:resource");
        accessGTMPLContent.setProperty("jcr:encoding", "UTF-8");
        accessGTMPLContent.setProperty("jcr:mimeType", "text/plain");
        accessGTMPLContent.setProperty("jcr:data", loadLogin());
        accessGTMPLContent.setProperty("jcr:lastModified", new Date().getTime()); 
      }

      String htmlContent = bannerWebContent.getProperty("default.html/jcr:content/jcr:data").getString();
      List<String> listImageUrl = getListImageUrl(htmlContent);
      for (String imageUrl : listImageUrl) {
        String newImageUrl = saveImage2JCR(imageUrl, imageFolder);
        if (newImageUrl != null) {
          htmlContent = htmlContent.replaceAll(imageUrl, newImageUrl);
        }
      }
      bannerWebContent.getNode("default.html/jcr:content").setProperty("jcr:data", htmlContent);
      
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
      portletPreferences.setValue("repository", bannerEditModeForm.repositoryName) ;
      portletPreferences.setValue("workspace", bannerWebContent.getSession().getWorkspace().getName()) ;
      portletPreferences.setValue("nodeUUID", bannerWebContent.getUUID()) ;
      portletPreferences.store();
      event.getRequestContext().setAttribute("nodePath", bannerWebContentPath);
      
      context.setApplicationMode(PortletMode.VIEW);
    }
    
    private String loadLogin() throws Exception {
      PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
      InputStream inputStream = portletRequestContext.getApplication().getResourceResolver().getInputStream(DEFAULT_LOGIN);
      return IOUtil.getStreamContentAsString(inputStream);
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

  public static class CancelActionListener extends EventListener<UIBannerEditModeForm> {
    public void execute(Event<UIBannerEditModeForm> event) throws Exception {
      PortletRequestContext portletRequestContext = (PortletRequestContext)event.getRequestContext();
      portletRequestContext.setApplicationMode(PortletMode.VIEW);
    }
  }

}
