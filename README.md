# Distributed Ring
DHT in five nodes

## Description

This program consists of two parts:
- A package containing the Ring Distributed
- A package containing the Client Distributed Ring


## Goals

- Load Balancing
- Replicaiton
- Consistency Through Quorum
- Versioning
- Dynamic Addition of New Servers to the Ring
- Failure detection and recovery: Anti-entropy


## How it wotks?

First of all you need start the Distributed Ring. To do that, you need run the file build.xml, this file starts five node in a parallel way. After this operation you have the Distributed Ring running.

The last part to run the application is start a client. When you start a client you can choose the options appeared in the menu and store data in the Distributed Ring.


## Copyright

This software is authored by (c) 2015 Manuel Rojo Horno <manuelrojohorno@gmail.com>


## License

This software is distributed under Eclipse Public License. See: https://www.eclipse.org/legal/epl-v10.html
