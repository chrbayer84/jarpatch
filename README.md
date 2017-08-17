jarpatch: Ant task to build a diff of 2 jar files, by Barbosa Norbert

Distribution content:
- src: contains source code and source test code
- lib: conatins required library to build the package
- doc: contains documentation
- classes: contains compiled java code
- build: contains result jar
- an Ant script to build/clean everything
- an Intellij Idea 4.0 project for managing all this stuff
   
Each distribution contains it's own ant build script, demo and documentation  
Minimal requirement to re-build the package, and use the package are:
    - JDK 1.4
    - jakarta ant 1.5 
