import settings_local, json, demjson
from parse_rest.connection import register
from parse_rest.datatypes import Object
from pprint import pprint

register(settings_local.APPLICATION_ID, settings_local.REST_API_KEY)

data = demjson.decode_file('data.json')
pprint(data)


class Outlook(Object):
    temperature = 0
    meanTemp = 0
    CO2 = 0
    humidity = 0


outlook = Outlook( temperature = data["temperature"],
                   meanTemp = data["meanTemp"],
                   CO2 = data["CO2"],
                   humidity = data["humidity"])

outlook.save()





