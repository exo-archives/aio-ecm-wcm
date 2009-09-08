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
 * dzungdev@gmail.com
 * May 28, 2008
 */
@SuppressWarnings("deprecation")
@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)
public class UICategorizing extends UIContainer implements UISelectable {

  /** The Constant PATH_CATEGORY. */
  final static String PATH_CATEGORY = "path".intern(); 

  /** The web content node. */
  private Node webContentNode = null;

  /** The existed categories. */
  private List<String> existedCategories = new ArrayList<String>();

  /**
   * Instantiates a new uI categorizing.
   * 
   * @throws Exception the exception
   */
  public UICategorizing() throws Exception {
    addChild(UICategoriesSelector.class, null, null);
  }

  /**
   * Inits the ui categories selector.
   * 
   * @throws Exception the exception
   */
  public void initUICategoriesSelector() throws Exception {
    UICategoriesSelector uiCateSelector = getChild(UICategoriesSelector.class);
    uiCateSelector.setExistedCategoryList(getExistedCategories());
    uiCateSelector.setSourceComponent(this,null);
    uiCateSelector.init();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.selector.UISelectable#doSelect(java.lang.String, java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  public void doSelect(String name, Object value) throws Exception {
    CategoriesService categoriesService = getApplicationComponent(CategoriesService.class);
    Node webContentNode = getWebContentNode();
    Session session = webContentNode.getSession();
    String repositoryName = ((ManageableRepository)session.getRepository()).getConfiguration().getName();
    List<String> newCategoryPaths = (List<String>) value;
    List<String> oldCategoryPaths = getExistedCategories();
    for (String oldCategory: oldCategoryPaths) {
      categoriesService.removeCategory(webContentNode, oldCategory, repositoryName);
    }
    for (String newCategory: newCategoryPaths) {
      newCategory = "/jcr:system/exo:ecm/exo:taxonomies/" + newCategory;
      categoriesService.addCategory(webContentNode, newCategory, repositoryName);
    }
  }

  /**
   * Gets the existed categories.
   * 
   * @return the existed categories
   */
  public List<String> getExistedCategories() {
    return this.existedCategories;
  }

  /**
   * Sets the existed categories.
   * 
   * @param existedCategories the new existed categories
   */
  public void setExistedCategories(List<String> existedCategories) {
    this.existedCategories = existedCategories;
  }

  /**
   * Gets the web content node.
   * 
   * @return the web content node
   */
  public Node getWebContentNode() {
    return this.webContentNode;
  }

  /**
   * Sets the web content node.
   * 
   * @param webContentNode the new web content node
   */
  public void setWebContentNode(Node webContentNode) {
    this.webContentNode = webContentNode;
  }
}
