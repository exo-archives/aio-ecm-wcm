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

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.phamvu@exoplatform.com
 * Apr 1, 2009
 */
public class StageAndVersionPublicationState {
  
  /** The Constant ENROLLED. */
  public static final String ENROLLED = "enrolled".intern(); 
  
  /** The Constant DRAFT. */
  public static final String DRAFT = "draft".intern();
  
  /** The Constant AWAITING. */
  public static final String AWAITING = "awaiting".intern();
  
  /** The Constant LIVE. */
//  public static final String LIVE = "live".intern();
  
  /** The Constant LIVE. */
  public static final String PUBLISHED = "published".intern();
  
  /** The Constant OBSOLETE. */
  public static final String OBSOLETE = "obsolete".intern();

  /**
   * Gets the revision state.
   * 
   * @param currentNode the current node
   * 
   * @return the revision state
   */
  public static String getRevisionState(Node currentNode) {
    String currentState = null;
    try {
      currentState = currentNode.getProperty("publication:currentState").getString();
    } catch (Exception e) {
    } 
    if(DRAFT.equals(currentState))
      return DRAFT;
    if (PUBLISHED.equals(currentState))
    	return PUBLISHED;
    if(ENROLLED.equals(currentState)) {
      return OBSOLETE;
    }
    return null;
  }
}
