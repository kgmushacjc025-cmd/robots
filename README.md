# Toy Robot Multiplayer Game

## Overview

This is a Java-based command-line multiplayer game where users launch robots into a shared world, connect multiple clients to a central server, and interact using JSON-based commands. The project is modular and includes unit tests for core functionality.

## Features

- Launch robots into a grid-based world
- Connect multiple clients to a server
- Send and receive JSON messages between client and server
- Extendable command system for new gameplay features
- Comprehensive unit tests for server and client logic

## Getting Started

### Prerequisites

- Java 21
- Maven (for building and running tests)

### Clone the Repository

```sh
git clone <repo-url>
cd oop-ex-toy-robot-group
```

### Build the Project

```sh
mvn clean compile
```

### Run the Server

```sh
mvn exec:java@run-server
#mvn exec:java -Dexec.mainClass="za.co.wethinkcode.robots.server.Server"

```
### Run the Client

```sh
 mvn exec:java@run-client  
#mvn exec:java -Dexec.mainClass="za.co.wethinkcode.robots.client.ClientMain" -Dexec.args="--host 127.0.0.1 --port 5000"
```

## Usage

When the client starts, you can enter commands such as:

- `launch <make> <name>` — Launch a robot
- `look` — Get objects in view
- `state` — Get robot state
- `dump` — Print the world
- `quit` — Disconnect

Example:

```
launch sniper Hal
look
state
quit
```

## Project Structure

```
oop-ex-toy-robot-group/
├── src/
│   ├── main/java/za/co/wethinkcode/robots/
│   │   ├── server/         # Server-side logic
│   │   └── client/         # Client-side logic
│   └── test/java/za/co/wethinkcode/robots/
│       ├── server/         # Server tests
│       └── client/         # Client tests
├── pom.xml                 # Maven build file
└── README.md               # Project documentation
```

## Testing

Run all unit tests with:

```sh
mvn test
```

## Contributing

- Follow Java best practices and code style
- Write or update tests for new features
- Document code and update README as the project evolves

## Authors

Group (?)
