from google.appengine.api import users
from google.appengine.ext import ndb
from dataBaseModels import *

import json

class WS_Search(webapp2.RequestHandler):
  def post(self):
    email = GetUserEmail(self)
    usr   = GetUserObj(email)
    params = {}
    params['searchStr'] = self.request.get('searchStr').lower()
    params['useName']   = self.request.get('useName')
    params['useDesc']   = self.request.get('useDesc')
    params['Category']  = self.request.get('Category')

    #load params
    contid = self.request.get('containerId')
    AllThings = getThingsInContainer(contid, usr)
    items = filterItems(AllThings['Items'], params)
    containers = filterItems(AllThings['Containers'], params)
    log(containers)

    self.response.write(ObjectsToJson(items,containers))

## first gets all items in the container
## then filters items by params cirteria
def getThingsInContainer(containerId, usr):
  if containerId == "Null":
    containerIds = usr.Shared
    containerIds.extend(usr.SemiShare)
    containerIds.extend(usr.Containers)
  else:
     containerIds = [containerId]
  params = {}
  params['Items'] = []
  params['Containers'] = []
  for contID in containerIds:
    RecursiveApplyDown(contID, GetItems, params, ignoreParent=True)
  return params

def filterItems(items, params):
  searchStr = params['searchStr']
  if searchStr == "*": #for debugging
    return items
  searches = searchStr.split()
  filteredList = []
  for string in searches:
    for i in xrange(len(items) -1, -1,-1):
      if params['Category'] != "All":
        if params['Category'] != items[i].Category:
          continue
      if params['useName'] == "true":
        # if searchStr in items[i].Name.lower().split(): #mayber faster
        if searchStr in items[i].Name.lower():
          filteredList.append(items.pop(i))
          continue
      if params['useDesc'] == "true":
        # if searchStr in items[i].Desc.lower().split(): #for whole wor match. maybe faster?
        if searchStr in items[i].Desc.lower():
          filteredList.append(items.pop(i))
          continue

  return filteredList
  



def GetItems(obj, params):
  if hasattr(obj, 'Containers'):
    params['Containers'].append(obj)
  else:
    params['Items'].append(obj)

"""
 d = {}
  d['Items']       = container.Items
  d['ItemsNames'], d['ItemsPics']   = GetNamesFromIDs(container.Items, ItemObj, container)
  d['Containers']   = container.Containers
  d['ContainersNames'],d['ContainersPics']  = GetNamesFromIDs(container.Containers, ContainerObj, container)
  d['Path']= container.Path
  d['PathID']= container.PathID
"""