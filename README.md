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

Once you have unzipped the release ZIP archive to a directory of your choice you can deploy any configuration profile using the following command line syntax:

    configdeployer [-ev [-vd <num>] [-vp <file>]] [-ff] profile [profile ...]

Most simple usage:

    configdeployer myprofile.xml

Profiles must be provided as files in the local file system, either with uncompressed XML content or as compressed GZip archives. The application treats files with the extension `.gz` or `.gzip` as compressed profiles.

The basic functionality can be extended by using one or more of the following command line options:

-   `-ev` or `--enable-variables` enables substitution of variables in certain elements of the configuration profile.
    -   Variables are resolved in the `location` attribute of the `properties-file` element and the `driver-class`, `jdbc-url`, `username` and `password` attributes of the `database` element.
    -   `${env:foobar}` will be resolved to the environment variable `foobar`.
    -   `${sys:user.home}` will be resolved to the Java system property `user.home` (the current user's home directory)
-   `-vd #` or `--variables-depth=#` sets the number of iterations for variable substitution to the given value (can only be used in conjunction with the `-ev` option). (Default = 1)
-   `-vp file` or `--variables-properties=file` specifies a separate properties file containing variable values (can only be used in conjunction with the `-ev` option). The pattern `${p:foobar}` will be resolved to the value of the property `foobar` from the given file.
-   `-ff` or `--fail-fast` forces the application to stop the deployment immediately once a failure occurs. Note that changes which have already been applied to resources are *not* rolled-back.

*Important:* If your profile needs to update databases you must put the Java library containing the appropriate JDBC driver into the `lib` directory. See the manual of your database vendor for more information where to find this library.

Other examples
--------------

Deploying a profile which contains variables:

    configdeployer -ev myprofile.xml

Deploying a profile with variables that should be resolved using an external properties file:

    configdeployer -ev -vp values.properties myprofile.xml

Deploying three profiles but abort deployment immediately if an error occurs:

    configdeployer -ff one.xml two.xml three.xml

Deploying a profile compressed in a GZip archive:

    configdeployer myprofile.xml.gz
