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

package com.hubspot.annotation.processor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;

@SupportedAnnotationTypes("java.lang.SuppressWarnings")
@AutoService(Processor.class)
public class ModernizerAnnotationProcessor extends AbstractProcessor {

    @Override
    public final SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public final boolean process(
        Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement annotation : annotations) {
            List<String> annotatedClasses =
                getAnnotatedClasses(roundEnv, annotation);
            makeAnnotatedClassesFile(annotatedClasses);
        }
        return true;
    }

    private List<String> getAnnotatedClasses(
        RoundEnvironment roundEnv, TypeElement annotation) {
        List<String> annotatedClasses = new ArrayList<String>();
        for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {
            List<String> warnings = Arrays.asList(
                    element.getAnnotation(SuppressWarnings.class).value());
            if (warnings.contains("modernizer")) {
                if (element.getKind().isClass()) {
                    annotatedClasses.add(getClassHeader(element));
                }
            }
        }
        return annotatedClasses;
    }

    private void makeAnnotatedClassesFile(List<String> annotatedClasses) {
        FileWriter writer = null;
        try {
            File dir = new File(getFilePath(), "modernizer");
            if (!dir.exists()) {
                dir.mkdir();
            }
            File file = new File(dir.getPath(),
                "modernizer-ignore-annotated-classes.txt");
            writer = new FileWriter(file);
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

    private String getFilePath() {
        try {
            FileObject fileObjectToGetPath =
                processingEnv.getFiler().createResource(
                    StandardLocation.CLASS_OUTPUT,
                    "",
                    "dummy-file.txt",
                    null);
            String path = fileObjectToGetPath.getName();
            fileObjectToGetPath.delete();
            while (!path.endsWith("target")) {
                path = path.substring(0, path.lastIndexOf("/"));
            }
            return path;
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
        String classHeader;
        if (!packageName.isEmpty()) {
            classHeader =
                packageName + "/" + getFullClassName(classElement);
        } else {
            classHeader = getFullClassName(classElement);
        }
        return classHeader.replace("$", "\\$") + "(\\$.+)?";
    }

    private String getFullClassName(Element classElement) {
        List<String> parentClasses = new ArrayList<String>();
        Element enclosingElement = classElement.getEnclosingElement();
        while (enclosingElement.getKind().isClass()) {
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
}
