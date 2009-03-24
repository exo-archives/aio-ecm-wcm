package org.exoplatform.wcm.webui.scv;

import javax.jcr.Node;

import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * Mar 19, 2009  
 */

@ComponentConfig(
    lifecycle = Lifecycle.class    
)

public class UIDraftContentPresentation extends UIPresentation {

  private Node originalNode;

  public UIDraftContentPresentation() throws Exception {
    super();
  }

  public Node getNode() throws Exception {
    return originalNode;
  }

  public void setNode(Node node) {
    this.originalNode = node;
  }

  @Override
  public String getTemplatePath() throws Exception {
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    return templateService.getTemplatePath(originalNode, false) ; 
  }
}
