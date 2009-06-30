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

import javax.jcr.Node;

import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.core.UITree;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Comment: Change event("ChangeNode", objId) by url("ChangeNode", objId) to display link for tree
 * Jun 30, 2009  
 */
@ComponentConfig(
    template = "system:/groovy/webui/core/UITree.gtmpl" , 
    events = @EventConfig(listeners = UITree.ChangeNodeActionListener.class)
)  
public class UICategoryNavigationTreeBase extends UITree {

  public String renderNode(Object obj) throws Exception {
    Node node = (Node) obj;
    String nodeTypeIcon = Utils.getNodeTypeIcon(node,"16x16Icon");
    String nodeIcon = this.getExpandIcon();
    String iconGroup = this.getIcon();
    String note = "" ; 
    if(isSelected(obj)) {
      nodeIcon = getColapseIcon();
      iconGroup = getSelectedIcon();
      note = " NodeSelected" ;             
    }    
    String beanIconField = getBeanIconField();
    if(beanIconField != null && beanIconField.length() > 0) {
      if(getFieldValue(obj, beanIconField) != null)
        iconGroup = (String)getFieldValue(obj, beanIconField);
    }
    String objId = String.valueOf(getId(obj)) ;
    String actionLink = url("ChangeNode", objId);
    StringBuilder builder = new StringBuilder();
    if(nodeIcon.equals(getExpandIcon())) {
      builder.append(" <a class=\"").append(nodeIcon).append(" ").append(nodeTypeIcon).append("\" href=\"").append(actionLink).append("\">") ;
    } else {
      builder.append(" <a class=\"").append(nodeIcon).append(" ").append(nodeTypeIcon).append("\" onclick=\"eXo.portal.UIPortalControl.collapseTree(this)").append("\">") ;
    }
    UIRightClickPopupMenu popupMenu = getUiPopupMenu();
    String beanLabelField = getBeanLabelField();
    String className="NodeIcon";
    boolean flgSymlink = false;
    if (Utils.isSymLink(node)) {
      flgSymlink = true;
      className = "NodeIconLink";
    }
    if(popupMenu == null) {
      builder.append(" <div class=\"").append(className).append(" ").append(iconGroup).append(" ").append(nodeTypeIcon)
          .append(note).append("\"").append(" title=\"").append(getFieldValue(obj, beanLabelField))
          .append("\"").append(">");
      if (flgSymlink) {
        builder.append("  <div class=\"LinkSmall\">")
          .append(getFieldValue(obj, beanLabelField))
          .append("</div>");
      } else {
        builder.append(getFieldValue(obj, beanLabelField));
      }
      builder.append("</div>");
    } else {
      builder.append(" <div class=\"").append(className).append(" ").append(iconGroup).append(" ").append(nodeTypeIcon)
          .append(note).append("\" ").append(popupMenu.getJSOnclickShowPopup(objId, null)).append(
              " title=\"").append(getFieldValue(obj, beanLabelField)).append("\"").append(">");
      if (flgSymlink) {
        builder.append("  <div class=\"LinkSmall\">")
          .append(getFieldValue(obj, beanLabelField))
          .append("</div>");
      } else {
        builder.append(getFieldValue(obj, beanLabelField));
      }
      builder.append("</div>");
    }
    builder.append(" </a>");
    return builder.toString();
  }

  public boolean isSelected(Object obj) throws Exception {
    Node selectedNode = this.getSelected();
    Node node = (Node) obj;
    if(selectedNode == null) return false;    
    return selectedNode.getPath().equals(node.getPath());
  }
  
}
