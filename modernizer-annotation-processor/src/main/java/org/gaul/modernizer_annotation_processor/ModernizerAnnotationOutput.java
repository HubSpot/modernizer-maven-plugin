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

package org.gaul.modernizer_annotation_processor;

import java.io.File;
import java.util.List;

import com.google.common.base.Joiner;

public final class ModernizerAnnotationOutput {

    public static final String IGNORE_CLASSES_FILE_NAME =
        "ignore-annotated-classes.txt";
    public static final String IGNORE_METHODS_FILE_NAME =
        "ignore-annotated-methods.txt";

    private ModernizerAnnotationOutput() { }

    public static File getOutputDir(File classOutputDir) {
        if (classOutputDir.getAbsolutePath().endsWith("/target/classes")) {
            return new File(classOutputDir.getParentFile(), "modernizer/main");
        } else if (classOutputDir.getAbsolutePath()
            .endsWith("/target/test-classes")) {
            return new File(classOutputDir.getParentFile(), "modernizer/test");
        }
        return classOutputDir;
    }

    /**
     * Returns the method representation by standardizing the format of
     * the processor output and ASM output in the plugin.
     * @param className Name of the class in which the method is present
     * @param methodName Name of the method
     * @param returnType Return type of the method
     * @param arguments A list of formal parameters of the method
     * @return A string concatenating the input parameters with spaces
     * Example:
     * Processor invocation
     * Input: {@code "ExampleTest$InnerClassTest", "<init>", "void",
     * "ExampleTest.InnerClassTest"}
     * Output: {@code "ExampleTest$InnerClassTest <init> void
     * ExampleTest.InnerClassTest" }
     *
     * Plugin invocation
     * Input: {@code "ExampleTest$InnerClassTest", "<init>", "void",
     * "ExampleTest$InnerClassTest"}
     * Output: {@code "ExampleTest$InnerClassTest <init> void
     * ExampleTest.InnerClassTest"}
     */
    public static String getMethodRep(
        String className,
        String methodName,
        String returnType,
        List<String> arguments
    ) {
        String returnTypeAndArguments = returnType +
            (!arguments.isEmpty() ? " " + Joiner.on(" ").join(arguments) : "");
        returnTypeAndArguments =
            returnTypeAndArguments.replace('$', '.').replace('/', '.');
        return className + " " + methodName + " " + returnTypeAndArguments;
    }
}
