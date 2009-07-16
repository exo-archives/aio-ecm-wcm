package org.exoplatform.services.wcm.publication;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.wcm.BaseWCMTestCase;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationConstant;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.StageAndVersionPublicationPlugin;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform
 * thang.do@exoplatform.com, thangcof@gmail.com Jul 15, 2009
 */
public class TestWCMPublicationService extends BaseWCMTestCase {
  private WCMPublicationService wcmPublicationService;
  
  public void setUp() throws Exception{
    super.setUp();
    wcmPublicationService = getService(WCMPublicationService.class);
  }
  
  public void testAddPublicationPlugin() {
    WebpagePublicationPlugin publicationPlugin = new StageAndVersionPublicationPlugin();
    publicationPlugin.setName(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    wcmPublicationService.addPublicationPlugin(publicationPlugin);

    assertEquals(1, wcmPublicationService.getWebpagePublicationPlugins().size());
    assertSame(
        publicationPlugin,
        wcmPublicationService.getWebpagePublicationPlugins().get(StageAndVersionPublicationConstant.LIFECYCLE_NAME));
  }

  public void testPublishContentToPage() throws Exception {
    RepositoryService repositoryService = getService(RepositoryService.class);

    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    Node testNode = createWebcontentNode(session.getRootNode(), "test"); 
    session.save();
    
    Session sessionSystem = repositoryService.getRepository(REPO_NAME).getSystemSession(SYSTEM_WS);
    ((Node)sessionSystem.getItem("/exo:registry/exo:applications")).addNode("MainPortalData").addNode("classic").addNode("pages");
    
    Page page = new Page();
    page.setPageId("portal::classic::testpage");
    page.setName("testpage");
    page.setOwnerType("portal");
    page.setOwnerId("classic");
    UserPortalConfigService userPortalConfigService = getService(UserPortalConfigService.class);
    userPortalConfigService.create(page);
    
    javax.jcr.NodeIterator nodeIterator = ((Node)sessionSystem.getItem("/exo:registry/exo:applications/MainPortalData/classic/pages")).getNodes();
    while (nodeIterator.hasNext()) {
      System.out.println("666====================>" + nodeIterator.nextNode().getPath());
    }
    

    WebpagePublicationPlugin publicationPlugin = new StageAndVersionPublicationPlugin();
    publicationPlugin.setName(StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    wcmPublicationService.addPublicationPlugin(publicationPlugin);
    
//    publicationPlugin.addMixin(testNode);
    wcmPublicationService.enrollNodeInLifecycle(testNode, StageAndVersionPublicationConstant.LIFECYCLE_NAME);
    wcmPublicationService.publishContentToPage(testNode, page, "classic");
    
    PropertyIterator iterator = testNode.getProperties();
    while(iterator.hasNext()) {
      Property property = iterator.nextProperty();
      try {
        System.out.println(property.getName() + ": " + property.getString()); 
      } catch (Exception e) {
        for(Value value : property.getValues()) {
          System.out.println(property.getName() + ": " + value.getString());
        }
      }
    }
    
  }
}