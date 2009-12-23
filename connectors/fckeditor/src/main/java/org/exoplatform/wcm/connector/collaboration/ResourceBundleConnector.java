/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wcm.connector.collaboration;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.services.resources.ResourceBundleService;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Dec 21, 2009  
 */
@URITemplate("/bundle/")
public class ResourceBundleConnector implements ResourceContainer {

	@HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getBundle/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getBundle (
    @QueryParam("key") String multiKey,
    @QueryParam("locale") String locale) {
		try {
			ResourceBundleService resourceBundleService = WCMCoreUtils.getService(ResourceBundleService.class);
			String resourceBundleNames[] = resourceBundleService.getSharedResourceBundleNames();
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element bundles = document.createElement("bundles");
			String keys[] = multiKey.split(",");
			for (String resourceBundleName : resourceBundleNames) {
				for (String key : keys) {
					try {
						ResourceBundle resourceBundle = resourceBundleService.getResourceBundle(resourceBundleName, new Locale(locale));
						String value = resourceBundle.getString(key);
						Element element = document.createElement("bundle");
						element.setAttribute("key", key); 
  					element.setAttribute("value", value);
  					element.setAttribute("locale", locale);
  					bundles.appendChild(element);
					} catch (MissingResourceException e) {}
				}
			}
			document.appendChild(bundles);
		  return Response.Builder.ok(document, "text/xml").build();
		} catch (Exception e) {
			return Response.Builder.serverError().build();
		}
	}
	
}
