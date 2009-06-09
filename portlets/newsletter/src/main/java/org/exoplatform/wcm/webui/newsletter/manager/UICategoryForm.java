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
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
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
	private String INPUT_CATEGORY_NAME = "CategoryName"; 
	private String INPUT_CATEGORY_TITLE = "CategoryTitle"; 
	private String INPUT_CATEGORY_DESCRIPTION = "CategoryDescription"; 
	private String INPUT_CATEGORY_MODERATOR = "CategoryModerator"; 
	private NewsletterCategoryConfig categoryConfig;
	public UICategoryForm() throws Exception{
		UIFormStringInput inputCateName = new UIFormStringInput(INPUT_CATEGORY_NAME, null);
		UIFormStringInput inputCateTitle = new UIFormStringInput(INPUT_CATEGORY_TITLE, null);
		UIFormTextAreaInput inputCateDescription = new UIFormTextAreaInput(INPUT_CATEGORY_DESCRIPTION, null, null);
		UIFormStringInput inputModerator = new UIFormStringInput(INPUT_CATEGORY_MODERATOR, null);
		addChild(inputCateName);
		addChild(inputCateTitle);
		addChild(inputCateDescription);
		addChild(inputModerator);
		
		setActions(new String[]{"Save", "Cancle"});
	}

	public void activate() throws Exception {	}

	public void deActivate() throws Exception {	}
	
	public void setCategoryInfo(NewsletterCategoryConfig categoryConfig){
	  this.categoryConfig = categoryConfig;
	  UIFormStringInput inputCateName = this.getChildById(INPUT_CATEGORY_NAME);
	  inputCateName.setValue(categoryConfig.getName());
	  inputCateName.setEditable(false);
    UIFormStringInput inputCateTitle = this.getChildById(INPUT_CATEGORY_TITLE);
    inputCateTitle.setValue(categoryConfig.getTitle());
    UIFormTextAreaInput inputCateDescription = this.getChildById(INPUT_CATEGORY_DESCRIPTION);
    inputCateDescription.setValue(categoryConfig.getDescription());
    UIFormStringInput inputModerator = this.getChildById(INPUT_CATEGORY_MODERATOR);
    inputModerator.setValue(categoryConfig.getModerator());
	}
	
	static  public class SaveActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategoryForm = event.getSource();
			UINewsletterManagerPortlet newsletterPortlet = uiCategoryForm.getAncestorOfType(UINewsletterManagerPortlet.class);
			NewsletterManagerService newsletterManagerService = 
				(NewsletterManagerService)PortalContainer.getInstance().getComponentInstanceOfType(NewsletterManagerService.class) ;
			NewsletterCategoryConfig categoryConfig = null;
			if(uiCategoryForm.categoryConfig == null) {
			  categoryConfig = new NewsletterCategoryConfig();
			  categoryConfig.setName(((UIFormStringInput)uiCategoryForm.getChildById(uiCategoryForm.INPUT_CATEGORY_NAME)).getValue());
			}else{
			  categoryConfig = uiCategoryForm.categoryConfig;
			}
			categoryConfig.setTitle(((UIFormStringInput)uiCategoryForm.getChildById(uiCategoryForm.INPUT_CATEGORY_TITLE)).getValue());
			categoryConfig.setDescription(((UIFormTextAreaInput)uiCategoryForm.getChildById(uiCategoryForm.INPUT_CATEGORY_DESCRIPTION)).getValue());
			categoryConfig.setModerator(((UIFormStringInput)uiCategoryForm.getChildById(uiCategoryForm.INPUT_CATEGORY_MODERATOR)).getValue());
			
			UIApplication uiApp = uiCategoryForm.getAncestorOfType(UIApplication.class);
			if(categoryConfig.getName() == null || categoryConfig.getName().trim().length() < 1){
	      uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.categoryNameIsNotEmpty", null, ApplicationMessage.WARNING));
	      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
	      return;
			} else if(categoryConfig.getTitle() == null || categoryConfig.getTitle().trim().length() < 1) {
				uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.categoryTitleIsNotEmpty", null, ApplicationMessage.WARNING));
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
				return;
			}
			NewsletterCategoryHandler categoryHandler = newsletterManagerService.getCategoryHandler();
			SessionProvider sessionProvider = NewsLetterUtil.getSesssionProvider();
			String portalName = NewsLetterUtil.getPortalName(); 
			try{
			  if(uiCategoryForm.categoryConfig == null){ // if add new category then check cateogry's name is already exist or not 
  			  if(categoryHandler.getCategoryByName(portalName, categoryConfig.getName(), sessionProvider) != null){
  			    uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.categoryNameIsAlreadyExist", null, ApplicationMessage.WARNING));
  			    event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
  			    sessionProvider.close();
  			    return;
  			  }
			  }else{ // Edit a category is already exist
			    categoryHandler.edit(portalName, categoryConfig, sessionProvider);
			  }
			}catch(Exception ex){
		    categoryHandler.add(portalName, categoryConfig, sessionProvider);
			}
			UIPopupContainer popupContainer = uiCategoryForm.getAncestorOfType(UIPopupContainer.class);
			popupContainer.deActivate();
			event.getRequestContext().addUIComponentToUpdateByAjax(newsletterPortlet) ;
			sessionProvider.close();
		}
	}
	
	static  public class CancleActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategoryForm = event.getSource();
			UIPopupContainer popupContainer = uiCategoryForm.getAncestorOfType(UIPopupContainer.class);
			popupContainer.deActivate();
		}
	}
}
