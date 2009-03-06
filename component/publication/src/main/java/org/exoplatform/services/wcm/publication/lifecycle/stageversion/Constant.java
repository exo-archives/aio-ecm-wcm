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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Mar 4, 2009  
 */
public interface Constant {  
  public static final String CURRENT_STATE = "publication:currentState".intern();  
  public static final String ENROLLED = "enrolled".intern(); 
  public static final String DRAFT = "draft".intern();
  public static final String AWAITING = "awaiting".intern();
  public static final String LIVE = "live".intern();
  public static final String OBSOLETE = "obsolete".intern();  
  public static final String PUBLICATION_LIFECYCLE_TYPE = "publication:stateAndVersionBasedPublication".intern();
  public static final String LIFECYCLE_NAME = "States and versions based publication".intern();
  public static final String PUBLICATION_LIFECYCLE_NAME = "publication:lifecycleName".intern();
  public static final String MIX_VERSIONABLE = "mix:versionable".intern();
  public static final String HISTORY = "publication:history".intern();
}
