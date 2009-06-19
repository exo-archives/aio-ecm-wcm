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
package org.exoplatform.wcm.webui.newsletter.manager;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 10, 2009  
 */
@ComponentConfig (
    lifecycle = UIContainerLifecycle.class
)
public class UINewsletterEntryContainer extends UIContainer {

  private boolean isAddNew = true;
  
  public boolean isAddNew() {
    return isAddNew;
  }
  
  public void setAddNew(boolean isAddNew) {
    this.isAddNew = isAddNew;
  }
  
  public UINewsletterEntryContainer() throws Exception {
    addChild(UINewsletterEntryDialogSelector.class, null, null);
    UINewsletterEntryForm newsletterEntryForm = createUIComponent(UINewsletterEntryForm.class, null, null);
    newsletterEntryForm.setStoredLocation("repository", "collaboration", "/sites content/live/classic");
    newsletterEntryForm.addNew(isAddNew);
    addChild(newsletterEntryForm);
  }

  
}
