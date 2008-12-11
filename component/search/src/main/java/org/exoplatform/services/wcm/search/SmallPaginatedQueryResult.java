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
package org.exoplatform.services.wcm.search;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jcr.Node;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.exoplatform.commons.exception.ExoMessageException;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.phamvu@exoplatform.com
 * Dec 10, 2008
 */
public class SmallPaginatedQueryResult extends WCMPaginatedQueryResult{
  
  /** The array list. */
  private CopyOnWriteArrayList<ResultNode> arrayList = new CopyOnWriteArrayList<ResultNode>();
  
  /**
   * Instantiates a new small paginated query result.
   * 
   * @param pageSize the page size
   */
  public SmallPaginatedQueryResult(int pageSize) {
    super(pageSize);
  }

  /**
   * Instantiates a new small paginated query result.
   * 
   * @param queryResult the query result
   * @param queryCriteria the query criteria
   * @param pageSize the page size
   * 
   * @throws Exception the exception
   */
  public SmallPaginatedQueryResult(QueryResult queryResult, QueryCriteria queryCriteria, int pageSize) throws Exception{
    super(queryResult,queryCriteria,pageSize);    
    RowIterator rowIterator = queryResult.getRows();    
    for(;nodeIterator.hasNext();) {
      Node node = nodeIterator.nextNode();
      Node viewNode = filterNodeToDisplay(node);
      if(viewNode == null) continue;      
      //Skip back 1 position to get current row mapping to the node
      long position = nodeIterator.getPosition();
      long rowPosition = rowIterator.getPosition();        
      long skipNum = position - rowPosition;
      rowIterator.skip(skipNum -1);        
      Row row = rowIterator.nextRow();
      ResultNode resultNode = new ResultNode(viewNode,row);
      arrayList.addIfAbsent(resultNode);
    }
    setPageSize(pageSize);
    setAvailablePage(arrayList.size());    
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.utils.PaginatedNodeIterator#getPage(int)
   */
  public List getPage(int page) throws Exception {
    checkAndSetPage(page);
    populateCurrentPage(page);
    return currentListPage_;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.search.PaginatedQueryResult#populateCurrentPage(int)
   */
  protected void populateCurrentPage(int page) throws Exception {    
    currentListPage_ = arrayList.subList(getFrom(), getTo());
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.commons.utils.PageList#checkAndSetPage(int)
   */
  protected void checkAndSetPage(int page) throws Exception {
    if (page < 1 || page > availablePage_) {
      Object[] args = { Integer.toString(page), Integer.toString(availablePage_) };
      throw new ExoMessageException("PageList.page-out-of-range", args);
    }
    currentPage_ = page;
  }
}