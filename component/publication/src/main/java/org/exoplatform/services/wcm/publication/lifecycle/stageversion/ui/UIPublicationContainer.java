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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.jcr.Node;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong_phan@exoplatform.com
 * Mar 4, 2009  
 */

@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "system:/groovy/webui/core/UITabPane.gtmpl"              
)
               
public class UIPublicationContainer extends UIForm implements UIPopupComponent {  
  private static String selectedTabId = "";  
  public UIPublicationContainer() { }
  
  private DateFormat dateTimeFormater;
  public void initContainer(Node node) throws Exception {
    UIPublicationPanel publicationPanel = addChild(UIPublicationPanel.class, null, null);
    publicationPanel.init(node);
    UIPublicationPagesContainer publicationPagesContainer = addChild(UIPublicationPagesContainer.class, null, null);
    publicationPagesContainer.init(node);
    publicationPagesContainer.setRendered(false);
    UIPublicationHistory publicationHistory = addChild(UIPublicationHistory.class, null, null);
    publicationHistory.init(node);
    publicationHistory.updateGrid();
    publicationHistory.setRendered(false);
    setSelectedTab(1);
    Locale locale = org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getLocale();
    dateTimeFormater = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM,SimpleDateFormat.MEDIUM,locale);
  }
  
  public DateFormat getDateTimeFormater() { return dateTimeFormater; }
  
  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }
  
  public String getSelectedTabId() { return selectedTabId; }
  public void setSelectedTab(String renderTabId) { selectedTabId = renderTabId; }
  public void setSelectedTab(int index) { selectedTabId = ((UIComponent)getChild(index-1)).getId();}
  
  public void setActiveTab(UIComponent component, WebuiRequestContext context) {
    for (UIComponent child : getChildren()) {
      child.setRendered(false);
    }
    component.setRendered(true);
    setSelectedTab(component.getId());
    context.addUIComponentToUpdateByAjax(this);
  }
}
