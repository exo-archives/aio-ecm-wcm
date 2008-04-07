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
package org.exoplatform.dms.model;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Apr 4, 2008  
 */
public class ContentStorePath {
  
  private String repository_ ;
  private String workspace_ ;
  private String path_ ;

  public String getPath() { return path_ ; }
  public void setPath(String p) { path_ = p ; }

  public String getRepository() { return repository_ ; }
  public void setRepository(String repo) { repository_ = repo ; }

  public String getWorkspace() { return workspace_ ; }
  public void setWorkspace(String ws) { workspace_ = ws ; }  

}
