package org.exoplatform.services.wcm.plugins.publication;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.ecm.publication.IncorrectStateUpdateLifecycleException;
import org.exoplatform.services.ecm.publication.PublicationPlugin;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;

public class WCMPublicationPlugin extends PublicationPlugin {

  public static final String ENROLLED = "enrolled".intern();
  public static final String UNPUBLISHED = "unpublished".intern();
  public static final String PUBLISHED = "published".intern();
  public static final String DEFAULT_STATE = UNPUBLISHED;

  public static final String PUBLICATION = "publication:publication".intern();
  public static final String LIFECYCLE_PROP = "publication:lifecycleName".intern();
  public static final String CURRENT_STATE = "publication:currentState".intern();
  public static final String HISTORY = "publication:history".intern();
  public static final String WCM_PUBLICATION_MIXIN = "publication:wcmPublication".intern();
  public static final String LIFECYCLE_NAME = "Web Content Publishing".intern();

  public void addMixin(Node node) throws Exception {
    node.addMixin(WCM_PUBLICATION_MIXIN);
    node.setProperty(LIFECYCLE_PROP, LIFECYCLE_NAME);
    node.getSession().save();
  }

  public boolean canAddMixin(Node node) throws Exception {
    List<String> runningPortals = getRunningPortals(node.getSession().getUserID());
    if(runningPortals.size() == 0) {
      throw new AccessControlException("Current user doesn't have access permission to any portal");      
    }
    String portalName = getPortalForContent(node);
    if(portalName == null) {
      throw new PortalNotFoundException("This content doen't belong to any portal");
    }

    if(!isSharedPortal(portalName) && !runningPortals.contains(portalName)) {
      throw new PortalNotFoundException("The portal can be dead.");
    }
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
    List<String> runningPortals = getRunningPortals(node.getSession().getUserID());
    String portalName = getPortalForContent(node);
    form.initPanel(node,portalName, runningPortals);
    return form;
  }

  public String getUserInfo(Node node, Locale locale) throws Exception {

    return null;
  }

  private String getPortalForContent(Node contentNode) throws Exception {
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
    LivePortalManagerService livePortalManagerService = (LivePortalManagerService)exoContainer.getComponentInstanceOfType(LivePortalManagerService.class);
    for(String portalPath:livePortalManagerService.getLivePortalsPath()) {
      if(contentNode.getPath().startsWith(portalPath)) {
        return livePortalManagerService.getPortalNameByPath(portalPath);
      }
    }
    
    return null;
  }

  private boolean isSharedPortal(String portalName) throws Exception{
    LivePortalManagerService livePortalManagerService = getServices(LivePortalManagerService.class);
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(SessionProviderFactory.createSessionProvider());
    return sharedPortal.getName().equals(portalName);    
  }

  public List<String> getRunningPortals(String userId) throws Exception {
    List<String> listPortalName = new ArrayList<String>();
    DataStorage service = getServices(DataStorage.class);
    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, PortalConfig.class) ;
    PageList pageList = service.find(query) ;
    UserACL userACL = getServices(UserACL.class);
    for(Object object:pageList.getAll()) {
      PortalConfig portalConfig = (PortalConfig)object;
      if(userACL.hasPermission(portalConfig, userId)) {
        listPortalName.add(portalConfig.getName());
      }
    }
    return listPortalName;
  }

  private <T> T getServices(Class<T> clazz) {
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
    return clazz.cast(exoContainer.getComponentInstanceOfType(clazz));
  }
}
