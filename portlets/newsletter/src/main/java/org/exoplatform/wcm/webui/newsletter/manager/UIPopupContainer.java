/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.wcm.webui.newsletter.manager;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

@ComponentConfig(lifecycle = UIContainerLifecycle.class)

public class UIPopupContainer extends UIContainer implements UIPopupComponent {
	public UIPopupContainer() throws Exception {
		UIPopupAction uiPopupAction =	addChild(UIPopupAction.class, null, "UINewsletterChildPopupAction").setRendered(true) ;
		uiPopupAction.getChild(UIPopupWindow.class).setId("UINewsletterChildPopupWindow") ;
	}
	public void activate() throws Exception {
		// TODO Auto-generated method stub
	}

	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
	}
}