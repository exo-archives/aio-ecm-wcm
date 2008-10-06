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
import org.exoplatform.web.application.ApplicationMessage;
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
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * May 23, 2007
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/groovy/simple-search/webui/component/UISimpleSearchForm.gtmpl", 
    events = {
      @EventConfig(listeners = UISimpleSearchForm.SearchSimpleActionListener.class),
      @EventConfig(listeners = UISimpleSearchForm.SearchAdvanceActionListener.class) 
    }
)
public class UISimpleSearchForm extends UIForm {

  protected final static String KEYWORD_INPUT     = "keyword";

  protected final static String DOCUMENT_CHECKING = "documentCheckBox";

  protected final static String PAGE_CHECKING     = "pageCheckBox";

  protected final static String PORTALS_SELECTION = "portalSelection";

  protected final static String ALL_OPTION        = "all";

  /**
   * Instantiates a new uI simple search form.
   * 
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public UISimpleSearchForm() throws Exception {
    UIFormStringInput keywordInput = new UIFormStringInput(KEYWORD_INPUT, KEYWORD_INPUT, null);
    UIFormSelectBox portalSelectBox = new UIFormSelectBox(PORTALS_SELECTION, PORTALS_SELECTION,
        getPortalList());
    UIFormCheckBoxInput pageCheckBoxInput = new UIFormCheckBoxInput(PAGE_CHECKING, PAGE_CHECKING,
        null);
    UIFormCheckBoxInput documentCheckBoxInput = new UIFormCheckBoxInput(DOCUMENT_CHECKING,
        DOCUMENT_CHECKING, null);

    pageCheckBoxInput.setChecked(true);
    documentCheckBoxInput.setChecked(true);

    addUIFormInput(keywordInput);
    addUIFormInput(portalSelectBox);
    addUIFormInput(pageCheckBoxInput);
    addUIFormInput(documentCheckBoxInput);
  }

  /**
   * The listener interface for receiving simpleSearchAction events. The class
   * that is interested in processing a simpleSearchAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addSimpleSearchActionListener<code> method. When
   * the simpleSearchAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SimpleSearchActionEvent
   */
  public static class SearchSimpleActionListener extends EventListener<UISimpleSearchForm> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UISimpleSearchForm> event) throws Exception {
      UISimpleSearchForm uiSearchForm = event.getSource();
      UISimpleSearchPortlet uiPortletSearch = uiSearchForm.getParent();
      UISearchResultForm uiSearchResult = uiPortletSearch.getChild(UISearchResultForm.class);
      UIApplication uiApp = uiSearchForm.getAncestorOfType(UIApplication.class);
      WcmSearchService searchService = uiSearchForm.getApplicationComponent(WcmSearchService.class);
      String keyword = uiSearchForm.getUIStringInput(KEYWORD_INPUT).getValue();
      if ((keyword == null) || (keyword.trim().length() == 0)) {
        uiApp.addMessage(new ApplicationMessage("UISimpleSearchForm.msg.keyword-require", null,
            ApplicationMessage.WARNING));
        uiSearchForm.clearAll();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      if (!uiSearchForm.isDocumentChecked() && !uiSearchForm.isPageChecked()) {
        uiApp.addMessage(new ApplicationMessage("UISimpleSearchForm.msg.case-require", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      String portalName = uiSearchForm.getUIFormSelectBox(PORTALS_SELECTION).getValue();
      if (portalName.equalsIgnoreCase(ALL_OPTION))
        portalName = null;
      String userId = Util.getPortalRequestContext().getRemoteUser();
      SessionProvider sessionProvider = null;
      if (userId == null) {
        sessionProvider = SessionProviderFactory.createAnonimProvider();
      } else {
        sessionProvider = SessionProviderFactory.createSystemProvider();
      }
      PageList resultList = searchService.searchWebContent(keyword, portalName, uiSearchForm
          .isDocumentChecked(), uiSearchForm.isPageChecked(), sessionProvider);
      resultList.setPageSize(2);
      uiSearchResult.setResultList(resultList);
      uiSearchResult.setPortalName(portalName);
      uiSearchForm.clearAll();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortletSearch);
    }
  }

  /**
   * The listener interface for receiving advanceSearchAction events. The class
   * that is interested in processing a advanceSearchAction event implements
   * this interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addAdvanceSearchActionListener<code> method. When
   * the advanceSearchAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see AdvanceSearchActionEvent
   */
  public static class SearchAdvanceActionListener extends EventListener<UISimpleSearchForm> {

    /*
     * (non-Javadoc)
     * 
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UISimpleSearchForm> event) throws Exception {

    }
  }

  private void clearAll() {
    getUIStringInput(KEYWORD_INPUT).setValue(null);
    getUIFormCheckBoxInput(DOCUMENT_CHECKING).setChecked(false);
    getUIFormCheckBoxInput(PAGE_CHECKING).setChecked(false);
    getUIFormSelectBox(PORTALS_SELECTION).setValue(ALL_OPTION);
  }

  private boolean isDocumentChecked() {
    return getUIFormCheckBoxInput(DOCUMENT_CHECKING).isChecked();
  }

  private boolean isPageChecked() {
    return getUIFormCheckBoxInput(PAGE_CHECKING).isChecked();
  }

  @SuppressWarnings("unchecked")
  private List getPortalList() throws Exception {
    List<SelectItemOption<String>> portals = new ArrayList<SelectItemOption<String>>();
    DataStorage service = getApplicationComponent(DataStorage.class);
    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, PortalConfig.class);
    List<PortalConfig> list = service.find(query).getAll();
    portals.add(new SelectItemOption<String>(ALL_OPTION, ALL_OPTION));
    for (PortalConfig portalConfig : list) {
      portals.add(new SelectItemOption<String>(portalConfig.getName(), portalConfig.getName()));
    }
    return portals;
  }

}
