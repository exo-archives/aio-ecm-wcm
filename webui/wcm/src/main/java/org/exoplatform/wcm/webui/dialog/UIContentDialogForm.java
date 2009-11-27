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
package org.exoplatform.wcm.webui.dialog;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemExistsException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.version.VersionException;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.form.DialogFormActionListeners;
import org.exoplatform.ecm.webui.form.UIDialogForm;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneTaxonomySelector;
import org.exoplatform.ecm.webui.utils.DialogFormUtil;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.BasePath;
import org.exoplatform.services.cms.CmsService;
import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.impl.DMSRepositoryConfiguration;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.ecm.publication.PublicationService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.publication.PublicationDefaultStates;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;
import org.exoplatform.webui.form.UIFormMultiValueInputSet;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Oct 29, 2009  
 */
@ComponentConfig (
    lifecycle = UIFormLifecycle.class,
    events = {
      @EventConfig(listeners = UIContentDialogForm.AddActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIContentDialogForm.RemoveReferenceActionListener.class, 
                   confirm = "DialogFormField.msg.confirm-delete", phase = Phase.DECODE),
      @EventConfig(listeners = UIContentDialogForm.SaveDraftActionListener.class),
      @EventConfig(listeners = UIContentDialogForm.FastPublishActionListener.class),
      @EventConfig(listeners = UIContentDialogForm.PreferencesActionListener.class),
      @EventConfig(listeners = UIContentDialogForm.CloseActionListener.class),
      @EventConfig(listeners = DialogFormActionListeners.RemoveDataActionListener.class)
    }
)
public class UIContentDialogForm extends UIDialogForm  implements UIPopupComponent, UISelectable {

	public static final String CONTENT_DIALOG_FORM_POPUP_WINDOW = "UIContentDialogFormPopupWindow";
	public static final String FIELD_TAXONOMY = "categories";
	public static final String TAXONOMY_CONTENT_POPUP_WINDOW = "UIContentPopupWindow";
	private NodeLocation webcontentNodeLocation;
	private List<String> listTaxonomy = new ArrayList<String>();
  private List<String> listTaxonomyName = new ArrayList<String>();
  
  public List<String> getListTaxonomy() {
    return listTaxonomy;
  }
  
  public List<String> getlistTaxonomyName() {
    return listTaxonomyName;
  }
  
  public void setListTaxonomy(List<String> listTaxonomyNew) {
    listTaxonomy = listTaxonomyNew;
  }
  
  public void setListTaxonomyName(List<String> listTaxonomyNameNew) {
    listTaxonomyName = listTaxonomyNameNew;
  }
	private Class<? extends UIContentDialogPreference> preferenceComponent;
	
	public NodeLocation getWebcontentNodeLocation() {
		return webcontentNodeLocation;
	}

	public void setWebcontentNodeLocation(NodeLocation webcontentNodeLocation) {
		this.webcontentNodeLocation = webcontentNodeLocation;
	}
	
	public Class<? extends UIContentDialogPreference> getPreferenceComponent() {
		return preferenceComponent;
	}
	
	public void setPreferenceComponent(Class<? extends UIContentDialogPreference> preferenceComponent) {
		this.preferenceComponent = preferenceComponent;
	}
	
