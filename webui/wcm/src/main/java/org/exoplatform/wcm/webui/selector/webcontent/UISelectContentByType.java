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
package org.exoplatform.wcm.webui.selector.webcontent;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.wcm.webui.selector.webContentView.UIWebContentTreeBuilder;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          maivanha1610@gmail.com
 * Aug 26, 2009  
 */
@ComponentConfig(
     template = "classpath:groovy/wcm/webui/UISelectContentByType.gtmpl",
     events = {
         @EventConfig(listeners = UISelectContentByType.ChangeContentTypeActionListener.class)
     }
 )
public class UISelectContentByType  extends UIContainer{
  public static final String WEBCONENT = "WebContent";
  public static final String DMSDOCUMENT = "DMSDocument";
  public static final String MEDIA = "Media";
  public final String SELECT_TYPE_CONTENT = "selectTypeContent";
  public String[] types = new String[]{WEBCONENT, DMSDOCUMENT, MEDIA};
  private String selectedValues = WEBCONENT;
  
  public UISelectContentByType() throws Exception{
    List<SelectItemOption<String>> listTypes = new ArrayList<SelectItemOption<String>>();
    SelectItemOption<String> option = null;
    for (String type : types) {
      option = new SelectItemOption<String>(type, type);
      listTypes.add(option);
    }
    UIFormSelectBox formSelectBox = new UIFormSelectBox(SELECT_TYPE_CONTENT, SELECT_TYPE_CONTENT, listTypes);
    formSelectBox.setOnChange("ChangeContentType");
    addChild(formSelectBox);
  }

  public static class ChangeContentTypeActionListener extends EventListener<UISelectContentByType> {
    public void execute(Event<UISelectContentByType> event) throws Exception {
      UISelectContentByType contentByType = event.getSource();
      String type = event.getRequestContext().getRequestParameter(OBJECTID);
      if(type.equals(contentByType.selectedValues)) return;
      contentByType.selectedValues = type;
      UIWebContentPathSelector contentPathSelector = contentByType.getParent();
      /*UIWebContentPathSelector uiWCPathSelector = contentPathSelector.getChild(UIBreadcumbs.class)
                                                                      .getAncestorOfType(UIWebContentPathSelector.class);*/
      //UIWebContentTreeBuilder uiWCTreeBuilder = uiWCPathSelector.getChild(UIWebContentTreeBuilder.class);
      //uiWCTreeBuilder.changeNode("", event.getRequestContext());
      contentPathSelector.reRenderChild(contentByType.selectedValues);
      contentPathSelector.init();
      event.getRequestContext().addUIComponentToUpdateByAjax(contentPathSelector);
    }
  }
}
