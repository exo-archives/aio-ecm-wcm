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
	private boolean isRenderUISubscription = false;
	private boolean isRenderUINewsLetters = false;
	
	public void setRenderUIcategories(){
	  isRenderUICategories = true;
	  isRenderUISubscription = false;
	  isRenderUINewsLetters = false;
	}
	
	public void setRenderUIsubscriptions(){
	  isRenderUICategories = false;
	  isRenderUISubscription = true;
	  isRenderUINewsLetters = false;
	}
	
	public void setRenderUInewsLetter(){
	  isRenderUICategories = false;
	  isRenderUISubscription = false;
	  isRenderUINewsLetters = true;
	}
	
	public UINewsletterManagerPortlet() throws Exception {
		addChild(UICategories.class, null, null).setRendered(isRenderUICategories);
		addChild(UISubscriptions.class, null, null).setRendered(isRenderUICategories);
		addChild(UIPopupContainer.class, null, null);
		addChild(UINewsletterEntryManager.class, null, null).setRendered(isRenderUINewsLetters);
	}
}
