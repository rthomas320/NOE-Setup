#!/usr/bin/env bash

function usage {
    echo "usage: $0 -a <vagrant_action> -r <rocky_version>(8,9) -p <provider>(virtualbox,parallels)"
    exit 1
}

export vagrant_action=""
export rocky_version="8"
export provider=""

while getopts "a:r:p:h:" opt; do
  case "$opt" in
    a)
        vagrant_action=$OPTARG
    ;;
    r)
        rocky_version=$OPTARG
    ;;
    p)
        provider="--provider $OPTARG"
    ;;
    ?|h)
        usage
    ;;
  esac
done

[[ -z $vagrant_action ]] && usage || true

export OS_ARCH=$(uname -m)
export ROCKY_VERSION=$rocky_version

vagrant $vagrant_action $provider
