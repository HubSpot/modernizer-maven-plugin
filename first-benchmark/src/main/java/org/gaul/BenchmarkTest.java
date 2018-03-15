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

package org.gaul;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.gaul.modernizer_maven_plugin.Modernizer;
import org.gaul.modernizer_maven_plugin.ModernizerNew;
import org.gaul.modernizer_maven_plugin.Utils;
import org.gaul.modernizer_maven_plugin.Violation;
import org.gaul.modernizer_maven_plugin.ViolationOccurrence;
import org.objectweb.asm.ClassReader;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.xml.sax.SAXException;

@State(Scope.Benchmark)
public class BenchmarkTest {

    private Map<String, Violation> violations;
    public final Collection<String> NO_EXCLUSIONS =
        Collections.emptySet();
    public final Collection<Pattern> NO_EXCLUSION_PATTERNS =
        Collections.emptySet();
    public final Collection<String> NO_IGNORED_PACKAGES =
        Collections.emptySet();
    public final Collection<String> NO_IGNORED_METHODS =
        Collections.emptySet();
    private ClassReader cr;
    private Collection<String> ignoreMethodNames;
    private Collection<ViolationOccurrence> occurences;
    private Modernizer modernizer;
    private ModernizerNew modernizerNew;

    @Setup
    public final void setup()
        throws ParserConfigurationException, SAXException, IOException {
        cr = new ClassReader(TestClass.class.getName());
        ignoreMethodNames = Arrays.asList(
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
                "Ljava/util/List;F",
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest," +
                "testOverloadedMethod,",
            "org/gaul/annotation_processor/" +
                "ModernizerAnnotationProcessorTest," +
                "testArrayParameters," +
                "[Ljava/lang/String;[[I[Ljava/util/List;"
        );
        InputStream is =
            Modernizer.class.getResourceAsStream("/modernizer.xml");
        try {
            violations = Modernizer.parseFromXml(is);
        } finally {
            Utils.closeQuietly(is);
        }
        modernizer =
            new Modernizer("1.6", violations, NO_EXCLUSIONS,
                NO_EXCLUSION_PATTERNS, NO_IGNORED_PACKAGES,
                NO_EXCLUSION_PATTERNS, ignoreMethodNames);
        modernizerNew =
            new ModernizerNew("1.6", violations, NO_EXCLUSIONS,
                NO_EXCLUSION_PATTERNS, NO_IGNORED_PACKAGES,
                NO_EXCLUSION_PATTERNS, ignoreMethodNames);
    }

    @GenerateMicroBenchmark
    public final int testIgnoreMethodIfViolationFound() throws IOException {
        occurences = modernizer.check(cr);
        return occurences.size();
    }

    @GenerateMicroBenchmark
    public final int testIgnoreMethodAlways() throws IOException {
        occurences = modernizerNew.check(cr);
        return occurences.size();
    }

    public static void main(String... args) throws RunnerException {
        Options opts = new OptionsBuilder()
            .include(".*")
            .warmupIterations(5)
            .measurementIterations(10)
            .jvmArgs("-Xms2g", "-Xmx2g")
            .shouldDoGC(true)
            .forks(1)
            .build();

        new Runner(opts).run();
    }
}
