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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.auto.service.AutoService;

@SupportedAnnotationTypes("java.lang.SuppressWarnings")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor.class)
public class ModernizerAnnotationProcessor extends AbstractProcessor {
    @Override
    public final boolean process(
        Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<String> annotatedElements;
        for (TypeElement annotation : annotations) {
            annotatedElements = getAnnotatedClasses(roundEnv, annotation);
            makeAnnotatedClassesFile(annotatedElements);
        }
        return true;
    }

    private List<String> getAnnotatedClasses(RoundEnvironment roundEnv,
                                             TypeElement annotation) {
        List<String> annotatedClasses = new ArrayList<>();
        for (Element element :
            roundEnv.getElementsAnnotatedWith(annotation)) {
            String[] elementValue = element
                .getAnnotation(SuppressWarnings.class)
                .value();
            if (elementValue.length == 1 &&
                elementValue[0].equals("modernizer")) {
                if (element.getKind().isClass()) {
                    annotatedClasses.add(getClassHeader(element));
                }
            }
        }
        return annotatedClasses;
    }

    private void makeAnnotatedClassesFile(List<String> annotatedClasses) {
        FileObject fileObject;
        Writer writer;
        try {
            fileObject = processingEnv.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT, "",
                "ModernizerIgnoreAnnotatedElements.txt",
                null);
            writer = fileObject.openWriter();
            for (String element : annotatedClasses) {
                writer.write(element + "\n");
            }
            writer.close();
        } catch (IOException e) {
            processingEnv.getMessager().
                printMessage(Kind.ERROR, e.getMessage());
        }
    }

    private String getClassHeader(Element classElement) {
        String classHeader =
            processingEnv.getElementUtils().
                getPackageOf(classElement).getQualifiedName()
                .toString().replace('.', '/');
        String className = classElement.getSimpleName().toString();
        classHeader += "/" + getParentClasses(classElement) + className;
        processingEnv.getMessager().
            printMessage(Kind.NOTE, classHeader.replace("$", "\\$"));
        return classHeader.replace("$", "\\$") + "(\\$.+)?";
    }

    private String getParentClasses(Element classElement) {
        List<String> parentClasses = new ArrayList<>();
        while (classElement.getEnclosingElement().getKind()
            .toString().equals("CLASS")) {
            parentClasses.add(classElement.getEnclosingElement()
                .getSimpleName().toString());
            classElement = classElement.getEnclosingElement();
        }
        StringBuilder parentClassNames = new StringBuilder();
        for (int index = parentClasses.size() - 1; index >= 0; index--) {
            parentClassNames.append(parentClasses.get(index) + "$");
        }
        return parentClassNames.toString();
    }
}
