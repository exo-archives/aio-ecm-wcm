package org.exoplatform.wcm.webui.selector.webContentView;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.wcm.core.NodeIdentifier;
import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.publication.NotInWCMPublicationException;
import org.exoplatform.services.wcm.publication.WCMPublicationService;
import org.exoplatform.wcm.webui.selector.document.UIDocumentPathSelector;
import org.exoplatform.wcm.webui.selector.document.UIDocumentTabSelector;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
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

  private NodeLocation presentNodeLocation;
  private boolean isDocument;

  public UIResultView() throws Exception {
    addChild(UIWCResultSearchPresentation.class, null, null);
  }

  public void init(Node presentNode, boolean isDocument) {
    UIWCResultSearchPresentation uiResultPresentation = 
      getChild(UIWCResultSearchPresentation.class);
    presentNodeLocation = NodeLocation.make(presentNode);
    this.isDocument = isDocument;
    uiResultPresentation.setDocument(isDocument);
    uiResultPresentation.setNode(presentNode);
  }

  public String[] getActions() {
    return new String[] {"Select"};
  }

  public static class SelectActionListener extends EventListener<UIResultView> {
    public void execute(Event<UIResultView> event) throws Exception {
      UIResultView uiResultView = event.getSource();
      UIWebContentTabSelector uiWCTabSelector = 
        uiResultView.getAncestorOfType(UIWebContentTabSelector.class);
      Node presentNode = NodeLocation.getNodeByLocation(uiResultView.presentNodeLocation);
      if(uiWCTabSelector == null) {
        UIDocumentTabSelector uiDocTabSelector = 
          uiResultView.getAncestorOfType(UIDocumentTabSelector.class);
        UIDocumentPathSelector uiDocPathSelector =
          uiDocTabSelector.getChild(UIDocumentPathSelector.class);
        String returnField = ((UIBaseNodeTreeSelector) uiDocPathSelector).getReturnFieldName();
        ((UISelectable)((UIBaseNodeTreeSelector) uiDocPathSelector)
            .getSourceComponent()).doSelect(returnField, presentNode.getPath());
        uiDocTabSelector.removeChild(UIResultView.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(
            ((UIBaseNodeTreeSelector) uiDocPathSelector).getSourceComponent().getParent());
      } else {
        Node webContent = presentNode;
        NodeIdentifier nodeIdentifier = NodeIdentifier.make(webContent);
        PortletRequestContext pContext = (PortletRequestContext) event.getRequestContext();
        PortletPreferences prefs = pContext.getRequest().getPreferences();
        prefs.setValue("repository", nodeIdentifier.getRepository());
        prefs.setValue("workspace", nodeIdentifier.getWorkspace());
        prefs.setValue("nodeIdentifier", nodeIdentifier.getUUID());
        prefs.store();

        String remoteUser = Util.getPortalRequestContext().getRemoteUser();
        String currentSite = Util.getPortalRequestContext().getPortalOwner();

        WCMPublicationService wcmPublicationService = uiResultView.getApplicationComponent(WCMPublicationService.class);

        try {
          wcmPublicationService.isEnrolledInWCMLifecycle(webContent);
        } catch (NotInWCMPublicationException e){
          wcmPublicationService.unsubcribeLifecycle(webContent);
          wcmPublicationService.enrollNodeInLifecycle(webContent, currentSite, remoteUser);          
        }
        
        UIPortal uiPortal = Util.getUIPortal();
        UIPageBody uiPageBody = uiPortal.findFirstComponentOfType(UIPageBody.class);
        uiPageBody.setUIComponent(null);
        uiPageBody.setMaximizedUIComponent(null);
        org.exoplatform.wcm.webui.Utils.updatePortal((PortletRequestContext)event.getRequestContext());
      }
    }    
  }
}
