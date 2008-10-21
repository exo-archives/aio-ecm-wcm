/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham  
 *          hoa.phamvu@exoplatform.com
 * Oct 17, 2008  
 */
@ComponentConfig(
    template = "classpath:groovy/wcm/webui/paginator/UINodePaginator.gtmpl",
    events = @EventConfig(listeners = UIQueryResultPaginator.ShowPageActionListener.class )    
)
public class UIQueryResultPaginator extends UIComponent {
  
  private NodePageList nodePageList;
  private boolean showFistLast = true;
  private int visiblePages = 5;
  private int pageJumpingNumber = 1;  
  
	public UIQueryResultPaginator() {
	}
	
	public int getVisiblePages() { return visiblePages; }
	public void setVisiblePages(int number) { this.visiblePages = number; }
	
	public int getPageJumpingNumber() { return pageJumpingNumber; }
	public void setPageJumpingNumber(int num) { this.pageJumpingNumber = num; }
	
	public boolean showFistLast() { return showFistLast; }
	public void setShowFistLast(boolean b) { this.showFistLast = b; }
	
	public PageList getNodePageList() { return nodePageList; }
	public void setPageList(NodePageList pageList) { 
	  nodePageList = pageList ;
  }      	
	
	public int getCurrentPage() { return nodePageList.getCurrentPage(); }
	
	public void changePage(int page) throws Exception {
	  nodePageList.changePage(page);
	}
	
	public long getTotalNodes() { return nodePageList.getTotalNodes(); }
	public int getTotalPages() { return nodePageList.getAvailablePage();}
	
	public List getCurrentPageData()  throws Exception { return nodePageList.getCurrentPageData(); }
	
  static  public class ShowPageActionListener extends EventListener<UIQueryResultPaginator> {
    public void execute(Event<UIQueryResultPaginator> event) throws Exception {
      UIQueryResultPaginator uiPageIterator = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      uiPageIterator.changePage(page);
      UIComponent parent = uiPageIterator.getParent();
      if(parent == null) return ;      
      event.getRequestContext().addUIComponentToUpdateByAjax(parent);           
      parent.broadcast(event,event.getExecutionPhase());
    }
  }
  
}
