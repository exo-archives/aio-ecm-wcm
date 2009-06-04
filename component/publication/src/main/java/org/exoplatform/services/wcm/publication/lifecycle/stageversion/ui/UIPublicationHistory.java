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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.ecm.publication.NotInPublicationLifecycleException;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.config.VersionLog;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponentDecorator;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Edited : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com
 * June 4, 2009 13:42:23 AM
 */
@ComponentConfig(
    template = "classpath:groovy/wcm/webui/publication/lifecycle/stageversion/ui/UIPublicationHistory.gtmpl",
    events = {
        @EventConfig(listeners = UIPublicationHistory.CloseActionListener.class)
    }
)
public class UIPublicationHistory extends UIComponentDecorator {
  
  private UIPageIterator uiPageIterator_ ;
  private Node currentNode_ ;
  
  public UIPublicationHistory() throws Exception {
    uiPageIterator_ = createUIComponent(UIPageIterator.class, null, "PublicationLogListIterator");
    setUIComponent(uiPageIterator_) ;
  }
  
  public void init(Node node) {
   currentNode_ = node;
  }  
  public List<VersionLog> getLog() throws NotInPublicationLifecycleException, Exception {
    if (currentNode_ == null) return new ArrayList<VersionLog>();
    List<VersionLog> logs = new ArrayList<VersionLog>();
    Value[] values = currentNode_.getProperty(StageAndVersionPublicationConstant.HISTORY).getValues();
    for (Value value : values) {
      String logString = value.getString();
      VersionLog bean = VersionLog.toVersionLog(logString);
      logs.add(bean); 
    }
    return logs;
  }
  
  @SuppressWarnings("unchecked")
  public void updateGrid() throws Exception {   
    ObjectPageList objPageList = new ObjectPageList(getLog(), 10) ;
    uiPageIterator_.setPageList(objPageList) ;
  }
  
  public UIPageIterator getUIPageIterator() { return uiPageIterator_ ; }
  
  @SuppressWarnings("unchecked")
  public List getLogList() throws Exception { return uiPageIterator_.getCurrentPageData() ; }

  public String[] getActions() {return new String[]{"Close"} ;}
  
  static public class CloseActionListener extends EventListener<UIPublicationHistory> {
    public void execute(Event<UIPublicationHistory> event) throws Exception {      
      UIPublicationHistory uiPublicationLogList = event.getSource() ;
      UIPopupContainer uiPopupContainer = (UIPopupContainer) uiPublicationLogList.getAncestorOfType(UIPopupContainer.class);
      uiPopupContainer.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
    }
  }
  
  public class HistoryBean {
    private String date;
    private String newState;
    private String user;
    private String description;
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNewState() { return newState; }
    public void setNewState(String newState) { this.newState = newState; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
    
    /**
     * Updated by Nguyen Van Chien
     * @param stringInput
     * @return
     */
    public String formatStringByDateTime(String stringInput) {      
      String dateYear = stringInput.substring(0, 4);
      String dateMonth = stringInput.substring(4, 6);
      String dateDay = stringInput.substring(6, 8);
      String dateHour = stringInput.substring(9, 11);
      String dateMinute = stringInput.substring(11, 13);
      String dateSecond = stringInput.substring(13, 15);      
      StringBuilder builder = new StringBuilder();      
      builder.append(dateMonth).append("/")
            .append(dateDay).append("/")
            .append(dateYear).append(" ")
            .append(dateHour).append(":")
            .append(dateMinute).append(":")
            .append(dateSecond);
      
      return builder.toString();
    }
  }
}
