package org.exoplatform.wcm.webui.selector.webcontent;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

// TODO: Auto-generated Javadoc
/**
 * Author : TAN DUNG DANG
 * dzungdev@gmail.com
 * Feb 12, 2009
 */
@ComponentConfig(
    lifecycle = Lifecycle.class    
)
public class UIWCResultSearchPresentation extends UIBaseNodePresentation {

  /** The present node. */
  private Node presentNode;
  
  /** The is document. */
  private boolean isDocument;
  
  /** The resource resolver. */
  private JCRResourceResolver resourceResolver;

  /**
   * Instantiates a new uIWC result search presentation.
   * 
   * @throws Exception the exception
   */
  public UIWCResultSearchPresentation() throws Exception {}

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getNode()
   */
  public Node getNode() throws Exception {
    return presentNode;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getOriginalNode()
   */
  @Override
  public Node getOriginalNode() throws Exception {
    return getNode();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getRepositoryName()
   */
  @Override
  public String getRepositoryName() throws Exception {
    RepositoryService repoService = getApplicationComponent(RepositoryService.class);
    return repoService.getCurrentRepository().getConfiguration().getName();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getRepository()
   */
  public String getRepository() {
    try{
      return getRepositoryName();
    }catch(Exception ex) {
      return null;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.portal.webui.portal.UIPortalComponent#getTemplate()
   */
  public String getTemplate() {
    try{
      return getTemplatePath() ;
    }catch (Exception e) {
      return null ;
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation#getTemplatePath()
   */
  @Override
  public String getTemplatePath() throws Exception {
    TemplateService tempService = getApplicationComponent(TemplateService.class);
    return tempService.getTemplatePath(getNode(), isDocument);
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
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#setNode(javax.jcr.Node)
   */
  public void setNode(Node node) {
    presentNode = node;
  }

  /**
   * Checks if is document.
   * 
   * @return true, if is document
   */
  public boolean isDocument() {
    return isDocument;
  }

  /**
   * Sets the document.
   * 
   * @param isDocument the new document
   */
  public void setDocument(boolean isDocument) {
    this.isDocument = isDocument;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#getTemplateResourceResolver(org.exoplatform.webui.application.WebuiRequestContext, java.lang.String)
   */
  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
      if(resourceResolver == null) {
        String repository = getRepositoryName();
        PortletRequestContext pContext = 
          (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
        @SuppressWarnings("unused")
        PortletPreferences prefs = pContext.getRequest().getPreferences();
        DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
        String workspace = dmsConfiguration.getConfig(repository).getSystemWorkspace();
        resourceResolver = new JCRResourceResolver(repository, workspace, "exo:templateFile");
      }
    }catch(Exception ex) {}
    return resourceResolver;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIComponent#processRender(org.exoplatform.webui.application.WebuiRequestContext)
   */
  public void processRender(WebuiRequestContext context) throws Exception {
    try{
      getTemplatePath();
    }catch(Exception ex) {}
    super.processRender(context) ;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.presentation.NodePresentation#getCommentComponent()
   */
  public UIComponent getCommentComponent() {
  	// TODO Auto-generated method stub
  	return null;
  }
}
