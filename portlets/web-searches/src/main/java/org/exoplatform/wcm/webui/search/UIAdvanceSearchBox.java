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

import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 31, 2008
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, events = { @EventConfig(listeners = UIAdvanceSearchBox.SearchActionListener.class) })
public class UIAdvanceSearchBox extends UIForm {
  
  private String             templatePath;

  private ResourceResolver   resourceResolver;

  public static final String KEYWORD_INPUT_SET = "keywordInputSet";

  public static final String KEYWORD_INPUT     = "keywordInput";

  public UIAdvanceSearchBox() throws Exception {
    UIFormInputSetWithAction uiKeywordInputSet = new UIFormInputSetWithAction(KEYWORD_INPUT_SET);
    UIFormStringInput uiKeywordInput = new UIFormStringInput(KEYWORD_INPUT, KEYWORD_INPUT, null);
    uiKeywordInputSet.addChild(uiKeywordInput);
    uiKeywordInputSet.setActionInfo(KEYWORD_INPUT, new String[] { "Search" });

    addChild(uiKeywordInputSet);
  }

  public void init(String templatePath, ResourceResolver resourceResolver) {
    this.templatePath = templatePath;
    this.resourceResolver = resourceResolver;
  }

  public String getTemplate() {
    return templatePath;
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    return resourceResolver;
  }
  
  public static class SearchActionListener extends EventListener<UIAdvanceSearchBox> {
    public void execute(Event<UIAdvanceSearchBox> arg0) throws Exception {

    }
  }

}
