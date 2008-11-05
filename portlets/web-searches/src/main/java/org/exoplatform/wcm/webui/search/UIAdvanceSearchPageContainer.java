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

import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.wcm.webui.search.config.UISearchPageLayoutManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 31, 2008
 */
@ComponentConfig(lifecycle = Lifecycle.class, template = "app:/groovy/webui/wcm-search/UIAdvanceSearchPageContainer.gtmpl")
public class UIAdvanceSearchPageContainer extends UIContainer {

  public UIAdvanceSearchPageContainer() throws Exception {
    UIAdvanceSearchForm uiSearchForm = addChild(UIAdvanceSearchForm.class, null, null).setRendered(
        false);
    UIAdvanceSearchBox uiSearchBox = addChild(UIAdvanceSearchBox.class, null, null).setRendered(
        false);
    UIAdvanceSearchResult uiSearchResult = addChild(UIAdvanceSearchResult.class, null, null)
        .setRendered(false);
    uiSearchBox.init(getTemplatePath(UIAdvanceSearchPortlet.SEARCH_BOX_TEMPLATE_PATH),
        getTemplateResourceResolver());
    System.out.println("\n\n===> " + getTemplatePath(UIAdvanceSearchPortlet.SEARCH_BOX_TEMPLATE_PATH) + "\n");
    uiSearchForm.init(getTemplatePath(UIAdvanceSearchPortlet.SEARCH_FORM_TEMPLATE_PATH),
        getTemplateResourceResolver());
    uiSearchResult.init(getTemplatePath(UIAdvanceSearchPortlet.SEARCH_RESULT_TEMPLATE_PATH),
        getTemplateResourceResolver());
    String searchMode = getPortletPreference().getValue(UIAdvanceSearchPortlet.SEARCH_MODE,
        UISearchPageLayoutManager.SEARCH_PAGE_MODE_OPTION);
    if (UISearchPageLayoutManager.SEARCH_PAGE_MODE_OPTION.equals(searchMode)) {
      uiSearchForm.setRendered(true);
      uiSearchResult.setRendered(true);
    } else if (UISearchPageLayoutManager.SEARCH_BOX_MODE_OPTION.equals(searchMode)) {      
      uiSearchBox.setRendered(true);
    }
  }

  private PortletPreferences getPortletPreference() {
    PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    return portletRequestContext.getRequest().getPreferences();
  }

  private String getRepository() {
    return getPortletPreference().getValue(UIAdvanceSearchPortlet.REPOSITORY, null);    
  }

  private String getTemplatePath(String templateType) {
    return getPortletPreference().getValue(templateType, null);
  }

  private ResourceResolver getTemplateResourceResolver() throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String repository = getRepository();
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);
    String workspace = manageableRepository.getConfiguration().getSystemWorkspaceName();
    return new JCRResourceResolver(repository, workspace, "exo:templateFile");
  }

}
