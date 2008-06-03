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
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 28, 2008  
 */
public class PortalFolderSchemaHandler extends BaseWebSchemaHandler {

  private ActionServiceContainer actionServiceContainer_ ;

  public PortalFolderSchemaHandler(ActionServiceContainer actionServiceContainer)  {
    this.actionServiceContainer_ = actionServiceContainer ;
  }

  public Node getCSSFolder(Node portalFolder) throws Exception { 
    return portalFolder.getNode("css") ; 
  }
  
  public Node getJSFolder(Node portalFolder) throws Exception {
    return portalFolder.getNode("js") ;
  }
  
  public Node getMultimediaFolder(Node portalFolder) throws Exception {
    return portalFolder.getNode("multimedia") ;
  }
  
  public Node getImagesFolder(Node portalFolder) throws Exception {
    return portalFolder.getNode("multimedia/images") ;
  }
  
  public Node getVideoFolder(Node portalFolder) throws Exception {
    return portalFolder.getNode("multimedia/videos") ;
  }
  
  public Node getAudioFolder(Node portalFolder) throws Exception{
    return portalFolder.getNode("multimedia/audio") ;
  }
  
  public Node getDocumentFolder(Node portalFolder) throws Exception {
    return portalFolder.getNode("documents") ;
  }
  
  public Node getWebContentFolder(Node portalFolder) throws Exception {
    return portalFolder.getNode("web content") ;
  }
  
  protected String getHandlerNodeType() { return "exo:portalFolder"; }
  protected String getParentNodeType() { return "nt:unstructured"; }

  public void process(Node portalFolder) throws Exception {
    //TODO hard coded here    
    Node jsFolder = portalFolder.addNode("js","exo:jsFolder") ;    
    addMixin(jsFolder,EXO_OWNABLE) ;        
    Node cssFolder = portalFolder.addNode("css","exo:cssFolder") ;    
    addMixin(cssFolder, EXO_OWNABLE);        
    Node multimedia = portalFolder.addNode("multimedia",NT_FOLDER);    
    addMixin(multimedia, "exo:multimediaFolder") ;
    addMixin(multimedia, EXO_OWNABLE) ;    
    Node images = multimedia.addNode("images",NT_FOLDER);    
    addMixin(images, "exo:pictureFolder") ;
    addMixin(images, EXO_OWNABLE);
    Node video = multimedia.addNode("videos",NT_FOLDER);
    addMixin(video, "exo:videoFolder") ;    
    addMixin(video, EXO_OWNABLE) ;
    Node audio = multimedia.addNode("audio",NT_FOLDER);    
    addMixin(audio, "exo:musicFolder") ;
    addMixin(audio, EXO_OWNABLE) ;
    Node document = portalFolder.addNode("documents",NT_UNSTRUCTURED) ;
    addMixin(document, "exo:documentFolder") ;
    addMixin(document, EXO_OWNABLE) ;
    Node webFolder = portalFolder.addNode("web content","exo:webFolder") ;    
    addMixin(webFolder, EXO_OWNABLE) ;
    portalFolder.getSession().save();
    bindCSSAction(cssFolder) ;    
    bindJSAction(jsFolder) ;    
    portalFolder.getSession().save();   
  }

  private void bindJSAction(Node jsNode) throws Exception {   
    HashMap<String, JcrInputProperty> mappings = createScriptActionMapping("AddSharedJSActionListener", "add", "Add shared js action") ;
    String repository = ((ManageableRepository)jsNode.getSession().getRepository()).getConfiguration().getName();
    actionServiceContainer_.addAction(jsNode, repository, "exo:addSharedJSAction", mappings) ;
    HashMap<String, JcrInputProperty> removeActionMappings = createScriptActionMapping("RemoveJSActionListener", "remove", "process when a js file is removed") ;
    actionServiceContainer_.addAction(jsNode, repository, "exo:updateSharedJSAction", removeActionMappings) ;
    HashMap<String, JcrInputProperty> updateActionMappings = createScriptActionMapping("UpdateJSActionListener", "modify", "process when a js file is update") ;
    actionServiceContainer_.addAction(jsNode, repository, "exo:updateSharedJSAction", updateActionMappings) ;
  }

  private void bindCSSAction(Node cssNode) throws Exception {    
    HashMap<String, JcrInputProperty> mappings = createScriptActionMapping("AddSharedCSSActionListener", "add", "process when a css file is added") ;
    String repository = ((ManageableRepository)cssNode.getSession().getRepository()).getConfiguration().getName();
    actionServiceContainer_.addAction(cssNode, repository, "exo:addSharedCSSAction", mappings) ;  
    HashMap<String, JcrInputProperty> removeActionMappings = createScriptActionMapping("RemoveJSActionListener", "remove", "process when a css file is removed") ;
    actionServiceContainer_.addAction(cssNode, repository, "exo:updateSharedCSSAction", removeActionMappings) ;
    HashMap<String, JcrInputProperty> updateActionMappings = createScriptActionMapping("UpdateJSActionListener", "modify", "process when a css file is update") ;
    actionServiceContainer_.addAction(cssNode, repository, "exo:updateSharedCSSAction", updateActionMappings) ;
  }

  private HashMap<String, JcrInputProperty> createScriptActionMapping(String name,String lifecycle,String description) throws Exception {
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
    return mappings ;
  }  

}
