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

package org.gaul.annotation_processsor;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;
import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

@SupportedAnnotationTypes("java.lang.SuppressWarnings")
@AutoService(Processor.class)
public class ModernizerAnnotationProcessor extends AbstractProcessor {

    @Override
    public final SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public final boolean process(
        Set<? extends TypeElement> annotations,
        RoundEnvironment roundEnv
    ) {
        for (TypeElement annotation : annotations) {
            List<String> annotatedClasses = new ArrayList<String>();
            List<String> annotatedMethods = new ArrayList<String>();
            getAnnotatedElements(roundEnv, annotation,
                annotatedClasses, annotatedMethods);
            makeAnnotatedElementsFiles(annotatedClasses, annotatedMethods);
        }
        return true;
    }

    private void getAnnotatedElements(
        RoundEnvironment roundEnv,
        TypeElement annotation,
        List<String> annotatedClasses,
        List<String> annotatedMethods
    ) {
        for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
            List<String> warnings = Arrays.asList(
                    element.getAnnotation(SuppressWarnings.class).value());
            if (warnings.contains("modernizer")) {
                if (element.getKind().isClass()) {
                    annotatedClasses.add(getClassHeader(element));
                } else if (element.getKind().toString().equals("METHOD") ||
                    element.getKind().toString().equals("CONSTRUCTOR")) {
                    annotatedMethods.add(getMethod(element));
                }
            }
        }
    }

    private void makeAnnotatedElementsFiles(
        List<String> annotatedClasses,
        List<String> annotatedMethods
    ) {
        if (annotatedClasses.isEmpty() && annotatedMethods.isEmpty()) {
            return;
        }
        File outputDir = getOutputDirectory();
        outputDir.mkdirs();
        if (!annotatedClasses.isEmpty()) {
            makeAnnotatedClassesFile(outputDir, annotatedClasses);
        }
        if (!annotatedMethods.isEmpty()) {
            makeAnnotatedMethodsFile(outputDir, annotatedMethods);
        }
    }

    private void makeAnnotatedClassesFile(
        File outputDir,
        List<String> annotatedClasses
    ) {
        File file = new File(
            outputDir,
            ModernizerAnnotationOutput.IGNORE_CLASSES_FILE_NAME);
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (String element : annotatedClasses) {
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

    private void makeAnnotatedMethodsFile(
        File outputDir,
        List<String> annotatedMethods
    ) {
        File file = new File(
            outputDir,
            ModernizerAnnotationOutput.IGNORE_METHODS_FILE_NAME);
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            for (String element : annotatedMethods) {
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

    private String getClassHeader(Element classElement) {
        String packageName = processingEnv
            .getElementUtils()
            .getPackageOf(classElement)
            .getQualifiedName()
            .toString()
            .replace('.', '/');
        String packagePrefix = !packageName.isEmpty() ? packageName + "/" : "";
        String classHeader = packagePrefix + getFullClassName(classElement);
        return classHeader.replace("$", "\\$") + "(\\$.+)?";
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

    private String getMethod(Element element) {
        ExecutableType emeth = (ExecutableType) element.asType();
        List<? extends TypeMirror> methodParams = emeth.getParameterTypes();
        String fullClassPattern =
            getClassHeader(element.getEnclosingElement());
        String fullClassName =
            fullClassPattern.substring(0, fullClassPattern.indexOf('('));
        String methodSignature = "";
        if (element.getKind().toString().equals("CONSTRUCTOR")) {
            int index = fullClassName.lastIndexOf("\\$");
            if (index != -1) {
                methodSignature +=
                    "L" + fullClassName.substring(0, index) + ";";
            }
        }
        methodSignature += getMethodSignature(methodParams);
        String methodName = element.getSimpleName().toString();
        return fullClassName + "," +
            methodName + "," +
            methodSignature.replace("\\$", "$");
    }

    private String getMethodSignature(List<? extends TypeMirror> methodParams) {
        if (methodParams.isEmpty()) {
            return "";
        }
        String methodSignature = "";
        for (TypeMirror param : methodParams) {
            if (param.getKind().name().equals("TYPEVAR")) {
                methodSignature += "Ljava/lang/Object;";
                continue;
            }
            String paramString = param.toString();
            if (paramString.indexOf('[') == -1) {
                methodSignature += getParameterType(paramString);
            } else {
                int count = CharMatcher.is('[').countIn(paramString);
                String array = Strings.repeat("[", count);
                String arrayType =
                    getParameterType(
                        paramString.substring(0, paramString.indexOf('[')));
                methodSignature += array + arrayType;
            }
        }
        return methodSignature;
    }

    private String getParameterType(String paramString) {
        if (paramString.equals("int")) {
            return "I";
        } else if (paramString.equals("boolean")) {
            return "Z";
        } else if (paramString.equals("byte")) {
            return "B";
        } else if (paramString.equals("char")) {
            return "C";
        } else if (paramString.equals("short")) {
            return "S";
        } else if (paramString.equals("long")) {
            return "J";
        } else if (paramString.equals("float")) {
            return "F";
        } else if (paramString.equals("double")) {
            return "D";
        } else {
            return "L" + paramString.replace('.', '/') + ";";
        }
    }
}
