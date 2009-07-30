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
package org.exoplatform.services.wcm.images;

import java.awt.BorderLayout;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Nguyen Ngoc
 *          ngoc.tran@exoplatform.com
 * Jul 29, 2009  
 */
public class test {

  public static void main(String [] args) {
    Image image = null;
    try {
        // Read from a file
        File file = new File("src/test/resources/08_resize.jpg");
        image = ImageIO.read(file);
    
        // Read from an input stream
        InputStream is = new BufferedInputStream(
            new FileInputStream("src/test/resources/08_resize.jpg"));
        image = ImageIO.read(is);
    
        // Read from a URL
        URL url = new URL("http://images8.dantri.com.vn/ThumbImages/Uploaded/2009/07/25/ts_190709_104.jpg");
        image = ImageIO.read(url);
    } catch (IOException e) {
    }
    
    // Use a label to display the image
    JFrame frame = new JFrame();
    JLabel label = new JLabel(new ImageIcon(image));
    frame.getContentPane().add(label, BorderLayout.CENTER);
    frame.pack();
    frame.setVisible(true);

  }
  

}
