#!/bin/sh
INSTANCES = $1

source $HOME/.bashrc

stop_instance() {
case $SID in 
tm10|tm10dev)
    export ORRRACLE_DATABASE_FILE_DIR="/opt/oracle/oradata/$SID"
    export ORACLE_SID=$SID
    sqlplus -S "/as sysdba"<<EOF
    shutdown
    quit;
EOF

    echo "emctl stop dbconsole $SID..."
    emctl stop dbconsole 
    ;;
*)
    export ORRRACLE_DATABASE_FILE_DIR="/home/oracle/tist/$SID"
    export ORACLE_SID=$SID
    sqlplus -S "/as sysdba"<<EOF
    shutdown
    quit;
EOF

    #only start dbconsole for tm10, tm10dev
    #echo "emctl stop dbconsole $SID ..."
    #emctl stop dbconsole
    ;;
esac
}

for inst in $INSTANCES
do
	SID="$inst"
        stop_instance
done

echo "lsnrctl stop"
lsnrctl stop

echo "isqlplusctl stop ..."
isqlplusctl stop
