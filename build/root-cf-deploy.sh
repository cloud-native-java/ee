#!/usr/bin/env bash

set -e

export ROOT_DIR=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
export SWAP=${SWAP:-$TMPDIR}

. cf-common.sh

pids=""

function traverse_and_deploy(){

    root=$1
    cmd=""

     while read l; do
        curd=$(cd $l && pwd)

        ( ls -la $curd | grep manifest.yml ) > /dev/null && cmd="manifest"
        ( ls -la $curd | grep cf-deploy.sh ) > /dev/null && cmd="script"

        if [ "$cmd" ==  "script" ]
        then

            cd $curd && $curd/cf-deploy.sh &
            pids="$pids $!"

        elif [ "$cmd" == "manifest" ]
        then
            cd $curd && cf push &
            pids="$pids $!"
            traverse_and_deploy $curd
        else
            traverse_and_deploy $curd
        fi
    done  < <(find $root -mindepth 1  -maxdepth 1 -type d );
}

function traverse_and_reset(){

    svcs_to_delete_file="$SWAP/services_to_delete_$RANDOM.txt"
    touch $svcs_to_delete_file

    find . -iname "manifest.yml" -type f | while read l ; do
        app_name=$( cat $l | grep name | cut -f 2 -d: );
        cf d -f $app_name

        ./cf-services.py $app_name "`cf oauth-token`" | while read s; do
            echo "$s" >> $svcs_to_delete_file
        done
    done

    grep -v '^$' $svcs_to_delete_file | while read svc ; do
        cf ds -f $svc ;
    done

    rm -rf $svcs_to_delete_file

    cf delete-orphaned-routes -f

}



here="`dirname $0`"

traverse_and_reset $here
traverse_and_deploy $here

# wait until background jobs terminate
echo "..about to wait for $pids"
wait $pids
echo "..finished waiting for $pids"

cf services
cf apps

