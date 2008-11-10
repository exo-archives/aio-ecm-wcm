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
package org.exoplatform.wcm.webui.search;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 31, 2008
 */
@ComponentConfig(lifecycle = Lifecycle.class, events = { @EventConfig(listeners = UISearchPageLayout.QuickEditActionListener.class) })
public class UISearchPageLayout extends UIContainer {

  public static final String SEARCH_FORM   = "uiSearchForm".intern();

  public static final String SEARCH_RESULT = "uiSearchResult".intern();

  public UISearchPageLayout() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    UISearchForm uiSearchForm = addChild(UISearchForm.class, null, null);
    UISearchResult uiSearchResult = addChild(UISearchResult.class, null, null);
    String searchFormTemplatePath = getTemplatePath(UIWCMSearchPortlet.SEARCH_FORM_TEMPLATE_PATH);
    uiSearchForm.init(searchFormTemplatePath, getTemplateResourceResolver(context,
        searchFormTemplatePath));
    String searchResultTemplatePath = getTemplatePath(UIWCMSearchPortlet.SEARCH_RESULT_TEMPLATE_PATH);
    uiSearchResult.init(searchResultTemplatePath, getTemplateResourceResolver(context,
        searchResultTemplatePath));    
  }

  private PortletPreferences getPortletPreference() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    return portletRequestContext.getRequest().getPreferences();
  }

  private String getRepository() {
    return getPortletPreference().getValue(UIWCMSearchPortlet.REPOSITORY, null);
  }

  private String getTemplatePath(String templateType) {
    return getPortletPreference().getValue(templateType, null);
  }

  public String getTemplate() {
    String template = getPortletPreference().getValue(
        UIWCMSearchPortlet.SEARCH_PAGE_LAYOUT_TEMPLATE_PATH, null);
    return template;
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try {
      RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
      String repository = getRepository();
      ManageableRepository manageableRepository = repositoryService.getRepository(repository);
      String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
      return new JCRResourceResolver(repository, workspace, "exo:templateFile");
    } catch (Exception e) {
      return null;
    }
  }

  public boolean isQuickEditable() throws Exception {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = portletRequestContext.getRequest().getPreferences();
    boolean isQuickEdit = Boolean.parseBoolean(prefs.getValue(
        UIWCMSearchPortlet.SHOW_QUICK_EDIT_BUTTON, null));
    UIWCMSearchPortlet uiSearchPortlet = getAncestorOfType(UIWCMSearchPortlet.class);
    if (isQuickEdit)
      return uiSearchPortlet.canEditPortlet();
    return false;
  }

  public String getPortletId() {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext
        .getCurrentInstance();
    return pContext.getWindowId();
  }

  public static class QuickEditActionListener extends EventListener<UISearchPageLayout> {
    public void execute(Event<UISearchPageLayout> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.EDIT);
    }
  }

}
