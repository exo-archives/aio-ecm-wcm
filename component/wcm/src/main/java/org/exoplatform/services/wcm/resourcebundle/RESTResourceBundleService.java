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
package org.exoplatform.services.wcm.resourcebundle;

import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.portal.application.PortalApplication;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.application.Application;
import org.exoplatform.webui.application.portlet.PortletApplication;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.phamvu@exoplatform.com
 * Nov 19, 2008
 */
@URITemplate("/resourceBundle/")
public class RESTResourceBundleService implements ResourceContainer {
  
  /** The web app controller. */
  public WebAppController webAppController;
  
  /**
   * Instantiates a new rEST resource bundle service.
   * 
   * @param webAppController the web app controller
   */
  public RESTResourceBundleService (WebAppController webAppController) {
    this.webAppController = webAppController;    
  }
  
  /**
   * Gets the portal message.
   * 
   * @param language the language
   * @param messageKey the message key
   * 
   * @return the portal message
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getString/{language}/{messageKey}/")
  @OutputTransformer(StringOutputTransformer.class) 
  public Response getPortalMessage(@URIParam("language") String language, 
      @URIParam("messageKey") String messageKey) throws Exception{
    PortalApplication portalApplication = webAppController.getApplication(PortalApplication.PORTAL_APPLICATION_ID);    
    Locale locale = getLocale(language);    
    ResourceBundle resourceBundle = portalApplication.getResourceBundle(locale);
    String message = getMessage(resourceBundle,messageKey);        
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(message).mediaType("text/plain").cacheControl(cacheControl).build();
  }

  /**
   * Gets the application message.
   * 
   * @param applicationId the application id
   * @param language the language
   * @param messageKey the message key
   * 
   * @return the application message
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getString/{applicationId}/{language}/{messageKey}/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response getApplicationMessage(@URIParam("applicationId") String applicationId,@URIParam("language") String language, 
      @URIParam("messageKey") String messageKey) throws Exception{
    Application application = webAppController.getApplication(applicationId);
    String message = messageKey;
    Locale locale = getLocale(language);
    if(application instanceof PortalApplication) {
      PortalApplication portalApplication = PortalApplication.class.cast(application);
      ResourceBundle resourceBundle = portalApplication.getResourceBundle(locale);
      message = getMessage(resourceBundle,messageKey);
    }else if (application instanceof PortletApplication) {
      PortletApplication portletApplication = PortletApplication.class.cast(application);
      ResourceBundle resourceBundle = portletApplication.getResourceBundle(locale);
      message = getMessage(resourceBundle,messageKey);
    }      
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(message).mediaType("text/plain").cacheControl(cacheControl).build();
  }
  
  /**
   * Gets the locale.
   * 
   * @param language the language
   * 
   * @return the locale
   */
  private Locale getLocale(String language) {    
    try {
      return new Locale(language);
    } catch (Exception e) {      
    }
    return Locale.ENGLISH;
  }

  /**
   * Gets the message.
   * 
   * @param resourceBundle the resource bundle
   * @param messageKey the message key
   * 
   * @return the message
   */
  private String getMessage(ResourceBundle resourceBundle, String messageKey) {    
    try {
      return resourceBundle.getString(messageKey);
    } catch (Exception e) {
    }
    return messageKey;
  }

}
