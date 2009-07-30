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
package org.exoplatform.wcm.webui.clv.config;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

/**
 * The Class UIFolderPathSelectorForm.
 */
@ComponentConfig(
  lifecycle = Lifecycle.class, 
  events = @EventConfig(listeners = UICLVFolderSelector.CloseActionListener.class), 
  template = "app:/groovy/ContentListViewer/config/UICLVFolderSelector.gtmpl"
)
public class UICLVFolderSelector extends UIBaseNodeTreeSelector {

  /**
   * Instantiates a new uI folder path selector form.
   * 
   * @throws Exception the exception
   */
  public UICLVFolderSelector() throws Exception {
    addChild(UICLVContentTree.class, null, UICLVContentTree.class.getSimpleName() + hashCode());
    addChild(UICLVFolderSelectionPanel.class, null, UICLVFolderSelectionPanel.class.getSimpleName() + hashCode());
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    UICLVContentTree treeBuilder = getChild(UICLVContentTree.class);
    UICLVFolderSelectionPanel pathPanel = getChild(UICLVFolderSelectionPanel.class);
    String[] acceptNodeTypes = new String[] { "nt:folder", "nt:unstructured" };
    pathPanel.setAcceptedNodeTypes(acceptNodeTypes);
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    String currentPortalName = Util.getUIPortal().getName();
    SessionProvider provider = Utils.getSessionProvider(this);
  	Node currentPortal = livePortalManagerService.getLivePortal(currentPortalName, provider);
  	Node sharedPortal = livePortalManagerService.getLiveSharedPortal(provider);
  	treeBuilder.setCurrentPortal(currentPortal);
  	treeBuilder.setSharedPortal(sharedPortal);
  	treeBuilder.setRootTreeNode(currentPortal.getParent());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector#onChange(javax.jcr.Node,
   *      java.lang.Object)
   */
  public void onChange(Node node, Object val) throws Exception {
    UICLVFolderSelectionPanel selectPathPanel = getChild(UICLVFolderSelectionPanel.class);
    selectPathPanel.setParentNode(node);
  }

  /**
   * The listener interface for receiving closeAction events. The class that is
   * interested in processing a closeAction event implements this interface, and
   * the object created with that class is registered with a component using the
   * component's <code>addCloseActionListener<code> method. When
   * the closeAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CloseActionEvent
   */
  public static class CloseActionListener extends EventListener<UICLVFolderSelector> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UICLVFolderSelector> event) throws Exception {
      UICLVFolderSelector uiFolderPathSelectorForm = event.getSource();
      Utils.closePopupWindow(uiFolderPathSelectorForm, UICLVConfig.FOLDER_PATH_SELECTOR_POPUP_WINDOW);
    }
  }

}
