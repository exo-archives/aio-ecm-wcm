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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.webui.tree.selectone.UISelectPathPanel;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 17, 2008
 */

@ComponentConfig(
    template = "classpath:groovy/ecm/webui/tree/selectone/UISelectPathPanel.gtmpl",
    events = { 
        @EventConfig(listeners = UISelectPathPanel.SelectActionListener.class) 
    }
)
public class UISelectFolderPathPanel extends UISelectPathPanel {

  public List<Node> getSelectableNodes() throws Exception {
    List<Node> list = new ArrayList<Node>();
    if(parentNode == null) return list;
    UIFolderPathSelectorForm uiFolderPathSelectorForm = getParent();
    UIFolderPathTreeBuilder uiFolderPathTreeBuilder = uiFolderPathSelectorForm
        .getChild(UIFolderPathTreeBuilder.class);
    Node root = uiFolderPathTreeBuilder.getRootTreeNode();
    Node currentPortal = uiFolderPathTreeBuilder.getCurrentPortal();
    Node sharedPortal = uiFolderPathTreeBuilder.getSharedPortal();
    Node webContentsFolder = null;
    Node documentsFolder = null;
    String parentNodePath = parentNode.getPath(); 
    if (parentNodePath.equals(root.getPath())) {
      list.add(currentPortal);
      list.add(sharedPortal);
    } else if (parentNodePath.equals(currentPortal.getPath())) {
      webContentsFolder = uiFolderPathTreeBuilder.getWebContentsFolder(currentPortal);
      documentsFolder = uiFolderPathTreeBuilder.getDocumentsFolder(currentPortal);
      list.add(webContentsFolder);
      list.add(documentsFolder);
    } else if (parentNodePath.equals(sharedPortal.getPath())) {
      webContentsFolder = uiFolderPathTreeBuilder.getWebContentsFolder(sharedPortal);
      documentsFolder = uiFolderPathTreeBuilder.getDocumentsFolder(sharedPortal);
      list.add(webContentsFolder);
      list.add(documentsFolder);
    } else {
      for(NodeIterator iterator = parentNode.getNodes();iterator.hasNext();) {
        Node child = iterator.nextNode();
        if(child.isNodeType("exo:hiddenable")) continue;
        if(matchMimeType(child) && matchNodeType(child)) {
          list.add(child);
        }
      }
    }
    return list;
  }
}
