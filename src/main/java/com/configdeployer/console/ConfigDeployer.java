package com.configdeployer.console;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.configdeployer.deployer.DeployerException;
import com.configdeployer.deployer.ProfileDeployer;
import com.configdeployer.preparer.EnvironmentVariablesResolver;
import com.configdeployer.preparer.SystemPropertiesResolver;
import com.configdeployer.preparer.VariablesPreparer;
import com.configdeployer.provider.FileInputStreamProvider;
import com.configdeployer.provider.GZipInputStreamProvider;
import com.configdeployer.provider.InputStreamProvider;
import com.configdeployer.provider.ProfileProvider;

public class ConfigDeployer
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigDeployer.class);

    public static void main(String[] args)
    {
        if (args == null || args.length < 1)
        {
            System.out.println("ConfigDeployer - Easy profile-based configuration deployment.");
            System.out.println();
            System.out.println("Usage:             configdeployer profile1.xml [profile2.xml] ... [profileN.xml]");
            System.out.println("Example (Windows): configdeployer.cmd %UserProfile%\\profiles\\myprofile.xml");
            System.out.println("Example (Unix):    ./configdeployer.sh ~/profiles/myprofile.xml");
            System.exit(1);
        }
        boolean success = true;
        for (String argument : args)
        {
            InputStreamProvider inputStreamProvider = new FileInputStreamProvider(new File(argument));
            if (argument.endsWith(".gz") || argument.endsWith(".gzip"))
            {
                inputStreamProvider = new GZipInputStreamProvider(inputStreamProvider);
            }
            try
            {
                VariablesPreparer variablesPreparer = new VariablesPreparer();
                variablesPreparer.setDepth(2);
                variablesPreparer.addResolver("env", new EnvironmentVariablesResolver());
                variablesPreparer.addResolver("sys", new SystemPropertiesResolver());
                ProfileDeployer profileDeployer = new ProfileDeployer(new ProfileProvider(inputStreamProvider),
                        variablesPreparer);
                success &= profileDeployer.deploy();
            }
            catch (DeployerException e)
            {
                success = false;
                logger.error("Could not deploy profile: " + argument, e);
            }
        }
        if (success)
        {
            logger.info("DEPLOYMENT SUCCESSFUL");
        }
        else
        {
            logger.error("DEPLOYMENT FAILED - See output for error messages.");
        }
        System.exit(success ? 0 : 1);
    }

}
