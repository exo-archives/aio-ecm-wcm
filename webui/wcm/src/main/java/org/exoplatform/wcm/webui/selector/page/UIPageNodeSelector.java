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
package org.exoplatform.wcm.webui.selector.page;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.navigation.UIPageNavigationActionListener.CreateNavigationActionListener;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIDropDownControl;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : chungnv
 *          nguyenchung136@yahoo.com
 * Jun 23, 2006
 * 10:07:15 AM
 */
@ComponentConfigs({
  @ComponentConfig(
      template = "app:/groovy/portal/webui/navigation/UIPageNodeSelector.gtmpl" ,
      events = {
        @EventConfig(listeners = CreateNavigationActionListener.class),
        @EventConfig(listeners = UIPageNodeSelector.SelectNavigationActionListener.class, phase=Phase.DECODE) 
      }
  ),
  @ComponentConfig (
      type = UIDropDownControl.class ,
      id = "UIDropDown",
      template = "system:/groovy/portal/webui/navigation/UINavigationSelector.gtmpl",
      events = {
        @EventConfig(listeners = UIPageNodeSelector.SelectNavigationActionListener.class)
      }
    )
})

public class UIPageNodeSelector extends UIContainer {
  
  private List<PageNavigation> navigations;
  
  private SelectedNode selectedNode;
  
  private SelectedNode copyNode;
  
  private List<PageNavigation> deleteNavigations = new ArrayList<PageNavigation>();
  
	public UIPageNodeSelector() throws Exception {    
    
    UIDropDownControl uiDopDownControl = addChild(UIDropDownControl.class, "UIDropDown", "UIDropDown");
    uiDopDownControl.setParent(this);
    
    UITree uiTree = addChild(UITree.class, null, "TreePageSelector");    
    uiTree.setIcon("DefaultPageIcon");    
    uiTree.setSelectedIcon("DefaultPageIcon");
    uiTree.setBeanIdField("uri");
    uiTree.setBeanLabelField("resolvedLabel");   
    uiTree.setBeanIconField("icon");
    
    loadNavigations();
	}
	
  public void loadNavigations() throws Exception {
    navigations = new ArrayList<PageNavigation>();
    List<PageNavigation> pnavigations = getExistedNavigation(Util.getUIPortal().getNavigations()) ;
    for(PageNavigation nav  : pnavigations){      
      if(nav.isModifiable()) navigations.add(nav);
    }
    
    updateUI() ;
    
    PageNavigation portalSelectedNav = Util.getUIPortal().getSelectedNavigation() ;
    if(getPageNavigation(portalSelectedNav.getId()) != null) {
      selectNavigation(portalSelectedNav.getId()) ;
      PageNode portalSelectedNode = Util.getUIPortal().getSelectedNode() ;
      if(portalSelectedNode != null) selectPageNodeByUri(portalSelectedNode.getUri()) ;  
      return;
    } 
    selectNavigation();
  }
  
  private void updateUI() {
    if(navigations == null || navigations.size() < 1) {
      getChild(UIDropDownControl.class).setOptions(null) ;
      getChild(UITree.class).setSibbling(null) ;
      return ;
    }
    
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    for(PageNavigation navigation: navigations) { //navigation.getOwnerId()
      options.add(new SelectItemOption<String>(navigation.getOwnerType() + ":" + navigation.getOwnerId(), String.valueOf(navigation.getId())));
    }
    UIDropDownControl uiNavigationSelector = getChild(UIDropDownControl.class);
    uiNavigationSelector.setOptions(options);
    if(options.size() > 0) uiNavigationSelector.setValue(0);
  }

  private void selectNavigation() {
    if(navigations == null || navigations.size() < 1) return;
    if (selectedNode == null) {
      PageNavigation navigation = navigations.get(0);
      selectedNode = new SelectedNode(navigation, null, null);
      if(navigation.getNodes().size() > 0) selectedNode.setNode(navigation.getNodes().get(0));
    }
    selectNavigation(selectedNode.getPageNavigation().getId()) ;
    if(selectedNode.getNode() != null) selectPageNodeByUri(selectedNode.getNode().getUri()) ;
  }
  
  public void selectNavigation(int id){    
    for(int i = 0; i < navigations.size(); i++){
      if(navigations.get(i).getId() != id) continue ;
      selectedNode = new SelectedNode(navigations.get(i), null, null);
      selectPageNodeByUri(null) ;
      UITree uiTree = getChild(UITree.class);
      uiTree.setSibbling(navigations.get(i).getNodes());      
      UIDropDownControl uiDropDownSelector = getChild(UIDropDownControl.class);
      uiDropDownSelector.setValue(i);
    }
  }
  
  public void selectPageNodeByUri(String uri){   
    if(selectedNode == null) return ;
    UITree tree = getChild(UITree.class);
    List<?> sibbling = tree.getSibbling();
    tree.setSibbling(null);
    tree.setParentSelected(null);
    selectedNode.setNode(searchPageNodeByUri(selectedNode.getPageNavigation(), uri));
    if(selectedNode.getNode() != null) {
      tree.setSelected(selectedNode.getNode());   
      tree.setChildren(selectedNode.getNode().getChildren());
      return ;
    }
    tree.setSelected(null);
    tree.setChildren(null);
    tree.setSibbling(sibbling);
  }
  
