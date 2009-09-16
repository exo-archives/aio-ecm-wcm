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
package org.exoplatform.wcm.webui.selector.webContentView;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai Van
 *          maivanha1610@gmail.com
 */
public class TreeNode {
  private String treePath;
  private int deep;
  private String name;
  private Node node_ ; 
  private String workSpaceName;

  public TreeNode(String path, String workSpaceName, Node node, int deep) {
    node_ = node ;
    this.deep = deep;
    this.workSpaceName = workSpaceName;
    try {
      this.treePath = path + "/" + getName();
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
  }
  
  public TreeNode(String name){
    this.name = name;
    this.treePath = "/" + name;
    node_ = null;
    deep = 0;
  }
  
  public TreeNode(String path, String name, String workSpaceName, Node node, int deep){
    this.name = name;
    this.node_ = node;
    this.deep = deep;
    this.workSpaceName = workSpaceName;
    this.treePath = path + "/" + name;
  }

  public String getName() throws RepositoryException {
    StringBuilder buffer = new StringBuilder(128);
    if(name == null || name.trim().length() < 1){
      buffer.append(node_.getName());
      int index = node_.getIndex();
      if (index > 1) {
        buffer.append('[');
        buffer.append(index);
        buffer.append(']');
      }
    }else{
      buffer.append(this.name);
    }
    return buffer.toString();  
  }

  public String getNodePath() throws RepositoryException { 
    if(node_ != null) return node_.getPath();
    else return null;
    }

  public Node getNode() { return node_ ; }  
  public void setNode(Node node) { node_ = node ; }

  public int getDeep() {
    return deep;
  }

  public void setDeep(int deep) {
    this.deep = deep;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getWorkSpaceName() {
    return workSpaceName;
  }

  public void setWorkSpaceName(String workSpaceName) {
    this.workSpaceName = workSpaceName;
  }

  public String getTreePath() {
    return treePath;
  }

  public void setTreePath(String treePath) {
    this.treePath = treePath;
  }
}