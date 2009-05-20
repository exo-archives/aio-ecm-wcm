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

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.BaseWebSchemaHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham hoa.pham@exoplatform.com May 28, 2008
 */
public class PortalFolderSchemaHandler extends BaseWebSchemaHandler {  
  /**
   * Instantiates a new portal folder schema handler.
   * 
   * @param actionServiceContainer the action service container
   */
  public PortalFolderSchemaHandler()  {
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
    return portalFolder.getNode("medias");
  }
  
  /**
   * Gets the images folder.
   * 
   * @param portalFolder the portal folder
   * @return the images folder
   * @throws Exception the exception
   */
  public Node getImagesFolder(final Node portalFolder) throws Exception {
    return portalFolder.getNode("medias/images");
  }
 
  /**
   * Gets the video folder.
   * 
   * @param portalFolder the portal folder
   * @return the video folder
   * @throws Exception the exception
   */
  public Node getVideoFolder(final Node portalFolder) throws Exception {
    return portalFolder.getNode("medias/videos");
  }
  
  /**
   * Gets the audio folder.
   * 
   * @param portalFolder the portal folder
   * @return the audio folder
   * @throws Exception the exception
   */
  public Node getAudioFolder(final Node portalFolder) throws Exception{
    return portalFolder.getNode("medias/audio");
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
    return portalFolder.getNode("web contents");
  }    
  
  /**
   * Gets the banner themes.
   * 
   * @param portalFolder the portal folder
   * @return the banner themes
   * @throws Exception the exception
   */
  public Node getBannerThemes(Node portalFolder) throws Exception{
    return portalFolder.getNode("web contents/site artifacts/banner");
  }
  
  /**
   * Gets the footer themes.
   * 
   * @param portalFolder the portal folder
   * @return the footer themes
   * @throws Exception the exception
   */
  public Node getFooterThemes(Node portalFolder) throws Exception{
    return portalFolder.getNode("web contents/site artifacts/footer");
  }
  
  /**
   * Gets the navigation themes.
   * 
   * @param portalFolder the portal folder
   * @return the navigation themes
   * @throws Exception the exception
   */
  public Node getNavigationThemes(Node portalFolder) throws Exception{
    return portalFolder.getNode("web contents/site artifacts/navigation");
  }
  
  /**
   * Gets the breadcums themes.
   * 
   * @param portalFolder the portal folder
   * @return the breadcums themes
   * @throws Exception the exception
   */
  public Node getBreadcumsThemes(Node portalFolder) throws Exception{
    return portalFolder.getNode("web contents/site artifacts/breadcums");
  }
  
