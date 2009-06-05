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
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ngoc.tran@exoplatform.com
 * May 22, 2009  
 */
@ComponentConfig (
		lifecycle = UIApplicationLifecycle.class
)
public class UINewsletterManagerPortlet extends UIPortletApplication {
	private boolean isRenderUICategories = true;
	private boolean isRenderUICategory = true;
	private boolean isRenderUISubscription = true;
	
	public UINewsletterManagerPortlet() throws Exception {
		this.addChild(UICategories.class, null, null).setRendered(isRenderUICategories);
		UIPopupContainer uiPopup = addChild(UIPopupContainer.class, null, "UINesletterPopup");
    uiPopup.getChild(UIPopupWindow.class).setId("UINesletterPopupWindow");
	}
}
