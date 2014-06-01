ConfigDeployer Console Application
==================================

ConfigDeployer Console is a Java application designed for the deployment of software configurations in different installation environments using XML format profiles. It is based on the [ConfigDeployer Core Module](https://github.com/georgwittberger/configdeployer-core).

Getting started
---------------

### Obtaining the source code

Install [Git](http://git-scm.com/downloads) on your computer, open the Git Bash and navigate to the directory where you want to check-out the repository. Then execute the following command:

    git clone https://github.com/georgwittberger/configdeployer-console.git

You will get a new sub-directory named `configdeployer-console` which contains the Maven project.

### Building the binaries

Install [Maven](http://maven.apache.org/download.cgi) on your computer, open a terminal and navigate to the directory where the ConfigDeployer project resides. Then execute the following command:

    mvn install

You will get a new sub-directory named `target` which contains the binary JAR file `configdeployer-console-VERSION.jar` and a release ZIP archive `configdeployer-console-VERSION-release.zip`. Note that *VERSION* is the version number you have downloaded.

The release ZIP is a ready-to-use package for distribution. It contains the following items:

    /
    +- lib/*                -- all required Java libraries
    +- configdeployer.cmd   -- Windows command script for execution
    +- configdeployer.sh    -- Unix bash script for execution
    +- LICENSE.txt          -- License agreement
    +- README.md            -- The file you are reading

Creating a profile
------------------

Please refer to the [ConfigDeployer Core Module](https://github.com/georgwittberger/configdeployer-core) documentation for more information.

Deploying a profile
-------------------

Once you have unzipped the release ZIP archive to a directory of your choice you can deploy any configuration profile using this simple command line syntax:

On Unix:

    ./configdeployer.sh profile1.xml profile2.xml ... profileN.xml

On Windows:

    configdeployer.cmd profile1.xml profile2.xml ... profileN.xml

If your profile is compressed inside a GZip archive you can also pass this file to the application:

    ./configdeployer.sh profile1.xml.gz

The console application uses the default `ProfileDeployer` class together with the `FileInputStreamProvider` to load and deploy configuration profiles from the local file system. Additionally, the `VariablesPreparer` is plugged in to enable variable substitution in certain elements of the profile (location of properties files and connection settings of databases). Settings of the variables resolvers:

-   Environment variables can be accessed with the `env` prefix, e.g. `${env:foo}` resolves to the value of the environment variable `foo`.
-   Java system properties can be accessed with the `sys` prefix, e.g. `${sys:user.home}` resolves to home directory of the user executing the program.
-   Replacement is performed twice (depth = 2), i.e. you can have environment variables which contain another variable pattern in their value.

Please refer to the [ConfigDeployer Core Module](https://github.com/georgwittberger/configdeployer-core) documentation for more information.
