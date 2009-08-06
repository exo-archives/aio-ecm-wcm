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
package org.exoplatform.wcm.webui.selector.page;

import java.util.ResourceBundle;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 30, 2009  
 */
@ComponentConfigs({
        @ComponentConfig(
            lifecycle = UIFormLifecycle.class,
            template = "classpath:groovy/wcm/webui/selector/page/UIPageSelector.gtmpl",
            events = {@EventConfig(listeners = UIPageSelector.ChangeNodeActionListener.class, phase = Phase.DECODE)}
        )
    }
)
public class UIPageSelector extends UIForm {
  
  private UIComponent sourceUIComponent ;
  
  private String returnFieldName ;
  
  private String pageTitle;
  
  public UIPageSelector() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    ResourceBundle bundle = context.getApplicationResourceBundle();
    String rootBundleKey = "UICategoryNavigationTargetPathPopupWindow.title.UIPageSelector";
    setPageTitle(bundle.getString(rootBundleKey));
    UIPageNodeSelector pageNodeSelector = addChild(UIPageNodeSelector.class, null, null);
    UITree uiTree = pageNodeSelector.getChild(UITree.class);
    uiTree.setUIRightClickPopupMenu(null);
    pageNodeSelector.selectPageNodeByUri(null);
    
    UIPageSelectorPanel pageSelectorPanel = addChild(UIPageSelectorPanel.class, null, null);
    pageSelectorPanel.updateGrid();
  }

  public String getReturnFieldName() { return returnFieldName; }

  public void setReturnFieldName(String name) { this.returnFieldName = name; }

  public UIComponent getSourceComponent() { return sourceUIComponent; }
  
  public void setSourceComponent(UIComponent uicomponent, String[] initParams) {
    sourceUIComponent = uicomponent ;
    if(initParams == null || initParams.length < 0) return ;
    for(int i = 0; i < initParams.length; i ++) {
      if(initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=") ;
        returnFieldName = array[1] ;
        break ;
      }
      returnFieldName = initParams[0] ;
    }
  }
  
  public void processDecode(WebuiRequestContext context) throws Exception {   
    super.processDecode(context);
    String action = context.getRequestParameter(UIForm.ACTION);
    Event<UIComponent> event = createEvent(action, Event.Phase.DECODE, context) ;   
    if(event != null) event.broadcast() ;   
  }
  
  public void setPageTitle(String pageTitle) {
    this.pageTitle = pageTitle;
  }

  public String getPageTitle() {
    return pageTitle;
  }

  public static class ChangeNodeActionListener extends EventListener<UIPageSelector> {
    public void execute(Event<UIPageSelector> event) throws Exception {
      UIPageSelector pageSelector = event.getSource() ;
      UIPageNodeSelector pageNodeSelector = pageSelector.getChild(UIPageNodeSelector.class) ; 
      String uri  = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITree tree = pageNodeSelector.getChild(UITree.class) ;
      if(tree.getParentSelected() == null && (uri == null || uri.length() < 1)){
        pageNodeSelector.selectNavigation(pageNodeSelector.getSelectedNavigation().getId()) ;
      } else {
        pageNodeSelector.selectPageNodeByUri(uri) ;
      }

      UIPageSelectorPanel pageSelectorPanel = pageSelector.getChild(UIPageSelectorPanel.class);
      pageSelectorPanel.setSelectedPage(pageNodeSelector.getSelectedNode().getNode());
      pageSelectorPanel.updateGrid();
      
      event.getRequestContext().addUIComponentToUpdateByAjax(pageSelector) ;
    }
  }
}
