import config.Config;
import config.Properties;
import exceptions.ConfigurationInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import props.Env;
import props.Spring;

import java.util.Map;

@Test
public class TestClass {
    private static final Logger LOG = LoggerFactory.getLogger(TestClass.class);

    @BeforeClass
    public void setUp() throws ConfigurationInitializationException {
        if (!Config.init()) {
            throw new ConfigurationInitializationException("There were errors during config initialization");
        }
    }

    @Test(testName = "Validate access to property via enum", dataProvider = "propertyProvider")
    public void testGetProperty(Object[] params) {
        Properties prop = (Properties) params[0];
        LOG.info("Testing param with name " + prop.getPropName());
        Assert.assertNotNull(Config.get(prop),
                "Не удалось считать значение свойства " + prop.getPropName());
    }

    @Test(testName = "Validate access to properties list via config class", dataProvider = "configProvider")
    public void testGetConfig(Object[] params) {
        Class<? extends Properties> configClass = (Class) params[0];
        LOG.info("Testing config with name " + configClass.getName());
        final Map<Properties, String> config = Config.getConfig(configClass);
        Assert.assertTrue(config != null && !config.isEmpty(), "Не удалось считать");
    }


    @DataProvider(name = "propertyProvider")
    private Object[][] provideProperty() {
        return new Object[][]{
                {Spring.LOG_FILE},
                {Env.DB_HOST}
        };
    }

    @DataProvider(name = "configProvider")
    private Object[][] provideConfig() {
        return new Object[][]{
                {Spring.class},
                {Env.class}
        };
    }
}
