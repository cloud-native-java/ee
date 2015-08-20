#!/usr/bin/env python

import os
import sys
import urllib2
import json


def cf_app_services(app_name, token):
    app_name = app_name.strip()
    root_uri = 'http://api.run.pivotal.io%s'


    def oauth(r, t):
        r.add_header('Authorization', t)
        return r

    def oauth_request(uri):
        return urllib2.urlopen(oauth(urllib2.Request(uri), token))

    apps = oauth_request(root_uri % '/v2/apps')
    app = [a['entity'] for a in json.loads(apps.read())['resources'] if a['entity']['name'] == app_name]
    if len(app) == 0:
        return []

    app = app[0]

    services = oauth_request(root_uri % app['service_bindings_url'])
    services = [a['entity'] for a in json.loads(services.read())['resources']]
    services_urls = [root_uri % s['service_instance_url'] for s in services]

    return [json.loads(oauth_request(su).read())['entity']['name'] for su in services_urls]


if __name__ == '__main__':

    # ./cf-app-services.py bootiful-app "`cf oauth-token`"

    app_name, token  = sys.argv[1:3]
    
    if token.lower().find('bearer') > -1 :
        token = 'bearer %s' %  token.split('bearer')[1].strip()

    print os.linesep.join(cf_app_services(app_name, token))
