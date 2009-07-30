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
import org.exoplatform.ecm.webui.form.UIFormInputSetWithAction;
import org.exoplatform.ecm.webui.selector.UIGroupMemberSelector;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.newsletter.NewsletterCategoryConfig;
import org.exoplatform.services.wcm.newsletter.NewsletterManagerService;
import org.exoplatform.services.wcm.newsletter.handler.NewsletterCategoryHandler;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.newsletter.UINewsletterConstant;
import org.exoplatform.wcm.webui.selector.UIUserMemberSelector;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NameValidator;

@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template = "system:/groovy/webui/form/UIForm.gtmpl",
		events = {
			@EventConfig(listeners = UICategoryForm.SaveActionListener.class),
			@EventConfig(listeners = UICategoryForm.CancelActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UICategoryForm.SelectUserActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UICategoryForm.SelectMemberActionListener.class, phase = Phase.DECODE)
		}
)
public class UICategoryForm extends UIForm implements UIPopupComponent, UISelectable {
  private NewsletterCategoryConfig categoryConfig;
  private String popupId;
  public static final String INPUT_CATEGORY_NAME           = "CategoryName"; 
	public static final String INPUT_CATEGORY_TITLE          = "CategoryTitle"; 
	public static final String INPUT_CATEGORY_DESCRIPTION    = "CategoryDescription";
	public static final String FORM_CATEGORY_MODERATOR       = "FormCategoryModerator";
	public static final String INPUT_CATEGORY_MODERATOR      = "CategoryModerator"; 
	
	public UICategoryForm() throws Exception{
		UIFormStringInput inputCateName = new UIFormStringInput(INPUT_CATEGORY_NAME, null, null);
		UIFormStringInput inputCateTitle = new UIFormStringInput(INPUT_CATEGORY_TITLE, null, null);
	  this.getChildren().clear();
		UIFormTextAreaInput inputCateDescription = new UIFormTextAreaInput(INPUT_CATEGORY_DESCRIPTION, null, null);
		UIFormStringInput inputModerator = new UIFormStringInput(INPUT_CATEGORY_MODERATOR, INPUT_CATEGORY_MODERATOR, null);
		inputModerator.setEditable(false);
		UIFormInputSetWithAction formCategoryModerator = new UIFormInputSetWithAction(FORM_CATEGORY_MODERATOR);
		formCategoryModerator.addChild(inputModerator);
		formCategoryModerator.setActionInfo(INPUT_CATEGORY_MODERATOR, new String[] {"SelectUser", "SelectMember"});
		formCategoryModerator.showActionInfo(true);
		
		inputCateName.addValidator(MandatoryValidator.class).addValidator(NameValidator.class);
		inputCateTitle.addValidator(MandatoryValidator.class);
		inputModerator.addValidator(MandatoryValidator.class);
		
		addChild(inputCateName);
		addChild(inputCateTitle);
		addChild(inputCateDescription);
		addChild(formCategoryModerator);
		setActions(new String[]{"Save", "Cancel"});
	}

  public void doSelect(String selectField, Object value) throws Exception {
    getUIStringInput(selectField).setValue((String) value);
    Utils.closePopupWindow(this, popupId);
  }

  public String getPopupId() {
    return popupId;
  }

  public void setPopupId(String popupId) {
    this.popupId = popupId;
  }
  
  public void activate() throws Exception {}

  public void deActivate() throws Exception {}
  
	public void setCategoryInfo(NewsletterCategoryConfig categoryConfig){
	  this.categoryConfig = categoryConfig;
	  UIFormStringInput inputCateName = this.getChildById(INPUT_CATEGORY_NAME);
	  inputCateName.setValue(categoryConfig.getName());
	  inputCateName.setEditable(false);
    UIFormStringInput inputCateTitle = this.getChildById(INPUT_CATEGORY_TITLE);
    inputCateTitle.setValue(categoryConfig.getTitle());
    UIFormTextAreaInput inputCateDescription = this.getChildById(INPUT_CATEGORY_DESCRIPTION);
    inputCateDescription.setValue(categoryConfig.getDescription());
    UIFormInputSetWithAction inputSetWithAction = getChildById(FORM_CATEGORY_MODERATOR);
    UIFormStringInput inputModerator = inputSetWithAction.getChildById(INPUT_CATEGORY_MODERATOR);
    inputModerator.setValue(categoryConfig.getModerator());
	}
	
