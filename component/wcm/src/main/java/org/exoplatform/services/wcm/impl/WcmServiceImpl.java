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

package org.exoplatform.services.wcm.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.html.HTMLDocument;
import org.exoplatform.services.html.HTMLNode;
import org.exoplatform.services.html.Name;
import org.exoplatform.services.html.parser.HTMLParser;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.wcm.WcmService;
import org.exoplatform.services.wcm.WebContentHandler;
import org.exoplatform.services.wcm.WebContentHandlerNotPound;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Xuan Hoa
 *          hoa.pham@exoplatform.com
 * Mar 6, 2008  
 */
public class WcmServiceImpl implements WcmService,Startable {

  private List<SharedPortalPlugin> sharedPortalPlugins = new ArrayList<SharedPortalPlugin>() ;

  private List<WebContentHandler> webContentHanlders = new ArrayList<WebContentHandler>() ;

  private NodeHierarchyCreator hierarchyCreator_ ;
  private RepositoryService repositoryService_ ;  
  private CmsService cmsService_ ;  

  public WcmServiceImpl(RepositoryService repositoryService, NodeHierarchyCreator hierarchyCreator,CmsService cmsService) {    
    this.hierarchyCreator_ = hierarchyCreator ;
    this.repositoryService_ = repositoryService ;
    this.cmsService_ = cmsService ;    
  }

  public void processWebContent(Node webContent) throws Exception {    
    for(WebContentHandler handler:webContentHanlders) {
      if(handler.matchHandler(webContent)) {
        handler.handle(webContent) ;
        return;
      }
    }
    throw new WebContentHandlerNotPound() ;
  }    

  public void addPlugin(ComponentPlugin plugin) {
    if(plugin instanceof SharedPortalPlugin) {
      sharedPortalPlugins.add(SharedPortalPlugin.class.cast(plugin)) ;
    }
  }

  public void addWebContentHandler(ComponentPlugin plugin) {
    if(plugin instanceof WebContentHandler) {
      webContentHanlders.add(WebContentHandler.class.cast(plugin)) ;
    }
  } 

  public void start() {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();               
    for(SharedPortalPlugin sharedPortalPlugin:sharedPortalPlugins) {
      try{
        sharedPortalPlugin.createSharePortaFolder(sessionProvider,repositoryService_) ;
      }catch (Exception e) {
        e.printStackTrace();
      }
    }
    sessionProvider.close();
  }

  public void stop() {

  }

  public String storeNode(String nodeTypeName, Node storeHomeNode, Map mappings, boolean isAddNew,
      String repository, String portletInstanceId) throws Exception {
    String storedPath = cmsService_.storeNode(nodeTypeName,storeHomeNode,mappings,isAddNew,repository) ;
    Session session = storeHomeNode.getSession() ;
    Node storedNode = (Node)session.getItem(storedPath) ;
    Value value = session.getValueFactory().createValue(portletInstanceId) ;
    if(storedNode.isNodeType("exo:applicationLinkable")) {
      List<Value> temp = new ArrayList<Value>() ;
      temp.add(value) ;
      for(Value v: storedNode.getProperty("exo:linkedApplications").getValues()) {
        temp.add(v) ;
      }
      storedNode.setProperty("exo:linkedApplications",temp.toArray(new Value[temp.size()])) ;
    }else {
      storedNode.addMixin("exo:applicationLinkable") ;
      storedNode.setProperty("exo:linkedApplications",new Value[]{value}) ;
    }
    session.save();
    return storedPath ;
  }

  public void updateWebContentReference(String repository,String worksapce,String path) throws Exception {
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();       
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;    
    Session session = sessionProvider.getSession(worksapce,manageableRepository);
    Node webContent = (Node) session.getItem(path);
    updateCssReference(webContent);
    updateScriptReference(webContent) ;
    session.save();
  }

