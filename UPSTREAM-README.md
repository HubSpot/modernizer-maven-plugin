This PR allows users to suppress modernizer errors by adding `@SuppressWarnings("modernizer")` at class/method level.

How can a user suppress modernizer warnings?

`modernizer-annotation-processor` should be added as a provided-scoped dependency in the project.
Any class or method can be annotated with `@SuppressWarnings("modernizer")` to suppress all modernizer errors
in that block of code.

## Implementation strategy

1. The code base has been organized into 3 modules:
   1. `modernizer-maven-plugin` has the main plugin code.
   2. `modernizer-maven-policy` has checkstyle rules and is added as a dependency to `maven-checkstyle-plugin`
   in the plugin's pom.
   3. `modernizer-annotation-processor` has annotation processing code and is added as a dependency in plugin.
2. When a project is run, before compiling the source code in the project, annotation processor scans to see if there are any
   methods/classes annotated as `@SuppressWarnings("modernizer")`. These annotations can be added to methods or classes.
   1. If the annotation is on a class, the processor constructs a regex that matches the fully qualified class name of the
   annotated class and its subclasses.
   For example: `org/gaul/package/ClassOne\$ClassTwo(\$.+)?`
   2. If the annotation is on a method, the processor constructs a string with 4 parts, delimited by spaces - the fully
   qualified class name, the method name, the return type, and the list of parameters. These are normalized to be compared to the format
   expected by ASM.
   For example: `org/gaul/package/ClassOne methodOne int[] java.util.List`
3. The results of step 2 are dumped into 2 different files - `ignore-annotated-classes.txt` and
   `ignore-annotated-methods.txt`. These files are created in the `/target/modernizer/test` and `/target/modernizer/main`
   directories of the processing environment. Adding the files to these directories makes them accessible to the plugin while excluding them
   from the jar of the project being built.
4. The plugin reads the files created in the previous steps and uses them to ignore violations in the places specified.

## Testing strategy

`ModernizerSuppressionsEndToEndTestClass` has helper classes/methods annotated with `@SuppressWarnings("modernizer")`.
The processor scans this class before compiling and creates the ignore files.
End-to-end flow tests are added in `ModernizerSuppressionsEndToEndTest` which read the ignore files and check if
violations in the code are as expected.

We have benchmarked the plugin after this implementation, and haven't seen any performance issues.

## Further improvements

 - The format of `ignore-methods` file could be improved by making it more structured by using XML, JSON, or YAML.
 - `@SuppressWarnings("modernizer")` blocks all modernizer errors in that code block. Suppressing specific checks/errors can
   be done by adding something like `<id>ViolationIdentifier</id>` to the violations file structure, which uniquely
   identifies each violation. We can then use `@SuppressWarnings("modernizer:ViolationIdentifier")` to suppress errors
   related to those particular checks in the code block.
