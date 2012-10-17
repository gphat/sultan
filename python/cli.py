import argparse, httplib, json, sys

parser = argparse.ArgumentParser(description='Talk to Sultan')
parser.add_argument('command', metavar='COMMAND', nargs=1, help='command', choices=['begin','close', 'fail', 'reset', 'show', 'success'])
parser.add_argument('-c', '--change', nargs=1, required=True, help='ID of change')
parser.add_argument('-H', '--host', nargs=1, required=True, help='URL of Sultan')
parser.add_argument('-t', '--token', nargs=1, required=True, help='Authentication token')

def main():
  args = parser.parse_args()

  headers = {
    'Authorization': 'Token token=' + args.token[0]
  }

  conn = httplib.HTTPConnection(args.host[0])

  if(args.command[0] == 'show'):
    conn.request("GET", '/api/change/' + args.change[0], "", headers)
  elif(args.command[0] == 'begin'):
    conn.request("POST", '/api/change/begin/' + args.change[0], "", headers)
  elif(args.command[0] == 'close'):
    conn.request("POST", '/api/change/close/' + args.change[0], "", headers)
  elif(args.command[0] == 'fail'):
    conn.request("POST", '/api/change/fail/' + args.change[0], "", headers)
  elif(args.command[0] == 'reset'):
    conn.request("POST", '/api/change/reset/' + args.change[0], "", headers)
  elif(args.command[0] == 'success'):
    conn.request("POST", '/api/change/success/' + args.change[0], "", headers)

  res = conn.getresponse()
  print json.dumps(json.loads(res.read()), sort_keys=True, indent=4)
  conn.close

if __name__ == "__main__":
    main()