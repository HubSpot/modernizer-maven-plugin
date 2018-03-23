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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;
import com.google.common.base.Strings;

@SupportedAnnotationTypes("java.lang.SuppressWarnings")
@AutoService(Processor.class)
public class ModernizerAnnotationProcessor extends AbstractProcessor {

    @Override
    public final SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    private class AnnotatedElements {
        private List<String> annotatedClasses;
        private List<String> annotatedMethods;

        AnnotatedElements() {
            annotatedClasses = new ArrayList<String>();
            annotatedMethods = new ArrayList<String>();
        }

        public List<String> getAnnotatedClasses() {
            return annotatedClasses;
        }

        public void setAnnotatedClasses(List<String> annotatedClasses) {
            this.annotatedClasses = annotatedClasses;
        }

        public List<String> getAnnotatedMethods() {
            return annotatedMethods;
        }

        public void setAnnotatedMethods(List<String> annotatedMethods) {
            this.annotatedMethods = annotatedMethods;
        }
    }

    @Override
    public final boolean process(
        Set<? extends TypeElement> annotations,
        RoundEnvironment roundEnv
    ) {
        for (TypeElement annotation : annotations) {
            AnnotatedElements annotatedElements =
                getAnnotatedElements(roundEnv, annotation);
            if (!(annotatedElements.getAnnotatedClasses().isEmpty() &&
                annotatedElements.getAnnotatedMethods().isEmpty())) {
                File outputDir = getOutputDirectory();
                outputDir.mkdirs();
                makeFile(new File(outputDir,
                    ModernizerAnnotationOutput.IGNORE_CLASSES_FILE_NAME),
                    annotatedElements.getAnnotatedClasses());
                makeFile(new File(outputDir,
                    ModernizerAnnotationOutput.IGNORE_METHODS_FILE_NAME),
                    annotatedElements.getAnnotatedMethods());
            }
        }
        return true;
    }

