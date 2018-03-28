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

package org.gaul.modernizer_maven_plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreConstructorTestClass;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreGenericClass;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreGenericClassConstructorTestClass;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodInGenericClassTest;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodReturningArrayClassTest;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodReturningArrayPrimitiveTypeClassTest;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodReturningDeclaredTypeClassTest;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodReturningGenericTypeClassTest;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodReturningPrimitiveTypeClassTest;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodWithArrayTypeParametersTestClass;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodWithDeclaredTypeParametersTestClass;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodWithEmptyParametersTestClass;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodWithGenericTypeParametersTestClass;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodWithPrimitiveAndGenericTypeParametersTestClass;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodWithPrimitiveTypeParametersTestClass;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreMethodWithVoidParameterTestClass;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreOverloadedMethodInGenericClassTest;
import org.gaul.modernizer_maven_plugin.ModernizerTestHelper
    .IgnoreOverloadedMethodTestClass;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

public final class ModernizerSuppressionsTest {
    private Map<String, Violation> violations;
    private static final Collection<String> NO_EXCLUSIONS =
        Collections.<String>emptySet();
    private static final Collection<Pattern> NO_EXCLUSION_PATTERNS =
        Collections.<Pattern>emptySet();
    private static final Collection<String> NO_IGNORED_PACKAGES =
        Collections.<String>emptySet();
    private static final Collection<String> NO_IGNORED_METHODS =
        Collections.<String>emptySet();
    private List<Pattern> ignoreClasses = new ArrayList<Pattern>();
    private List<String> ignoreMethods = new ArrayList<String>();

    @Before
    public void setUp() throws Exception {
        violations = ModernizerTestUtils.readViolations();
        String currentDirectory = System.getProperty("user.dir");
        String ignoreClassesFilePath = currentDirectory +
            "/target/modernizer/test/ignore-annotated-classes.txt";
        String ignoreMethodsFilePath = currentDirectory +
            "/target/modernizer/test/ignore-annotated-methods.txt";
        for (String ignoreClass : readExclusionsFile(ignoreClassesFilePath)) {
            ignoreClasses.add(Pattern.compile(ignoreClass));
        }
        ignoreMethods.addAll(readExclusionsFile(ignoreMethodsFilePath));
    }

    public Collection<String> readExclusionsFile(String filePath) {
        InputStream is = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                is = new FileInputStream(filePath);
            } else {
                is = this.getClass().getClassLoader().getResourceAsStream(
                    filePath);
            }
            if (is == null) {
                throw new RuntimeException(
                    "Could not find exclusion file: " +
                        filePath);
            }

            return Utils.readAllLines(is);
        } catch (IOException ioe) {
            throw new RuntimeException(
                "Error reading exclusion file: " +
                    filePath, ioe);
        } finally {
            Utils.closeQuietly(is);
        }
    }

    public Collection<ViolationOccurrence> getViolationsInMethods(
        String className
    ) throws Exception {
        ClassReader cr = new ClassReader(className);
        Modernizer modernizer = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS,
            NO_EXCLUSION_PATTERNS, NO_IGNORED_PACKAGES,
            NO_EXCLUSION_PATTERNS, ignoreMethods);
        return modernizer.check(cr);
    }

    public Collection<ViolationOccurrence> getViolationsInClasses(
        String className
    ) throws Exception {
        ClassReader cr = new ClassReader(className);
        Modernizer modernizer = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS,
            NO_EXCLUSION_PATTERNS, NO_IGNORED_PACKAGES,
            ignoreClasses, NO_IGNORED_METHODS);
        return modernizer.check(cr);
    }

    @Test
    public void checkIgnoreMethodWithEmptyParameters() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodWithEmptyParametersTestClass.class.getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodWithVoidParameter() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodWithVoidParameterTestClass.class.getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodWithPrimitiveTypeParameters()
    throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodWithPrimitiveTypeParametersTestClass.class.getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreOverloadedConstructor() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreConstructorTestClass.class.getName())
        ).hasSize(1);
    }

    @Test
    public void checkIgnoreMethodWithGenericTypeParameters() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodWithGenericTypeParametersTestClass.class.getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodWithDeclaredTypeParameter() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodWithDeclaredTypeParametersTestClass.class.getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodWithPrimitiveAndGenericParameters()
    throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodWithPrimitiveAndGenericTypeParametersTestClass.class
            .getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreOverloadedMethod() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreOverloadedMethodTestClass.class.getName())
        ).hasSize(1);
    }

    @Test
    public void checkIgnoreMethodWithArrayTypeParameters() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodWithArrayTypeParametersTestClass.class.getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreGenericClassConstructor() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreGenericClassConstructorTestClass.class.getName())
        ).hasSize(1);
    }

    @Test
    public void checkIgnoreMethodInGenericClass() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodInGenericClassTest.class.getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreOverloadedMethodInGenericClass() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreOverloadedMethodInGenericClassTest.class.getName())
        ).hasSize(1);
    }

    @Test
    public void checkIgnoreGenericClass() throws Exception {
        assertThat(getViolationsInClasses(
            IgnoreGenericClass.class.getName()
        )).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodReturningArrayOfDeclaredType()
    throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodReturningArrayClassTest.class.getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodReturningArrayOfPrimitiveType()
    throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodReturningArrayPrimitiveTypeClassTest.class.getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodReturningPrimitiveType() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodReturningPrimitiveTypeClassTest.class.getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodReturningDeclaredType() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodReturningDeclaredTypeClassTest.class.getName())
        ).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodReturningGenericType() throws Exception {
        assertThat(getViolationsInMethods(
            IgnoreMethodReturningGenericTypeClassTest.class.getName())
        ).hasSize(0);
    }
}
