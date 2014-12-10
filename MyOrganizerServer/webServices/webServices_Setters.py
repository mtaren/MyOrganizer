from google.appengine.api import users
from google.appengine.ext import ndb
from dataBaseModels import *

from google.appengine.ext.webapp import blobstore_handlers

#need notion of ParentContainer-Pass Id
#return value meaningless, for postpman checks
class WS_AddContainer(webapp2.RequestHandler):
  def post(self):
    #check owner first, valid request.
    owner = GetUserEmail(self)
    if owner == None:
      self.response.write("nologin");
      return
    params = buildDictFromPostParams(self.request)
    # for key in params:
    #   log(key)
    containerId = AddContainer(params, owner)
    self.response.write(containerId);
#return value meaningless
class WS_AddItem(webapp2.RequestHandler):
  def post(self):
    #check if logged in
    owner = GetUserEmail(self)
    if owner == None:
      self.response.write("nologin");
      return
    
    params = buildDictFromPostParams(self.request)
    itemId = AddItem(params, owner)
    self.response.write(itemId);

class WS_AddPicture(blobstore_handlers.BlobstoreUploadHandler):
  #see if there is image uploaded
  #then save image to blobstore
  #if image already there, delete old one.
  def post(self):
    upload = self.get_uploads()
    objId = self.request.get("id")
    objType = self.request.get("objType")
    log("upload sucess, running callback")
    log(objType)
    log(str(len(upload)) + " is the length of uploads")
    if(objType =="item"):
      obj = GetItem(objId)
    if(objType == "container"):
      obj= GetContainer(objId)
    if obj.Blobkey:
      blobstore.delete(obj.Blobkey) #delete old one.
    obj.Blobkey = upload[0].key()
    obj.PicUrl = "/WS_GetPicture/" + str(obj.Blobkey)
    obj.put()

#check if top level container and shared.
class WS_DeleteContainer(webapp2.RequestHandler):
  def post(self):
    containerID =  self.request.get("ContainerId")
    container = GetContainer(containerID)
    RemoveFromParent(container, "container")
    DeleteThingsInContainer(containerID)
    #DeleteContainer(containerID)

class WS_DeleteItem(webapp2.RequestHandler):
  def post(self):
    itemID = self.request.get("ItemId")
    item = GetItem(itemID)
    RemoveFromParent(item, "item")
    #TODO check if owner is correct, or is shared
    DeleteObj(item)

class WS_Move(webapp2.RequestHandler):
  def post(self):
    desitination = self.request.get("DestinationID")
    thingtoMove  = self.request.get("ObjectID")
    ObjectType   = self.request.get("ObjectType") 
    MoveObject(desitination, thingtoMove, ObjectType)

class WS_ModifyObj(webapp2.RequestHandler):
  def post(self):
    args =  self.request.arguments()
    d = {}
    for key in args:
      d[key] = self.request.get(key)
    objID = d.pop("ObjId")
    objType = d.pop("ObjType")
    if(objType == "item"):
      obj = GetItem(objID)
      d['Qty'] = int(d['Qty'])
    if(objType == "container"):
      obj = GetContainer(objID)
    ModifyObject(obj, d)
    self.response.write(objID)

def ModifyObject(obj, params):
  for entry in params:
    setattr(obj, entry, params[entry])
  obj.put()



def MoveObject(destObjID, objMoveID, objType ):
  destObj = GetContainer()
  params = {}
  params['Path'] = destObj.Path
  params['PathID'] = destObj.PathID
  params['Owner'] = destObj.Owner
  if objType == "item":
    destObj.Items.append(objMoveID)
    item = GetItem(objMoveID)
    RemoveFromParent(item, "item")
    UpdatePath(item, params)
   
  if objType == "container":
    destObj.Containers.append(objMoveID)
    container = GetContainer(objMoveID)
    RemoveFromParent(container, "container")
    RecursiveApplyDown(objMoveID, UpdatePath, params, changeParamsUpdatePath)

  destObj.put()

def UpdatePath(obj, params):
  newPathID = params['PathID'][:]
  newPath   = params['Path'][:]
  newPathID.append(obj.key.id())
  newPath.append(obj.Name)
  obj.put()

def changeParamsUpdatePath(container, params):
  newParams = {}
  newParams['PathID'] = container.PathID[:]
  newParams['Path']   = container.Path[:]
  return newParams




