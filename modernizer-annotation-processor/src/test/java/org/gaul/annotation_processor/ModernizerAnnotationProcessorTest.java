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
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public final class ModernizerAnnotationProcessorTest {

    private List<String> ignoreClasses = new ArrayList<String>();
    private List<String> ignoreMethods = new ArrayList<String>();

    @Before
    public void readIgnoreClassesAndMethodsFiles() throws IOException {
        File ignoreClassesFile = new File(System.getProperty("user.dir") +
            "/target/modernizer/test/ignore-annotated-classes.txt");
        File ignoreMethodsFile = new File(System.getProperty("user.dir") +
            "/target/modernizer/test/ignore-annotated-methods.txt");

        BufferedReader br =
            new BufferedReader(new FileReader(ignoreClassesFile));
        String line;
        while ((line = br.readLine()) != null) {
            ignoreClasses.add(line);
        }

        br = new BufferedReader(new FileReader(ignoreMethodsFile));
        ignoreMethods = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            ignoreMethods.add(line);
        }
    }

    @Test
    public void checkIgnoreClass() {
        String classHeader = "org/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest\\$TestClass(\\$.+)?";
        assertThat(ignoreClasses).contains(classHeader);
    }

    @Test
    public void checkIgnoreInnerClass() {
        String classHeader = "org/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest\\$TestClass" +
            "\\$InnerTestClass(\\$.+)?";
        assertThat(ignoreClasses).contains(classHeader);
    }

    @Test
    public void checkIgnoreGenericClass() {
        String classHeader = "org/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest\\$TestGenericClass(\\$.+)?";
        assertThat(ignoreClasses).contains(classHeader);
    }

    @Test
    public void checkInnerClassWithoutSuppressionNotIgnored() {
        String classHeader = "org/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest\\$TestClass" +
            "\\$InnerClassNotToBeIgnored(\\$.+)?";
        assertThat(ignoreClasses).doesNotContain(classHeader);
    }

    @Test
    public void checkIgnoreConstructor() {
        String method =
            "org/gaul/annotation_processor/ModernizerAnnotationProcessorTest" +
            "\\$TestClass,<init>,Lorg/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest;";
        assertThat(ignoreMethods).contains(method);
    }

    @Test
    public void checkIgnoreGenericClassConstructor() {
        String method =
            "org/gaul/annotation_processor/ModernizerAnnotationProcessorTest" +
            "\\$TestGenericClass,<init>,Lorg/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest;";
        assertThat(ignoreMethods).contains(method);
    }

    @Test
    public void checkIgnoreMethodInGenericClass() {
        String method =
            "org/gaul/annotation_processor/ModernizerAnnotationProcessorTest" +
            "\\$TestGenericClass,testGenericMethod,Ljava/lang/Object;";
        assertThat(ignoreMethods).contains(method);
    }

    @Test
    public void checkIgnoreMethodWithEmptyParameters() {
        String method =
            "org/gaul/annotation_processor/ModernizerAnnotationProcessorTest," +
            "testMethodEmptyParameters,";
        assertThat(ignoreMethods).contains(method);
    }

    @Test
    public void checkIgnoreMethodWithPrimitiveTypeParameters() {
        String method =
            "org/gaul/annotation_processor/ModernizerAnnotationProcessorTest," +
            "testMethodPrimitiveTypeParameters,IZBCSJFD";
        assertThat(ignoreMethods).contains(method);
    }

    @Test
    public void checkIgnoreMethodWithDeclaredTypeParameters() {
        String method =
            "org/gaul/annotation_processor/ModernizerAnnotationProcessorTest," +
            "testMethodDeclaredTypeParameter,Lorg/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest/TestClass;";
        assertThat(ignoreMethods).contains(method);
    }

    @Test
    public void checkIgnoreMethodWithPrimitiveAndGenericTypeParameters() {
        String method =
            "org/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest," +
            "testMethodPrimitiveAndGenericTypeParameters,Ljava/util/List;F";
        assertThat(ignoreMethods).contains(method);
    }

    @Test
    public void checkIgnoreOverloadedMethod() {
        String method =
            "org/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest,testOverloadedMethod,";
        assertThat(ignoreMethods).contains(method);
    }

    @Test
    public void checkIgnoreMethodWithArrayTypeParameters() {
        String method =
            "org/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest,testArrayParameters," +
            "[Ljava/lang/String;[[I[Ljava/util/List;";
        assertThat(ignoreMethods).contains(method);
    }

    @Test
    public void checkOverloadedMethodNotIgnored() {
        String method =
            "org/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest,testOverloadedMethod,I";
        assertThat(ignoreMethods).doesNotContain(method);
    }

    @Test
    public void checkOverloadedConstructorNotIgnored() {
        String method =
            "org/gaul/annotation_processor/ModernizerAnnotationProcessorTest" +
            "\\$TestGenericClass," +
            "<init>," +
            "Lorg/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessorTest;" +
            "Lorg/gaul/annotation_processor/" +
            "ModernizerAnnotationProcessor$TestClass";
        assertThat(ignoreMethods).doesNotContain(method);
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
