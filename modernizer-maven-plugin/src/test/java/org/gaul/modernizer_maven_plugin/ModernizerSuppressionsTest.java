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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
        InputStream is = Modernizer.class.getResourceAsStream(
            "/modernizer.xml");
        try {
            violations = Modernizer.parseFromXml(is);
        } finally {
            Utils.closeQuietly(is);
        }
        String currentDirectory = System.getProperty("user.dir");
        File ignoreClassesFile = new File(currentDirectory,
            "/target/modernizer/test/ignore-annotated-classes.txt");
        File ignoreMethodsFile = new File(currentDirectory,
            "/target/modernizer/test/ignore-annotated-methods.txt");

        BufferedReader br =
            new BufferedReader(new FileReader(ignoreClassesFile));
        String line;
        while ((line = br.readLine()) != null) {
            ignoreClasses.add(Pattern.compile(line));
        }

        br = new BufferedReader(new FileReader(ignoreMethodsFile));
        ignoreMethods = new ArrayList<String>();
        while ((line = br.readLine()) != null) {
            ignoreMethods.add(line);
        }
    }

    @Test
    public void checkIgnoreMethodWithEmptyParameters() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodWithEmptyParametersTestClass.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodWithVoidParameter() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodWithVoidParameterTestClass.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodWithPrimitiveTypeParameters()
        throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodWithPrimitiveTypeParametersTestClass.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreOverloadedConstructor() throws Exception {
        ClassReader cr = new ClassReader(
            ModernizerTestHelper.IgnoreConstructorTestClass.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(1);
    }

    @Test
    public void checkIgnoreMethodWithGenericTypeParameters() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodWithGenericTypeParametersTestClass.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodWithDeclaredTypeParameter() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodWithDeclaredTypeParametersTestClass.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodWithPrimitiveAndGenericParameters()
        throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodWithPrimitiveAndGenericTypeParametersTestClass.class
            .getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreOverloadedMethod() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreOverloadedMethodTestClass.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(1);
    }

    @Test
    public void checkIgnoreMethodWithArrayTypeParameters() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodWithArrayTypeParametersTestClass.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreGenericClassConstructor() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreGenericClassConstructorTestClass.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(1);
    }

    @Test
    public void checkIgnoreMethodInGenericClass() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodInGenericClassTest.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreOverloadedMethodInGenericClass() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreOverloadedMethodInGenericClassTest.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(1);
    }

    @Test
    public void checkIgnoreGenericClass() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreGenericClass.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, ignoreClasses, NO_IGNORED_METHODS)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodReturningArrayOfDeclaredType()
        throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodReturningArrayClassTest.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodReturningArrayOfPrimitiveType()
        throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodReturningArrayPrimitiveTypeClassTest.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodReturningPrimitiveType() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodReturningPrimitiveTypeClassTest.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodReturningDeclaredType() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodReturningDeclaredTypeClassTest.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

    @Test
    public void checkIgnoreMethodReturningGenericType() throws Exception {
        ClassReader cr = new ClassReader(ModernizerTestHelper
            .IgnoreMethodReturningGenericTypeClassTest.class.getName());
        Collection<ViolationOccurrence> occurences = new Modernizer(
            "1.6", violations, NO_EXCLUSIONS, NO_EXCLUSION_PATTERNS,
            NO_IGNORED_PACKAGES, NO_EXCLUSION_PATTERNS, ignoreMethods)
            .check(cr);
        assertThat(occurences).hasSize(0);
    }

}
