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
package org.exoplatform.wcm.presentation.acp.config;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.cms.folksonomy.FolksonomyService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.wcm.presentation.acp.config.quickcreation.UIQuickCreationWizard;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 28, 2008  
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIForm.gtmpl", 
  events = {
    @EventConfig(listeners = UITagging.AddTagsActionListener.class)
  }
)
  public class UITagging extends UIForm {
  final static public String TAG_NAMES       = "TagNames";

  final static public String LINKED_TAGS     = "LinkedTags";

  public UITagging() throws Exception {
    UIFormStringInput tagsName = new UIFormStringInput(TAG_NAMES, TAG_NAMES, null);
    UIFormInputInfo linkedTags = new UIFormInputInfo(LINKED_TAGS, LINKED_TAGS, null);
    linkedTags.setEnable(false);
    addChild(tagsName);
    addChild(linkedTags);
  }

  public static class AddTagsActionListener extends EventListener<UITagging> {
    public void execute(Event<UITagging> event) throws Exception {
      UITagging taggingForm = event.getSource();
      UIQuickCreationWizard quickCreationWizard = taggingForm
      .getAncestorOfType(UIQuickCreationWizard.class);
      UIContentDialogForm contentDialogForm = quickCreationWizard
      .getChild(UIContentDialogForm.class);
      String webContentUUID = contentDialogForm.savedNodeIdentifier.getUUID();
      RepositoryService repositoryService = taggingForm
      .getApplicationComponent(RepositoryService.class);
      ManageableRepository manageableRepository = repositoryService.getCurrentRepository();

      String workspace = manageableRepository.getConfiguration().getDefaultWorkspaceName();
      String repository = manageableRepository.getConfiguration().getName();
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      Session session = sessionProvider.getSession(workspace, manageableRepository);
      Node webContent = session.getNodeByUUID(webContentUUID);

      FolksonomyService folksonomyService = taggingForm
      .getApplicationComponent(FolksonomyService.class);
      UIApplication uiApp = taggingForm.getAncestorOfType(UIApplication.class);
      String tagName = taggingForm.getUIStringInput(TAG_NAMES).getValue();
      String[] tagNames = null;
      if (tagName == null || tagName.trim().length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UITagging.msg.tag-name-empty", null,
            ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

      if (tagName.indexOf(",") > -1)
        tagNames = tagName.split(",");
      else
        tagNames = new String[] { tagName };

      for (String t : tagNames) {
        if (t.trim().length() == 0) {
          uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.tag-name-empty", null,
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        if (t.trim().length() > 20) {
          uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.tagName-too-long", null,
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
        String[] arrFilterChar = { "&", "'", "$", "@", ":", "]", "[", "*", "%", "!", "/", "\\" };
        for (String filterChar : arrFilterChar) {
          if (t.indexOf(filterChar) > -1) {
            uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.tagName-invalid", null,
                ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
      }
      for (Node tag : folksonomyService.getLinkedTagsOfDocument(webContent, repository)) {
        for (String t : tagNames) {
          if (t.equals(tag.getName())) {
            Object[] args = { t };
            uiApp.addMessage(new ApplicationMessage("UITaggingForm.msg.name-exist", args,
                ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
        }
      }
      folksonomyService.addTag(webContent, tagNames, repository);
      taggingForm.addTags(webContent, repository, folksonomyService);
      taggingForm.getUIStringInput(TAG_NAMES).setValue(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(taggingForm);
    }
  }
  
  private void addTags(Node node, String repository, FolksonomyService folksonomyService)
  throws Exception {
    StringBuilder linkedTags = new StringBuilder();
    for (Node tag : folksonomyService.getLinkedTagsOfDocument(node, repository)) {      
      if (linkedTags.length() > 0)
        linkedTags = linkedTags.append(",");
      linkedTags.append(tag.getName());
    }    
    UIFormInputInfo uiLinkedTags = getChildById(LINKED_TAGS);    
    uiLinkedTags.setValue("<br>[" + linkedTags.toString() + "]");
    uiLinkedTags.setEnable(true);
  }
}
