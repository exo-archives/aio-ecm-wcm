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
package org.exoplatform.wcm.presentation.acp.config;

import javax.portlet.PortletMode;

import org.exoplatform.wcm.presentation.acp.config.advanced.UIContentCreationWizard;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIWizard;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 *          dzungdev@gmail.com
 * May 27, 2008  
 */
public abstract class UIBaseWizard extends UIWizard {
  protected final String EXO_WEB_CONTENT = "exo:webContent".intern();
  protected final String EXO_JS_FILE = "exo:jsFile".intern();
  private int numberStep;
  public UIBaseWizard() throws Exception { }
  public void setNumberSteps(int number) { numberStep = number; }
  public int getNumberSteps() { return numberStep; }

  public String url(String name) throws Exception {
    if ("Back".equals(name)) return event(name);
    return super.url(name);
  }

  public abstract String[] getActionsByStep();

  public static class AbortActionListener extends EventListener<UIBaseWizard> {
    public void execute(Event<UIBaseWizard> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.VIEW);
    }
  }

  public static class BackActionListener extends EventListener<UIContentCreationWizard> {
    public void execute(Event<UIContentCreationWizard> event) throws Exception {
      UIWizard uiWizard = event.getSource();
      uiWizard.viewStep(uiWizard.getCurrentStep() - 1);
    }
  }
}
