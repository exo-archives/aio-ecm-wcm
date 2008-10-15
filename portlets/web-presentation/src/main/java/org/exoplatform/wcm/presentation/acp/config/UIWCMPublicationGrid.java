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
package org.exoplatform.wcm.presentation.acp.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
import org.exoplatform.wcm.presentation.acp.config.selector.UIWebContentSelectorForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * Oct 9, 2008  
 */
@ComponentConfig (
    template = "app:/groovy/advancedPresentation/config/UIWCMPublicationGrid.gtmpl",
    events = {
        @EventConfig(listeners = UIWCMPublicationGrid.SelectActionListener.class)
    }
)

public class UIWCMPublicationGrid extends UIGrid {

  public static final String LIFECYCLE_NAME = "LifecycleName".intern();
  public static final String LIFECYCLE_DESC = "LifecycleDesc".intern();
  private String[] LIFECYCLE_FIELDS = {LIFECYCLE_NAME, LIFECYCLE_DESC};
  private String[] LIFECYCLE_ACTIONS = {"Select"};
  private String lifecycleNameSelected;
  private boolean isViewGrid;
  private UIComponent sourceComponent;

  public UIWCMPublicationGrid() throws Exception {
    configure(LIFECYCLE_NAME, LIFECYCLE_FIELDS, LIFECYCLE_ACTIONS);
    getUIPageIterator().setId("WCMPublicationIterator");
  }

  public void updateGrid() {
    WCMPublicationService wcmPublicationService = getApplicationComponent(WCMPublicationService.class);
    Map<String,WebpagePublicationPlugin> publicationPlugins = wcmPublicationService.getWebpagePublicationPlugins();
    List<LifecycleBean> lifecycleBeanList = new ArrayList<LifecycleBean>();
    Set<String> keySet = publicationPlugins.keySet();
    if (keySet.size() == 1) {
      for (String str: keySet) {
        lifecycleNameSelected = publicationPlugins.get(str).getLifecycleName();
        return;
      }
    }
    isViewGrid = true;
    for (String key: keySet) {
      WebpagePublicationPlugin webPublicationPlugin = publicationPlugins.get(key);
      LifecycleBean lifecycleBean = new LifecycleBean();
      lifecycleBean.setLifecycleName(webPublicationPlugin.getLifecycleName());
      lifecycleBean.setLifecycleDesc(webPublicationPlugin.getDescription());
      lifecycleBeanList.add(lifecycleBean);
    }
    ObjectPageList objectPageList = new ObjectPageList(lifecycleBeanList, 10);
    getUIPageIterator().setPageList(objectPageList);
  }

  public String getLifecycleNameSelected() { return lifecycleNameSelected; }

  public boolean isViewGrid() {
    return isViewGrid;
  }

  public static class LifecycleBean {
    private String LifecycleName;
    private String LifecycleDesc;
    public String getLifecycleDesc() {
      return LifecycleDesc;
    }
    public void setLifecycleDesc(String lifecycleDesc) {
      LifecycleDesc = lifecycleDesc;
    }
    public String getLifecycleName() {
      return LifecycleName;
    }
    public void setLifecycleName(String lifecycleName) {
      LifecycleName = lifecycleName;
    }

  }

  public static class SelectActionListener extends EventListener<UIWCMPublicationGrid> {
    public void execute(Event<UIWCMPublicationGrid> event) throws Exception {
      UIWCMPublicationGrid wcmPublicationGrid = event.getSource();
      String lifecycleName = event.getRequestContext().getRequestParameter(OBJECTID);
      wcmPublicationGrid.setLifecycleNameSelected(lifecycleName);
      UIPopupWindow popupWindow = wcmPublicationGrid.getAncestorOfType(UIPopupWindow.class);
      if (popupWindow == null) {
      event.getRequestContext().addUIComponentToUpdateByAjax(wcmPublicationGrid);
      } else {
        ((UISelectable)wcmPublicationGrid.getSourceComponent()).doSelect("PublicationPath", lifecycleName);
      }
    }    
  }

  public void setLifecycleNameSelected(String lifecycleNameSelected) {
    this.lifecycleNameSelected = lifecycleNameSelected;
  }

  public UIComponent getSourceComponent() {
    return sourceComponent;
  }

  public void setSourceComponent(UIComponent sourceComponent) {
    this.sourceComponent = sourceComponent;
  }
}
