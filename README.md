- [ancillary](#org3fabde8)
  - [Installation](#orgac31d11)
    - [Build from source](#orgbcd4a7e)
    - [Download Jar from Github](#org32961e8)
  - [Running Ancillary](#orgc6a3e9e)
  - [Writing Plugins](#org464e6cf)
  - [License](#orgf8e5f37)



<a id="org3fabde8"></a>

# ancillary

Ancillary is a monitor for your service that runs on tha Java JVM runtime for monitoring Java based applications and for running commandline programs for testing a service&rsquo;s status on the local machine for the purpose of determining if the cluster is healthy and/or if the local instance is healthy and servicing requests.

Ancillary uses a dynamic class loader to load plugins that are compiled separately in their own jars.

Ancillary was inspired by an internal program we wrote in Python that would check cluster health during a deploy to ensure there was no downtime while taking down cluster members to replace them. There were a few instances where Python proved to be lacking when these clusters spoke Java, and Python did not.


<a id="orgac31d11"></a>

## Installation


<a id="orgbcd4a7e"></a>

### Build from source

**Requirements:** Leiningen

```sh
git clone https://github.com/timotheosh/ancillary.git
cd ancillary
lein uberjar
```


<a id="org32961e8"></a>

### Download Jar from Github

Just click on releases, download the latest release jar, and you are ready.


<a id="orgc6a3e9e"></a>

## Running Ancillary

Run the following:

```sh
jar xvf ancillary-<version>.jar example-ancillary.yml
```

It will extract an example configuration file you can edit to your own needs. Be sure to read the comments in that file for guidance.

You run ancillary with:

```sh
java -jar ancillary-<version>-standalone.jar -c example-ancillary.yml
```


<a id="org464e6cf"></a>

## Writing Plugins

Currently, Ancillary expects to be able to call static methods from the classes you write for your plugins. There is a working example of an Ancillary plugin in my [kafka-health-check project](https://github.com/timotheosh/kafka-health-check.git).

You should be able to just create a simple app with leiningen:

```sh
lein new app myplugin
```

Add the functions that correspond to the preferred methods for your service:

```clojure
(defn GET
  [ctx]
  {:status 200
   :data "iamok"})

(defn POST
  [ctx]
  (....)
```

Note that your functions should return a standard Clojure hash (or java.util.HashMap). The status field informs Ancillary what http status code to use, and Ancillary always sends both status and data to the client. Ancillary uses [Liberator](http://clojure-liberator.github.io/liberator/) REST library, so supports all the http methods Liberator does.

My [kafka-health-check project](https://github.com/timotheosh/kafka-health-check.git) shows an example using GET, and POST, where the client is expected to send Ancillary json data (that had to be handled in your plugin if you use it).

You&rsquo;ll need use gen-class like so for your plugin class:

```clojure
(ns myapp.core
  (:gen-class
   :name kafka_health_check.core
   :methods [#^{:static true}
            [GET [String] java.util.HashMap]
            [POST [String] java.util.HashMap]]]))
```


<a id="orgf8e5f37"></a>

## License

This software is licensed under the MIT License.

Copyright © 2017-2018 Tim Hawes