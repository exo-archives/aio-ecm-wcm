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

import javax.jcr.NodeIterator;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.jcr.impl.core.query.lucene.TwoWayRangeIterator;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Oct 17, 2008  
 */
public class NodePageList  extends PageList{

  private NodeIterator nodeIterator;

  public NodePageList(int pageSize) {
    super(pageSize);
  }
  
  public NodePageList(NodeIterator nodeIterator, int pageSize) {
    super(pageSize);
    this.nodeIterator = nodeIterator;    
  }   
  
  protected void populateCurrentPage(int page) throws Exception {
    //Iterate next
    if(page>currentPage_) {
      long skipNextNum = ((page-currentPage_)+1)*getPageSize();
      nodeIterator.skip(skipNextNum);
    }else if(page<currentPage_) {
      //Iterate back
      long skipBackNum = ((currentPage_ - page) + 1)* getPageSize();
      ((TwoWayRangeIterator)nodeIterator).skipBack(skipBackNum);
    }
    currentListPage_ = new ArrayList();
    int count = 0;
    while (nodeIterator.hasNext()) {
      count ++;
      if(count == getPageSize()) {
        break;
      }
      currentListPage_.add(nodeIterator.next());
    }
    currentPage_ = page;
  }
  
  public NodeIterator getNodeIterator() { return this.nodeIterator; }
  public void setNodeIterator(NodeIterator iterator) { this.nodeIterator = iterator; }
  
  public int getTotalPages() { return getAvailablePage(); }  
  public int getNodesPerPage() { return getPageSize(); }    
  public long getTotalNodes() { return nodeIterator.getSize(); } 
  public long getCurrentNodePosition() { return nodeIterator.getPosition(); }
  
  public List getCurrentPageData() throws Exception {
    return currentPage();
  }
  
  public void changePage(int page) throws Exception {
    populateCurrentPage(page);
  }
  
  public List getAll() throws Exception {
    throw new Exception("Unsupported this method");
  }
}
