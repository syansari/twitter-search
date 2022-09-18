import base64
import os

HOME = os.getenv('HOME')
f = open(HOME + "/.kube/config", "r")
config = ""
for line in f:
    config += line.split("\n")[0]

encodedStr = base64.b64encode(bytes(config, 'utf-8'))
print(encodedStr)