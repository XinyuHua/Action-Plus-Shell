javac -d bin -sourcepath src -cp ".:lib/*:bin/" src/Main.java 
java -Xmx50g -cp "bin:lib/*" Main 90 100
