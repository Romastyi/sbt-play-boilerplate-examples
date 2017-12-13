sbt-play-boilerplate-examples
---------------------------

This project will show you how to use [`sbt-play-boilerplate`](https://github.com/Romastyi/sbt-play-boilerplate) plugin. 

# Overview

This project is simulated system with 2 micro-services and a web gateway:

* **auth service**: simple implementation of user authorization service.
* **petStore service**: manages items of a pet store. 
* **web-gateway**: application providing web UI and acting as gateway to services.

## How to run

Open two terminals into the root directory of this project.

First of all compile project with command:
```
sbt clean compile
```

In the first run **petStore service**:
```
sbt "petStore-impl/run 9001"
```

Into the second terminal run **web-gateway**:
```
sbt "web-gateway/run 9000"
```

After that you should try to open **[web UI](http://localhost:9000)** from the browser.

### Authorization
 
**Login**: _XXX@example.com_ (where _XXX_ - one of roles' name: _admin_, _user_, _api_. For example: _admin@example.com_)
**Password**: _pass_
