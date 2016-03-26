package com.empcraft.xpbank.test;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.test.helpers.ConfigHelper;
import com.empcraft.xpbank.text.Text;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

@PrepareForTest({ ExpBankConfig.class, JavaPlugin.class })
public class LanguageProvidedTest {

  @Rule
  public PowerMockRule rule = new PowerMockRule();

  private static final Logger LOG = Logger.getLogger(LanguageProvidedTest.class.getName());

  private Text[] testTokens = Text.values();

  private String[] languages = {};

  @Test
  public void testEnglish() throws ConfigurationException, FileNotFoundException, IOException,
      InvalidConfigurationException, URISyntaxException {
    ExpBankConfig config = ConfigHelper.getFakeConfig().withLanguage("english").build();
    YamlLanguageProvider ylp = new YamlLanguageProvider(config);

    for (Text token : testTokens) {
      String message = ylp.getMessage(token);
      Assert.assertNotNull(message);
      Assert.assertNotEquals("", message);
    }
  }

  @Test
  public void testAllLanguages() throws FileNotFoundException, ConfigurationException, IOException,
      InvalidConfigurationException {
    for (String lang : languages) {
      if ("english".equals(lang)) {
        continue;
      }

      testLanguage(lang);
    }
  }

  private void testLanguage(String lang) throws FileNotFoundException, ConfigurationException,
      IOException, InvalidConfigurationException {
    ExpBankConfig config = ConfigHelper.getFakeConfig().withLanguage(lang).build();
    YamlLanguageProvider ylp = new YamlLanguageProvider(config);

    for (Text token : testTokens) {
      String message = ylp.getMessage(token);
      Assume.assumeNotNull(message);
      Assume.assumeFalse(!"".equals(message));
    }
  }

}
