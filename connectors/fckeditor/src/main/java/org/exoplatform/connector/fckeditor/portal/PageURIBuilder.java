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
package org.exoplatform.connector.fckeditor.portal;

import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 22, 2008  
 */
public class PageURIBuilder {

  final private String PUBLIC_ACCESS = "public".intern();
  final private String PRIVATE_ACCESS = "private".intern();
  final private String EVERYONE_PERMISSION = "Everyone".intern() ;
  final private String RESOURCE_TYPE = "PortalPageURI".intern();
  final private String PORTAL_CONTEXT = "portal".intern();

  private UserPortalConfigService portalConfigService_ ;
  private DataStorage dataStorage_ ;
  private UserACL userACL_ ;
  
  public PageURIBuilder(UserPortalConfigService portalConfigService, DataStorage dataStorage, UserACL userACL) throws Exception {
    this.portalConfigService_ = portalConfigService ;    
    this.dataStorage_ = dataStorage ;
    this.userACL_ = userACL ;
  }  
  public Response buildReponse(String currentFolder, String command, String userId) throws Exception {                              
    if(command == null) command = "" ;
    Document document = null ;
    if(currentFolder == null || "/".equals(currentFolder)) {      
      document = buildPortalXMLResponse("/", command,userId) ;
    }else {      
      document = buildNavigationXMLResponse(currentFolder, command, userId);
    }          
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true) ;
    return Response.Builder.ok(document).cacheControl(cacheControl).build() ;
  }

  private Document buildPortalXMLResponse(String currentFolder,String command,String userId) throws Exception {
    Element rootElement = initRootElement(command,currentFolder) ;                         
    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, PortalConfig.class) ;
    PageList pageList = dataStorage_.find(query, new Comparator<PortalConfig>(){
      public int compare(PortalConfig pconfig1, PortalConfig pconfig2) {
        return pconfig1.getName().compareTo(pconfig2.getName());
      }
    }) ;
    //should use PermissionManager to check access permission        
    for(Object object: pageList.getAll()) {      
      PortalConfig config = (PortalConfig) object ;
      if(!userACL_.hasPermission(config, userId)) continue ;
      Element nodesElement = rootElement.getOwnerDocument().createElement("Folders");
      rootElement.appendChild(nodesElement);    
      Element fileElement = rootElement.getOwnerDocument().createElement("Folder");        
      fileElement.setAttribute("name", config.getName());    
      nodesElement.appendChild(fileElement);
    }
    return rootElement.getOwnerDocument();
  }

  private Document buildNavigationXMLResponse(String currentFolder,String command,String userId) throws Exception {    
    Element rootElement = initRootElement(command,currentFolder) ;    
    String portalName = currentFolder.substring(1,currentFolder.indexOf("/", 1)) ;
    String pageNodeUri = currentFolder.substring(portalName.length() + 1) ;            
    List<PageNavigation> navigations = portalConfigService_.getUserPortalConfig(portalName,userId).getNavigations() ;    
    for(PageNavigation navigation:navigations) {           
      for(PageNode pageNode: navigation.getNodes()) {               
        if("/".equalsIgnoreCase(pageNodeUri)) {
          processPageNode(portalName,pageNode,rootElement,userId) ;
        }else {
          PageNode node = getPageNode(pageNode,pageNodeUri) ;          
          if(node != null && node.getChildren() != null) {
            for(PageNode child: node.getChildren()) {
              processPageNode(portalName,child,rootElement,userId) ; 
            }
          }
        }                                                 
      }
    }
    return rootElement.getOwnerDocument();
  }

  private Element initRootElement (String commandStr, String currentPath) throws ParserConfigurationException {
    Document doc = null;
    DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    doc=builder.newDocument();
    Element rootElement = doc.createElement("Connector");
    doc.appendChild(rootElement);
    rootElement.setAttribute("command",commandStr);
    rootElement.setAttribute("resourceType",RESOURCE_TYPE);

    Element myEl = doc.createElement("CurrentFolder");
    myEl.setAttribute("path",currentPath);
    myEl.setAttribute("url", "");
    rootElement.appendChild(myEl);
    return rootElement ;
  }   
  private PageNode getPageNode(PageNode root,String uri) {
    if(uri.equals("/"+root.getUri()+"/")) return root ;
    List<PageNode> list = root.getChildren() ;        
    if(list == null) return null ;
    for(PageNode child: list) {      
      if(uri.equals("/"+child.getUri()+"/")) return child ;
      PageNode deepChild =  getPageNode(child,uri) ;
      if(deepChild != null) return deepChild; 
    }
    return null ;
  }

  private void processPageNode(String portalName, PageNode pageNode,Element rootElement,String userId) throws Exception {
    String pageId = pageNode.getPageReference() ;
    Page page = portalConfigService_.getPage(pageId, userId) ;
    if(page == null) return ;
    String accessMode = PRIVATE_ACCESS ;
    for(String role:page.getAccessPermissions()) {
      if(EVERYONE_PERMISSION.equalsIgnoreCase(role)) {
        accessMode = PUBLIC_ACCESS ;
        break ;
      }
    }    
    String pageUri = PORTAL_CONTEXT + "/"+ accessMode + "/" + portalName + "/" + pageNode.getUri() ;
    Element foldersElement = rootElement.getOwnerDocument().createElement("Folders");
    rootElement.appendChild(foldersElement);    
    Element folderElement = rootElement.getOwnerDocument().createElement("Folder");        
    folderElement.setAttribute("name", pageNode.getName());
    folderElement.setAttribute("pageUri", pageUri);
    foldersElement.appendChild(folderElement);     
  }  
}
