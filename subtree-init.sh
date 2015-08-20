#!/bin/bash
set -e

mkdir -p code

cat cnj-modules.txt | while read l ; do
  echo "adding $l.."
  #git="git@github.com:cloud-native-java/$l.git"
  git="https://github.com/cloud-native-java/$l.git"
  git subtree add --prefix code/$l $git master  
done
