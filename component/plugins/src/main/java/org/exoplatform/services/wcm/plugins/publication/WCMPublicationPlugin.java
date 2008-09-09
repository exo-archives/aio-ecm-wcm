package org.exoplatform.services.wcm.plugins.publication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import javax.jcr.Node;

import org.exoplatform.services.ecm.publication.IncorrectStateUpdateLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;

public class WCMPublicationPlugin extends PublicationPlugin {

  public static final String ENROLLED = "enrolled".intern();
  public static final String UNPUBLISHED = "unpublished".intern();
  public static final String PUBLISHED = "published".intern();
  public static final String DEFAULT_STATE = UNPUBLISHED;

  public static final String PUBLICATION = "publication:publication".intern();
  public static final String LIFECYCLE_NAME = "publication:lifecycleName".intern();
  public static final String CURRENT_STATE = "publication:currentState".intern();
  public static final String HISTORY = "publication:history".intern();
  public static final String WCM_PUBLICATION_MIXIN = "publication:wcmPublication".intern();

  public void addMixin(Node node) throws Exception {
    node.addMixin(WCM_PUBLICATION_MIXIN);
  }

  public boolean canAddMixin(Node node) throws Exception {
    return node.canAddMixin(WCM_PUBLICATION_MIXIN);		
  }

  public void changeState(Node node, String newState,
      HashMap<String, String> context)
  throws IncorrectStateUpdateLifecycleException, Exception {		
  }

  public String[] getPossibleStates() { return new String[] { ENROLLED,UNPUBLISHED,PUBLISHED }; }

  public byte[] getStateImage(Node node, Locale locale) throws IOException,
  FileNotFoundException, Exception {	
    return null;
  }

  public UIForm getStateUI(Node node, UIComponent component) throws Exception {
    UIPublishingPanel form = component.createUIComponent(UIPublishingPanel.class,null,null);
    form.initPanel(node);
    return form;
  }

  public String getUserInfo(Node node, Locale locale) throws Exception {

    return null;
  }

}
