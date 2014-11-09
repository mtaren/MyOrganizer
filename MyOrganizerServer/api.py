import webapp2
from google.appengine.api import users
from google.appengine.api import oauth
from apiclient.discovery import build
import logging as log

class GetUser(webapp2.RequestHandler):

    def get(self):
        user = users.get_current_user()
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.out.write('Hello, {}\n'.format('None' if user is None else user.nickname()))
        try:
            #user = oauth.get_current_user()
            #self.response.out.write('Hello OAuth, {}\n'.format('None' if user is None else user.nickname()))
            log.info("enter")
            access_token = self.request.headers['Authorization']
            log.info("here")
            resp = build('oauth2', 'v1').tokeninfo(access_token=access_token).execute()
            log.info("finished resp")
            log.info(resp)
            self.response.out.write(str(resp))
        except Exception as e:
            self.response.out.write(str(e)+'\n')

class SignIn(webapp2.RequestHandler):
    def get(self):
        if users.get_current_user() is None:
            self.redirect(users.create_login_url(self.request.uri))

APP = webapp2.WSGIApplication([
    ('/api/user', GetUser),
    ('/api/signin', SignIn),
], debug = True)