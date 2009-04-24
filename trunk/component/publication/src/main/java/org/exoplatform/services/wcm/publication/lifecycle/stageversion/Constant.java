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
  //Publication lifecycle
  public static final String ENROLLED_STATE = "enrolled".intern(); 
  public static final String DRAFT_STATE = "draft".intern();
  public static final String AWAITING = "awaiting".intern();
  public static final String LIVE_STATE = "live".intern();
  public static final String OBSOLETE_STATE = "obsolete".intern();
  //publication lifecycle definition
  public static final String PUBLICATION_LIFECYCLE_TYPE = "publication:stateAndVersionBasedPublication".intern();
  public static final String LIFECYCLE_NAME = "States and versions based publication".intern();   
  public static final String LOCALIZATION = "artifacts.lifecycle.StageAndVersionPublication".intern();
  //history log description
  public static final String ENROLLED_TO_LIFECYCLE = "Publication.log.description.enrolled".intern();
  public static final String CHANGE_TO_DRAFT = "Publication.log.description.change-to-draft-state".intern();
  public static final String CHANGE_TO_AWAITNG = "Publication.log.description.change-to-awaiting-state".intern();
  public static final String CHANGE_TO_LIVE = "Publication.log.description.change-to-live-state".intern();
  public static final String CHANGE_TO_OBSOLETE = "Publication.log.description.change-to-obsolete-state".intern();
  //properties, nodetype
  public static final String PUBLICATION_LIFECYCLE_NAME = "publication:lifecycleName".intern();
  public static final String CURRENT_STATE = "publication:currentState".intern();
  public static final String MIX_VERSIONABLE = "mix:versionable".intern();
  public static final String HISTORY = "publication:history".intern();
  public static final String LIVE_REVISION_PROP = "publication:liveRevision".intern();
  public static final String LIVE_DATE_PROP = "publication:liveDate".intern();  
  public static final String REVISION_DATA_PROP = "publication:revisionData".intern();
  //context parameter name
  public static final String RUNTIME_MODE = "wcm.runtime.mode".intern();
  public static final String CURRENT_REVISION_NAME = "Publication.context.currentVersion".intern();
  //runtime site mode
  public static enum SITE_MODE {LIVE, EDITING};
  
}
