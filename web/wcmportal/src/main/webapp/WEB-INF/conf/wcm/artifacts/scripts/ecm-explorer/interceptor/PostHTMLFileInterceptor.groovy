/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;

import org.exoplatform.services.cms.scripts.CmsScript;
import org.exoplatform.services.cms.records.RecordsService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.wcm.WcmService;

public class PostHTMLFileInterceptor implements CmsScript { 

  private WcmService wcmService_ ;

  public PostHTMLFileInterceptor(WcmService wcmService) {
    wcmService_ = wcmService; 
  }

  public void execute(Object context) {
    String path = (String) context;         		
    String[] splittedPath = path.split("&workspaceName=");
    String[] splittedContent = splittedPath[1].split("&repository=");      
    String repository = splittedContent[1];
    String worksapce = splittedContent[0] ;
    String nodepath = splittedPath[0];
    wcmService_.updateWebContentReference(repository,worksapce,nodepath) ;      
  }

  public void setParams(String[] params) {}

}