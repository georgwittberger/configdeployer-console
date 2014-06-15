package com.configdeployer.console;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.configdeployer.deployer.ProfileDeployer;
import com.configdeployer.preparer.EnvironmentVariablesResolver;
import com.configdeployer.preparer.PreparerException;
import com.configdeployer.preparer.ProfilePreparer;
import com.configdeployer.preparer.PropertiesVariablesResolver;
import com.configdeployer.preparer.SystemPropertiesResolver;
import com.configdeployer.preparer.VariablesPreparer;
import com.configdeployer.provider.FileInputStreamProvider;
import com.configdeployer.provider.GZipInputStreamProvider;
import com.configdeployer.provider.InputStreamProvider;
import com.configdeployer.provider.ProfileProvider;

public class ConfigDeployer
{

    private static final String CLI_SYNOPSIS = "configdeployer [-ev [-vd <num>] [-vp <file>]] [-ff] profile [profile ...]";
    private static final String CLI_DESCRIPTION = "Deploys configuration profiles on the current environment.";
    private static final Logger logger = LoggerFactory.getLogger(ConfigDeployer.class);

    public static void main(String[] args)
    {
        // Phase 1: Create the command line options definition.
        Options commandLineOptions = createCommandLineOptions();
        CommandLineParser commandLineParser = new PosixParser();
        CommandLine commandLine = null;
        try
        {
            commandLine = commandLineParser.parse(commandLineOptions, args, true);
        }
        catch (ParseException e)
        {
            logger.error("Could not parse command line!", e);
            System.exit(1);
        }

        // Phase 2: Check if provided command line arguments are sufficient.
        if (commandLine.getArgs().length < 1)
        {
            HelpFormatter helpGenerator = new HelpFormatter();
            helpGenerator.printHelp(CLI_SYNOPSIS, CLI_DESCRIPTION, commandLineOptions, null);
            System.exit(1);
        }

        // Phase 3: Determine deployment configuration from command line.
        boolean enableVariables = commandLine.hasOption(CommandLineOption.ENABLE_VARIABLES.getName());
        boolean failFast = commandLine.hasOption(CommandLineOption.FAIL_FAST.getName());
        int variablesDepth = 1;
        String variablesPropertiesFile = null;
        if (enableVariables)
        {
            if (commandLine.hasOption(CommandLineOption.VARIABLES_DEPTH.getName()))
            {
                try
                {
                    variablesDepth = Integer.parseInt(commandLine.getOptionValue(CommandLineOption.VARIABLES_DEPTH
                            .getName()));
                }
                catch (NumberFormatException e)
                {
                    logger.error("Argument for parameter '" + CommandLineOption.VARIABLES_DEPTH.getLongName()
                            + "' is not a valid integer number.", e);
                    System.exit(1);
                }
            }
            if (commandLine.hasOption(CommandLineOption.VARIABLES_PROPERTIES.getName()))
            {
                variablesPropertiesFile = commandLine.getOptionValue(CommandLineOption.VARIABLES_PROPERTIES.getName());
            }
        }
        logger.info("Deployment configuration: {} = {}, {} = {}, {} = {}, {} = {}",
                CommandLineOption.ENABLE_VARIABLES.getLongName(), enableVariables,
                CommandLineOption.VARIABLES_DEPTH.getLongName(), variablesDepth,
                CommandLineOption.VARIABLES_PROPERTIES.getLongName(), variablesPropertiesFile,
                CommandLineOption.FAIL_FAST.getLongName(), failFast);

        // Phase 4: Create the profile preparers (reused for every profile).
        List<ProfilePreparer> profilePreparers = new ArrayList<ProfilePreparer>(1);
        if (enableVariables)
        {
            VariablesPreparer variablesPreparer = new VariablesPreparer();
            variablesPreparer.setDepth(variablesDepth);
            variablesPreparer.addResolver("env", new EnvironmentVariablesResolver());
            variablesPreparer.addResolver("sys", new SystemPropertiesResolver());
            if (StringUtils.isNotBlank(variablesPropertiesFile))
            {
                try
                {
                    File propertiesFile = new File(variablesPropertiesFile);
                    if (!propertiesFile.isFile())
                    {
                        logger.error("Properties file for variable substitution not found: " + variablesPropertiesFile);
                        System.exit(1);
                    }
                    variablesPreparer.addResolver("p", new PropertiesVariablesResolver(propertiesFile));
                }
                catch (PreparerException e)
                {
                    logger.error("Could not create properties variables resolver.", e);
                    System.exit(1);
                }
            }
            profilePreparers.add(variablesPreparer);
        }

        // Phase 5: Load and deploy each single configuration profile.
        boolean success = true;
        for (String fileArgument : commandLine.getArgs())
        {
            InputStreamProvider inputStreamProvider = new FileInputStreamProvider(new File(fileArgument));
            if (fileArgument.endsWith(".gz") || fileArgument.endsWith(".gzip"))
            {
                inputStreamProvider = new GZipInputStreamProvider(inputStreamProvider);
            }

            try
            {
                ProfileDeployer profileDeployer = new ProfileDeployer(new ProfileProvider(inputStreamProvider),
                        profilePreparers);
                profileDeployer.setFailFast(failFast);
                success &= profileDeployer.deploy();
            }
            catch (Exception e)
            {
                success = false;
                logger.error("Could not deploy profile: " + fileArgument, e);
            }
            if (!success && failFast)
            {
                logger.info("Aborting deployment because last profile could not be applied successfully.");
                break;
            }
        }

        // Phase 6: Give feedback on the deployment.
        if (success)
        {
            logger.info("DEPLOYMENT SUCCESSFUL");
        }
        else
        {
            logger.error("DEPLOYMENT FAILED - See output for error messages.");
        }
        System.exit(success ? 0 : 2);
    }

    private static Options createCommandLineOptions()
    {
        Options commandLineOptions = new Options();
        for (CommandLineOption option : CommandLineOption.values())
        {
            commandLineOptions.addOption(option.getName(), option.getLongName(), option.isHasArgs(),
                    option.getDescription());
        }
        return commandLineOptions;
    }

    private static enum CommandLineOption
    {
        ENABLE_VARIABLES("ev", "enable-variables", false,
                "Enable variable substitution in certain elements of the profile."),
        VARIABLES_DEPTH("vd", "variables-depth", true,
                "Sets the number of cycles for variable substitution (default = 1)."),
        VARIABLES_PROPERTIES("vp", "variables-properties", true,
                "Sets the properties file to use for variable substitution."),
        FAIL_FAST("ff", "fail-fast", false, "Abort immediately if deployment of a profile fails.");

        private final String name;
        private final String longName;
        private final boolean hasArgs;
        private final String description;

        private CommandLineOption(String name, String longName, boolean hasArgs, String description)
        {
            this.name = name;
            this.longName = longName;
            this.hasArgs = hasArgs;
            this.description = description;
        }

        public String getName()
        {
            return name;
        }

        public String getLongName()
        {
            return longName;
        }

        public boolean isHasArgs()
        {
            return hasArgs;
        }

        public String getDescription()
        {
            return description;
        }

    }

}
