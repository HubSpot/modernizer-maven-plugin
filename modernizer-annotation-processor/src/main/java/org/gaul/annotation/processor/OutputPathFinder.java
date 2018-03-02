/*
 * Copyright 2014-2018 Andrew Gaul <andrew@gaul.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gaul.annotation.processor;

import java.io.File;

public final class OutputPathFinder {

    private static final String[] FILE_LOCATIONS =
        {"/target/classes", "/target/test-classes"};

    private OutputPathFinder() { }

    public static String getOutputPath(String outputDir) {
        String requiredPath = outputDir;
        if (!checkIfPathEndsWithFileLocations(requiredPath)) {
            return requiredPath;
        }
        File parentFile = new File(requiredPath).getParentFile();
        while (!parentFile.getName().equals("target")) {
            parentFile = parentFile.getParentFile();
        }
        return parentFile.getName();
    }

    private static boolean checkIfPathEndsWithFileLocations(String path) {
        for (int i = 0; i < FILE_LOCATIONS.length; i++) {
            if (path.endsWith(FILE_LOCATIONS[i])) {
                return true;
            }
        }
        return false;
    }
}
