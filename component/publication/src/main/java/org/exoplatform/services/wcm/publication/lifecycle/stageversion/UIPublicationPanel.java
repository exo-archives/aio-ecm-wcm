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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.version.Version;
import javax.jcr.version.VersionIterator;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong_phan@exoplatform.com
 * Mar 2, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/wcm/webui/publication/lifecycle/stageversion/UIPublicationPanel.gtmpl"
)
public class UIPublicationPanel extends UIForm {

  private List<Version> versions = new ArrayList<Version>();

  public void initPanel(Node node) throws Exception {         
    VersionIterator versionIterator = node.getVersionHistory().getAllVersions();
    for (;versionIterator.hasNext();) {
      versions.add(versionIterator.nextVersion());
    }    
  }

  public List<Version> getVersions() {
    return versions;
  }

  public void setVersions(List<Version> versions) {
    this.versions = versions;
  }
}
