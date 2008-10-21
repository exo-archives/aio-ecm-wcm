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
package org.exoplatform.wcm.webui.paginator;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.query.QueryResult;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.phamvu@exoplatform.com
 * Oct 21, 2008
 */
public class WCMPaginatedQueryResult extends PaginatedQueryResult {  
  
  /** The allow duplicated. */
  private boolean allowDuplicated;
  
  /** The displayed node paths. */
  private List<String> displayedNodePaths = new ArrayList<String>();
  
  /**
   * Instantiates a new wCM paginated query result.
   * 
   * @param pageSize the page size
   */
  public WCMPaginatedQueryResult(int pageSize) {
    super(pageSize);
  }
  
  /**
   * Instantiates a new wCM paginated query result.
   * 
   * @param queryResult the query result
   * @param pageSize the page size
   * @param allowDuplicated the allow duplicated
   * 
   * @throws Exception the exception
   */
  public WCMPaginatedQueryResult(QueryResult queryResult,int pageSize, boolean allowDuplicated) throws Exception{
    super(queryResult,pageSize);    
    this.allowDuplicated = allowDuplicated;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.wcm.webui.paginator.PaginatedQueryResult#filterNodeToDisplay(javax.jcr.Node)
   */
  protected Node filterNodeToDisplay(Node node) throws Exception {
    Node displayNode = node;
    if(displayNode.isNodeType("nt:resource")) {
      displayNode = node.getParent();
    }
    if(displayNode.isNodeType("exo:htmlFile")) {
      Node parent = displayNode.getParent();
      if(parent.isNodeType("exo:webcontent"))
        displayNode = parent;
    }
    if(!allowDuplicated) {
      if(displayedNodePaths.contains(displayNode.getPath())) 
        return null;
      displayedNodePaths.add(displayNode.getPath());
    }
    return displayNode;
  }
}
