# gisDTM: an interactive Digital Elevation Model visualizer

## Build and Run
The program is not packaged nor it has a well-tested Makefile since it is a class assignment.
The right way to build it is to import the project in the NetBeans IDE and run the `GUI.MainForm` class.

The Java 3D library must be already installed on the system in order for the build to end correctly.

## Test data
The repo ships with a ready available database of points representing the North area of Trieste. The elevation data is freely available at the [Friuli Venezia Giulia website](http://www.regione.fvg.it/)

![Screenshot of gisDTM](https://github.com/zarelit/gisDTM/raw/master/screenshot.jpg)

## Features

  * Import/Export from GPX and grid formatted (.XYZ)
  * Possibility to cut the available data to a specific subregion
  * Interactive 3D visualization with pan/zoom/tilt

## Authors
David Costa <david@zarel.net>
Giuliano Peraz <giuliano.peraz@gmail.com>