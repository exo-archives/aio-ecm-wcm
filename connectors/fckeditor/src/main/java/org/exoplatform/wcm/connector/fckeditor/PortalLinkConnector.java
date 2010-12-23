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
package org.exoplatform.wcm.connector.fckeditor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.security.ConversationState;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Jul 11, 2008
 */
@URITemplate("/portalLinks/")
public class PortalLinkConnector implements ResourceContainer {

  /** The PUBLI c_ access. */
  final private String PUBLIC_ACCESS       = "public".intern();

  /** The PRIVAT e_ access. */
  final private String PRIVATE_ACCESS      = "private".intern();

  /** The EVERYON e_ permission. */
  final private String EVERYONE_PERMISSION = "Everyone".intern();

  /** The RESOURC e_ type. */
  final private String RESOURCE_TYPE       = "PortalPageURI".intern();
  
  /** The Constant LAST_MODIFIED_PROPERTY. */
  private static final String LAST_MODIFIED_PROPERTY = "Last-Modified";

  /** The Constant IF_MODIFIED_SINCE_DATE_FORMAT. */
  private static final String IF_MODIFIED_SINCE_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

  /** The portal data storage. */
  private DataStorage  portalDataStorage;

  /** The portal user acl. */
  private UserACL      portalUserACL;
  
  /** The servlet context. */
  private ServletContext servletContext;

  /** The log. */
  private static Log log = ExoLogger.getLogger(PortalLinkConnector.class);
  
  /**
   * Instantiates a new portal link connector.
   * 
   * @param params the params
   * @param dataStorage the data storage
   * @param userACL the user acl
   * @param servletContext the servlet context
   * 
   * @throws Exception the exception
   */
  public PortalLinkConnector(InitParams params, DataStorage dataStorage, UserACL userACL, ServletContext servletContext) throws Exception {
    this.portalDataStorage = dataStorage;
    this.portalUserACL = userACL;
    this.servletContext = servletContext;
  }

  /**
   * Gets the page uri.
   * 
   * @param currentFolder the current folder
   * @param command the command
   * @param type the type
   * 
   * @return the page uri
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getPageURI(@QueryParam("currentFolder") String currentFolder,
                             @QueryParam("command") String command,
                             @QueryParam("type") String type) throws Exception {
    try {
      String userId = getCurrentUser();
      return buildReponse(currentFolder, command, userId); 
    } catch (Exception e) {
      log.error("Error when perform getPageURI: ", e.fillInStackTrace());
    }    
    DateFormat dateFormat = new SimpleDateFormat(IF_MODIFIED_SINCE_DATE_FORMAT);
    return Response.Builder.ok().header(LAST_MODIFIED_PROPERTY, dateFormat.format(new Date())).build();
  }

  /**
   * Gets the current user.
   * 
   * @return the current user
   */
  private String getCurrentUser() {
    try {
      ConversationState conversationState = ConversationState.getCurrent();
      return conversationState.getIdentity().getUserId();
    } catch (Exception e) {
      log.error("Error when perform getCurrentUser: ", e.fillInStackTrace());
    }
    return null;
  }