	/**
   * Instantiates a new uI content dialog form.
   * 
   * @throws Exception the exception
   */
  public UIContentDialogForm() throws Exception {
    setActions(new String [] {"SaveDraft", "FastPublish", "Preferences", "Close"});
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init(Node webcontent, boolean isAddNew) throws Exception {
  	NodeLocation webcontentNodeLocation = NodeLocation.make(webcontent);
  	this.webcontentNodeLocation = webcontentNodeLocation;
    this.repositoryName = webcontentNodeLocation.getRepository();
    this.workspaceName = webcontentNodeLocation.getWorkspace();
    this.contentType = webcontent.getPrimaryNodeType().getName();
    this.nodePath = webcontent.getPath();
    this.isAddNew = isAddNew;
    setStoredPath(webcontent.getParent().getPath());
    resetProperties();
  }

  public Node getCurrentNode() {
    return NodeLocation.getNodeByLocation(webcontentNodeLocation);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.form.UIDialogForm#getTemplate()
   */
  public String getTemplate() {
    TemplateService templateService = getApplicationComponent(TemplateService.class) ;
    String userName = Util.getPortalRequestContext().getRemoteUser();
    try{
      return templateService.getTemplatePathByUser(true, contentType, userName, repositoryName);
    } catch(Exception e) {
      UIApplication uiApp = getAncestorOfType(UIApplication.class);
      Object[] arg = {contentType};
      uiApp.addMessage(new ApplicationMessage("UIDocumentForm.msg.not-support", arg, ApplicationMessage.ERROR));
      return null;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#getTemplateResourceResolver(org.exoplatform.webui.application.WebuiRequestContext, java.lang.String)
   */
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
  	DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    String workspace = dmsConfiguration.getConfig(this.repositoryName).getSystemWorkspace();
    return new JCRResourceResolver(this.repositoryName, workspace, TemplateService.EXO_TEMPLATE_FILE_PROP);
  }

  /**
   * The listener interface for receiving closeAction events.
   * The class that is interested in processing a cancelAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addCloseActionListener<code> method. When
   * the cancelAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CloseActionEvent
   */
  static public class CloseActionListener extends EventListener<UIContentDialogForm> {

  	/* (non-Javadoc)
	   * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
	   */
    public void execute(Event<UIContentDialogForm> event) throws Exception {
      UIContentDialogForm contentDialogForm = event.getSource();
      Utils.closePopupWindow(contentDialogForm, CONTENT_DIALOG_FORM_POPUP_WINDOW);      
    }
  }

  /**
   * The listener interface for receiving preferencesAction events.
   * The class that is interested in processing a preferencesAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addPreferencesActionListener<code> method. When
   * the PreferencesAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see PreferencesActionEvent
   */
  static public class PreferencesActionListener extends EventListener<UIContentDialogForm> {

  	/* (non-Javadoc)
	   * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
	   */
	  public void execute(Event<UIContentDialogForm> event) throws Exception {
      UIContentDialogForm contentDialogForm = event.getSource();
      UIPopupContainer popupContainer = Utils.getPopupContainer(contentDialogForm);
      popupContainer.addChild(contentDialogForm);
      contentDialogForm.setParent(popupContainer);
      UIContentDialogPreference contentDialogPreference = null;
      if (contentDialogForm.getPreferenceComponent() != null)
      	contentDialogPreference = contentDialogForm.createUIComponent(contentDialogForm.getPreferenceComponent(), null, null);
      else 
      	contentDialogPreference = contentDialogForm.createUIComponent(UIContentDialogPreference.class, null, null);
      
      Utils.updatePopupWindow(contentDialogForm, contentDialogPreference, CONTENT_DIALOG_FORM_POPUP_WINDOW);
      contentDialogPreference.init();
	  }
  }
  
  /**
   * The listener interface for receiving saveAction events.
   * The class that is interested in processing a saveAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addSaveDraftActionListener<code> method. When
   * the saveAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see SaveDraftActionEvent
   */
  public static class SaveDraftActionListener extends EventListener<UIContentDialogForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIContentDialogForm> event) throws Exception {
    	UIContentDialogForm contentDialogForm = event.getSource();
    	try{
        Node webContentNode = contentDialogForm.getNode();
        if (!webContentNode.isCheckedOut()) {
          webContentNode.checkout();
        }
      	List<UIComponent> inputs = contentDialogForm.getChildren();
      	if(contentDialogForm.checkCategories(contentDialogForm)) {
      	  Utils.createPopupMessage(contentDialogForm, "UIContentDialogForm.msg.non-categories", null, ApplicationMessage.WARNING);
          return;
      	}
      	Map<String, JcrInputProperty> inputProperties = DialogFormUtil.prepareMap(inputs, contentDialogForm.getInputProperties());
        CmsService cmsService = contentDialogForm.getApplicationComponent(CmsService.class);
        cmsService.storeNode(contentDialogForm.contentType, contentDialogForm.getNode().getParent(), inputProperties, contentDialogForm.isAddNew, contentDialogForm.repositoryName);
      } catch(LockException le) {
      	Object[] args = {contentDialogForm.getNode().getPath()};
      	Utils.createPopupMessage(contentDialogForm, "UIContentDialogForm.msg.node-locked", args, ApplicationMessage.WARNING);
      } catch(AccessControlException ace) {
      } catch(VersionException ve) {
      	Utils.createPopupMessage(contentDialogForm, "UIDocumentForm.msg.in-versioning", null, ApplicationMessage.WARNING);
      } catch(ItemNotFoundException item) {
      	Utils.createPopupMessage(contentDialogForm, "UIDocumentForm.msg.item-not-found", null, ApplicationMessage.WARNING);
      } catch(RepositoryException repo) {
        String key = "UIDocumentForm.msg.repository-exception";
        if (ItemExistsException.class.isInstance(repo)) key = "UIDocumentForm.msg.not-allowed-same-name-sibling";
        Utils.createPopupMessage(contentDialogForm, key, null, ApplicationMessage.WARNING);
      }catch(NumberFormatException nfe) {
      	Utils.createPopupMessage(contentDialogForm, "UIDocumentForm.msg.numberformat-exception", null, ApplicationMessage.WARNING);
      }catch(Exception e) {
      	Utils.createPopupMessage(contentDialogForm, "UIDocumentForm.msg.cannot-save", null, ApplicationMessage.WARNING);
      }
      Utils.closePopupWindow(contentDialogForm, CONTENT_DIALOG_FORM_POPUP_WINDOW);      
    }
  }
  
  private boolean checkCategories(UIContentDialogForm contentDialogForm) {
    String categoriesPath = "";
    String[] categoriesPathList = null;
    int index = 0;
    if (contentDialogForm.isReference) {
      UIFormMultiValueInputSet uiSet = contentDialogForm.getChild(UIFormMultiValueInputSet.class);
      if((uiSet != null) && (uiSet.getName() != null) && uiSet.getName().equals(FIELD_TAXONOMY)) {
        List<UIComponent> listChildren = uiSet.getChildren();         
        for (UIComponent component : listChildren) {
          UIFormStringInput uiStringInput = (UIFormStringInput)component;          
          if(uiStringInput.getValue() != null) {
            String value = uiStringInput.getValue().trim();            
            categoriesPath += value + ",";
          }
        }
        if (categoriesPath != null && categoriesPath.length() > 0) {
          try {
            if (categoriesPath.endsWith(",")) {
              categoriesPath = categoriesPath.substring(0, categoriesPath.length()-1).trim();
              if(categoriesPath.trim().length() == 0) {
                return true;
              }
            }
            categoriesPathList = categoriesPath.split(",");
            if ((categoriesPathList == null) || (categoriesPathList.length == 0)) {
              return true;
            }
            for (String categoryPath : categoriesPathList) {
              index = categoryPath.indexOf("/");
              if (index < 0) {
                return true;
              }
            }
          } catch (Exception e) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  /**
   * The listener interface for receiving fastPublishAction events.
   * The class that is interested in processing a cancelAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addFastPublishActionListener<code> method. When
   * the cancelAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see FastPublishActionEvent
   */
  public static class FastPublishActionListener extends EventListener<UIContentDialogForm> {
  	
  	/* (non-Javadoc)
	   * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
	   */
	  public void execute(Event<UIContentDialogForm> event) throws Exception {
	  	UIContentDialogForm contentDialogForm = event.getSource();
    	try{
        Node webContentNode = contentDialogForm.getNode();
        if (!webContentNode.isCheckedOut()) {
          webContentNode.checkout();
        }
      	List<UIComponent> inputs = contentDialogForm.getChildren();
      	if(contentDialogForm.checkCategories(contentDialogForm)) {
          Utils.createPopupMessage(contentDialogForm, "UIContentDialogForm.msg.non-categories", null, ApplicationMessage.WARNING);
          return;
        }
      	Map<String, JcrInputProperty> inputProperties = DialogFormUtil.prepareMap(inputs, contentDialogForm.getInputProperties());
        CmsService cmsService = contentDialogForm.getApplicationComponent(CmsService.class);
        cmsService.storeNode(contentDialogForm.contentType, contentDialogForm.getNode().getParent(), inputProperties, contentDialogForm.isAddNew, contentDialogForm.repositoryName);
        
        PublicationService publicationService = contentDialogForm.getApplicationComponent(PublicationService.class);
	      PublicationPlugin publicationPlugin = publicationService.getPublicationPlugins().get(publicationService.getNodeLifecycleName(webContentNode));
	      HashMap<String, String> context = new HashMap<String, String>();
	      if(webContentNode != null) {
	    	  context.put(StageAndVersionPublicationConstant.CURRENT_REVISION_NAME, webContentNode.getName());
	      }
	      publicationPlugin.changeState(webContentNode, PublicationDefaultStates.PUBLISHED, context);
	      
      } catch(LockException le) {
      	Object[] args = {contentDialogForm.getNode().getPath()};
      	Utils.createPopupMessage(contentDialogForm, "UIContentDialogForm.msg.node-locked", args, ApplicationMessage.WARNING);
      } catch(AccessControlException ace) {
      } catch(VersionException ve) {
      	Utils.createPopupMessage(contentDialogForm, "UIDocumentForm.msg.in-versioning", null, ApplicationMessage.WARNING);
      } catch(ItemNotFoundException item) {
      	Utils.createPopupMessage(contentDialogForm, "UIDocumentForm.msg.item-not-found", null, ApplicationMessage.WARNING);
      } catch(RepositoryException repo) {
        String key = "UIDocumentForm.msg.repository-exception";
        if (ItemExistsException.class.isInstance(repo)) key = "UIDocumentForm.msg.not-allowed-same-name-sibling";
        Utils.createPopupMessage(contentDialogForm, key, null, ApplicationMessage.WARNING);
      }catch(NumberFormatException nfe) {
      	Utils.createPopupMessage(contentDialogForm, "UIDocumentForm.msg.numberformat-exception", null, ApplicationMessage.WARNING);
      }catch(Exception e) {
      	Utils.createPopupMessage(contentDialogForm, "UIDocumentForm.msg.cannot-save", null, ApplicationMessage.WARNING);
      }
      
      Utils.closePopupWindow(contentDialogForm, CONTENT_DIALOG_FORM_POPUP_WINDOW);
	  }
  }
  
  static public class AddActionListener extends EventListener<UIContentDialogForm> {

    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UIContentDialogForm> event) throws Exception {
      UIContentDialogForm contentDialogForm = event.getSource();
      String clickedField = event.getRequestContext().getRequestParameter(OBJECTID);
      if (contentDialogForm.isReference) {
        UIApplication uiApp = contentDialogForm.getAncestorOfType(UIApplication.class);
        try{
          UIFormMultiValueInputSet uiSet = contentDialogForm.getChildById(FIELD_TAXONOMY);
          if((uiSet != null) && (uiSet.getName() != null) && uiSet.getName().equals(FIELD_TAXONOMY)) {
            if ((clickedField != null) && (clickedField.equals(FIELD_TAXONOMY))){
              NodeHierarchyCreator nodeHierarchyCreator = 
                contentDialogForm.getApplicationComponent(NodeHierarchyCreator.class);
              String repository = contentDialogForm.repositoryName;
              DMSConfiguration dmsConfiguration = contentDialogForm.getApplicationComponent(DMSConfiguration.class);
              DMSRepositoryConfiguration repositoryConfiguration = dmsConfiguration.getConfig(repository);
              String workspaceName = repositoryConfiguration.getSystemWorkspace();
              UIOneTaxonomySelector uiOneTaxonomySelector = 
                contentDialogForm.createUIComponent(UIOneTaxonomySelector.class, null, null);
              if(uiSet.getValue().size() == 0) uiSet.setValue(new ArrayList<Value>()); 
              String rootTreePath = nodeHierarchyCreator.getJcrPath(BasePath.TAXONOMIES_TREE_STORAGE_PATH);
              RepositoryService repositoryService = (RepositoryService)contentDialogForm.getApplicationComponent(RepositoryService.class);
              ManageableRepository manageableRepository = repositoryService.getRepository(repository);
              Session session = Utils.getSessionProvider(contentDialogForm).getSession(workspaceName, manageableRepository);
              Node rootTree = (Node) session.getItem(rootTreePath);
              NodeIterator childrenIterator = rootTree.getNodes();
              while (childrenIterator.hasNext()) {
                Node childNode = childrenIterator.nextNode();
                rootTreePath = childNode.getPath();
                break;
              }
              uiOneTaxonomySelector.setRootNodeLocation(repository, workspaceName, rootTreePath);
              uiOneTaxonomySelector.setExceptedNodeTypesInPathPanel(new String[] {"exo:symlink"});
              uiOneTaxonomySelector.init(Utils.getSystemProvider(contentDialogForm));
              String param = "returnField=" + FIELD_TAXONOMY;
              uiOneTaxonomySelector.setSourceComponent(contentDialogForm, new String[]{param});
              Utils.createPopupWindow(contentDialogForm, uiOneTaxonomySelector, TAXONOMY_CONTENT_POPUP_WINDOW, 700, 450);
            }
          }
        }catch (AccessDeniedException accessDeniedException) {
          uiApp.addMessage(new ApplicationMessage("UIContentDialogForm.msg.access-denied", null, 
                                                  ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        } catch (Exception e) {
          uiApp.addMessage(new ApplicationMessage("UIContentDialogForm.msg.exception", null, 
              ApplicationMessage.WARNING));
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
          return;
        }
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(contentDialogForm);
      }
    }
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  @SuppressWarnings("unchecked")
  public void doSelect(String selectField, Object value) throws Exception {
    isUpdateSelect = true;    
    UIFormInput formInput = getUIInput(selectField);
    if(formInput instanceof UIFormInputBase) {
      ((UIFormInputBase)formInput).setValue(value.toString());
    }else if(formInput instanceof UIFormMultiValueInputSet) {
      UIFormMultiValueInputSet  inputSet = (UIFormMultiValueInputSet) formInput;            
      String valueTaxonomy = String.valueOf(value).trim();
      List taxonomylist = inputSet.getValue();
      if (!taxonomylist.contains(valueTaxonomy)) {
        listTaxonomy.add(valueTaxonomy);
        listTaxonomyName.add(valueTaxonomy);
        taxonomylist.add(valueTaxonomy);
      }      
      inputSet.setValue(taxonomylist);
    }
  }
  
  static public class RemoveReferenceActionListener extends EventListener<UIContentDialogForm> {
    public void execute(Event<UIContentDialogForm> event) throws Exception {
      UIContentDialogForm contentDialogForm = event.getSource();
      contentDialogForm.isRemovePreference = true;
      String fieldName = event.getRequestContext().getRequestParameter(OBJECTID);
      contentDialogForm.getUIStringInput(fieldName).setValue(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(contentDialogForm);
    }
  }
}