  public Node getHomepageThemes(Node portalFolder) throws Exception{
    return portalFolder.getNode("web contents/site artifacts/home page");
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
  public void onCreateNode(final Node portalFolder, SessionProvider sessionProvider) throws Exception {
    Calendar calendar = new GregorianCalendar();
    Node jsFolder = portalFolder.addNode("js","exo:jsFolder");
    addMixin(jsFolder,"exo:owneable");
    addMixin(jsFolder,"exo:datetime");    
    jsFolder.setProperty("exo:dateCreated",calendar);    
    
    Node cssFolder = portalFolder.addNode("css","exo:cssFolder");
    addMixin(cssFolder,"exo:owneable");
    addMixin(cssFolder,"exo:datetime");
    cssFolder.setProperty("exo:dateCreated",calendar);    
    
    Node multimedia = portalFolder.addNode("medias","exo:multimediaFolder");           
    addMixin(multimedia,"exo:owneable");
    addMixin(multimedia,"exo:datetime");    
    multimedia.setProperty("exo:dateCreated",calendar);    
    
    Node images = multimedia.addNode("images",NT_FOLDER);    
    addMixin(images, "exo:pictureFolder");
    addMixin(images,"exo:owneable");
    addMixin(images,"exo:datetime");
    images.setProperty("exo:dateCreated",calendar);    
    
    Node video = multimedia.addNode("videos",NT_FOLDER);
    addMixin(video, "exo:videoFolder");
    addMixin(video,"exo:owneable");
    addMixin(video,"exo:datetime");
    video.setProperty("exo:dateCreated",calendar);    
    
    Node audio = multimedia.addNode("audio",NT_FOLDER);    
    addMixin(audio, "exo:musicFolder");
    addMixin(audio,"exo:owneable");
    addMixin(audio,"exo:datetime");
    audio.setProperty("exo:dateCreated",calendar);    
    
    Node document = portalFolder.addNode("documents",NT_UNSTRUCTURED);
    addMixin(document, "exo:documentFolder");
    addMixin(document,"exo:owneable");
    addMixin(document,"exo:datetime");
    document.setProperty("exo:dateCreated",calendar);    
    
    Node webContents = portalFolder.addNode("web contents","exo:webFolder");
    addMixin(webContents,"exo:owneable");
    addMixin(webContents,"exo:datetime");
    webContents.setProperty("exo:dateCreated",calendar);    
    
    Node themes = webContents.addNode("site artifacts","exo:themeFolder");
    addMixin(themes,"exo:owneable");
    addMixin(themes,"exo:datetime");
    themes.setProperty("exo:dateCreated",calendar);   
    
    Node bannerFolder = themes.addNode("banner","nt:unstructured");
    addMixin(bannerFolder,"exo:owneable");
    addMixin(bannerFolder,"exo:datetime");
    bannerFolder.setProperty("exo:dateCreated",calendar);    
    
    Node searchboxFolder = themes.addNode("searchbox","nt:unstructured");
    addMixin(searchboxFolder,"exo:owneable");
    addMixin(searchboxFolder,"exo:datetime");
    searchboxFolder.setProperty("exo:dateCreated",calendar);   
    
    Node navigationFolder = themes.addNode("navigation","nt:unstructured");
    addMixin(navigationFolder,"exo:owneable");
    addMixin(navigationFolder,"exo:datetime");
    navigationFolder.setProperty("exo:dateCreated",calendar);       
    
    Node breadcumbsFolder = themes.addNode("breadcrumb","nt:unstructured");
    addMixin(breadcumbsFolder,"exo:owneable");
    addMixin(breadcumbsFolder,"exo:datetime");
    breadcumbsFolder.setProperty("exo:dateCreated",calendar);    
    
    Node homepageFolder = themes.addNode("homepage","nt:unstructured");
    addMixin(homepageFolder,"exo:owneable");
    addMixin(homepageFolder,"exo:datetime");
    homepageFolder.setProperty("exo:dateCreated",calendar);    
    
    Node footerFolder = themes.addNode("footer","nt:unstructured");
    addMixin(footerFolder,"exo:owneable");
    addMixin(footerFolder,"exo:datetime");
    footerFolder.setProperty("exo:dateCreated",calendar);    
    
    Node sitemapFolder = themes.addNode("sitemap","nt:unstructured");
    addMixin(sitemapFolder,"exo:owneable");
    addMixin(sitemapFolder,"exo:datetime");
    sitemapFolder.setProperty("exo:dateCreated",calendar);    
    
    Node accessFolder = themes.addNode("access","nt:unstructured");
    addMixin(accessFolder,"exo:owneable");
    addMixin(accessFolder,"exo:datetime");
    accessFolder.setProperty("exo:dateCreated",calendar);    
    
    Node links = portalFolder.addNode("links", "exo:linkFolder");
    addMixin(links,"exo:owneable");
    addMixin(links,"exo:datetime");
    links.setProperty("exo:dateCreated",calendar);    
    
    Node applicationDataFolder = portalFolder.addNode("ApplicationData", NT_FOLDER);
    addMixin(applicationDataFolder, "exo:owneable");
    addMixin(applicationDataFolder,"exo:datetime");
    applicationDataFolder.setProperty("exo:dateCreated", calendar);    
    
    Node newsletterApplicationFolder = applicationDataFolder.addNode("NewsletterApplication", NT_FOLDER);
    addMixin(newsletterApplicationFolder, "exo:owneable");
    addMixin(newsletterApplicationFolder,"exo:datetime");
    newsletterApplicationFolder.setProperty("exo:dateCreated", calendar);
    
    Node categoriesFolder = newsletterApplicationFolder.addNode("Categories", NT_FOLDER);
    addMixin(categoriesFolder, "exo:owneable");
    addMixin(categoriesFolder,"exo:datetime");
    categoriesFolder.setProperty("exo:dateCreated", calendar);
    
    Node userFolder = newsletterApplicationFolder.addNode("Users", NT_FOLDER);
    addMixin(userFolder, "exo:owneable");
    addMixin(userFolder,"exo:datetime");
    userFolder.setProperty("exo:dateCreated", calendar);
    
    portalFolder.getSession().save();       
  }  
}
