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
package org.exoplatform.services.wcm.portal;

import java.util.HashMap;

import javax.jcr.Node;

import org.exoplatform.services.cms.JcrInputProperty;
import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.core.BaseWebSchemaHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham hoa.pham@exoplatform.com May 28, 2008
 */
public class PortalFolderSchemaHandler extends BaseWebSchemaHandler {

  private ActionServiceContainer actionServiceContainer;

  /**
   * Instantiates a new portal folder schema handler.
   * 
   * @param actionServiceContainer the action service container
   */
  public PortalFolderSchemaHandler(ActionServiceContainer actionServiceContainer)  {
    this.actionServiceContainer = actionServiceContainer;
  }

  /**
   * Gets the CSS folder.
   * 
   * @param portalFolder the portal folder
   * @return the cSS folder
   * @throws Exception the exception
   */
  public Node getCSSFolder(final Node portalFolder) throws Exception { 
    return portalFolder.getNode("css"); 
  }
  
  /**
   * Gets the javasscript folder.
   * 
   * @param portalFolder the portal folder
   * @return the javasscript folder node
   * @throws Exception the exception
   */
  public Node getJSFolder(final Node portalFolder) throws Exception {
    return portalFolder.getNode("js");
  }
  
  /**
   * Gets the multimedia folder.
   * 
   * @param portalFolder the portal folder
   * @return the multimedia folder
   * @throws Exception the exception
   */
  public Node getMultimediaFolder(final Node portalFolder) throws Exception {
    return portalFolder.getNode("multimedia");
  }
  
  /**
   * Gets the images folder.
   * 
   * @param portalFolder the portal folder
   * @return the images folder
   * @throws Exception the exception
   */
  public Node getImagesFolder(final Node portalFolder) throws Exception {
    return portalFolder.getNode("multimedia/images");
  }
 
  /**
   * Gets the video folder.
   * 
   * @param portalFolder the portal folder
   * @return the video folder
   * @throws Exception the exception
   */
  public Node getVideoFolder(final Node portalFolder) throws Exception {
    return portalFolder.getNode("multimedia/videos");
  }
  
  /**
   * Gets the audio folder.
   * 
   * @param portalFolder the portal folder
   * @return the audio folder
   * @throws Exception the exception
   */
  public Node getAudioFolder(final Node portalFolder) throws Exception{
    return portalFolder.getNode("multimedia/audio");
  }
  
  /**
   * Gets the document storage.
   * 
   * @param portalFolder the portal folder
   * @return the document storage
   * @throws Exception the exception
   */
  public Node getDocumentStorage(Node portalFolder) throws Exception {
    return portalFolder.getNode("documents");
  }
  
  /**
   * Gets the link folder.
   * 
   * @param portalFolder the portal folder
   * @return the link folder
   * @throws Exception the exception
   */
  public Node getLinkFolder(Node portalFolder) throws Exception {
    return portalFolder.getNode("links");
  }
  
  /**
   * Gets the web content storage.
   * 
   * @param portalFolder the portal folder
   * @return the web content storage
   * @throws Exception the exception
   */
  public Node getWebContentStorage (final Node portalFolder) throws Exception {
    return portalFolder.getNode("web content storage");
  }    
  
  /**
   * Gets the banner themes.
   * 
   * @param portalFolder the portal folder
   * @return the banner themes
   * @throws Exception the exception
   */
  public Node getBannerThemes(Node portalFolder) throws Exception{
    return portalFolder.getNode("themes/banner");
  }
  
  /**
   * Gets the footer themes.
   * 
   * @param portalFolder the portal folder
   * @return the footer themes
   * @throws Exception the exception
   */
  public Node getFooterThemes(Node portalFolder) throws Exception{
    return portalFolder.getNode("themes/footer");
  }
  
  /**
   * Gets the navigation themes.
   * 
   * @param portalFolder the portal folder
   * @return the navigation themes
   * @throws Exception the exception
   */
  public Node getNavigationThemes(Node portalFolder) throws Exception{
    return portalFolder.getNode("themes/navigation");
  }
  
