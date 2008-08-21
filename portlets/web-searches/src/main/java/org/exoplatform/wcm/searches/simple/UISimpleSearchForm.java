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
package org.exoplatform.wcm.searches.simple;


import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.search.WcmSearchService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

// TODO: Auto-generated Javadoc
/**
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 * anh.do@exoplatform.com
 * May 23, 2007
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/groovy/simple-search/webui/component/UISimpleSearchForm.gtmpl", 
    events = {
      @EventConfig(listeners = UISimpleSearchForm.SimpleSearchActionListener.class),
      @EventConfig(listeners = UISimpleSearchForm.AdvanceSearchActionListener.class) 
    }
)
public class UISimpleSearchForm extends UIForm {

  /**
   * Instantiates a new uI simple search form.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public UISimpleSearchForm() throws Exception {
    List<SelectItemOption<String>> portals = new ArrayList<SelectItemOption<String>>();
    DataStorage service = getApplicationComponent(DataStorage.class);
    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, PortalConfig.class);
    List<PortalConfig> list = service.find(query).getAll();
    portals.add(new SelectItemOption<String>("all", "all"));
    for (PortalConfig portalConfig : list) {
      portals.add(new SelectItemOption<String>(portalConfig.getName(), portalConfig.getName()));
    }
    addUIFormInput(new UIFormStringInput("SearchInput", "SearchInput", null));
    addUIFormInput(new UIFormSelectBox("PortalSelection", "PortalSelection", portals));
    addUIFormInput(new UIFormCheckBoxInput("PageCheckBoxInput", "PageCheckBoxInput", null));
    addUIFormInput(new UIFormCheckBoxInput("DocumentCheckBoxInput", "DocumentCheckBoxInput", null));

  }

  /**
   * The listener interface for receiving simpleSearchAction events.
   * The class that is interested in processing a simpleSearchAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSimpleSearchActionListener<code> method. When
   * the simpleSearchAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SimpleSearchActionEvent
   */
  public static class SimpleSearchActionListener extends EventListener<UISimpleSearchForm> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UISimpleSearchForm> event) throws Exception {	  
      UISimpleSearchForm searchForm = event.getSource();
      UISimpleSearchPortlet searchPortlet = searchForm.getParent();
      UISearchResultForm searchResult = searchPortlet.getChild(UISearchResultForm.class);
      WcmSearchService searchService = searchForm.getApplicationComponent(WcmSearchService.class);
      String keyword = searchForm.getUIStringInput("SearchInput").getValue();
      if (keyword == null) {
        UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
        Util.getPortalRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        event.getRequestContext().addUIComponentToUpdateByAjax(searchPortlet);
        return;
      }
      boolean searchDocument = false;
      boolean searchPage = false;

      if (searchForm.getUIFormCheckBoxInput("PageCheckBoxInput").isChecked()
          && searchForm.getUIFormCheckBoxInput("DocumentCheckBoxInput").isChecked()) {
        searchDocument = true;
        searchPage = true;
      } else if (searchForm.getUIFormCheckBoxInput("DocumentCheckBoxInput").isChecked()) {
        searchDocument = true;
      } else if (searchForm.getUIFormCheckBoxInput("PageCheckBoxInput").isChecked()) {
        searchPage = true;
      }
      String portalName = searchForm.getUIFormSelectBox("PortalSelection").getValue();
      if (portalName.equalsIgnoreCase("all"))
        portalName = null;
      String userId = Util.getPortalRequestContext().getRemoteUser();
      SessionProvider sessionProvider = null;
      if (userId == null) {
        sessionProvider = SessionProviderFactory.createAnonimProvider();
      } else {
        sessionProvider = SessionProviderFactory.createSystemProvider();
      }
      PageList resultList = searchService.searchWebContent(keyword, portalName, searchDocument,
          searchPage, sessionProvider);
      resultList.setPageSize(2);
      searchResult.setResultList(resultList);
      searchResult.setPortalName(portalName);
      event.getRequestContext().addUIComponentToUpdateByAjax(searchPortlet);
    }
  }

  /**
   * The listener interface for receiving advanceSearchAction events.
   * The class that is interested in processing a advanceSearchAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addAdvanceSearchActionListener<code> method. When
   * the advanceSearchAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see AdvanceSearchActionEvent
   */
  public static class AdvanceSearchActionListener extends EventListener<UISimpleSearchForm> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UISimpleSearchForm> event) throws Exception {

    }
  }
  
}
