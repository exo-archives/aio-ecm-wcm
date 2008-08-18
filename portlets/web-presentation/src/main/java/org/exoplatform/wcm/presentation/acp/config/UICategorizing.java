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

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectmany.UICategoriesSelector;
import org.exoplatform.services.cms.categories.CategoriesService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 28, 2008  
 */

@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)

public class UICategorizing extends UIContainer implements UISelectable {

  final static String PATH_CATEGORY = "path".intern(); 

  private Node webContentNode = null;
  private List<String> existedCategories = new ArrayList<String>();


  public UICategorizing() throws Exception {
    UICategoriesSelector uiCategoriesSelector = addChild(UICategoriesSelector.class, null, null);
  }

  public void initUICategoriesSelector() throws Exception {
    UICategoriesSelector uiCategoriesSelector = getChild(UICategoriesSelector.class);
    uiCategoriesSelector.setExistedCategoryList(getExistedCategories());
    uiCategoriesSelector.setSourceComponent(this,null);
    uiCategoriesSelector.init();
  }

  @SuppressWarnings("unchecked")
  public void doSelect(String name, Object value) throws Exception {
    CategoriesService categoriesService = getApplicationComponent(CategoriesService.class);
    Node webContentNode = getWebContentNode();
    Session session = webContentNode.getSession();
    String repositoryName = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
    List<String> newCategoryPaths = (List<String>) value;
    List<String> oldCategoryPaths = getExistedCategories();
    List<String> tempCategoryPaths = new ArrayList<String>(oldCategoryPaths);
    tempCategoryPaths.removeAll(newCategoryPaths);

    for(String categoryPath: tempCategoryPaths) {
      categoriesService.removeCategory(webContentNode, categoryPath, repositoryName);
    }
    newCategoryPaths.removeAll(oldCategoryPaths);
    for (String categoryPath: newCategoryPaths) {
      categoriesService.addCategory(webContentNode, categoryPath, repositoryName);
    }
  }

  public List<String> getExistedCategories() {
    return this.existedCategories;
  }

  public void setExistedCategories(List<String> existedCategories) {
    this.existedCategories = existedCategories;
  }

  public Node getWebContentNode() {
    return this.webContentNode;
  }

  public void setWebContentNode(Node webContentNode) {
    this.webContentNode = webContentNode;
  }
}
