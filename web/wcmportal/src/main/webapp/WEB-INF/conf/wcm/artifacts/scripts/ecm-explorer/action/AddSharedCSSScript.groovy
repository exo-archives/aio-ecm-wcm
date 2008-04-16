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
 
import org.exoplatform.services.wcm.skin.XSkinService;
import org.exoplatform.services.cms.scripts.CmsScript;


public class AddSharedCSSScript implements CmsScript {

  private XSkinService eSkinService_ = null;      
  
  public AddSharedCSSScript(XSkinService extendedSkinService) {
    this.eSkinService_ = extendedSkinService ;
  }
  
  public void execute(Object context) {        
    String workspace = (String) ((Map) context).get("srcWorkspace") ;
    String repository = (String) ((Map) context).get("repository") ;
    String path = (String)((Map) context).get("nodePath");
    eSkinService_.makeSharedCSS(repository, workspace, path) ;
    eSkinService_.merge(repository, workspace, path) ;    
  }

  public void setParams(String[] params) {}
}