  private void updateCssReference(Node htmlFile) throws Exception {
    HTMLDocument document = HTMLParser.createDocument(getTextFileContent(htmlFile)) ;
    HTMLNode head = getHTMLNodeByName(document.getRoot(),Name.HEAD) ;
    List<HTMLNode> styleNodes = getHTMLNodesByName(head,Name.STYLE) ;    
    List<Value> values = getReferenceValues(htmlFile,styleNodes) ;    
    htmlFile.setProperty("exo:referenceCSS",values.toArray(new Value[values.size()])) ;
  }

  private void updateScriptReference(Node htmlFile) throws Exception {
    HTMLDocument document = HTMLParser.createDocument(getTextFileContent(htmlFile)) ;
    HTMLNode head = getHTMLNodeByName(document.getRoot(),Name.HEAD) ;
    List<HTMLNode> scriptsInHead = getHTMLNodesByName(head,Name.SCRIPT) ;
    HTMLNode body = getHTMLNodeByName(document.getRoot(),Name.BODY) ;
    List<HTMLNode> scriptsInBody = getHTMLNodesByName(body,Name.SCRIPT) ;
    List<Value> values = getReferenceValues(htmlFile,scriptsInHead) ;
    values.addAll(getReferenceValues(htmlFile,scriptsInBody)) ;

    htmlFile.setProperty("exo:referenceJS",values.toArray(new Value[values.size()])) ;
  }

  private List<Value> getReferenceValues(Node htmlFile,List<HTMLNode> htmlNodes) throws Exception {
    List<Value> values = new ArrayList<Value>() ;
    ValueFactory valueFactory = htmlFile.getSession().getValueFactory();
    for(HTMLNode htmlNode:htmlNodes) {
      String style = htmlNode.getTextValue() ;
      for(String element: style.split("\n")) {
        element = element.trim() ;
        String includedPath = null ;              
        if(element.startsWith("<%import(")) {
          includedPath = element.substring("<%import(".length(),element.indexOf(")")) ;        
        }else if(element.startsWith("<%import(\"")||element.startsWith("<%import('")){
          includedPath = element.substring("<%import(".length()+2,element.indexOf(")")) ;        
        }   
        if(includedPath != null) {    
          Node referenceNode = getNodeByPath(htmlFile,includedPath) ;
          if(referenceNode != null) {
            values.add(valueFactory.createValue(referenceNode)) ; 
          }          
        }
      }
    }
    return values;
  }
  private HTMLNode getHTMLNodeByName(HTMLNode root,Name name) {    
    for(HTMLNode node: root.getChildren()) {      
      if(node.getName().equals(name)) return node;
    }
    return null ;
  }

  private List<HTMLNode> getHTMLNodesByName(HTMLNode root,Name name) {
    List<HTMLNode> list = new ArrayList<HTMLNode>();    
    for(HTMLNode node: root.getChildren()) {      
      if(node.getName().equals(name)) list.add(node);
    }
    return list ;
  }

  private Node getNodeByPath(Node currentNode,String referencePath) throws Exception {
    if(referencePath.indexOf("::")>0) {
      //path like: repository::worksapce::relativePath
    }else if(referencePath.indexOf("::/")>0) {
      //user for path in Drive: driveName ::relativePath in drive
    }else if(referencePath.startsWith("/")) {
      //reference node in same worksapce
      try{
        return (Node)currentNode.getSession().getItem(referencePath) ;        
      }catch (Exception e) {        
        return null ;
      }
    }else {
      //reference node has path like aaa/bb... so this is child of current node
      try{
        return currentNode.getParent().getNode(referencePath);
      }catch (Exception e) {
        return null ;
      }      
    }
    return null ;
  }

  private String getTextFileContent(Node node) throws Exception {
    if(!node.isNodeType("nt:file"))  return null ;
    String mimetype = node.getNode("jcr:content").getProperty("jcr:mimeType").getString() ;
    if(!mimetype.startsWith("text")) return null ;  
    return node.getNode("jcr:content").getProperty("jcr:data").getString() ;
  }

}
