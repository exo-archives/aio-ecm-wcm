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
package org.exoplatform.wcm.webui.scv;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.AbstractActionComponent;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.ecm.webui.presentation.removeattach.RemoveAttachmentComponent;
import org.exoplatform.ecm.webui.presentation.removecomment.RemoveCommentComponent;
import org.exoplatform.ecm.webui.utils.PermissionUtil;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.comments.CommentsService;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.publication.WCMComposer;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.component.action.CommentActionComponent;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Jun 9, 2008
 */
@ComponentConfig(
    lifecycle = Lifecycle.class    
)

public class UIPresentation extends UIBaseNodePresentation {

  private NodeLocation originalNodeLocation;
  
  private NodeLocation viewNodeLocation;
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getOriginalNode()
   */
  public Node getOriginalNode() throws Exception {
	  return Utils.getViewableNodeByComposer(originalNodeLocation.getRepository(), originalNodeLocation.getWorkspace(), originalNodeLocation.getPath(), WCMComposer.BASE_VERSION);
  }  

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getNode()
   */
  public void setOriginalNode(Node node) throws Exception{
    originalNodeLocation = NodeLocation.make(node);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getNode()
   */
  public Node getNode() throws Exception {
  	return Utils.getViewableNodeByComposer(viewNodeLocation.getRepository(), viewNodeLocation.getWorkspace(), viewNodeLocation.getPath());
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#setNode(javax.jcr.Node)
   */
  public void setNode(Node node) {
    viewNodeLocation = NodeLocation.make(node);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getRepositoryName()
   */
  public String getRepositoryName() {
  	PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
    return portletPreferences.getValue(UISingleContentViewerPortlet.REPOSITORY, "repository");
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.portal.webui.portal.UIPortalComponent#getTemplate()
   */
  public String getTemplate() {
    try{
      return getTemplatePath() ;
    } catch (Exception e) {
      return null ;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getTemplatePath()
   */
  public String getTemplatePath() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    return templateService.getTemplatePath(getOriginalNode(), false) ;
  }
    
  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#getTemplateResourceResolver(org.exoplatform.webui.application.WebuiRequestContext, java.lang.String)
   */
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    String repository = getRepositoryName();
    DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
    String workspace = dmsConfiguration.getConfig(repository).getSystemWorkspace();
    return new JCRResourceResolver(repository, workspace, "exo:templateFile");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getNodeType()
   */
  public String getNodeType() throws Exception {   
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#isNodeTypeSupported()
   */
  public boolean isNodeTypeSupported() {
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getCommentComponent()
   */
  public UIComponent getCommentComponent() {
    try {
      Node node = getOriginalNode();
      if (!PermissionUtil.canAddNode(node) || !node.isNodeType(org.exoplatform.ecm.webui.utils.Utils.MIX_COMMENTABLE) ||
          !node.isCheckedOut() || Utils.nodeIsLocked(node))
        return null;
      removeChild(CommentActionComponent.class);
      UIComponent uicomponent = addChild(CommentActionComponent.class, null,
          "DocumentInfoCommentComponent");
      return uicomponent;
    } catch (Exception e) {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getRemoveAttach()
   */
  public UIComponent getRemoveAttach() throws Exception {
    if (WCMComposer.MODE_LIVE.equals(Utils.getCurrentMode()))
      return null;    
    removeChild(RemoveAttachmentComponent.class);
    UIComponent uicomponent = addChild(RemoveAttachmentComponent.class, null,
        "DocumentInfoRemoveAttach");
    ((AbstractActionComponent) uicomponent).setLstComponentupdate(Arrays
        .asList(new Class[] { UIPresentationContainer.class }));
    return uicomponent;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getRemoveComment()
   */
  public UIComponent getRemoveComment() throws Exception {
    Node node = getOriginalNode();
    if (!PermissionUtil.canRemoveNode(node) || !node.isNodeType(org.exoplatform.ecm.webui.utils.Utils.MIX_COMMENTABLE) ||
        !node.isCheckedOut() || Utils.nodeIsLocked(node))
      return null;
    removeChild(RemoveCommentComponent.class);
    UIComponent uicomponent = addChild(RemoveCommentComponent.class, null,
        "DocumentInfoRemoveAttach");
    ((AbstractActionComponent) uicomponent).setLstComponentupdate(Arrays
        .asList(new Class[] { UIPresentationContainer.class }));
    return uicomponent;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getComments()
   */
  public List<Node> getComments() throws Exception {
    return getApplicationComponent(CommentsService.class).getComments(getOriginalNode(), getLanguage()) ;
  }
  
  
}
