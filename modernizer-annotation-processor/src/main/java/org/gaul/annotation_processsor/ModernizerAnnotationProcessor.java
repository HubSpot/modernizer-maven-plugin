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
            List<List<String>> annotatedElements =
                getAnnotatedElements(roundEnv, annotation);
            makeAnnotatedElementsFiles(
                annotatedElements.get(0), annotatedElements.get(1));
        }
        return true;
    }

    private List<List<String>> getAnnotatedElements(
        RoundEnvironment roundEnv,
        TypeElement annotation
    ) {
        List<String> annotatedClasses = new ArrayList<String>();
        List<String> annotatedMethods = new ArrayList<String>();
        List<List<String>> annotatedElements = new ArrayList<List<String>>();
        for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
            List<String> warnings = Arrays.asList(
                    element.getAnnotation(SuppressWarnings.class).value());
            if (warnings.contains("modernizer")) {
                if (element.getKind().isClass()) {
                    annotatedClasses.add(getClassHeader(element));
                } else if (element.getKind().equals(ElementKind.METHOD) ||
                    element.getKind().equals(ElementKind.CONSTRUCTOR)) {
                    annotatedMethods.add(getMethodIdentifierString(element));
                }
            }
        }
        annotatedElements.add(annotatedClasses);
        annotatedElements.add(annotatedMethods);
        return annotatedElements;
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
        List<? extends TypeMirror> methodParams =
            methodElement.asType()
                .accept(executableTypeVisitor, null)
                .getParameterTypes();
        String fullClassPattern =
            getClassHeader(methodElement.getEnclosingElement());
        String fullClassName =
            fullClassPattern.substring(0, fullClassPattern.indexOf('('));
        String methodSignature = "";
        if (methodElement.getKind().equals(ElementKind.CONSTRUCTOR)) {
            int index = fullClassName.lastIndexOf("\\$");
            if (index != -1) {
                methodSignature +=
                    "L" + fullClassName.substring(0, index)
                        .replace("\\$", "/") + ";";
            }
        }
        methodSignature += getMethodSignature(methodParams);
        return fullClassName + "," +
            methodElement.getSimpleName().toString() + "," +
            methodSignature.replace("\\$", "$");
    }

    private String getMethodSignature(List<? extends TypeMirror> methodParams) {
        if (methodParams.isEmpty()) {
            return "";
        }
        String methodSignature = "";
        for (TypeMirror param : methodParams) {
            if (param.getKind().equals(TypeKind.TYPEVAR)) {
                methodSignature += "Ljava/lang/Object;";
                continue;
            } else if (param.getKind().equals(TypeKind.ARRAY)) {
                int count = CharMatcher.is('[').countIn(param.toString());
                String arrayString = Strings.repeat("[", count);
                String arrayType = getArrayType(param);
                methodSignature += arrayString + arrayType;
            } else {
                methodSignature += getParameterType(param);
            }
        }
        return methodSignature;
    }

    public final String getArrayType(TypeMirror param) {
        while (param.getKind().equals(TypeKind.ARRAY)) {
            param = ((ArrayType) param).getComponentType();
        }
        return getParameterType(param);
    }

    private String getParameterType(TypeMirror param) {
        if (param.getKind().equals(TypeKind.INT)) {
            return "I";
        } else if (param.getKind().equals(TypeKind.BOOLEAN)) {
            return "Z";
        } else if (param.getKind().equals(TypeKind.BYTE)) {
            return "B";
        } else if (param.getKind().equals(TypeKind.CHAR)) {
            return "C";
        } else if (param.getKind().equals(TypeKind.SHORT)) {
            return "S";
        } else if (param.getKind().equals(TypeKind.LONG)) {
            return "J";
        } else if (param.getKind().equals(TypeKind.FLOAT)) {
            return "F";
        } else if (param.getKind().equals(TypeKind.DOUBLE)) {
            return "D";
        } else {
            String paramString = param.toString();
            if (paramString.contains("<")) {
                paramString =
                    paramString.substring(0, paramString.indexOf('<'));
            }
            return "L" + paramString.replace('.', '/') + ";";
        }
    }
}
