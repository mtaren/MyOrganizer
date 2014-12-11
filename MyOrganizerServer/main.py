import webapp2
from google.appengine.api import users
from apiclient.discovery import build
import logging as log

from webServices.webServices_Getters import *
from webServices.webServices_Setters import *
from webServices.webServices_Pending import *
from webServices.webServices_Search import *

class GetUser(webapp2.RequestHandler):

    def get(self):
        #self.redirect("/u/home.html")
        user = users.get_current_user()
        #self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('Hello, {}\n'.format('None' if user is None else user.nickname()))
        try:
            #user = oauth.get_current_user()
            #self.response.out.write('Hello OAuth, {}\n'.format('None' if user is None else user.nickname()))
            log("enter")
            access_token = self.request.headers['Authorization']
            resp = build('oauth2', 'v1').tokeninfo(access_token=access_token).execute()
            log("finished resp")
            log(resp)
            self.response.out.write(str(resp['email']))
            #self.response.out.write(user.user_id())
        except Exception as e:
            self.response.out.write(str(e)+'\n')

class SignIn(webapp2.RequestHandler):
    def get(self):
        if users.get_current_user() is None:
            self.redirect(users.create_login_url(self.request.uri))
        else:
            #self.redirect(users.create_logout_url("/u/home.html"))
            self.redirect("/u/home.html")
APP = webapp2.WSGIApplication([
    # ('/', GetHome),
    ('/', GetUser),
    ('/signin', SignIn),
    ('/test', Tester),
    ('/WS_TestCreateThings', WS_TestCreateThings),
    #getters
    ('/WS_GetHouses', WS_GetHouses),
    ('/WS_GetContainer', WS_GetContainer),
    ('/WS_GetBlobUrl', WS_GetBlobUrl),
    ('/WS_GetBlobUrlBatchAdd', WS_GetBlobUrlBatchAdd),
    ('/WS_GetPicture/([^/]+)?', WS_GetPicture),
    ('/WS_GetObjectAttributes', WS_GetObjectAttributes),
    ('/WS_GetOneObjectAttributes', WS_GetOneObjectAttributes),
    ('/WS_GetIDfromQRCode', WS_GetIDfromQRCode),
    #setters
    ('/WS_AddContainer', WS_AddContainer),
    ('/WS_AddPicture', WS_AddPicture),
    ('/WS_AddItem', WS_AddItem),
    ('/WS_Move', WS_Move),
    ('/WS_ModifyObj', WS_ModifyObj),
    ('/WS_DeleteContainer', WS_DeleteContainer),
    ('/WS_DeleteItem', WS_DeleteItem),
    #penders
    ('/WS_FetchPendingEvents', WS_FetchPendingEvents),
    ('/WS_FetchNumPendingEvents', WS_FetchPendingEvents),
    ('/WS_GetPendingEventData', WS_GetPendingEventData),
    ('/WS_GetPendingEventDataBatchAdd', WS_GetPendingEventDataBatchAdd),
    ('/WS_AddPictureToBlankItem', WS_AddPictureToBlankItem),
    ('/WS_ShareContainers', WS_ShareContainers),
    ('/WS_AddPictureToBlankItem', WS_AddPictureToBlankItem),
    
    ('/WS_ResolvePendEvent', WS_ResolvePendEvent),
    #search
    ('/WS_Search', WS_Search)
], debug = True)

## saved for later when incorporating android app
