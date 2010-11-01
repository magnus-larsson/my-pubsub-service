====
    /**
     * Copyright 2010 Västra Götalandsregionen
     *
     *   This library is free software; you can redistribute it and/or modify
     *   it under the terms of version 2.1 of the GNU Lesser General Public
     *   License as published by the Free Software Foundation.
     *
     *   This library is distributed in the hope that it will be useful,
     *   but WITHOUT ANY WARRANTY; without even the implied warranty of
     *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     *   GNU Lesser General Public License for more details.
     *
     *   You should have received a copy of the GNU Lesser General Public
     *   License along with this library; if not, write to the
     *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
     *   Boston, MA 02111-1307  USA
     *
     */
====

Run test instance:
Deply to Tomcat or similar

Run a publishing web server:
(Requires Python. But, any web server can be used instead)

cd to test-repo
python -m SimpleHTTPServer

Test publishing:
curl http://localhost:8080/pubsubhubbub-hub-module-web/ -v -d hub.mode=publish -d hub.url=http://localhost:8000/0e1383718a2889c12af18febb1a2e3de

curl http://localhost:8080/pubsubhubbub-hub-module-web/ -v -d hub.mode=publish -d hub.url=http://feeds.feedburner.com/protocol7/main
