This PR allows users to suppress modernizer errors by adding `@SuppressWarnings("modernizer")` at class/method level.
The implementation details are as follows -

1. We have structured the code into 3 modules. The main plugin code is in `modernizer-maven-plugin` module.
   `modernizer-maven-policy` module has the checkstyle rules and is added as a dependency to `maven-checkstyle-plugin`
   in plugin module's pom.
   `modernizer-annotation-processor` module has annotation processing code and is added as a dependency in plugin module
   to share code implemented as a part of annotation processing during build-time.
2. To facilitate this suppression, users have to add `modernizer-annotation-processor` as a dependency in their projects.
3. When a project which has modernizer plugin is run, before compiling the source code in the project, annotation processor
   scans to see if there are any methods/classes annotated as `SuppressWarnings("modernizer")`. There are two cases here -
   1. If the annotation is on a class, the processor constructs a fully qualified class name of the class and adds
   `(\$.+)?` at the end. This string is formatted by replacing `.` with `\` and `$` with `\\$`. This is the regex format.
   For every annotated class, these regex strings are constructed and added to a list.
   2. If the annotation is on a method, the processor constructs a string which has 3 parts, delimited by spaces - fully
   qualified class name, method name, list of formal parameters. These are normalized to be compared to the format
   expected by ASM. For every annotated method, these strings are constructed and added to a list.
4. Strings in the lists mentioned above, are dumped into 2 different files - `ignore-annotated-classes.txt` and
   `ignore-annotated-methods.txt`. These files are created in the `/target/modernizer/test` and/or `/target/modernizer/main`
   directory of the processing environment.
5. When the plugin run, in the Mojo, these files are read from the output directory (if they exist). There are two flows here -
   If the `ignore-classes` file exists, it is read and the strings are added to `allIgnoreFullClassNamePatterns`.
   If the `ignore-methods` file exists, it is read and the strings are added to `allIgnoreMethodNames`.
6. When visiting each class, the existing code will handle ignoring the classes which were added to
   `allIgnoreFullClassNamePatterns`. We added and  additional method - `ignoreMethod` which handles ignoring the methods in
   `allIgnoreMethodNames`. We used the utilities provided by ASM to parse the method descriptor in the plugin.

