/*
 *  Copyright Technophobia Ltd 2012
 *
 *   This file is part of Substeps.
 *
 *    Substeps is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    Substeps is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public License
 *    along with Substeps.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.technophobia.substeps.runner.syntax;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author ian
 */
public class FileUtils {

    private FileUtils(){
        // uninstantiable
    }

    public static Collection<File> getFiles(final File fFile, final String extension) {

        //  the parameter might be a dir or a single file
        final Collection<File> files;
        if (fFile.isFile()){
            List<File> fileList = new ArrayList<>();
            fileList.add(fFile);
            files = Collections.unmodifiableCollection(fileList);
        }
        else {
            files = org.apache.commons.io.FileUtils.listFiles(fFile, new String[]{extension}, true);
        }

      return files;
    }
}
