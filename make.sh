
javac -d bin -sourcepath src -cp ".:lib/*:bin/" src/Main.java 
java -cp -Xmx50g "bin:lib/*" Main 80 90
