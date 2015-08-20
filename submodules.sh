#!/bin/bash

set -ex

git submodule update --init
git submodule foreach '(git checkout master && git pull --rebase && git submodule update --init) || echo "Not attempting to sync"'
