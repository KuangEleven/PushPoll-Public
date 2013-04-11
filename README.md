PushPoll-Public
===============

Open Source code for the PushPoll Android App, an app that allows for rapid polling of friends

All code here is released under the GNU General Public License

Running this code will require some additional toolchain setup:

ActionBar Sherlock must be referenced as a library from PushPoll, from sources grabbed directly from GitHub. As of the last time I checked, the released version had a bug that prevented proper functioning of PushPoll.

A Keys.java file should be made with the following entries:
public static final String GCM_KEY //Key for use with the GCM service for notification
public static final String ANALYTICS_KEY //Key for use with Google Analytics

Copyright 2011 Bay Grabowski

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