  /**
   * Builds the reponse.
   * 
   * @param currentFolder the current folder
   * @param command the command
   * @param userId the user id
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  private Response buildReponse(String currentFolder, String command, String userId) throws Exception {
    Document document = null;
    if (currentFolder == null || "/".equals(currentFolder)) {
      document = buildPortalXMLResponse("/", command, userId);
    } else {
      document = buildNavigationXMLResponse(currentFolder, command, userId);
    }
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    DateFormat dateFormat = new SimpleDateFormat(IF_MODIFIED_SINCE_DATE_FORMAT);
    return Response.Builder.ok(document).header(LAST_MODIFIED_PROPERTY, dateFormat.format(new Date())).mediaType("text/xml").cacheControl(cacheControl).build();
  }

  /**
   * Builds the portal xml response.
   * 
   * @param currentFolder the current folder
   * @param command the command
   * @param userId the user id
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document buildPortalXMLResponse(String currentFolder, String command, String userId) throws Exception {
    Element rootElement = initRootElement(command, currentFolder);
    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, null, PortalConfig.class);
    PageList pageList = portalDataStorage.find(query, new Comparator<PortalConfig>() {
      public int compare(PortalConfig pconfig1, PortalConfig pconfig2) {
        return pconfig1.getName().compareTo(pconfig2.getName());
      }
    });
    // should use PermissionManager to check access permission
    Element foldersElement = rootElement.getOwnerDocument().createElement("Folders");
    rootElement.appendChild(foldersElement);
    for (Object object : pageList.getAll()) {
      PortalConfig config = (PortalConfig) object;
      if (!portalUserACL.hasPermission(config, userId)) {
        continue;
      }
      Element folderElement = rootElement.getOwnerDocument().createElement("Folder");
      folderElement.setAttribute("name", config.getName());
      folderElement.setAttribute("url", "");
      folderElement.setAttribute("folderType", "");
      foldersElement.appendChild(folderElement);
    }
    return rootElement.getOwnerDocument();
  }

  /**
   * Builds the navigation xml response.
   * 
   * @param currentFolder the current folder
   * @param command the command
   * @param userId the user id
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document buildNavigationXMLResponse(String currentFolder, String command, String userId) throws Exception {
    Element rootElement = initRootElement(command, currentFolder);
    String portalName = currentFolder.substring(1, currentFolder.indexOf('/', 1));
    String pageNodeUri = currentFolder.substring(portalName.length() + 1);
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    UserPortalConfigService pConfig = (UserPortalConfigService) container.getComponentInstanceOfType(UserPortalConfigService.class);

    List<PageNavigation> navigations = pConfig.getUserPortalConfig(portalName, userId)
                                              .getNavigations();
    Element foldersElement = rootElement.getOwnerDocument().createElement("Folders");
    Element filesElement = rootElement.getOwnerDocument().createElement("Files");
    rootElement.appendChild(foldersElement);
    rootElement.appendChild(filesElement);
    for (PageNavigation navigation : navigations) {
      for (PageNode pageNode : navigation.getNodes()) {
        if ("/".equalsIgnoreCase(pageNodeUri)) {
          processPageNode(portalName, pageNode, foldersElement, filesElement, userId, pConfig);
        } else {
          PageNode node = getPageNode(pageNode, pageNodeUri);
          if (node != null && node.getChildren() != null) {
            for (PageNode child : node.getChildren()) {
              processPageNode(portalName, child, foldersElement, filesElement, userId, pConfig);
            }
          }
        }
      }
    }
    return rootElement.getOwnerDocument();
  }

  /**
   * Inits the root element.
   * 
   * @param commandStr the command str
   * @param currentPath the current path
   * 
   * @return the element
   * 
   * @throws ParserConfigurationException the parser configuration exception
   */
  private Element initRootElement(String commandStr, String currentPath) throws ParserConfigurationException {
    Document doc = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    doc = builder.newDocument();
    Element rootElement = doc.createElement("Connector");
    doc.appendChild(rootElement);
    rootElement.setAttribute("command", commandStr);
    rootElement.setAttribute("resourceType", RESOURCE_TYPE);
    Element myEl = doc.createElement("CurrentFolder");
    myEl.setAttribute("path", currentPath);
    myEl.setAttribute("url", "");
    rootElement.appendChild(myEl);
    return rootElement;
  }

  /**
   * Process page node.
   * 
   * @param portalName the portal name
   * @param pageNode the page node
   * @param foldersElement the root element
   * @param filesElement
   * @param userId the user id
   * @param portalConfigService the portal config service
   * 
   * @throws Exception the exception
   */
  private void processPageNode(String portalName,
                               PageNode pageNode,
                               Element foldersElement,
                               Element filesElement,
                               String userId,
                               UserPortalConfigService portalConfigService) throws Exception {
    if (!pageNode.isDisplay()) {
      return;
    }
    String pageId = pageNode.getPageReference();
    Page page = portalConfigService.getPage(pageId, userId);
    String accessMode = PRIVATE_ACCESS;
    if (page == null) {
      accessMode = PUBLIC_ACCESS;
    } else {
      for (String role : page.getAccessPermissions()) {
        if (EVERYONE_PERMISSION.equalsIgnoreCase(role)) {
          accessMode = PUBLIC_ACCESS;
          break;
        }
      }
    }
    String pageUri = "/" + servletContext.getServletContextName() + "/" + accessMode + "/" + portalName + "/" + pageNode.getUri();

    Element folderElement = foldersElement.getOwnerDocument().createElement("Folder");
    folderElement.setAttribute("name", pageNode.getName());
    folderElement.setAttribute("folderType", "");
    folderElement.setAttribute("url", pageUri);
    foldersElement.appendChild(folderElement);

    SimpleDateFormat formatter = new SimpleDateFormat(ISO8601.SIMPLE_DATETIME_FORMAT);    
    String datetime = formatter.format(new Date());
    Element fileElement = filesElement.getOwnerDocument().createElement("File");
    fileElement.setAttribute("name", pageNode.getName());
    fileElement.setAttribute("dateCreated", datetime);
    fileElement.setAttribute("fileType", "page node");
    fileElement.setAttribute("url", pageUri);
    fileElement.setAttribute("size", "");
    filesElement.appendChild(fileElement);
  }

  /**
   * Gets the page node.
   * 
   * @param root the root
   * @param uri the uri
   * 
   * @return the page node
   */
  private PageNode getPageNode(PageNode root, String uri) {
    if (uri.equals("/" + root.getUri() + "/")) {
      return root;
    }
    List<PageNode> list = root.getChildren();
    if (list == null) {
      return null;
    }
    for (PageNode child : list) {
      if (uri.equals("/" + child.getUri() + "/")) {
        return child;
      }
      PageNode deepChild = getPageNode(child, uri);
      if (deepChild != null) {
        return deepChild;
      }
    }
    return null;
  }
}
