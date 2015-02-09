import webapp2 
from google.appengine.ext import ndb
from google.appengine.api import users
from google.appengine.ext import blobstore
import logging
import json
from apiclient.discovery import build



class UserObj (ndb.Model):
  UserEmail = ndb.StringProperty(required=True)
  Containers = ndb.PickleProperty(required=True,default=[]) #First layer of containers
  Shared = ndb.PickleProperty(required=True,default=[]) #shared containers- editable
  SemiShare = ndb.PickleProperty(required=True,default=[]) #viewable only containers
  PendingEvents = ndb.PickleProperty(required=True,default=[]) #notifications

class ContainerObj(ndb.Model):
  Name   = ndb.StringProperty(required=True)
  Path   = ndb.PickleProperty(required=True,default=[])
  PathID = ndb.PickleProperty(required=True,default=[])
  PicUrl = ndb.StringProperty(required=False, default = "/img/container.jpg") #TODO change this to default pic
  Items  = ndb.PickleProperty(required=True,default=[])
  Owner  = ndb.StringProperty(required=True)
  Category = ndb.StringProperty(required=True, default="None")
  Desc   = ndb.StringProperty(required=True) #describes where it is
  Public = ndb.BooleanProperty(required=False, default=False)
  Blobkey= ndb.BlobKeyProperty(required=False, default=None)
  Containers = ndb.PickleProperty(required=True,default=[])
  QRCode = ndb.StringProperty(required=True, default="")
  

class ItemObj(ndb.Model):
  Name     = ndb.StringProperty(required=True)
  Path     = ndb.PickleProperty(required=True,default=[])
  PathID   = ndb.PickleProperty(required=True,default=[]) 
  PicUrl   = ndb.StringProperty(required=False, default="/img/item.png")
  Owner    = ndb.StringProperty(required=True)
  Borrowed = ndb.StringProperty(required=True, default="None")
  Category = ndb.StringProperty(required=True, default="None")
  Desc     = ndb.StringProperty(required=True) #like keywords
  Public   = ndb.BooleanProperty(required=False, default=False)
  Blobkey  = ndb.BlobKeyProperty(required=False, default=None)
  Qty      = ndb.IntegerProperty(required=True, default=1) 
 
class PendingEvent(ndb.Model):
  Type = ndb.StringProperty(required=True)
  Data = ndb.PickleProperty(required=True,default={})
  Blobkey  = ndb.BlobKeyProperty(required=False, default=None)

#used for all services - replaces self.request.get("email")
##TODO get the token, get the email from that for mobile
def GetUserEmail(this):
  #request.get("email")
  user = users.get_current_user()
  if user:
    return user.email().lower()
  else:
    if 'Authorization' in this.request.headers:
      access_token = this.request.headers['Authorization']
      log(access_token)
      resp = build('oauth2', 'v1').tokeninfo(access_token=access_token).execute()
      return resp['email']
  return None

#Check Owns Container
#Check Owns Item
def isOwned(UserObj, Item):
  return UserObj.UserEmail == Item.Owner

def isShared(UserObj, Item):
  return (Item.key.id() in UserObj.Shared)

##create if email doesnt exist
##Just use this to get he user object as well
def GetUserObj(email):
  usr = UserObj.query(UserObj.UserEmail == email).get()
  log("checking for " +  email)
  if usr == None: 
    log("no user found with email " +  email)
    usr = UserObj(UserEmail = email.lower())
    #usr = usr.put()
    return usr
  else:
    log("user found")
    return usr

#gets parent of object. not id!
#returns object
def GetParent(Obj):
  if len(Obj.PathID) > 1:
    return GetContainer(Obj.PathID[-2])
  else:
    return "Null"

