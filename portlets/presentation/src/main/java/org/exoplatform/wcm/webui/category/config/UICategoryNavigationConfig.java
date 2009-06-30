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
package org.exoplatform.wcm.webui.category.config;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.services.cms.taxonomy.TaxonomyService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.category.UICategoryNavigationConstant;
import org.exoplatform.wcm.webui.category.UICategoryNavigationPortlet;
import org.exoplatform.wcm.webui.category.UICategoryNavigationUtils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.ext.UIFormInputSetWithAction;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 28, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
        @EventConfig(listeners = UICategoryNavigationConfig.SaveActionListener.class),
        @EventConfig(listeners = UICategoryNavigationConfig.ChangeRepositoryActionListener.class),
        @EventConfig(listeners = UICategoryNavigationConfig.SelectTargetPathActionListener.class)
    }
)
public class UICategoryNavigationConfig extends UIForm implements UISelectable {

  private String popupId;
  
  public UICategoryNavigationConfig() throws Exception {
    PortletPreferences preferences = UICategoryNavigationUtils.getPortletPreferences();

    String preferenceRepository = preferences.getValue(UICategoryNavigationConstant.PREFERENCE_REPOSITORY, "");
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class) ;  
    List<SelectItemOption<String>> repositories = new ArrayList<SelectItemOption<String>>() ;
    for(RepositoryEntry repositoryEntry : repositoryService.getConfig().getRepositoryConfigurations()) {
      repositories.add(new SelectItemOption<String>(repositoryEntry.getName())) ;
    }
    UIFormSelectBox repositoryFormSelectBox = new UIFormSelectBox(UICategoryNavigationConstant.REPOSITORY_FORM_SELECTBOX, UICategoryNavigationConstant.REPOSITORY_FORM_SELECTBOX, repositories);
    repositoryFormSelectBox.setValue(preferenceRepository);
    repositoryFormSelectBox.setOnChange("ChangeRepository");
    addUIFormInput(repositoryFormSelectBox);
    
    String preferenceTreeName = preferences.getValue(UICategoryNavigationConstant.PREFERENCE_TREE_NAME, "");
    List<SelectItemOption<String>> trees = getTaxonomyTrees(preferenceRepository);
    UIFormSelectBox treeNameFormSelectBox = new UIFormSelectBox(UICategoryNavigationConstant.TREE_NAME_FORM_SELECTBOX, UICategoryNavigationConstant.TREE_NAME_FORM_SELECTBOX, trees);
    treeNameFormSelectBox.setValue(preferenceTreeName);
    addUIFormInput(treeNameFormSelectBox);
    
    String preferenceTreeTitle = preferences.getValue(UICategoryNavigationConstant.PREFERENCE_TREE_TITLE, "");
    addUIFormInput(new UIFormStringInput(UICategoryNavigationConstant.TREE_TITLE_FORM_STRING_INPUT, UICategoryNavigationConstant.TREE_TITLE_FORM_STRING_INPUT, preferenceTreeTitle));
    
    String preferenceTargetPath = preferences.getValue(UICategoryNavigationConstant.PREFERENCE_TARGET_PATH, "");
    UIFormInputSetWithAction targetPathFormInputSet = new UIFormInputSetWithAction(UICategoryNavigationConstant.TARGET_PATH_FORM_INPUT_SET);
    UIFormStringInput targetPathFormStringInput = new UIFormStringInput(UICategoryNavigationConstant.TARGET_PATH_FORM_STRING_INPUT, UICategoryNavigationConstant.TARGET_PATH_FORM_STRING_INPUT, preferenceTargetPath);
    targetPathFormStringInput.setEditable(false);
    targetPathFormInputSet.setActionInfo(UICategoryNavigationConstant.TARGET_PATH_FORM_STRING_INPUT, new String[] {"SelectTargetPath"}) ;
    targetPathFormInputSet.addUIFormInput(targetPathFormStringInput);
    addChild(targetPathFormInputSet);
    
    setActions(new String[] {"Save"});
  }
  
  public String getPopupId() {
    return popupId;
  }

  public void setPopupId(String popupId) {
    this.popupId = popupId;
  }

  public List<SelectItemOption<String>> getTaxonomyTrees(String repository) throws Exception {
    TaxonomyService taxonomyService = getApplicationComponent(TaxonomyService.class);
    List<Node> taxonomyNodes = taxonomyService.getAllTaxonomyTrees(repository);
    List<SelectItemOption<String>> taxonomyTrees = new ArrayList<SelectItemOption<String>>();
    for(Node itemNode : taxonomyNodes) {
      taxonomyTrees.add(new SelectItemOption<String>(itemNode.getName(), itemNode.getName()));
    }
    return taxonomyTrees;
  }
  
  public void doSelect(String selectField, Object value) throws Exception {
    UIFormStringInput formStringInput = findComponentById(selectField);
    formStringInput.setValue(value.toString()) ;
    
    UICategoryNavigationPortlet categoryNavigationPortlet = getAncestorOfType(UICategoryNavigationPortlet.class);
    UIPopupContainer popupContainer = categoryNavigationPortlet.getChild(UIPopupContainer.class);
    Utils.closePopupWindow(popupContainer, popupId);
  }
  
  public static class SaveActionListener extends EventListener<UICategoryNavigationConfig> {
    public void execute(Event<UICategoryNavigationConfig> event) throws Exception {
      UICategoryNavigationConfig categoryNavigationConfig = event.getSource();
      String preferenceRepository = categoryNavigationConfig.getUIFormSelectBox(UICategoryNavigationConstant.REPOSITORY_FORM_SELECTBOX).getValue();
      String preferenceTreeName = categoryNavigationConfig.getUIFormSelectBox(UICategoryNavigationConstant.TREE_NAME_FORM_SELECTBOX).getValue();
      String preferenceTreeTitle = categoryNavigationConfig.getUIStringInput(UICategoryNavigationConstant.TREE_TITLE_FORM_STRING_INPUT).getValue();
      String preferenceTargetPath = categoryNavigationConfig.getUIStringInput(UICategoryNavigationConstant.TARGET_PATH_FORM_STRING_INPUT).getValue();
      PortletPreferences portletPreferences = UICategoryNavigationUtils.getPortletPreferences();
      portletPreferences.setValue(UICategoryNavigationConstant.PREFERENCE_REPOSITORY, preferenceRepository);
      portletPreferences.setValue(UICategoryNavigationConstant.PREFERENCE_TREE_NAME, preferenceTreeName);
      portletPreferences.setValue(UICategoryNavigationConstant.PREFERENCE_TREE_TITLE, preferenceTreeTitle);
      portletPreferences.setValue(UICategoryNavigationConstant.PREFERENCE_TARGET_PATH, preferenceTargetPath);
      portletPreferences.store();
      ((PortletRequestContext)WebuiRequestContext.getCurrentInstance()).setApplicationMode(PortletMode.VIEW);
    }
  }
  
  public static class ChangeRepositoryActionListener extends EventListener<UICategoryNavigationConfig> {
    public void execute(Event<UICategoryNavigationConfig> event) throws Exception {
      
    }
  }
  
  public static class SelectTargetPathActionListener extends EventListener<UICategoryNavigationConfig> {
    public void execute(Event<UICategoryNavigationConfig> event) throws Exception {
      UICategoryNavigationConfig categoryNavigationConfig = event.getSource();
      categoryNavigationConfig.setPopupId(UICategoryNavigationConstant.TARGET_PATH_SELECTOR_POPUP_WINDOW);
    }
  }

}
