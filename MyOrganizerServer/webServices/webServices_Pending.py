from google.appengine.api import users
from google.appengine.ext import ndb
from dataBaseModels import *

import json

##
class WS_ShareContainers(webapp2.RequestHandler):
  def post(self):
    containerIDs = json.loads(self.request.get("ContainerIDList"))
    email = self.request.get("email")
    ownerEmail = GetUserEmail(self)
    log(containerIDs)
    log(email)
    # check if the email == owner, then return
    usr = GetUserObj(email)
    ShareContainersRequest(usr, containerIDs, ownerEmail)
    
      #if can View, move to shared

class WS_FetchNumPendingEvents(webapp2.RequestHandler):
  def post(self):
    userEmail = GetUserEmail(self)
    usr  = GetUserObj(userEmail)
    d = { 'NumPend': len(usr.PendingEvents)}
    self.response.write(json.dumps(d))

class WS_FetchPendingEvents(webapp2.RequestHandler):
  def post(self):
    userEmail = GetUserEmail(self)
    usr  = GetUserObj(userEmail)
    if usr: # prevents first time createing double usrs
      pendingEventList = []
      for pendID in usr.PendingEvents:
        pend = GetPendingEvent(pendID)
        pendingEventList.append( StripForWS(pend) )

    d = {'pendLists': pendingEventList}
    self.response.write(json.dumps(d))

class WS_ResolvePendEvent(webapp2.RequestHandler):
  def post(self):
    userEmail = GetUserEmail(self)
    usr = GetUserObj(userEmail)
    action = self.request.get("action")
    PendId = self.request.get("PendId")
    PendE = GetPendingEvent(PendId)
    if PendE:
      log("pende found")
      if PendE.Type == "ShareRequest":
        ProcessShareRequest(PendE, usr, action) 
      if PendE.Type == "BatchAdd":
        ProcessBatchAdd(PendE, usr, action)

class WS_GetPendingEventData(webapp2.RequestHandler):
  def post(self):
    PendId = self.request.get("PendId")
    PendE = GetPendingEvent(PendId)
    params = {"PendId": str(PendE.key.Id()), "Data": Pende.Data}
    self.response.write( json.dumps(params))

class WS_GetPendingEventDataBatchAdd(webapp2.RequestHandler):
  def post(self):
    PendId = self.request.get("PendId")
    PendE = GetPendingEvent(PendId)
    ItemList = []
    for itemID in PendE.Data['ItemList']:
      item = GetItem(itemID)
      ItemList.append( GetObjDict(item, "item"))

    params = {"PendId": str(PendE.key.id()), "ItemList": ItemList}
    self.response.write( json.dumps(params))

def ProcessShareRequest(PendE, usr, action):
  log("processing share request " + action)
  if action == "Accept":
    ShareContainer(usr, PendE.Data['ListOfObjects'])
    RemovePendEvent(usr, PendE)
  if action == "Deny":
    RemovePendEvent(usr, PendE)

def ProcessBatchAdd(PendE, usr, action):
  #log(action)
  if action == "Start Batch Add":
    log("skipping first Batch")
    return
  log("id is " + str(action))
  log(PendE.Data['ItemList'])
  if int(action) in PendE.Data['ItemList']:
    PendE.Data['ItemList'].remove(int(action))
  else:
    PendE.Data['ItemList'].remove(str(action))
  if len(PendE.Data['ItemList']) == 0:
    RemovePendEvent(usr, PendE)
  else:
    PendE.put()




def ShareContainer(usr, listOfContainers):
  usr.Shared.extend(listOfContainers)
  usr.Shared = list(set(usr.Shared))

#usr is the target usr to share with
#owner email is person who initiated the share
def ShareContainersRequest(usr, containerIDList, ownerEmail):
  eventType = "ShareRequest"
  data = {}
  shareList = []
  for containerId in containerIDList:
    cont = GetContainer(containerId)
    if isOwned(usr, cont): #sharing an item with its owner..
      log("container is owned")
      continue 
    if isShared(usr, cont):
      log("container is already shared")
      continue
    shareList.append(containerId)

  data['ListOfObjects'] = shareList
  names, notused = GetNamesFromIDs(shareList, ContainerObj, None)
  data['MessageTitle'] = ownerEmail + " is sharing something"
  data['MessageBody'] = " Sharing: "+", ".join(str(i) for i in names)
  CreatePendEvent(eventType, data, usr)

def CreatePendEvent(t, d, usr):
  p = PendingEvent(Type = t, Data=d);
  p.put()
  usr.PendingEvents.append(p.key.id())
  usr.put()
  return p

#Strips Pending event data to make only m
def StripForWS(pend):
  d = {}
  d['MessageTitle'] = pend.Data['MessageTitle']
  d['MessageBody'] = pend.Data['MessageBody']
  d['Type'] = pend.Type
  d['Id']  = str(pend.key.id())
  return d

#removes from usr, deletes pending
def RemovePendEvent(usr, PendE):
  if str(PendE.key.id()) in usr.PendingEvents:
    usr.PendingEvents.remove(str(PendE.key.id()))
  else:
    usr.PendingEvents.remove(int(PendE.key.id()))
  usr.put()
  DeleteObj(PendE)






