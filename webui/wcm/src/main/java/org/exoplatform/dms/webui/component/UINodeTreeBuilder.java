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
package org.exoplatform.dms.webui.component;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.dms.webui.utils.PermissionUtil;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Oct 17, 2006
 * 10:45:01 AM 
 */
@ComponentConfig(
    //template =  "app:/groovy/webui/component/UITreeJCRExplorer.gtmpl",
    events = @EventConfig(listeners = UINodeTreeBuilder.ChangeNodeActionListener.class)
)
    
public class UINodeTreeBuilder extends UIContainer {  
  
  //TODO reference to UITreeJCRExplorer
  private Node currentNode_ ;
  private Node rootNode_ = null ;
  private boolean isTab_ = false;
  private String rootPath_ ;
  private String [] allowedNodes ;
  
  public UINodeTreeBuilder() throws Exception {
    UITree tree = addChild(UITree.class, null, UITree.class.getSimpleName()+hashCode()) ;
    tree.setBeanLabelField("name") ;
    tree.setBeanIdField("path") ;
    tree.setIcon("nt_unstructured16x16Icon")  ;    
    tree.setSelectedIcon("nt_unstructured16x16Icon") ;
  }  
  public void setRootNode(Node rootNode) { rootNode_ = rootNode ; }
  
  
  public void buildTree() throws Exception {
    UINodesExplorer uiJCRBrowser = getParent() ;
    String workspace = uiJCRBrowser.getWorkspace() ;
    String repositoryName = uiJCRBrowser.getRepository() ;
    // TODO need use SessionProvider outside this component
    //Session session = SessionProviderFactory.createSessionProvider().getSession(workspace, getRepository(repositoryName)) ;
    Session session = SessionProviderFactory.createSystemProvider().getSession(workspace, getRepository(repositoryName)) ;
    Iterator sibbling = null ;
    Iterator children = null ;
    if(rootNode_ == null ) {
      rootNode_ = session.getRootNode() ;
      currentNode_ = rootNode_ ;
      children = rootNode_.getNodes() ;
      changeNode(rootNode_) ;
    }
    UITree tree = getChild(UITree.class) ;
    Node nodeSelected = getSelectedNode() ;
    if(!rootNode_.getPath().equals("/")) {
      if(nodeSelected.getPath().equals(rootNode_.getParent().getPath())) nodeSelected = rootNode_ ; 
    } 
    if(nodeSelected.getPath().equals("/")) {
      nodeSelected = session.getRootNode() ;
      children = nodeSelected.getNodes() ;
    }
    tree.setSelected(nodeSelected) ;
    if(nodeSelected.getDepth() > 0) {
      tree.setParentSelected(nodeSelected.getParent()) ;
      sibbling = nodeSelected.getParent().getNodes() ;
      children = nodeSelected.getNodes() ;
    } else {
      tree.setParentSelected(nodeSelected) ;
      sibbling = nodeSelected.getNodes() ;
    }
    List<Node> sibblingList = new ArrayList<Node>() ;
    List<Node> childrenList = new ArrayList<Node>() ;
    if(currentNode_.getName().equals(rootNode_.getName()) 
        || currentNode_.getParent().getName().equals(rootNode_.getName())) {
      sibblingList = getAllowedNodes(sibbling) ;
    } else sibblingList = getNodes(sibbling) ;
    if(children != null) {
      if(currentNode_.getName().equals(rootNode_.getName())) childrenList = getAllowedNodes(children) ;
      else childrenList = getNodes(children) ;
    }
    if(nodeSelected.getPath().equals(rootNode_.getPath())) { tree.setSibbling(childrenList) ; } 
    else { tree.setSibbling(sibblingList) ; }
    tree.setChildren(childrenList) ;
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {
    Writer writer = context.getWriter() ;
    writer.write("<div class=\"Explorer\">") ;
      writer.write("<div class=\"ExplorerTree\">") ;
        writer.write("<div class=\"InnerExplorerTree\">") ;
          renderChildren() ;
        writer.write("</div>") ;
      writer.write("</div>") ;
    writer.write("</div>") ;
  }
  
  public void renderChildren() throws Exception {
    buildTree() ;
    super.renderChildren() ;
  } 
  
  public void setRootPath(String path) throws Exception {         
    rootPath_ = path ;
    UINodesExplorer uiJCRBrowser = getParent() ;        
    String workspace = uiJCRBrowser.getWorkspace() ;    
    String repositoryName = uiJCRBrowser.getRepository() ;
    ManageableRepository repository = getRepository(repositoryName) ;
    if(workspace == null) {
      workspace = repository.getConfiguration().getDefaultWorkspaceName() ;
    }
    //TODO: use SessionProvider() or createSystemProvider() ?
    //Session session = SessionProviderFactory.createSessionProvider().getSession(workspace, repository) ;
    Session session = SessionProviderFactory.createSystemProvider().getSession(workspace, repository) ;
    rootNode_ = (Node) session.getItem(path) ;
    currentNode_ = rootNode_ ;
    changeNode(rootNode_) ;
  }
  
  public String getRootPath() { return rootPath_ ; }
  
  public void setNodeSelect(String path) throws Exception {
    UINodesExplorer uiJCRBrowser = getParent() ;
    String repositoryName = uiJCRBrowser.getRepository() ;    
    String workspace = uiJCRBrowser.getWorkspace() ;
   //Session session = SessionProviderFactory.createSessionProvider().getSession(workspace, getRepository(repositoryName)) ;
    Session session = SessionProviderFactory.createSystemProvider().getSession(workspace, getRepository(repositoryName)) ;
    currentNode_ = (Node) session.getItem(path);
    if(!rootNode_.getPath().equals("/")) {
      if(currentNode_.getPath().equals(rootNode_.getParent().getPath())) currentNode_ = rootNode_ ;
    }
    if(currentNode_.getPath().equals("/")){
      currentNode_ = rootNode_ ;
    }
    changeNode(currentNode_) ;
  }
   
  public void setIsTab(boolean isTab) { isTab_ = isTab ; }
  
  public void changeNode(Node nodeSelected) throws Exception {
    NodeIterator nodeIter = nodeSelected.getNodes() ;
    List<Node> nodes = new ArrayList<Node>(5) ;
    if(currentNode_.getName().equals(rootNode_.getName())) nodes = getAllowedNodes(nodeIter) ;
    else nodes = getNodes(nodeIter) ; 
    UIContainer uiParent = getParent() ;
    UIBaseNodeList uiTreeList = uiParent.getChild(UIBaseNodeList.class) ;
    uiTreeList.setNodeList(nodes) ;
    if(isTab_) {
      UIContainer uiRoot = uiParent.getParent() ;
      uiRoot.setRenderedChild(uiParent.getId()) ;
    }
  }
  
  public Node getSelectedNode() { return currentNode_ ; }
  
  public ManageableRepository getRepository(String repositoryName) throws Exception{
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;
    return repositoryService.getRepository(repositoryName) ;
  } 
  
  //TODO: This is temporary
  //-------------------------------------------------------------------------------------
  public void setAllowedNodes(String [] strs) { allowedNodes = strs ; }
  
  private List<Node> getNodes(Iterator itr) throws Exception {
    List<Node> list = new ArrayList<Node>(5) ;
      while(itr.hasNext()) {
        Node node = (Node) itr.next();
        if(PermissionUtil.canRead(node) && !node.isNodeType("exo:hiddenable")) list.add(node) ;        
      }
    return list ;
  }

  private List<Node> getAllowedNodes(Iterator itr) throws Exception {
    if(allowedNodes == null || allowedNodes.length < 1) return getNodes(itr) ;
    List<Node> list = new ArrayList<Node>(5) ;
      while(itr.hasNext()) {
        Node node = (Node) itr.next();
        if(PermissionUtil.canRead(node) && !node.isNodeType("exo:hiddenable") && isAllowed(node)) list.add(node) ;        
      }
    return list ;
  }
  
  private boolean isAllowed(Node node) throws Exception {
    for(String ele : allowedNodes) {
      if(ele.equals(node.getName())) return true ;
    }
    return false ;
  }
  //-------------------------------------------------------------------------------------
  
  static public class ChangeNodeActionListener extends EventListener<UITree> {
    public void execute(Event<UITree> event) throws Exception {
      UINodeTreeBuilder uiTreeJCR = event.getSource().getParent() ;
      String uri = event.getRequestContext().getRequestParameter(OBJECTID)  ;
      uiTreeJCR.setNodeSelect(uri) ;
      UINodesExplorer uiJCRBrowser = uiTreeJCR.getParent() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiJCRBrowser) ;
    }
  }
}