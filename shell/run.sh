CP="lib/org.sat4j-csp.jar:lib/org.sat4j.pb-src.jar:lib/org.sat4j.pb.jar:lib/pddl4j-3.8.3.jar:lib/sat4j-csp.jar:build"

if [ -z "$1$2$3" ]
then
    echo "Usage: shell/run.sh <-a, -s, -p> <domain>.pddl <problem>.pddl\n"
else
    java -server -Xms16384m -Xmx16384m -cp $CP Launcher $1 -o $2 -f $3
fi
