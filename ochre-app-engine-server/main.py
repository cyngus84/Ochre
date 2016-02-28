"""`main` is the top level module for your Flask application."""

# Import the Flask Framework
from flask import Flask
from flask import request
from flask import Response
from google.appengine.api import memcache
import logging
app = Flask(__name__)
# Note: We don't need to call run() since our application is embedded within
# the App Engine WSGI application server.

from google.appengine.ext import ndb

primary_url = <DESIRED URL HERE>

@app.route('/')
def hello():
    """Return a friendly HTTP greeting."""
    return 'Hello World!'


@app.errorhandler(404)
def page_not_found(e):
    """Return a custom 404 error."""
    return 'Sorry, Nothing at this URL.', 404


@app.errorhandler(500)
def application_error(e):
    """Return a custom 500 error."""
    return 'Sorry, unexpected error: {}'.format(e), 500


@app.route(primary_url, methods=['POST'])
def game_post(game_id):
    # access the existing game record, if there is one
    game_data = GameData.query(GameData.id==game_id)
    target = game_data.get()

    # if the game record doesn't exist, create it
    if target is None:
        target = GameData(game_data=request.form['data'],
                          id=game_id)
        logging.info('New record created.')
    else:
        target.game_data = game_data=request.form['data']
        logging.info('Existing record updated.')

    # store updated or created game data
    target.put()

    memcache.delete(game_id)

    return 'Stored update for ' + game_id
    # request.form['data']


@app.route(primary_url, methods=['GET'])
def game_get(game_id):
    cache_data = memcache.get(game_id)
    resp_data = None

    if cache_data is None:
        game_data = GameData.query(GameData.id==game_id)
        game = game_data.get()
        if game is None:
            return 'Sorry, game \'' + game_id + '\' is unknown', 404
        else:
            memcache.set(game_id, game.game_data)
            resp_data = game.game_data
    else:
        resp_data = cache_data

    return Response(response=resp_data,
                    mimetype="application/json",
                    status=200)

# Data model
class GameData(ndb.Model):
    game_data = ndb.StringProperty(indexed=False)
    id = ndb.StringProperty()
