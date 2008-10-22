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
package org.exoplatform.services.wcm.utils;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.NodeIterator;

import org.exoplatform.commons.exception.ExoMessageException;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.jcr.impl.core.query.lucene.TwoWayRangeIterator;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.phamvu@exoplatform.com
 * Oct 17, 2008
 */
public class PaginatedNodeIterator  extends PageList {
  
  protected NodeIterator nodeIterator;

  /**
   * Instantiates a new paginated node iterator.
   * 
   * @param pageSize the page size
   */
  public PaginatedNodeIterator(int pageSize) {
    super(pageSize);
  }
  
  /**
   * Instantiates a new paginated node iterator.
   * 
   * @param nodeIterator the node iterator
   * @param pageSize the page size
   */
  public PaginatedNodeIterator(NodeIterator nodeIterator, int pageSize) {
    super(pageSize);
    this.nodeIterator = nodeIterator;
    this.setAvailablePage((int)nodeIterator.getSize());
  }   
  
  /* (non-Javadoc)
   * @see org.exoplatform.commons.utils.PageList#populateCurrentPage(int)
   */
  protected void populateCurrentPage(int page) throws Exception {
    checkAndSetPosition(page);
    currentListPage_ = new ArrayList();
    int count = 0;
    while (nodeIterator.hasNext()) {
      currentListPage_.add(nodeIterator.next());
      count ++;
      if(count == getPageSize()) {
        break;
      }      
    }
    currentPage_ = page;
  }
  
  /**
   * Retrieve the node iterator.
   * 
   * @return the node iterator
   */
  public NodeIterator getNodeIterator() { return this.nodeIterator; }
  
  /**
   * Sets the node iterator.
   * 
   * @param iterator the new node iterator
   */
  public void setNodeIterator(NodeIterator iterator) { this.nodeIterator = iterator; }
  
  /**
   * Retrieve the total pages.
   * 
   * @return the total pages
   */
  public int getTotalPages() { return getAvailablePage(); }  
  
  /**
   * Retrieve the nodes per page.
   * 
   * @return the nodes per page
   */
  public int getNodesPerPage() { return getPageSize(); }    
  
  /**
   * Retrieve the total nodes
   * 
   * @return the total nodes
   */
  public long getTotalNodes() { return nodeIterator.getSize(); }
  
  /**
   * Retrieve the nodes of current page
   * 
   * @return the current page data
   * 
   * @throws Exception the exception
   */
  public List getCurrentPageData() throws Exception {
    return currentPage();
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.commons.utils.PageList#getPage(int)
   */
  public List getPage(int page) throws Exception {
    if (page < 1 || page > availablePage_) {
      Object[] args = { Integer.toString(page), Integer.toString(availablePage_) };
      throw new ExoMessageException("PageList.page-out-of-range", args);
    }
    populateCurrentPage(page);
    return currentListPage_;
  }
  
  /**
   * Change page.
   * 
   * @param page the page
   * 
   * @throws Exception the exception
   */
  public void changePage(int page) throws Exception {
    populateCurrentPage(page);
  }
  
  /**
   * Check and set current cursor position in iterator
   * 
   * @param page the page
   */
  protected void checkAndSetPosition(int page) {
    //Iterate next
    if (page > currentPage_) {
      long skipNextNum = (page - (currentPage_ + 1)) * getPageSize();
      nodeIterator.skip(skipNextNum);
    } else if(page < currentPage_) {
      //Iterate back
      int currentPageSize = currentListPage_.size();
      long skipBackNum = ((currentPage_ - page) * getPageSize()) + currentPageSize;      
      ((TwoWayRangeIterator)nodeIterator).skipBack(skipBackNum);           
    }
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.commons.utils.PageList#getAll()
   */
  public List getAll() throws Exception {
    throw new UnsupportedOperationException();
  }
}
