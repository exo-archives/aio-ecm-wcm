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
package org.exoplatform.wcm.presentation.scp;

import org.exoplatform.webui.core.UIWizard;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Mar 26, 2008  
 */
public class UIContentWizard extends UIWizard {
  
  private int numberStep_ ; 
  
  public UIContentWizard() throws Exception {
  }
  
  public void setNumberSteps(int s) { numberStep_ = s ; }
  public int getNumberSteps() {return numberStep_ ; }
    
//  public String url(String name) throws Exception {
//    UIComponent renderedChild = getChild(getCurrentStep() - 1);
//    org.exoplatform.webui.config.Event event = config.getUIComponentEventConfig(name) ;
//    if(event != null && !(renderedChild instanceof UIForm)) return event(name) ;
//    return renderedChild.event(name) ;
//  }
  
  public static class AbortActionListener extends EventListener<UIContentWizard> {

    public void execute(Event<UIContentWizard> event) throws Exception {
      UIContentWizard uiWizard = event.getSource() ;
      UIPortletConfig uiConfig = uiWizard.getAncestorOfType(UIPortletConfig.class) ;
      uiConfig.getChildren().clear() ;
      uiConfig.addChild(uiConfig.getBackComponent()) ;
    }
    
  }

}
