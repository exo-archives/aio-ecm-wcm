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
package org.exoplatform.services.wcm.publication;

/**
 * This class is responsible to create a unique hashcode for a given message
 *
 * @author Benjamin Paillereau - benjamin.paillereau@exoplatform.com
 * @version 1.0
 */
public class MessageDigester {

    public static String getHash(String message) throws Exception {
        java.security.MessageDigest msgDigest = java.security.MessageDigest.getInstance("MD5");
        msgDigest.update(message.getBytes());
        byte[] aMessageDigest = msgDigest.digest();
        // C'est pas beau mais on ne l'a pas d�cid� ;-)
        StringBuffer ticket = new StringBuffer();
        ticket = new StringBuffer();
        String tmp = null;
        for (int i = 0; i < aMessageDigest.length; i++) {
            tmp = Integer.toHexString(0xFF & aMessageDigest[i]);
            if (tmp.length() == 2) {
                ticket.append(tmp);
            } else {
                ticket.append("0");
                ticket.append(tmp);
            }
        }
        return ticket.toString();
    }
}
