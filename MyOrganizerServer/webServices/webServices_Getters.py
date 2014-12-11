from google.appengine.api import users
from google.appengine.ext import ndb
from google.appengine.ext import blobstore
from google.appengine.ext.webapp import blobstore_handlers
from dataBaseModels import *
import json

# Given a user, get the top level containers
class Tester(webapp2.RequestHandler):
  def post(self):

    # d = {"one": "The ONE string"}
    # d2 = {"one": "The Two string"}
    # dli = { "oneList": [d,d2]}
    # self.response.write(json.dumps(dli))

    d = {"testList" : ["s","d","a"]}
    self.response.write(json.dumps(d))

class WS_GetHouses(webapp2.RequestHandler):
  def post(self):
    email = GetUserEmail(self)
    if email == None:
      self.response.write("nologin");
      return
    d = GetThingsInHouses(email)
    self.response.write(json.dumps(d))

class WS_GetContainer(webapp2.RequestHandler):
  def post(self):
    email = GetUserEmail(self)
    if email == None:
      self.response.write("nologin");
      return
    ContainerID = self.request.get("ContainerID")
    ##todo check if you have view access to this container
    d = GetThingsInContainer(ContainerID)
    self.response.write(json.dumps(d))

class WS_GetBlobUrl(webapp2.RequestHandler):
  def post(self):
    objId = self.request.get("ObjId")
    objType = self.request.get("ObjType")
    upload_url = blobstore.create_upload_url('/WS_AddPicture?id='+ str(objId)+"&objType="+str(objType))
    self.response.out.write(upload_url)

class WS_GetBlobUrlBatchAdd(webapp2.RequestHandler):
  def post(self):
    email = GetUserEmail(self)
    containerId =  self.request.get("containerId")
    upload_url = blobstore.create_upload_url('/WS_AddPictureToBlankItem?email='+ email +"&cId="+containerId )
    self.response.out.write(upload_url)

class WS_GetPicture(blobstore_handlers.BlobstoreDownloadHandler):
  def get(self, photo_key):
    # print photo_key
    #log("someone try to get pic")
    if not blobstore.get(photo_key):
      self.response.write("Error no photo key<br>")
    else:
      self.send_blob(photo_key)

#pass in a item list and container list(ID's)
#get list of item and container attributes
class WS_GetObjectAttributes(webapp2.RequestHandler):
  def post(self):
    ItemList = []
    ContainerList = []
    ItemIDs = json.loads(self.request.get("ItemIdList"))
    ContainerIDs = json.loads(self.request.get("ContainerIDList"))
    for itemID in ItemIDs:
      item = GetItem(itemID)
      ItemList.append( GetObjDict(item, "item"))
    for ContainerID in ContainerIDs:
      container = GetContainer(ContainerID)
      ContainerList.append( GetObjDict(container, "container"))
    resp = {}
    resp["ItemList"] = ItemList
    resp["ContainerList"] = ContainerList

    self.response.write( json.dumps(resp))


def GetObjectFromQRCode(QRcode):
  container = ContainerObj.query(ContainerObj.QRCode == QRcode).get()
  return str(container.key.id())

class WS_GetIDfromQRCode(webapp2.RequestHandler):
  def post(self):
    QRcode = self.request.get("QRCode")
    ObjId = GetObjectFromQRCode(QRcode)
    self.response.write(ObjId)

class WS_GetOneObjectAttributes(webapp2.RequestHandler):
  def post(self):
    objId = self.request.get("ObjId")
    objType = self.request.get("ObjType")

    if objType == "item":
      obj = GetItem(objId)
    if objType == "container":
      obj = GetContainer(objId)

    resp = GetObjDict(obj, objType)
    # resp["Path"] = "Homes/" + '/'.join(obj.Path[:-1])
    # resp["ParentId"] = str(GetParent(obj))
    self.response.write( json.dumps(resp))

#For home page, gets things to be displayed
def GetThingsInHouses(email):
  usrObj = GetUserObj( email )
  d = {}
  d['MyHouses']      = usrObj.Containers
  d['Shared']        = usrObj.Shared
  d['SemiShare']     = usrObj.SemiShare
  d['MyHousesNames'], d['MyHousesPics'] = GetNamesFromIDs(usrObj.Containers, ContainerObj,usrObj)
  d['SharedNames'], d['SharedPics']   = GetNamesFromIDs(usrObj.Shared,ContainerObj,usrObj)
  d['SemiShareNames'], d['SemiSharePics']= GetNamesFromIDs(usrObj.SemiShare,ContainerObj,usrObj)
  d['Path']= []
  d['PathID']= []
  return d

#clikc on a container, returns the things in the container
def GetThingsInContainer(ContainerID):
  container = GetContainer(ContainerID)
  d = {}
  d['Items']       = container.Items
  d['ItemsNames'], d['ItemsPics']   = GetNamesFromIDs(container.Items, ItemObj, container)
  #sort
  if len(d['ItemsNames']) >0:
    d['ItemsNames'], d['ItemsPics'],d['Items'] = (list(t) for t in zip(*sorted(zip(d['ItemsNames'], d['ItemsPics'],d['Items']))))

  d['Containers']   = container.Containers
  d['ContainersNames'],d['ContainersPics']  = GetNamesFromIDs(container.Containers, ContainerObj, container)  
  if len(d['ContainersNames']) >0:#sort
    d['ContainersNames'], d['ContainersPics'],d['Containers'] = (list(t) for t in zip(*sorted(zip(d['ContainersNames'], d['ContainersPics'],d['Containers']))))

  d['Path']= container.Path ##todo get names from path id
  d['PathID']= container.PathID

  return d



