package org.exoplatform.wcm.webui.selector.webcontent;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.wcm.webui.selector.document.UIDocumentPathSelector;
import org.exoplatform.wcm.webui.selector.document.UIDocumentTabSelector;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Author : TAN DUNG DANG
 *          dzungdev@gmail.com
 * Feb 12, 2009  
 */

@ComponentConfig (
    template="classpath:groovy/wcm/webui/UIResultView.gtmpl",
    events = {
        @EventConfig(listeners = UIResultView.SelectActionListener.class)
    }
)

public class UIResultView extends UIContainer {

  private Node presentNode;
  private boolean isDocument;

  public UIResultView() throws Exception {
    addChild(UIWCResultSearchPresentation.class, null, null);
  }

  public void init(Node presentNode, boolean isDocument) {
    UIWCResultSearchPresentation uiResultPresentation = 
      getChild(UIWCResultSearchPresentation.class);
    this.presentNode = presentNode;
    this.isDocument = isDocument;
    uiResultPresentation.setDocument(this.isDocument);
    uiResultPresentation.setNode(this.presentNode);
  }

  public String[] getActions() {
    return new String[] {"Select"};
  }

  public static class SelectActionListener extends EventListener<UIResultView> {
    public void execute(Event<UIResultView> event) throws Exception {
      UIResultView uiResultView = event.getSource();
      UIWebContentTabSelector uiWCTabSelector = 
        uiResultView.getAncestorOfType(UIWebContentTabSelector.class);
      if(uiWCTabSelector == null) {
        UIDocumentTabSelector uiDocTabSelector = 
          uiResultView.getAncestorOfType(UIDocumentTabSelector.class);
        UIDocumentPathSelector uiDocPathSelector =
          uiDocTabSelector.getChild(UIDocumentPathSelector.class);
        String returnField = ((UIBaseNodeTreeSelector) uiDocPathSelector).getReturnFieldName();
        ((UISelectable)((UIBaseNodeTreeSelector) uiDocPathSelector)
            .getSourceComponent()).doSelect(returnField, uiResultView.presentNode.getPath());
        uiDocTabSelector.removeChild(UIResultView.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(
            ((UIBaseNodeTreeSelector) uiDocPathSelector).getSourceComponent().getParent());
      } else {
        UIWebContentPathSelector uiWCPathSelector = 
          uiWCTabSelector.getChild(UIWebContentPathSelector.class);
        String returnField = ((UIBaseNodeTreeSelector) uiWCPathSelector).getReturnFieldName();
        ((UISelectable)((UIBaseNodeTreeSelector) uiWCPathSelector)
            .getSourceComponent()).doSelect(returnField, uiResultView.presentNode.getPath());
        uiWCTabSelector.removeChild(UIResultView.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(
            ((UIBaseNodeTreeSelector) uiWCPathSelector).getSourceComponent().getParent());
      }
    }    
  }
}
