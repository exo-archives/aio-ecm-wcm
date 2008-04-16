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

import org.exoplatform.services.wcm.javascript.XJavascriptService;
import org.exoplatform.services.cms.scripts.CmsScript;

public class AddSharedJSScript implements CmsScript {

  private XJavascriptService eJavascriptService_;
  
  public AddSharedJSScript(XJavascriptService extendedJavascriptService) {                        
    this.eJavascriptService_ = extendedJavascriptService;
  }

  public void execute(Object context) {        
      String workspace = (String) ((Map) context).get("srcWorkspace") ;
      String repository = (String) ((Map) context).get("repository") ;
      String javascriptPath = (String)((Map) context).get("nodePath") ;
      eJavascriptService_.makeSharedJavascript(repository,workspace,javascriptPath);
      eJavascriptService_.mergeJavascript(repository,workspace,javascriptPath);
  }

  public void setParams(String[] params) {}
}
