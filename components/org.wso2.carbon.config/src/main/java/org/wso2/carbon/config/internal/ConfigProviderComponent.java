/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.config.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigConstants;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.config.provider.ConfigProviderImpl;
import org.wso2.carbon.config.reader.ConfigFileReader;
import org.wso2.carbon.config.reader.YAMLBasedConfigFileReader;
import org.wso2.carbon.secvault.SecureVault;
import org.wso2.carbon.utils.Utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This service component is responsible for registering ConfigProvider OSGi service.
 *
 * @since 1.0.0
 */
@Component(
        name = "ConfigProviderComponent",
        immediate = true
)
public class ConfigProviderComponent {
    private static final Logger logger = LoggerFactory.getLogger(ConfigProviderComponent.class);

    @Activate
    protected void activate(BundleContext bundleContext) {
        initializeConfigProvider(bundleContext);
        logger.debug("Carbon Configuration Component activated");
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
        logger.debug("Stopping ConfigProviderComponent");
    }

    @Reference(
            name = "org.wso2.carbon.secvault.securevault.SecureVault",
            service = SecureVault.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterSecureVault"
    )
    protected void registerSecureVault(SecureVault secureVault) {
        ConfigProviderDataHolder.getInstance().setSecureVault(secureVault);
    }

    protected void unRegisterSecureVault(SecureVault secureVault) {
        ConfigProviderDataHolder.getInstance().setSecureVault(null);
    }

    /**
     * Initialise carbon config provider.
     *
     * @param bundleContext OSGi Bundle Context
     */
    private void initializeConfigProvider(BundleContext bundleContext) {
        // TODO: Support getting configProviderImpl in non-OSGi mode
        Path deploymentConfigPath = Paths.get(Utils.getCarbonConfigHome().toString(),
                ConfigConstants.DEPLOYMENT_CONFIG_YAML);
        ConfigFileReader configFileReader = new YAMLBasedConfigFileReader(deploymentConfigPath);
        ConfigProvider configProvider = new ConfigProviderImpl(configFileReader);
        bundleContext.registerService(ConfigProvider.class, configProvider, null);
        logger.debug("ConfigProvider OSGi service registered");
    }
}