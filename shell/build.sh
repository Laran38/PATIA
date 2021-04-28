CP="src/lib/org.sat4j-csp.jar:src/lib/org.sat4j.pb-src.jar:src/lib/org.sat4j.pb.jar:src/lib/pddl4j-3.8.3.jar:src/lib/sat4j-csp.jar:build"
LONG_CP="src/fr/uga/pddl4j/tutorial"
SRC="$LONG_CP/*.java $LONG_CP/util/*.java $LONG_CP/asp/*.java $LONG_CP/sat/*.java src/*.java"

mkdir build &

javac -cp $CP -d build $SRC
