package ru.adserver.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * ApplicationLauncher
 *
 * @author Kontsur Alex (bona)
 * @since 12.11.2015
 */
public class ApplicationLauncher {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationLauncher.class);
    private static final String DEFAULT_CONTEXT_ROOT = "classpath*:beans.xml";

    public static void main(String... args) {
        try {
            String contextRoot = null;
            List<String> profiles = new ArrayList<>();

            if (args.length > 1) {
                for (String arg : args) {
                    if (arg.startsWith("-C")) {
                        if (contextRoot == null) {
                            contextRoot = arg.substring(2);
                        } else {
                            logger.error(String.format("ERROR: Detected multiple context roots: [%s]", arg));
                        }
                    } else if (arg.startsWith("-P")) {
                        profiles.add(arg.substring(2));
                    } else {
                        logger.error(String.format("ERROR: Wrong parameter specified: [%s]", arg));
                    }
                }
            } else if (args.length == 1) {
                String arg = args[0];
                if (arg.startsWith("-C")) {
                    contextRoot = arg.substring(2);
                } else {
                    contextRoot = arg;
                }
            }

            if (contextRoot == null) {
                contextRoot = DEFAULT_CONTEXT_ROOT;
            }
            logger.info(String.format("Starting application from: [%s]", contextRoot));
            ClassPathXmlApplicationContext context;
            if (profiles.isEmpty()) {
                context = new ClassPathXmlApplicationContext(new String[]{contextRoot});
            } else {
                logger.info(String.format("Using specified profiles: %s", profiles));
                context = new ClassPathXmlApplicationContext(new String[]{contextRoot}, false);
                ConfigurableEnvironment environment = context.getEnvironment();
                environment.setActiveProfiles(profiles.toArray(new String[profiles.size()]));
                String array = StringUtils.arrayToDelimitedString(environment.getActiveProfiles(), ",");
                System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, array);
                context.refresh();
            }
            context.registerShutdownHook();
            logger.info("Application successfully started");
        } catch (Exception th) {
            logger.error("ERROR: Application failed to start", th);
        }
    }
    
}
