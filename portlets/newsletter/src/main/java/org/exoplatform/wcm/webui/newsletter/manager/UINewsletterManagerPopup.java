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
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ngoc.tran@exoplatform.com
 * Jun 11, 2009  
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class ,
                 template = "app:/groovy/webui/newsletter/NewsletterManager/UINewsletterMangerPopup.gtmpl"
 )
public class UINewsletterManagerPopup extends UIForm implements UIPopupComponent {

  public UINewsletterManagerPopup () {
    
    UIFormWYSIWYGInput formWYSIWYGInput = new UIFormWYSIWYGInput("TestFCKEditor", null, null, true);
    this.addChild(formWYSIWYGInput);
  }
  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

}
