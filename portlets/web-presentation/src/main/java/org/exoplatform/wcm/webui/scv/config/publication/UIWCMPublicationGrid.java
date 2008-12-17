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
package org.exoplatform.wcm.webui.scv.config.publication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.services.wcm.publication.WebpagePublicationPlugin;
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
 * dzungdev@gmail.com
 * Oct 9, 2008
 */
@ComponentConfig (
    template = "app:/groovy/SingleContentViewer/config/UIWCMPublicationGrid.gtmpl",
    events = {
        @EventConfig(listeners = UIWCMPublicationGrid.SelectActionListener.class)
    }
)

public class UIWCMPublicationGrid extends UIGrid {

  /** The Constant LIFECYCLE_NAME. */
  public static final String LIFECYCLE_NAME = "LifecycleName".intern();

  /** The Constant LIFECYCLE_DESC. */
  public static final String LIFECYCLE_DESC = "LifecycleDesc".intern();

  /** The LIFECYCL e_ fields. */
  private String[] LIFECYCLE_FIELDS = {LIFECYCLE_NAME, LIFECYCLE_DESC};

  /** The LIFECYCL e_ actions. */
  private String[] LIFECYCLE_ACTIONS = {"Select"};

  /** The lifecycle name selected. */
  private String lifecycleNameSelected;

  /** The is view grid. */
  private boolean isViewGrid;

  /** The source component. */
  private UIComponent sourceComponent;

  /**
   * Instantiates a new uIWCM publication grid.
   * 
   * @throws Exception the exception
   */
  public UIWCMPublicationGrid() throws Exception {
    configure(LIFECYCLE_NAME, LIFECYCLE_FIELDS, LIFECYCLE_ACTIONS);
    getUIPageIterator().setId("WCMPublicationIterator");
  }

  /**
   * Update grid.
   */
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

  /**
   * Gets the lifecycle name selected.
   * 
   * @return the lifecycle name selected
   */
  public String getLifecycleNameSelected() { return lifecycleNameSelected; }

  /**
   * Checks if is view grid.
   * 
   * @return true, if is view grid
   */
  public boolean isViewGrid() {
    return isViewGrid;
  }

  /**
   * The Class LifecycleBean.
   */
  public static class LifecycleBean {

    /** The Lifecycle name. */
    private String LifecycleName;

    /** The Lifecycle desc. */
    private String LifecycleDesc;

    /**
     * Gets the lifecycle desc.
     * 
     * @return the lifecycle desc
     */
    public String getLifecycleDesc() {
      return LifecycleDesc;
    }

    /**
     * Sets the lifecycle desc.
     * 
     * @param lifecycleDesc the new lifecycle desc
     */
    public void setLifecycleDesc(String lifecycleDesc) {
      LifecycleDesc = lifecycleDesc;
    }

    /**
     * Gets the lifecycle name.
     * 
     * @return the lifecycle name
     */
    public String getLifecycleName() {
      return LifecycleName;
    }

    /**
     * Sets the lifecycle name.
     * 
     * @param lifecycleName the new lifecycle name
     */
    public void setLifecycleName(String lifecycleName) {
      LifecycleName = lifecycleName;
    }

  }

  /**
   * The listener interface for receiving selectAction events.
   * The class that is interested in processing a selectAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSelectActionListener<code> method. When
   * the selectAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SelectActionEvent
   */
  public static class SelectActionListener extends EventListener<UIWCMPublicationGrid> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
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

  /**
   * Sets the lifecycle name selected.
   * 
   * @param lifecycleNameSelected the new lifecycle name selected
   */
  public void setLifecycleNameSelected(String lifecycleNameSelected) {
    this.lifecycleNameSelected = lifecycleNameSelected;
  }

  /**
   * Gets the source component.
   * 
   * @return the source component
   */
  public UIComponent getSourceComponent() {
    return sourceComponent;
  }

  /**
   * Sets the source component.
   * 
   * @param sourceComponent the new source component
   */
  public void setSourceComponent(UIComponent sourceComponent) {
    this.sourceComponent = sourceComponent;
  }
}
