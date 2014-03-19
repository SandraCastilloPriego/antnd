/*
 * Copyright 2007-2012 
 * This file is part of MetModels.
 *
 * MetModels is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * MetModels is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * MetModels; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */
package ND.desktop.impl.helpsystem;

import java.util.Comparator;
import javax.help.TOCItem;

/**
 * @author Taken from MZmine2
 * http://mzmine.sourceforge.net/
 */
public class TOCItemSorterByName implements Comparator<TOCItem> {

        @Override
        public int compare(TOCItem dp1, TOCItem dp2) {
                String mz1 = dp1.getName();
                String mz2 = dp2.getName();
                int result = mz1.compareTo(mz2);
                if (result == 0) {
                        return dp1.getID().toString().compareTo(dp2.getID().toString());
                }
                return result;
        }
}
