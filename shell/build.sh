CP=lib/org.sat4j.pb-src.jar:lib/org.sat4j.pb.jar:lib/pddl4j-3.8.3.jar:lib/sat4j-csp.jar:build

mkdir build &

javac -cp $CP -d build src/fr/uga/pddl4j/tutorial/asp/*.java
javac -cp $CP -d build src/*.java