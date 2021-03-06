package org.jetbrains.postfixCompletion.completion;

import com.intellij.codeInsight.completion.CompletionAutoPopupTestCase;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.postfixCompletion.settings.PostfixCompletionSettings;
import org.jetbrains.postfixCompletion.templates.InstanceofExpressionPostfixTemplate;
import org.jetbrains.postfixCompletion.templates.PostfixTemplate;
import org.jetbrains.postfixCompletion.templates.SwitchStatementPostfixTemplate;

public class TemplatesCompletionTest extends CompletionAutoPopupTestCase {
  public void testDoNotShowTemplateInInappropriateContext() {
    doAutoPopupTest("instanceof", null);
  }

  public void testShowTemplateInAutoPopup() {
    doAutoPopupTest("instanceof", InstanceofExpressionPostfixTemplate.class);
  }

  public void testDoNotShowTemplateIfPluginIsDisabled() {
    PostfixCompletionSettings settings = PostfixCompletionSettings.getInstance();
    assertNotNull(settings);
    settings.setPostfixPluginEnabled(false);
    doAutoPopupTest("instanceof", null);
  }
  
  public void testDoNotShowTemplateIfTemplateCompletionIsDisabled() {
    PostfixCompletionSettings settings = PostfixCompletionSettings.getInstance();
    assertNotNull(settings);
    settings.setTemplatesCompletionEnabled(false);
    doAutoPopupTest("instanceof", null);
  }

  public void testShowTemplateOnDoubleLiteral() {
    doAutoPopupTest("switch", SwitchStatementPostfixTemplate.class);
  }

  public void testSelectTemplateByTab() {
    doCompleteTest("par", '\t');
  }

  public void testSelectTemplateByEnter() {
    doCompleteTest("par", '\n');
  }

  public void testQuickTypingWithTab() {
    doQuickTypingTest("par", '\t');
  }

  public void testQuickTypingWithEnter() {
    doQuickTypingTest("par", '\n');
  }

  public void testDoNotShowDisabledTemplate() {
    PostfixCompletionSettings settings = PostfixCompletionSettings.getInstance();
    assertNotNull(settings);
    settings.disableTemplate(new InstanceofExpressionPostfixTemplate());
    doAutoPopupTest("instanceof", null);
  }

  public void testDoNotShowTemplateOnCompletion() {
    configureByFile();
    myFixture.completeBasic();
    LookupElement[] elements = myFixture.getLookupElements();
    assertNotNull(elements);
    assertNull(ContainerUtil.findInstance(elements, PostfixTemplateLookupElement.class));
  }

  public void testRecalculatePrefix() {
    configureByFile();
    type("par");
    myFixture.assertPreferredCompletionItems(1, ".par", "parents");

    type("\b");
    assertNotNull(getLookup());
    myFixture.assertPreferredCompletionItems(0, "parents");

    type("r");
    myFixture.assertPreferredCompletionItems(1, ".par", "parents");
  }

  @Override
  public void tearDown() throws Exception {
    PostfixCompletionSettings settings = PostfixCompletionSettings.getInstance();
    assertNotNull(settings);
    settings.setTemplatesState(ContainerUtil.<String, Boolean>newHashMap());
    settings.setPostfixPluginEnabled(true);
    settings.setTemplatesCompletionEnabled(true);
    super.tearDown();
  }

  @Override
  protected String getTestDataPath() {
    return "testData/completion";
  }

  private void doQuickTypingTest(String textToType, char c) {
    configureByFile();
    myFixture.type(textToType + c);
    checkResultByFile();
  }

  private void doCompleteTest(String textToType, char c) {
    configureByFile();
    type(textToType);
    assertNotNull(getLookup());
    myFixture.type(c);
    checkResultByFile();
  }

  private void doAutoPopupTest(@NotNull String textToType, @Nullable Class<? extends PostfixTemplate> expectedClass) {
    configureByFile();
    type(textToType);
    LookupImpl lookup = getLookup();
    if (expectedClass != null) {
      assertNotNull(lookup);
      LookupElement item = lookup.getCurrentItem();
      assertNotNull(item);
      assertInstanceOf(item, PostfixTemplateLookupElement.class);
      assertInstanceOf(((PostfixTemplateLookupElement)item).getPostfixTemplate(), expectedClass);
    }
    else {
      assertNull(lookup);
    }
  }

  private void configureByFile() {
    edt(new Runnable() {
      @Override
      public void run() {
        myFixture.configureByFile(getTestName(true) + ".java");
      }
    });
  }

  private void checkResultByFile() {
    myFixture.checkResultByFile(getTestName(true) + "_after.java");
  }
}