  public PageNode searchPageNodeByUri(PageNavigation pageNav, String uri) {
    if(pageNav == null || uri == null) return null;
    List<PageNode> pageNodes = pageNav.getNodes();
    UITree uiTree = getChild(UITree.class);
    for(PageNode ele : pageNodes){
      PageNode returnPageNode = searchPageNodeByUri(ele, uri, uiTree);
      if(returnPageNode == null) continue;
      if(uiTree.getSibbling() == null) uiTree.setSibbling(pageNodes);      
      return returnPageNode;
    }
    return null; 
  }  
    
  private PageNode searchPageNodeByUri(PageNode pageNode, String uri, UITree tree){
    if(pageNode.getUri().equals(uri)) return pageNode;
    List<PageNode> children = pageNode.getChildren();
    if(children == null) return null;
    for(PageNode ele : children){
      PageNode returnPageNode = searchPageNodeByUri(ele, uri, tree);
      if(returnPageNode == null) continue;
      if(tree.getSibbling() == null) tree.setSibbling(children);
      if(tree.getParentSelected() == null) tree.setParentSelected(pageNode);
      selectedNode.setParentNode(pageNode);
      return returnPageNode;
    }
    return null;
  }
  
  public List<PageNavigation> getPageNavigations() { 
    if(navigations == null) navigations = new ArrayList<PageNavigation>();    
    return navigations;  
  }

  public PageNavigation getPageNavigation(int id) {
    for(PageNavigation ele : getPageNavigations()) {
      if(ele.getId() == id) return ele ;
    }
    return null ;
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {
    UIRightClickPopupMenu uiPopupMenu = getChild(UIRightClickPopupMenu.class);
    if(uiPopupMenu != null) {
      if(navigations == null || navigations.size() < 1) uiPopupMenu.setRendered(false) ;
      else uiPopupMenu.setRendered(true) ;
    }
    super.processRender(context) ;
  }
  
  public SelectedNode getCopyNode() { return copyNode; }
  public void setCopyNode(SelectedNode copyNode) { this.copyNode = copyNode; }
  
  private List<PageNavigation> getExistedNavigation(List<PageNavigation> navis) throws Exception {
    Iterator<PageNavigation> itr = navis.iterator() ;
    UserPortalConfigService configService = getApplicationComponent(UserPortalConfigService.class);
    while(itr.hasNext()) {
      PageNavigation nav = itr.next() ;
      if(configService.getPageNavigation(nav.getOwnerType(), nav.getOwnerId()) == null) itr.remove() ;
    }
    return navis ;
  }
  
  static public class SelectNavigationActionListener  extends EventListener<UIDropDownControl> {
    public void execute(Event<UIDropDownControl> event) throws Exception {
      String id = event.getRequestContext().getRequestParameter(OBJECTID);
      UIDropDownControl uiDropDownControl = event.getSource();
      UIPageNodeSelector uiPageNodeSelector = uiDropDownControl.getAncestorOfType(UIPageNodeSelector.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPageNodeSelector.getParent()) ;
      if(id != null) uiPageNodeSelector.selectNavigation(Integer.parseInt(id));
      try{
        UIPageSelector pageSelector = uiPageNodeSelector.getAncestorOfType(UIPageSelector.class);
        UIPageSelectorPanel pageSelectorPanel = pageSelector.getChild(UIPageSelectorPanel.class);
        pageSelectorPanel.setSelectedPage(uiPageNodeSelector.getSelectedNode().getNode());
        pageSelectorPanel.updateGrid();
        
        event.getRequestContext().addUIComponentToUpdateByAjax(pageSelector) ;
      }catch(Exception ex){
        ex.printStackTrace();
      }
      uiPageNodeSelector.<UIComponent>getParent().broadcast(event, event.getExecutionPhase()) ;
      
    }
  }

  public static class SelectedNode {
    
    private PageNavigation nav;
    
    private PageNode parentNode;
    
    private PageNode node;
    
    private boolean deleteNode = false;
    
    private boolean cloneNode = false;
    
    public SelectedNode(PageNavigation nav, PageNode parentNode, PageNode node) {
      this.nav = nav;
      this.parentNode = parentNode;
      this.node = node;
    }

    public PageNavigation getPageNavigation() { return nav; }
    public void setPageNavigation(PageNavigation nav) { this.nav = nav; }

    public PageNode getParentNode() { return parentNode; }
    public void setParentNode(PageNode parentNode) { this.parentNode = parentNode; }

    public PageNode getNode() { return node; }
    public void setNode(PageNode node) { this.node = node; }

    public boolean isDeleteNode() { return deleteNode; }
    public void setDeleteNode(boolean deleteNode) { this.deleteNode = deleteNode; }
    
    public boolean isCloneNode() { return cloneNode; }
    public void setCloneNode(boolean b) { cloneNode = b; }
  }

  public SelectedNode getSelectedNode() { return selectedNode; }
  
  public PageNavigation getSelectedNavigation(){ 
    return selectedNode == null ? null : selectedNode.getPageNavigation(); 
  }  
  
  public PageNode getSelectedPageNode() { 
    return selectedNode == null ? null : selectedNode.getNode() ; 
  }
  
  public String getUpLevelUri () { return selectedNode.getParentNode().getUri() ; }

  public List<PageNavigation> getDeleteNavigations() { return deleteNavigations; }
}
