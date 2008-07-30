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
package org.exoplatform.wcm.presentation.acp.config.selector;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.selectone.UISelectPathPanel;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham hoa.pham@exoplatform.com Jun 23, 2008
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIWebContentPathSelector extends UIBaseNodeTreeSelector {
  
  private UIComponent uiComponent;
  private String returnFieldName;
  
  /**
   * Instantiates a new uI web content path selector.
   * 
   * @throws Exception the exception
   */
  public UIWebContentPathSelector() throws Exception {
    addChild(UIWebContentTreeBuilder.class,null, UIWebContentTreeBuilder.class.getSimpleName()+hashCode());
    addChild(UISelectPathPanel.class,null,UISelectPathPanel.class.getSimpleName()+hashCode());        
  }
  
  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    List<String> acceptedNodeTypes = new ArrayList<String>();
    acceptedNodeTypes.add("exo:webContent");
    UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
    selectPathPanel.setAcceptedNodeTypes(acceptedNodeTypes);       
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);    
    String currentPortalName = "classic";
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    Node currentPortal = livePortalManagerService.getLivePortal(currentPortalName,provider);
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(provider);
    UIWebContentTreeBuilder builder = getChild(UIWebContentTreeBuilder.class);    
    builder.setCurrentPortal(currentPortal);
    builder.setSharedPortal(sharedPortal);
    builder.setRootTreeNode(currentPortal.getParent());
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector#onChange(javax.jcr.Node, org.exoplatform.webui.application.WebuiRequestContext)
   */
  public void onChange(Node node, Object context) throws Exception {
    UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
    selectPathPanel.setParentNode(node);
  }
  
  /**
   * Gets the return component.
   * 
   * @return the return component
   */
  public UIComponent getSourceComponent() { return uiComponent; }
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.selector.ComponentSelector#setComponent(org.exoplatform.webui.core.UIComponent, java.lang.String[])
   */
  public void setSourceComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent ;
    if(initParams == null || initParams.length < 0) return ;
    for(int i = 0; i < initParams.length; i ++) {
      if(initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=") ;
        returnFieldName = array[1] ;
        break ;
      }
      returnFieldName = initParams[0] ;
    }
  }   
}
