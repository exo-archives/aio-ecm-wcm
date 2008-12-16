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
package org.exoplatform.wcm.webui;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.phamvu@exoplatform.com
 * Dec 13, 2008  
 */
public class WebUIPropertiesConfigService {  
  public final static String SCV_POPUP_SIZE_EDIT_PORTLET_MODE = "SCV.popup.size.in.edit.portlet.mode".intern();
  public final static String SCV_POPUP_SIZE_QUICK_EDIT = "SCV.popup.size.in.quickdedit".intern();
  public final static String CLV_POPUP_SIZE_EDIT_PORTLET_MODE = "CLV.popup.size.in.edit.portlet.mode".intern();
  public final static String CLV_POPUP_SIZE_QUICK_EDIT = "CLV.popup.size.in.quickedit".intern(); 

  private ConcurrentHashMap<String,Object> propertiesMap = new ConcurrentHashMap<String,Object>();

  public WebUIPropertiesConfigService(InitParams params) {    
    for(Iterator iterator = params.getPropertiesParamIterator();iterator.hasNext();) {
      PropertiesParam propertiesParam = (PropertiesParam)iterator.next();
      if(SCV_POPUP_SIZE_EDIT_PORTLET_MODE.equalsIgnoreCase(propertiesParam.getName())) {
        PopupWindowProperties properties = readPropertiesFromXML(propertiesParam);
        propertiesMap.put(SCV_POPUP_SIZE_EDIT_PORTLET_MODE,properties);
      }else if(SCV_POPUP_SIZE_QUICK_EDIT.equals(propertiesParam.getName())) {
        PopupWindowProperties properties = readPropertiesFromXML(propertiesParam);
        propertiesMap.put(SCV_POPUP_SIZE_QUICK_EDIT,properties);
      }else if(CLV_POPUP_SIZE_QUICK_EDIT.equals(propertiesParam.getName())) {
        PopupWindowProperties properties = readPropertiesFromXML(propertiesParam);
        propertiesMap.put(CLV_POPUP_SIZE_QUICK_EDIT,properties);
      }else if(CLV_POPUP_SIZE_EDIT_PORTLET_MODE.equals(propertiesParam.getName())) {
        PopupWindowProperties properties = readPropertiesFromXML(propertiesParam);
        propertiesMap.put(CLV_POPUP_SIZE_EDIT_PORTLET_MODE,properties);
      }       
    }
  }  
  
  public Object getProperties(String name) {
    return propertiesMap.get(name);
  }
  
  private PopupWindowProperties readPropertiesFromXML(PropertiesParam param) {
    PopupWindowProperties properties = new PopupWindowProperties();
    String width = param.getProperty(PopupWindowProperties.WIDTH);
    String height = param.getProperty(PopupWindowProperties.HEIGHT);
    if(width != null && StringUtils.isNumeric(width)) {
      properties.setWidth(Integer.parseInt(width));
    }
    if(height != null && StringUtils.isNumeric(height)) {
      properties.setHeight(Integer.parseInt(height));
    }
    return properties;
  }

  public static class PopupWindowProperties {
    public final static String WIDTH = "width".intern();
    public final static String HEIGHT = "height".intern();
    
    private int width = 500;
    private int height = 300;

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width;}

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }    
  }
}
