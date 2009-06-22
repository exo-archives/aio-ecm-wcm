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
package org.exoplatform.wcm.webui.category;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 20, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UICategoryNavigationTreeSelector.ChangeCategoryTreeActionListener.class)
    }
)
public class UICategoryNavigationTreeSelector extends UIForm {

  public UICategoryNavigationTreeSelector() throws Exception {
    String repositoryName = "repository";
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    List<Node> listNode = taxonomyService.getAllTaxonomyTrees(repositoryName);
    List<SelectItemOption<String>> categories = new ArrayList<SelectItemOption<String>>();
    for(Node itemNode : listNode) {
      String value = itemNode.getSession().getWorkspace().getName() + ":" + itemNode.getPath(); 
      categories.add(new SelectItemOption<String>(itemNode.getName(), value));
    }
    UIFormSelectBox categoryNavigationTreeSelectBox = new UIFormSelectBox(UICategoryNavigationConstant.TREE_SELECTBOX, UICategoryNavigationConstant.TREE_SELECTBOX, categories);
    categoryNavigationTreeSelectBox.setOnChange("ChangeCategoryTree");
    addChild(categoryNavigationTreeSelectBox);
  }
  
  public static class ChangeCategoryTreeActionListener extends EventListener<UICategoryNavigationTreeSelector> {
    public void execute(Event<UICategoryNavigationTreeSelector> event) throws Exception {
      UICategoryNavigationTreeSelector categoryNavigationTreeSelector = event.getSource();
      UICategoryNavigationContainer categoryNavigationContainer = categoryNavigationTreeSelector.getAncestorOfType(UICategoryNavigationContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(categoryNavigationContainer);
    }
  }
  
}