def RemoveFromParent(Obj, ObjType):
  objID = int(Obj.key.id())
  Parent = GetParent(Obj)
  if ObjType =="item":
    if str(objID) in Parent.Items:
      Parent.Items.remove(str(objID)) 
    else:
      Parent.Items.remove(objID)    
    Parent.put()
  if ObjType =="container":
    if Parent == "Null": #no parent have to get user
      usr = GetUserObj(Obj.Owner)
      if objID in usr.Containers:
        usr.Containers.remove(objID)
      if objID in usr.Shared:
        usr.Shared.remove(objID)
      usr.put()
    else:
      log(Parent.Containers)
      log(objID)
      if str(objID) in Parent.Containers:
        Parent.Containers.remove(str(objID)) 
      else:
        Parent.Containers.remove(objID)
      Parent.put()

#if you cant find it in shared, remove it.
def DeleteObj(Obj, p=None):
  if Obj.Blobkey:
   blobstore.delete(Obj.Blobkey)
  Obj.key.delete()

## pass in a list of ID's, returns a list of corrosponding names
# if id is deleted, remove from list
def GetNamesFromIDs(listOfIDs, objectType, mainObj):
  nameList = []
  picList = []
  for thingID in listOfIDs:
    log("id is "+ str(thingID))
    # obj = objectType.get_by_id(int(thingID))
    obj = objectType.get_by_id(int(thingID))
    if obj == None:
      listOfIDs.remove(thingID)
      if mainObj:
        mainObj.put()
      continue
    nameList.append(obj.Name)
    picList.append(obj.PicUrl)
  return nameList, picList

#gets the item for you
#func must put() the item/container to save it! 
def RecursiveApplyDown(containerID, func, params=None, changeParams=None, ignoreParent=False):
  container = GetContainer(containerID)
  containerList = container.Containers
  itemList = container.Items
  if not ignoreParent:
    func(container, params)
  if changeParams != None:
    newParams = changeParams(container, params)
  else:
    newParams = params
  for contID in containerList:
      RecursiveApplyDown(contID, func, newParams)
  for itemID in itemList:
      item = GetItem(itemID)
      func(item, newParams)




### utilities

#assumes you pass in a list of objects
def ObjectsToJson(ItemList, ContainerList):
  params = {}
  params['Items'] = []
  params['ItemsNames'] = []
  params['ItemsPics'] = []
  params['Containers'] = []
  params['ContainersNames'] = []
  params['ContainersPics'] = []

  for item in ItemList:
    params['Items'].append(item.key.id())
    params['ItemsNames'].append(item.Name)
    params['ItemsPics'].append(item.PicUrl)
  for cont in ContainerList:
    params['Containers'].append(cont.key.id())
    params['ContainersNames'].append(cont.Name)
    params['ContainersPics'].append(cont.PicUrl)
  params['Path']= ["Search Results"]
  params['PathID']= ["Null"]

  return json.dumps(params)


def GetPendEventMatchParams(PendType, usr, params):
  for pendId in usr.PendingEvents:
    pend = GetPendingEvent(pendId)
    found = True
    for key in params:
      if key not in pend.Data:
        log(key + " not in pendData")
        found = False
        break
      if params[key] != pend.Data[key]:
        log(str(params[key] + " not equal" + str(pend.Data[key])))
        found = False
        break
    if found == True:
      return pend
  return None


def GetContainer(Id):
  return ContainerObj.get_by_id(int(Id))

def GetPendingEvent(Id):
  return PendingEvent.get_by_id(int(Id))

def GetItem(Id):
  return ItemObj.get_by_id(int(Id))

## builds a dictionary from post params
def buildDictFromPostParams(request):
  arguments = request.arguments()
  d = {}
  for arg in arguments:
    d[arg] = request.get(arg)
  return d

def GetObjDict(obj, objType):
  params ={}
  params["Name"]     = obj.Name
  params["PicUrl"]   = obj.PicUrl
  params["Category"] = obj.Category
  params["Desc"]     = obj.Desc
  params["Id"]       = str(obj.key.id())
  params["Path"] = "Homes/" + '/ '.join(obj.Path[:-1])
  params["ParentId"] = str(GetParent(obj))
  if objType == "item":
    params["Qty"] =  obj.Qty
  return params




#levels are:
# CRITICAL  50
# ERROR 40
# WARNING 30
# INFO  20
# DEBUG 10
# NOTSET  0
def log(msg, level = 20):
  logging.log(level, msg)

