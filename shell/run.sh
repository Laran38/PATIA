CP=lib/org.sat4j.pb-src.jar:lib/org.sat4j.pb.jar:lib/pddl4j-3.8.3.jar:lib/sat4j-csp.jar:build

if [ -z "$1$2$3$4" ]
then
    echo "Usage: shell/run.sh -o <domain>.pddl -f <problem>.pddl\n"
else
    # $1 $2 $3 $4 = -o file/testMove.pddl -f file/testMoveProblem.pddl
    java -cp $CP fr.uga.pddl4j.tutorial.asp.ASP $1 $2 $3 $4
fi
