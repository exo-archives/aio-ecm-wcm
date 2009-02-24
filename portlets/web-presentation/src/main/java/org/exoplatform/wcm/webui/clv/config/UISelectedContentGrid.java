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
package org.exoplatform.wcm.webui.clv.config;

import java.util.List;

import org.exoplatform.ecm.webui.tree.selectmany.UISelectedCategoriesGrid;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : anh.do anh.do@exoplatform.com,
 * anhdn86@gmail.com Feb 16, 2009
 */

@ComponentConfig(
  template = "app:/groovy/ContentListViewer/config/UISelectedContentGrid.gtmpl", 
  events = {
    @EventConfig(listeners = UISelectedContentGrid.DeleteActionListener.class, confirm = "UISelectedContentGrid.msg.confirm-delete"),
    @EventConfig(listeners = UISelectedContentGrid.SaveCategoriesActionListener.class),
    @EventConfig(listeners = UISelectedContentGrid.CancelActionListener.class) 
  }
)
public class UISelectedContentGrid extends UISelectedCategoriesGrid {

  public UISelectedContentGrid() throws Exception {
    super();
  }

  public static class DeleteActionListener extends EventListener<UISelectedContentGrid> {
    public void execute(Event<UISelectedContentGrid> event) throws Exception {
      UISelectedContentGrid uiSelectedContentGrid = event.getSource();
      String value = event.getRequestContext().getRequestParameter(OBJECTID);
      uiSelectedContentGrid.removeCategory(value);
      if (uiSelectedContentGrid.getSelectedCategories().size() == 0) uiSelectedContentGrid.setDeleteAllCategory(true);
      uiSelectedContentGrid.updateGrid();
      if (uiSelectedContentGrid.getSelectedCategories().size() == 0) {
        uiSelectedContentGrid.setRendered(false);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSelectedContentGrid.getParent());      
    }
  }

  public static class SaveCategoriesActionListener extends EventListener<UISelectedContentGrid> {
    public void execute(Event<UISelectedContentGrid> event) throws Exception {
      UISelectedContentGrid uiSelectedContentGrid = event.getSource();
      UICorrectContentSelectorForm uiCorrectContentSelectorForm = uiSelectedContentGrid.getAncestorOfType(UICorrectContentSelectorForm.class);
      String returnField = uiCorrectContentSelectorForm.getReturnFieldName();
      List<String> selectedCategories = uiSelectedContentGrid.getSelectedCategories();
      UIApplication uiApplication = uiSelectedContentGrid.getAncestorOfType(UIApplication.class);
      if (selectedCategories.size() == 0 && !uiSelectedContentGrid.isDeleteAllCategory()) {
        uiApplication.addMessage(new ApplicationMessage("UISelectedContentGrid.msg.non-content", null, ApplicationMessage.INFO));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
        return;
      }
      try {
        StringBuilder contents = new StringBuilder();
        for (String item : selectedCategories) {
          contents.append(item).append(";");
        }
        UIViewerManagementForm uiViewerManagementForm = (UIViewerManagementForm) uiCorrectContentSelectorForm.getSourceComponent();
        uiViewerManagementForm.doSelect(returnField, contents.toString());
        uiViewerManagementForm.setViewAbleContentList(selectedCategories);
      } catch (Exception e) {
        e.printStackTrace();
        uiApplication.addMessage(new ApplicationMessage("UISelectedCategoriesGrid.msg.cannot-save", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages());
      }
      uiCorrectContentSelectorForm.deActivate();
    }
  }

  public static class CancelActionListener extends EventListener<UISelectedContentGrid> {
    public void execute(Event<UISelectedContentGrid> event) throws Exception {
      UISelectedContentGrid uiSelectedContent = event.getSource();
      UIPopupContainer uiPopupContainer = (UIPopupContainer) uiSelectedContent.getAncestorOfType(UIPopupContainer.class);
      uiPopupContainer.deActivate();    
    }
  }
}
