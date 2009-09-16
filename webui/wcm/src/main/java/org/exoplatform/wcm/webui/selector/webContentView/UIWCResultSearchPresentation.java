package org.exoplatform.wcm.webui.selector.webContentView;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.resolver.JCRResourceResolver;
import org.exoplatform.ecm.webui.presentation.UIBaseNodePresentation;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.cms.impl.DMSConfiguration;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.core.WCMConfigurationService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * Feb 12, 2009  
 */

@ComponentConfig(
    lifecycle = Lifecycle.class    
)

public class UIWCResultSearchPresentation extends UIBaseNodePresentation {

  private Node presentNode;
  private boolean isDocument;
  private JCRResourceResolver resourceResolver;

  public UIWCResultSearchPresentation() throws Exception {}

  public Node getNode() throws Exception {
    return presentNode;
  }

  @Override
  public Node getOriginalNode() throws Exception {
    return getNode();
  }

  @Override
  public String getRepositoryName() throws Exception {
    RepositoryService repoService = getApplicationComponent(RepositoryService.class);
    return repoService.getCurrentRepository().getConfiguration().getName();
  }

  public String getRepository() {
    try{
      return getRepositoryName();
    }catch(Exception ex) {
      return null;
    }
  }

  public String getTemplate() {
    try{
      return getTemplatePath() ;
    }catch (Exception e) {
      return null ;
    }
  }

  @Override
  public String getTemplatePath() throws Exception {
    TemplateService tempService = getApplicationComponent(TemplateService.class);
    return tempService.getTemplatePath(getNode(), isDocument);
  }

  public String getNodeType() throws Exception {
    return null;
  }

  public boolean isNodeTypeSupported() {
    return false;
  }

  public void setNode(Node node) {
    presentNode = node;
  }

  public boolean isDocument() {
    return isDocument;
  }

  public void setDocument(boolean isDocument) {
    this.isDocument = isDocument;
  }

  public ResourceResolver getTemplateResourceResolver(WebuiRequestContext context, String template) {
    try{
      if(resourceResolver == null) {
        String repository = getRepositoryName();
        PortletRequestContext pContext = 
          (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
        PortletPreferences prefs = pContext.getRequest().getPreferences();
//        String workspace = prefs.getValue("workspace", null);
//        if(workspace == null) {
          //WCMConfigurationService wcmConfService =
            //getApplicationComponent(WCMConfigurationService.class);
          //NodeLocation nodeLocation = wcmConfService.getLivePortalsLocation(repository);
          //workspace = nodeLocation.getWorkspace();
            DMSConfiguration dmsConfiguration = getApplicationComponent(DMSConfiguration.class);
            String workspace = dmsConfiguration.getConfig(repository).getSystemWorkspace();
//        }
        resourceResolver = new JCRResourceResolver(repository, workspace, "exo:templateFile");
      }
    }catch(Exception ex) {
      ex.printStackTrace();
    }
    return resourceResolver;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    try{
      getTemplatePath();
    }catch(Exception ex) {
      ex.printStackTrace();
    }
    super.processRender(context) ;
  }

  public UIComponent getCommentComponent() {
    // TODO Auto-generated method stub
    return null;
  }
}
