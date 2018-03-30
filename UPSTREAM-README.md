This PR allows users to suppress modernizer errors by adding `@SuppressWarnings("modernizer")` at class/method level.
How can a user suppress modernizer warnings?

`modernizer-annotation-processor` should be added as a dependency in the project.
Any class/method can be annotated with `@SuppressWarnings("modernizer")` to suppress all modernizer errors
in that block of code.

Implementation strategy

1. Code base has been organized into 3 modules.
   `modernizer-maven-plugin` module has the main plugin code.
   `modernizer-maven-policy` module has checkstyle rules and is added as a dependency to `maven-checkstyle-plugin`
   in plugin module's pom.
   `modernizer-annotation-processor` module has annotation processing code and is added as a dependency in plugin module.
3. When a project is run, before compiling the source code in the project, annotation processor
   scans to see if there are any methods/classes annotated as `@SuppressWarnings("modernizer")`. It can be found in two places -
   1. If the annotation is on a class, the processor constructs a fully qualified class name of the class and adds
   `(\$.+)?` at the end. This is the regex format.
   For every annotated class, these regex strings are constructed and added to a list.
   For example: "org/gaul/package/ClassOne\$ClassTwo(\$.+)?"
   2. If the annotation is on a method, the processor constructs a string with 3 parts, delimited by spaces - fully
   qualified class name, method name, list of formal parameters. These are normalized to be compared to the format
   expected by ASM. For every annotated method, these strings are constructed and added to a list.
   For example: "org/gaul/package/ClassOne methodOne int[] java.util.List"
4. Strings in the lists mentioned above, are dumped into 2 different files - `ignore-annotated-classes.txt` and
   `ignore-annotated-methods.txt`. These files are created in the `/target/modernizer/test` and/or `/target/modernizer/main`
   directory of the processing environment.
5. When the plugin run, in the Mojo, these files are read from the output directory (if they exist).
   If the `ignore-classes` file exists, it is read and the strings are added to `allIgnoreFullClassNamePatterns`.
   If the `ignore-methods` file exists, it is read and the strings are added to `allIgnoreMethodNames`.
6. When visiting each class, the existing code will handle ignoring the classes which were added to
   `allIgnoreFullClassNamePatterns`.

   When visiting each token, if there is a violation, `shouldIgnore` method is called to check if this token is a part of the
   code which needs to be ignored by modernizer. `ignoreMethod` forms a string which represents the method being visited
   and checks if this string is present in `allIgnoreMethodNames`. If yes, this violation is ignored.

   We have benchmarked the plugin after this implementation, and haven't seen any performance issues.

Testing strategy

`ModernizerSuppressionsEndToEndTestClass` has helper classes/methods annotated with `@SuppressWarnings("modernizer")`.
The processor scans this class before compiling and creates the ignore files.
End-to-end flow tests are added in `ModernizerSuppressionsEndToEndTest` which read the ignore files and check if
violations in the code are as expected.

Further improvements

 - The format of `ignore-methods` file could be improved by making it more structured by using XML, JSON, YAML.
 - `@SuppressWarnings("modernizer")` blocks all modernizer errors in that code block. Suppressing specific checks/errors can
   be done by adding something like an `<id>ViolationIdentifier</id>` to the violations file structure, which uniquely
   identifies each violation. We can then use `@SuppressWarnings("modernizer : ViolationIdentifier")` to suppress errors
   related to those particular checks in the code block.
