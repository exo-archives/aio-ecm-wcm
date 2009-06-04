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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template = "app:/groovy/webui/newsletter/NewsletterManager/UICategoryForm.gtmpl",
		events = {
			@EventConfig(listeners = UICategoryForm.SaveActionListener.class),
			@EventConfig(listeners = UICategoryForm.CancleActionListener.class)
		}
)
public class UICategoryForm extends UIForm implements UIPopupComponent{
	private String INPUT_CATEGORY_NAME = "Newsletter_CategoryName"; 
	private String INPUT_CATEGORY_DESCRIPTION = "Newsletter_CategoryDescription"; 
	private String INPUT_CATEGORY_MODERATOR = "Newsletter_CategoryModerator"; 
	public UICategoryForm() throws Exception{
		System.out.println("\n\n\n\n--------------------->view uiCategoryForm");
		UIFormStringInput inputCateName = new UIFormStringInput(INPUT_CATEGORY_NAME, null);
		UIFormTextAreaInput inputCateDescription = new UIFormTextAreaInput(INPUT_CATEGORY_DESCRIPTION, null, null);
		UIFormStringInput inputModerator = new UIFormStringInput(INPUT_CATEGORY_MODERATOR, null);
		addChild(inputCateName);
		addChild(inputCateDescription);
		addChild(inputModerator);
		
		setActions(new String[]{"Save", "Cancle"});
	}

	public void activate() throws Exception {	}

	public void deActivate() throws Exception {	}
	
	static  public class SaveActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategoryForm = event.getSource();
			UINewsletterManagerPortlet newsletterPortlet = uiCategoryForm.getAncestorOfType(UINewsletterManagerPortlet.class);
			NewsletterManagerService newsletterManagerService = 
				(NewsletterManagerService)PortalContainer.getInstance().getComponentInstanceOfType(NewsletterManagerService.class) ;
			NewsletterCategoryConfig categoryConfig = new NewsletterCategoryConfig();
			categoryConfig.setName(((UIFormStringInput)uiCategoryForm.getChildById(uiCategoryForm.INPUT_CATEGORY_NAME)).getValue());
			categoryConfig.setDescription(((UIFormTextAreaInput)uiCategoryForm.getChildById(uiCategoryForm.INPUT_CATEGORY_DESCRIPTION)).getValue());
			categoryConfig.setModerator(((UIFormStringInput)uiCategoryForm.getChildById(uiCategoryForm.INPUT_CATEGORY_MODERATOR)).getValue());
			if(categoryConfig.getName() != null){
				NewsletterCategoryHandler categoryHandler = newsletterManagerService.getCategoryHandler();
				categoryHandler.add(NewsLetterUtil.getPortalName(), categoryConfig, NewsLetterUtil.getSystemProvider());
			}else{
				
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(newsletterPortlet) ;
		}
	}
	
	static  public class CancleActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			System.out.println("~~~~~~~~~~~~~~~~~~~>run CancleActionListener");
			/*UICategoryForm uiCategories = event.getSource();
			UIPopupAction popupAction = uiCategories.getAncestorOfType(UINewsletterManagementPortlet.class)
																							.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UICategoryForm categoryForm = popupContainer.addChild(UICategoryForm.class, null, null) ;
			popupContainer.setId("NewsletterCategoryForm") ;
			popupAction.activate(popupContainer, 500, 300) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;*/
		}
	}
}
