sbt-play-boilerplate-examples
---------------------------

This project will show you how to use [`sbt-play-boilerplate`](https://github.com/Romastyi/sbt-play-boilerplate) plugin. There is a project for the client and a project for the server.

Open a terminal go into this project base dir and type `sbt`.

The example is provided with a `yaml` file that is copy pasted from swagger `simple_petstore_example`.
This file is located into `src/main/swagger` (in both `client` and `server` projects) that is the default directory where the plugin will look for swagger files.

##How to run

Here are instructions to programmatically check your services.

Open two terminals into the root directory of this project.

In the first run:
```
sbt ";compile;server/run"
```

Into the second terminal run:
```
sbt ";compile;client/run 9001"
```

Then, to trigger the client run, you should try to open http://localhost:9001 from whatever ```curl``` or the browser and you should check the output generated.
