
Contributing to DockDockBuild
==========================

Thank you for taking the time to contribute to DockDockBuild!

Development
--------------------------------------------

The plugin is built using Gradle and uses [gradle-intellij-plugin][gradle-intellij-plugin] to integrate with IntelliJ Platform.

First, make sure gradle is using java 11, and not any other version.

### Directly from IntelliJ
You can run and debug DockDockBuild directly from the IntelliJ.
Just go to the Gradle toolbar and select runIde:

![gradle_toolbar.png](https://github.com/intuit/DockDockBuild/blob/master/images/gradle_toolbar.png?raw=true)

(You can right-click on the target to run in debug mode)

This will launch a new IDE with your DockDockBuild installed.

### Using the terminal
To build the plugin run

```
$ ./gradlew buildPlugin
```

Plugin zip file will be created in `build/distributions`

To build & test the plugin in IDE run `./gradlew runIdea`


### New PR
To create a new PR please do the following:
1. Fork the repo
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create a new Pull Request

### Grammar modifications

The plugin uses [Grammar-Kit][Grammar-Kit] to generate parser and lexer. Please install [Grammar-Kit plugin][Grammar-Kit plugin] and refer to the documentation if you want to modify grammar.

To regenerate a parser, open Makefile.bnf and press Ctrl+Shift+G (Cmd+Shift+G on Mac)
To regenerate a lexer, open Makefile.flex and press Ctrl+Shift+G (Cmd+Shift+G on Mac)

Please make sure to add test to MakefileParserTest.kt for any parser modifications.

The plugin is written in [Kotlin](http://kotlinlang.org/).

[GNU head icon](https://www.gnu.org/graphics/heckert_gnu.html) licensed under CC-BY-SA 2.0

[Grammar-Kit]:https://github.com/jetbrains/grammar-kit
[Grammar-Kit plugin]:https://plugins.jetbrains.com/plugin/6606-grammar-kit
