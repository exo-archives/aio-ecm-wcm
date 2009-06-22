/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wcm.webui.category;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.webui.tree.UINodeTree;
import org.exoplatform.ecm.webui.tree.UINodeTreeBuilder;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 19, 2009  
 */
@ComponentConfig(
    events = {
        @EventConfig(listeners = UINodeTreeBuilder.ChangeNodeActionListener.class)
    }
)
public class UICategoryNavigationTree extends UINodeTreeBuilder {

  public UICategoryNavigationTree() throws Exception {}

  public void buildTree() throws Exception {
    NodeIterator sibbling = null ;
    NodeIterator children = null ;    
    UINodeTree tree = getChild(UINodeTree.class) ;
    tree.setSelected(currentNode);
    if (Utils.getNodeSymLink(currentNode).getDepth() > 0) {
      tree.setParentSelected(currentNode.getParent()) ;
      sibbling = Utils.getNodeSymLink(currentNode).getNodes() ;
      children = Utils.getNodeSymLink(currentNode).getNodes() ;
    } else {
      tree.setParentSelected(currentNode) ;
      sibbling = Utils.getNodeSymLink(currentNode).getNodes() ;
      children = null;
    }
    if (sibbling != null) {
      tree.setSibbling(filter(sibbling));
    }
    if (children != null) {
      tree.setChildren(filter(children));
    }
  }
  
  private List<Node> filter(final NodeIterator iterator) throws Exception{
    List<Node> list = new ArrayList<Node>();
    for(;iterator.hasNext();) {
      Node sibbling = iterator.nextNode();
      if(sibbling.isNodeType("exo:hiddenable")) continue;
      list.add(sibbling);                  
    }            
    return list;
  }
}