  /**
   * Gets the breadcums themes.
   * 
   * @param portalFolder the portal folder
   * @return the breadcums themes
   * @throws Exception the exception
   */
  public Node getBreadcumsThemes(Node portalFolder) throws Exception{
    return portalFolder.getNode("themes/breadcums");
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.BaseWebSchemaHandler#getHandlerNodeType()
   */
  protected String getHandlerNodeType() { return "exo:portalFolder"; }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.BaseWebSchemaHandler#getParentNodeType()
   */
  protected String getParentNodeType() { return "nt:unstructured"; }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.BaseWebSchemaHandler#process(javax.jcr.Node)
   */
  public void process(final Node portalFolder) throws Exception {
    Node jsFolder = portalFolder.addNode("js","exo:jsFolder");                
    Node cssFolder = portalFolder.addNode("css","exo:cssFolder");        
    Node multimedia = portalFolder.addNode("multimedia",NT_FOLDER);    
    addMixin(multimedia, "exo:multimediaFolder");       
    Node images = multimedia.addNode("images",NT_FOLDER);    
    addMixin(images, "exo:pictureFolder");    
    Node video = multimedia.addNode("videos",NT_FOLDER);
    addMixin(video, "exo:videoFolder");        
    Node audio = multimedia.addNode("audio",NT_FOLDER);    
    addMixin(audio, "exo:musicFolder");    
    Node document = portalFolder.addNode("documents",NT_UNSTRUCTURED);
    addMixin(document, "exo:documentFolder");        
    portalFolder.addNode("web content storage","exo:webFolder");       
    Node themes = portalFolder.addNode("themes","exo:themeFolder");
    themes.addNode("banner","nt:unstructured");
    themes.addNode("footer","nt:unstructured");
    themes.addNode("navigation","nt:unstructured");
    themes.addNode("breadcums","nt:unstructured");
    portalFolder.addNode("links", "exo:linkFolder");
    
    portalFolder.getSession().save();
    bindCSSAction(cssFolder);    
    bindJSAction(jsFolder);    
    portalFolder.getSession().save();   
  }

  private void bindJSAction(final Node jsNode) throws Exception {   
    HashMap<String, JcrInputProperty> mappings = createScriptActionMapping("AddSharedJSActionListener", "add", "Add shared js action");
    String repository = ((ManageableRepository)jsNode.getSession().getRepository()).getConfiguration().getName();
    actionServiceContainer.addAction(jsNode, repository, "exo:addSharedJSAction", mappings);
    HashMap<String, JcrInputProperty> removeActionMappings = createScriptActionMapping("RemoveJSActionListener", "remove", "process when a js file is removed");
    actionServiceContainer.addAction(jsNode, repository, "exo:updateSharedJSAction", removeActionMappings);
    HashMap<String, JcrInputProperty> updateActionMappings = createScriptActionMapping("UpdateJSActionListener", "modify", "process when a js file is update");
    actionServiceContainer.addAction(jsNode, repository, "exo:updateSharedJSAction", updateActionMappings);
  }

  private void bindCSSAction(final Node cssNode) throws Exception {    
    HashMap<String, JcrInputProperty> mappings = createScriptActionMapping("AddSharedCSSActionListener", "add", "process when a css file is added");
    String repository = ((ManageableRepository)cssNode.getSession().getRepository()).getConfiguration().getName();
    actionServiceContainer.addAction(cssNode, repository, "exo:addSharedCSSAction", mappings);  
    HashMap<String, JcrInputProperty> removeActionMappings = createScriptActionMapping("RemoveJSActionListener", "remove", "process when a css file is removed");
    actionServiceContainer.addAction(cssNode, repository, "exo:updateSharedCSSAction", removeActionMappings);
    HashMap<String, JcrInputProperty> updateActionMappings = createScriptActionMapping("UpdateJSActionListener", "modify", "process when a css file is update");
    actionServiceContainer.addAction(cssNode, repository, "exo:updateSharedCSSAction", updateActionMappings);
  }

  private HashMap<String, JcrInputProperty> createScriptActionMapping(final String name, final String lifecycle, final String description) throws Exception {
    HashMap<String,JcrInputProperty> mappings = new HashMap<String,JcrInputProperty>();
    JcrInputProperty nodeTypeInputProperty = new JcrInputProperty();
    nodeTypeInputProperty.setJcrPath("/node");
    nodeTypeInputProperty.setValue(name);
    mappings.put("/node", nodeTypeInputProperty);

    JcrInputProperty nameInputProperty = new JcrInputProperty();
    nameInputProperty.setJcrPath("/node/exo:name");
    nameInputProperty.setValue(name);
    mappings.put("/node/exo:name", nameInputProperty);

    JcrInputProperty lifeCycleInputProperty = new JcrInputProperty();
    lifeCycleInputProperty.setJcrPath("/node/exo:lifecyclePhase");
    lifeCycleInputProperty.setValue(lifecycle);
    mappings.put("/node/exo:lifecyclePhase", lifeCycleInputProperty);

    JcrInputProperty descriptionInputProperty = new JcrInputProperty();
    descriptionInputProperty.setJcrPath("/node/exo:description");
    descriptionInputProperty.setValue(description);
    mappings.put("/node/exo:description", descriptionInputProperty); 
    return mappings;
  }  

}
