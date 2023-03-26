cd Server
rmiregistry &
sleep 2
java Replica 1 &
sleep 2
java Replica 2 &
sleep 2
java Replica 3 &
sleep 2
java FrontEnd &