def DeleteContainer(containerID):
  container = GetContainer(containerID)
  if container.Blobkey:
    blobstore.delete(container.Blobkey)
  #also delete picture
  container.key.delete()

def DeleteThingsInContainer(containerID):
  RecursiveApplyDown(containerID, DeleteObj)


def AddContainer(params, owner):
  #get parentContainer ID, remove from params 
  parentContainerID = params['Parent']
  params.pop('Parent',None)

  if parentContainerID == "Null": #This is first level container
    params['Owner']  = owner
    params['PicUrl'] = "/img/house.jpg"
    container = ContainerObj(**params)
    usrObj = GetUserObj(owner)
    container.put() # need a put to get the key
    usrObj.Containers.append(container.key.id())
    usrObj.put()
    #now set paths 
    container.Path = [container.Name]
    container.PathID = [container.key.id()]
    container.put()

  
  elif GetContainer(parentContainerID): #if container exists 
    #add path list to params
    parentContainer = GetContainer(parentContainerID)
    params['Owner'] = parentContainer.Owner
    params['Path'] = parentContainer.Path
    params['PathID'] = parentContainer.PathID
    container = ContainerObj(**params)
    container.put()
    #now set paths
    container.Path.append(container.Name)
    container.PathID.append(container.key.id())
    container.put()
    #now add to parent container
    parentContainer.Containers.append(container.key.id())
    parentContainer.put()
  else:
    log("container doesnt exist! create failed")
    return None #container doesnt exists! create failed
  
  return container.key.id()

def AddItem(params, owner):
  parentContainerID = params['Parent']
  params.pop('Parent',None)
  if GetContainer(parentContainerID): #if container exists 
    #add path list to params
    parentContainer = GetContainer(parentContainerID)
    params['Owner'] = parentContainer.Owner
    params['Path'] = parentContainer.Path
    params['PathID'] = parentContainer.PathID
    params['Qty'] =  int(params['Qty'])
    item = ItemObj(**params)
    item.put()
    #now set paths
    item.Path.append(item.Name)
    item.PathID.append(item.key.id())
    item.put()
    #now add to parent container
    parentContainer.Items.append(item.key.id())
    parentContainer.put()
    return item.key.id()


  
class WS_TestCreateThings(webapp2.RequestHandler):
  def post(self):
    #check owner first, valid request.
    NUM_CONTAINERS_PER_LEVEL = 3
    NUM_ITEMS_PER_LEVEL = 15
    owner = GetUserEmail(self)
    containerParams = {"Name":"ctst", "Desc":"tst"}
    itemParams = {"Name":"ctstItem", "Desc":"tst", "Qty": 1}
    #containerParams['Parent'] = "Null"
    for i in xrange(0,3):
      log(i)
      containerParams['Parent'] = "Null"
      containerId = AddContainer(containerParams, owner)
      for j in xrange(0,NUM_CONTAINERS_PER_LEVEL):
        containerParams['Parent'] = containerId
        containerIdTwo = AddContainer(containerParams, owner)
        for k in xrange(0,NUM_CONTAINERS_PER_LEVEL):
          containerParams['Parent'] = containerIdTwo
          containerIdThree = AddContainer(containerParams, owner)
          for q in xrange(0,NUM_CONTAINERS_PER_LEVEL):
            containerParams['Parent'] = containerIdThree
            containerIdFour = AddContainer(containerParams, owner)
            for w in xrange(0,NUM_CONTAINERS_PER_LEVEL):
              containerParams['Parent'] = containerIdFour
              containerIdFive = AddContainer(containerParams, owner)
            for w in xrange(0,NUM_ITEMS_PER_LEVEL):
              itemParams['Parent'] = containerIdFour
              AddItem(itemParams, owner)
          for q in xrange(0,NUM_ITEMS_PER_LEVEL):
            itemParams['Parent'] = containerIdThree
            AddItem(itemParams, owner)
        for k in xrange(0,NUM_ITEMS_PER_LEVEL):
          itemParams['Parent'] = containerIdTwo
          AddItem(itemParams, owner)
      for j in xrange(0,NUM_ITEMS_PER_LEVEL):
        itemParams['Parent'] = containerId
        AddItem(itemParams, owner)


    self.response.write(containerId);