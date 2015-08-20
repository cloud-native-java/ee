#!/bin/sh

set -e

killPort() {
  lsof -i TCP:$1 | grep LISTEN | awk '{print $2}' | xargs kill -9
}

killPort 4242

ruby -run -e httpd . -p 4242 &

asciidoctor --version

#gem install bundler
#bundle install
bundle exec guard 

