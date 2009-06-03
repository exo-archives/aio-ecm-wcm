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
package org.exoplatform.wcm.webui.scv.config.publication;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Oct 10, 2008
 */
@ComponentConfig (
    lifecycle = UIContainerLifecycle.class
)

public class UIWCMPublicationGridContainer extends UIContainer{

  /**
   * Instantiates a new uIWCM publication grid container.
   * 
   * @throws Exception the exception
   */
  public UIWCMPublicationGridContainer() throws Exception {
    UIFormCheckBoxInput<String> checkBoxInput = new UIFormCheckBoxInput<String>("lifecycleName", null, null);
    checkBoxInput.setLabel("LifecycleName: ");
    addChild(checkBoxInput);
    addChild(UIWCMPublicationGrid.class, null, null);
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    getChild(UIWCMPublicationGrid.class).updateGrid();
  }
}
