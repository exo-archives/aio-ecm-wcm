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
package org.exoplatform.wcm.webui.formgenerator;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 23, 2009  
 */
public class UIFormGeneratorInputBean {
  
  private List<UIFormGeneratorInputBean> inputs;
  
  private String type;
  
  private String name;
  
  private String value;
  
  private String advanced;
  
  private String guildLine;
  
  private int size;
  
  private boolean mandatory;
  
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getGuildLine() {
    return guildLine;
  }

  public void setGuildLine(String guildLine) {
    this.guildLine = guildLine;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }

  public String getAdvanced() {
    return advanced;
  }

  public void setAdvanced(String advanced) {
    this.advanced = advanced;
  }

  public List<UIFormGeneratorInputBean> getInputs() {
    return inputs;
  }

  public void setInputs(List<UIFormGeneratorInputBean> inputs) {
    this.inputs = inputs;
  }
}
