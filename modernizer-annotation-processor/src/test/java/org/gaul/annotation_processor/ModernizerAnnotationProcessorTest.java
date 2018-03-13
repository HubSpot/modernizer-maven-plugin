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

package org.gaul.annotation_processor;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public final class ModernizerAnnotationProcessorTest {

    @Test
    public void checkAnnotatedClasses() throws IOException {
        List<String> classesList = Arrays.asList(
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest\\$TestClass(\\$.+)?",
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest\\$TestClass" +
                "\\$InnerTestClass(\\$.+)?",
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest" +
                "\\$TestGenericClass(\\$.+)?");
        File ignoreClassesFile = new File(System.getProperty("user.dir") +
            "/target/modernizer/test/ignore-annotated-classes.txt");
        BufferedReader br =
            new BufferedReader(new FileReader(ignoreClassesFile));
        String st;
        List<String> ignoredClasses = new ArrayList<String>();
        while ((st = br.readLine()) != null) {
            ignoredClasses.add(st);
        }
        assertThat(classesList).isEqualTo(ignoredClasses);
        assertThat(classesList).doesNotContain(
            "org/gaul/annotation_processor/ModernizerAnnotationProcessorTest" +
                "\\$TestClass\\$InnerClassNotToBeIgnored(\\$.+)?");
    }

    @Test
    public void checkAnnotatedMethods() throws IOException {
        List<String> methodsList = Arrays.asList(
            "org/gaul/annotation_processor/ModernizerAnnotationProcessorTest" +
                "\\$TestClass," +
                "<init>," +
                "Lorg/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest;",
            "org/gaul/annotation_processor/ModernizerAnnotationProcessorTest" +
                "\\$TestGenericClass," +
                "<init>," +
                "Lorg/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest;",
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest\\$TestGenericClass," +
                "testGenericMethod," +
                "Ljava/lang/Object;",
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest," +
                "testMethodEmptyParameters,",
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest," +
                "testMethodPrimitiveTypeParameters," +
                "IZBCSJFD",
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest," +
                "testMethodDeclaredTypeParameter," +
                "Lorg/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest/TestClass;",
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest," +
                "testMethodPrimitiveAndGenericTypeParameters," +
                "Ljava/util/List<java/lang/String>;F",
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest," +
                "testOverloadedMethod,",
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest," +
                "testArrayParameters," +
                "[Ljava/lang/String;[[I[Ljava/util/List<java/lang/Integer>;");
        File ignoreMethodsFile = new File(System.getProperty("user.dir") +
            "/target/modernizer/test/ignore-annotated-methods.txt");
        BufferedReader br =
            new BufferedReader(new FileReader(ignoreMethodsFile));
        String st;
        List<String> ignoredMethods = new ArrayList<String>();
        while ((st = br.readLine()) != null) {
            ignoredMethods.add(st);
        }
        assertThat(methodsList).isEqualTo(ignoredMethods);
        assertThat(methodsList).doesNotContain(
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest," +
                "testOverloadedMethod,I");
        assertThat(methodsList).doesNotContain(
            "org/gaul/annotation_processor/ModernizerAnnotationProcessorTest" +
                "\\$TestGenericClass," +
                "<init>," +
                "Lorg/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest;" +
                "Lorg/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessor$TestClass");
    }

    // Helper methods and classes

    @SuppressWarnings("modernizer")
    private class TestClass {

        @SuppressWarnings("modernizer")
        TestClass() {

        }

        @SuppressWarnings("modernizer")
        private class InnerTestClass {

        }

        private class InnerClassNotToBeIgnored {

        }
    }

    @SuppressWarnings("modernizer")
    private class TestGenericClass<E> {

        @SuppressWarnings("modernizer")
        TestGenericClass() {
        }

        TestGenericClass(TestClass obj) {
        }

        @SuppressWarnings("modernizer")
        public void testGenericMethod(E var) {
        }
    }

    @SuppressWarnings("modernizer")
    public void testMethodEmptyParameters() throws IOException {
    }

    @SuppressWarnings("modernizer")
    public void testMethodPrimitiveTypeParameters(
        int intVar,
        boolean booleanVar,
        byte byteVar,
        char charVar,
        short shortVar,
        long longVar,
        float floatVar,
        double doubleVar
    ) {
    }

    @SuppressWarnings("modernizer")
    public void testMethodDeclaredTypeParameter(TestClass obj) {
    }

    @SuppressWarnings("modernizer")
    public void testMethodPrimitiveAndGenericTypeParameters(
        List<String> list,
        float floatVar
    ) {
    }

    @SuppressWarnings("modernizer")
    public void testOverloadedMethod() {
    }

    public void testOverLoadedMethod(int intVar) {
    }

    @SuppressWarnings("modernizer")
    public void testArrayParameters(
        String[] strings,
        int[][] arrayOfArrays,
        List<Integer> [] listOfArrays
    ) {
    }
}