    private AnnotatedElements getAnnotatedElements(
        RoundEnvironment roundEnv,
        TypeElement annotation
    ) {
        AnnotatedElements elements = new AnnotatedElements();
        List<String> annotatedClasses = new ArrayList<String>();
        List<String> annotatedMethods = new ArrayList<String>();
        for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
            List<String> warnings = Arrays.asList(
                    element.getAnnotation(SuppressWarnings.class).value());
            if (warnings.contains("modernizer")) {
                if (element.getKind().isClass()) {
                    annotatedClasses.add(
                        getClassHeaderRegex(element));
                } else if (element.getKind().equals(ElementKind.METHOD) ||
                    element.getKind().equals(ElementKind.CONSTRUCTOR)) {
                    annotatedMethods.add(getMethodIdentifierString(element));
                }
            }
        }
        elements.setAnnotatedClasses(annotatedClasses);
        elements.setAnnotatedMethods(annotatedMethods);
        return elements;
    }


    private void makeFile(
        File file,
        List<String> annotatedElements
    ) {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (String element : annotatedElements) {
                writer.write(element + "\n");
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private File getOutputDirectory() {
        try {
            FileObject fileObjectToGetPath =
                processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "dummy-file.txt");
            File outputDir =
                new File(fileObjectToGetPath.getName()).getParentFile();
            return ModernizerAnnotationOutput.getOutputDir(outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /* Return class header in a regex format
     * to perform necessary checks in the plugin
     */
    private String getClassHeaderRegex(Element classElement) {
        return getClassHeader(classElement).replace("$", "\\$") + "(\\$.+)?";
    }

    private String getClassHeader(Element classElement) {
        String packageName = processingEnv
            .getElementUtils()
            .getPackageOf(classElement)
            .getQualifiedName()
            .toString()
            .replace('.', '/');
        String packagePrefix = !packageName.isEmpty() ? packageName + "/" : "";
        String classHeader = packagePrefix + getFullClassName(classElement);
        return classHeader;
    }

    private String getFullClassName(Element classElement) {
        List<String> parentClasses = new ArrayList<String>();
        Element enclosingElement = classElement.getEnclosingElement();
        while (enclosingElement != null &&
            enclosingElement.getKind().isClass()) {
            parentClasses.add(enclosingElement.getSimpleName().toString());
            enclosingElement = enclosingElement.getEnclosingElement();
        }
        StringBuilder className = new StringBuilder();
        for (int index = parentClasses.size() - 1; index >= 0; index--) {
            className.append(parentClasses.get(index));
            className.append("$");
        }
        return className.toString() + classElement.getSimpleName();
    }

    private String getMethodIdentifierString(Element methodElement) {
        TypeVisitor<ExecutableType, Void> executableTypeVisitor =
            new SimpleTypeVisitor6<ExecutableType, Void>() {
                @Override
                public ExecutableType visitExecutable(
                    ExecutableType executableType, Void obj
                ) {
                    return executableType;
                }
            };
        ExecutableType method =
            methodElement.asType().accept(executableTypeVisitor, null);
        List<? extends TypeMirror> methodParams = method.getParameterTypes();

        /* Fetching the return type of the method in the format ASM
         * parses the return type in a method descriptor.
         */
        TypeMirror returnType = method.getReturnType();
        String returnTypeString = "";
        if (returnType.getKind().equals(TypeKind.ARRAY)) {
            returnTypeString = getArray(returnType);
        } else {
            returnTypeString = getParameter(returnType);
        }

        String methodArguments = "";

        /* For an inner class constructor, adding the outer class object as an
         * argument.
         * Example:
         * fullClassName = "org.gaul.example_package.OuterClass$InnerClass"
         * methodArguments += "org.gaul.example_package.OuterClass"
         */
        String fullClassName =
            getClassHeader(methodElement.getEnclosingElement());
        if (methodElement.getKind().equals(ElementKind.CONSTRUCTOR)) {
            int index = fullClassName.lastIndexOf("$");
            if (index != -1) {
                methodArguments += fullClassName.substring(0, index) + " ";
            }
        }

        methodArguments += getMethodArgumentTypes(methodParams);
        return ModernizerAnnotationOutput.getMethodRep(
            fullClassName, methodElement.getSimpleName().toString(),
            returnTypeString, methodArguments);
    }

    /* Fetching the method arguments in the format the way ASM parses a method
     * descriptor
     */
    private String getMethodArgumentTypes(
        List<? extends TypeMirror> methodParams
    ) {
        if (methodParams.isEmpty()) {
            return "";
        }
        StringBuilder methodArguments = new StringBuilder("");
        for (TypeMirror param : methodParams) {
            String arg;
            if (param.getKind().equals(TypeKind.ARRAY)) {
                arg = getArray(param);
            } else {
                arg = getParameter(param);
            }
            methodArguments.append(arg);
            methodArguments.append(" ");
        }
        return methodArguments.toString();
    }

    /* Fetching the parameter string and returning it along with the
     * array dimensions. This is done to format it the way ASM parses it.
     * Example:
     * #1 - Genreric type
     * Input: "java.util.List<String>[]"
     * paramType = "java.util.List<String>"
     * Output: "java.lang.List[][]"
     * #2 - Type Varaiable
     * Input: "E[]"
     * paramType = "E"
     * Output: "java.lang.Object[]"
     * #3 - Primitive type
     * Input: "int[]"
     * paramType = "int"
     * Output: "int[]"
     */
    public final String getArray(TypeMirror param) {
        int arrayLength = 0;
        TypeMirror paramType = param;
        while (paramType.getKind().equals(TypeKind.ARRAY)) {
            paramType = ((ArrayType) paramType).getComponentType();
            arrayLength++;
        }
        return getParameter(paramType) +
            Strings.repeat("[]", arrayLength);
    }

    /* Fetching the parameter string by formatting type variables and
     * generic types the way ASM parses.
     * This is done to perform the necessary checks against the ASM parsed
     * parameters in the plugin.
     * Example:
     * #1 - Generic type
     * Input: "java.lang.List<String>"
     * Output: "java.lang.List"
     * #2 - Type Varaiable
     * Input: "E"
     * Output: "java.lang.Object"
     * #3 - Primitive type
     * Input: "int"
     * Output: "int"
     */
    public final String getParameter(TypeMirror param) {
        if (param.getKind().equals(TypeKind.TYPEVAR)) {
            return Object.class.getName();
        } else {
            String paramString = param.toString();
            if (paramString.contains("<")) {
                return paramString.substring(0, paramString.indexOf("<"));
            }
            return paramString;
        }
    }
}