	static  public class SaveActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategoryForm = event.getSource();
			NewsletterManagerService newsletterManagerService = 
				(NewsletterManagerService)PortalContainer.getInstance().getComponentInstanceOfType(NewsletterManagerService.class) ;
			NewsletterCategoryConfig categoryConfig = null;
			if(uiCategoryForm.categoryConfig == null) {
			  categoryConfig = new NewsletterCategoryConfig();
			  categoryConfig.setName(((UIFormStringInput)uiCategoryForm.getChildById(INPUT_CATEGORY_NAME)).getValue());
			}else{
			  categoryConfig = uiCategoryForm.categoryConfig;
			}
			categoryConfig.setTitle(((UIFormStringInput)uiCategoryForm.getChildById(INPUT_CATEGORY_TITLE)).getValue());
			categoryConfig.setDescription(((UIFormTextAreaInput)uiCategoryForm.getChildById(INPUT_CATEGORY_DESCRIPTION)).getValue());
			
			UIFormInputSetWithAction inputSetWithAction = uiCategoryForm.getChildById(FORM_CATEGORY_MODERATOR);
      UIFormStringInput inputModerator = inputSetWithAction.getChildById(INPUT_CATEGORY_MODERATOR);
      
      String inputMderatorValue = inputModerator.getValue();
      if (("".equals(inputMderatorValue)) || (inputMderatorValue == null)) {
        UIApplication uiApp = uiCategoryForm.getAncestorOfType(UIApplication.class);
        uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.inputModeratorEmpty", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

			categoryConfig.setModerator(((UIFormStringInput)inputSetWithAction.getChildById(INPUT_CATEGORY_MODERATOR)).getValue());
			
			UIApplication uiApp = uiCategoryForm.getAncestorOfType(UIApplication.class);
			NewsletterCategoryHandler categoryHandler = newsletterManagerService.getCategoryHandler();

			String portalName = NewsLetterUtil.getPortalName(); 
			try{
				SessionProvider sessionProvider = Utils.getSessionProvider(uiCategoryForm);
			  if(uiCategoryForm.categoryConfig == null){ // if add new category then check cateogry's name is already exist or not
  			  if(categoryHandler.getCategoryByName(portalName, categoryConfig.getName(), sessionProvider) != null){
  			    uiApp.addMessage(new ApplicationMessage("UICategoryForm.msg.categoryNameIsAlreadyExist", null, ApplicationMessage.WARNING));
  			    event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
  			    return;
  			  }else{
  			    categoryHandler.add(portalName, categoryConfig, sessionProvider);
  			  }
			  }else{ // Edit a category is already exist
			    categoryHandler.edit(portalName, categoryConfig, sessionProvider);
			  }
			}catch(Exception ex){
			  ex.printStackTrace();
			}
			Utils.closePopupWindow(uiCategoryForm, UINewsletterConstant.CATEGORY_FORM_POPUP_WINDOW);
		}
	}
	
	static  public class CancelActionListener extends EventListener<UICategoryForm> {
		public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategoryForm = event.getSource();
			Utils.closePopupWindow(uiCategoryForm, UINewsletterConstant.CATEGORY_FORM_POPUP_WINDOW);
		}
	}

	public static class SelectUserActionListener extends EventListener<UICategoryForm> {
	  public void execute(Event<UICategoryForm> event) throws Exception {
	    UICategoryForm categoryForm = event.getSource();
      UIUserMemberSelector userMemberSelector = categoryForm.createUIComponent(UIUserMemberSelector.class, null, null);
      userMemberSelector.setMulti(false);
      userMemberSelector.setShowSearch(true);
      userMemberSelector.setSourceComponent(categoryForm, new String[] {INPUT_CATEGORY_MODERATOR});
      userMemberSelector.init();
      Utils.createPopupWindow(categoryForm, userMemberSelector, UINewsletterConstant.USER_SELECTOR_POPUP_WINDOW, 700, 315);
      categoryForm.setPopupId(UINewsletterConstant.USER_SELECTOR_POPUP_WINDOW);
	  }
	}

	public static class SelectMemberActionListener extends EventListener<UICategoryForm> {
	  public void execute(Event<UICategoryForm> event) throws Exception {
	    UICategoryForm categoryForm = event.getSource();
      UIGroupMemberSelector groupMemberSelector = categoryForm.createUIComponent(UIGroupMemberSelector.class, null, null);
      groupMemberSelector.setShowAnyPermission(false);
      groupMemberSelector.setSourceComponent(categoryForm, new String[] {INPUT_CATEGORY_MODERATOR});
      Utils.createPopupWindow(categoryForm, groupMemberSelector, UINewsletterConstant.GROUP_SELECTOR_POPUP_WINDOW, 540, 300);
	    categoryForm.setPopupId(UINewsletterConstant.GROUP_SELECTOR_POPUP_WINDOW);
	  }
	}

}