import sys
import os

# Asegurar que el directorio actual esté en el PATH de Python
sys.path.insert(0, os.path.dirname(__file__))

from a2wsgi import ASGIMiddleware
from app.main import app

# Passenger en cPanel busca la variable 'application'
application = ASGIMiddleware(app)
