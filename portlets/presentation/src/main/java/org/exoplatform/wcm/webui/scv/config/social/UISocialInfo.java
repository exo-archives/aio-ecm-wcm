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
package org.exoplatform.wcm.webui.scv.config.social;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.wcm.webui.scv.config.UIMiscellaneousInfo;
import org.exoplatform.wcm.webui.scv.config.access.UIPermissionInfo;
import org.exoplatform.wcm.webui.scv.config.access.UIPermissionManager;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * May 28, 2008
 */

@ComponentConfig(
    lifecycle=Lifecycle.class,
    template =  "app:/groovy/SingleContentViewer/config/UITabPane_New.gtmpl"
)

public class UISocialInfo extends UITabPane {
  
  /**
   * Instantiates a new uI social info.
   * 
   * @throws Exception the exception
   */
  public UISocialInfo() throws Exception {
    UIPermissionManager uiPermission = addChild(UIPermissionManager.class, null, null);
	  UIMiscellaneousInfo uiMiscellaneousInfo = addChild(UIMiscellaneousInfo.class, null, null);
    // Comment adding UITagging and UICategorizing but don't delete.
    // because WCM can use UITagging and UICategorizing later.
//    addChild(UITagging.class, null, null);
//    addChild(UICategorizing.class, null, null);
	  setSelectedTab(uiPermission.getId()) ;
  }
  
  public void update() throws Exception {
	  UIPermissionManager uiPermissionManager = getChild(UIPermissionManager.class);
	  uiPermissionManager.getChild(UIPermissionInfo.class).updateGrid();
  }

  /**
   * Inits the ui categorizing.
   * 
   * @param webContentNode the web content node
   * 
   * @throws Exception the exception
   */
//  public void initUICategorizing(Node webContentNode) throws Exception {
//    UICategorizing uiCategorizing = getChild(UICategorizing.class);
//    uiCategorizing.setWebContentNode(webContentNode);
//    uiCategorizing.setExistedCategories(getExistedCategory(webContentNode));
//    uiCategorizing.initUICategoriesSelector();
//  }

  /**
   * Gets the existed category.
   * 
   * @param webContentNode the web content node
   * 
   * @return the existed category
   * 
   * @throws Exception the exception
   */
  private List<String> getExistedCategory(Node webContentNode) throws Exception {
    List<String> existedCategory = new ArrayList<String>();
    Session session = webContentNode.getSession();
    String repositoryName = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
    CategoriesService categoriesService = getApplicationComponent(CategoriesService.class);
    if(categoriesService.hasCategories(webContentNode)) {
      List<Node> categoryNodeList = categoriesService.getCategories(webContentNode, repositoryName);
      for (Node category: categoryNodeList) {
        existedCategory.add(category.getPath());
      }
    }
    return existedCategory;
  }
}
