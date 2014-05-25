ConfigDeployer Console Application
==================================

ConfigDeployer Console is a Java application designed for the deployment of software configurations in different installation environments using XML format profiles. It is based on the equally named core framework *ConfigDeployer*.

Getting started
---------------

### Obtaining the source code

Install *Git* on your computer, open the *Git Bash* and navigate to the directory where you want to check-out the repository. Then execute the following command:

    git clone https://github.com/georgwittberger/configdeployer-console.git

You will get a new sub-directory named `configdeployer-console` which contains the Maven project.

### Building the binaries

Install *Maven* on your computer, open a terminal and navigate to the directory where the ConfigDeployer project resides. Then execute the following command:

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

Please refer to the *ConfigDeployer Core Module* documentation for more info on creating a profile.

Deploying a profile
-------------------

Once you have unzipped the release ZIP archive to a directory of your choice you can deploy any configuration profile using this simple command line syntax:

On Unix:

    ./configdeployer.sh profile1.xml profile2.xml ... profileN.xml

On Windows:

    configdeployer.cmd profile1.xml profile2.xml ... profileN.xml

If your profile is compressed inside a GZip archive you may also pass this file to the application:

    ./configdeployer.sh profile1.gz

The console application uses the `ProfileDeployer` class together with the `VariablesPreparer` to enable variable substitution in certain elements of the configuration profile. You can use the following variable patterns:

-   `${env:foo}` - Resolves to the value of the environment variable `foo`.
-   `${sys:bar}` - Resolves to the value of the Java system property `bar`.

Replacement is performed with a depth of 2 which allows you to have environment variables containing another variable pattern.

Please refer to the *ConfigDeployer Core Module* documentation for more info on the deployment process.
