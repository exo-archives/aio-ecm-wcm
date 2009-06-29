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
package org.exoplatform.wcm.webui.pclv;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          ngoc.tran@exoplatform.com
 * Jun 23, 2009  
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class, 
                 template = "app:/groovy/ParameterizedContentListViewer/UIParameterizedContentListViewerForm.gtmpl"
               )
public class UIParameterizedContentListViewerForm extends UIForm {

  private List<PCLVViewerConfig> listPCLVConfig;
  
  public UIParameterizedContentListViewerForm(){
    PCLVViewerConfig pclvConfig = null;
    listPCLVConfig = new ArrayList<PCLVViewerConfig>();
    for(int i = 0; i < 4; i++) {
      pclvConfig = new PCLVViewerConfig();
      pclvConfig.setTitle("This is the title of PCLV");
      pclvConfig.setIllustrationSumary("This is the illustration of PCLV");
      pclvConfig.setIllustrationImage("This is the illustration image");

      listPCLVConfig.add(pclvConfig);
    }
  }
}
