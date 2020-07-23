#This script copies OnDie ECDSA artifacts to a specified local directory.
#Source URL(s) for the artifacts are set in source_urls.
#Artifact types are specified by artifact_types. 

source_urls = ["https://tsci.intel.com/content/OnDieCA/crls/","https://tsci.intel.com/content/OnDieCA/certs/"]
artifact_types = [".cer",".crl"]
cache_touch_file = "cache_updated"

import sys, getopt
import urllib.request
import html
from html.parser import HTMLParser
import argparse
import os.path

def artifact_copy(artifact_name):
    fi = urllib.request.urlopen(current_source + artifact_name)
    fo = open(os.path.join(args.cachedir, artifact_name + ".new"), "+wb")
    fo.write(fi.read())
    fi.close
    fo.close

class MyHTMLParser(HTMLParser):
    def handle_starttag(self, tag, attrs):
        if tag == "a":
            for attr in attrs:
                if attr[0] == "href":
                    for type in artifact_types:
                       if type in attr[1]:
                           artifact_copy(attr[1])
                           print("downloaded:", attr[1], flush=True)


parser = argparse.ArgumentParser(description='Update local OnDieCache.')
parser = argparse.ArgumentParser(prog='ondieCache', usage='%(prog)s --cachedir CACHEDIR -f')
parser.add_argument('-c', '--cachedir', required=True, help='local directory to store cache artifacts')
parser.add_argument('-f', required=False, action='store_true', help='force update when previous update not yet processed')

args = parser.parse_args()
print ("downloading files to: " + args.cachedir, flush=True)

#make sure that there is not a current update in process
if (args.f == False and os.path.exists(os.path.join(args.cachedir, cache_touch_file))):
    print ("previous update not yet processed (touch file still exists), exiting.", flush=True)
    exit()

for url in source_urls:
    current_source = url
    print ("downloading from: " + url, flush=True)
    fp = urllib.request.urlopen(url)
    htmlbytes = fp.read()
    fp.close()
    htmlstr = htmlbytes.decode("utf8")
    parser = MyHTMLParser()
    parser.feed(htmlstr)

    # create the touch file to indicate that the cache has been updated
    fo = open(os.path.join(args.cachedir, cache_touch_file), "+wb")
    fo.close


