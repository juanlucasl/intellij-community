// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.intellij.codeInsight.daemon.impl.quickfix;

import com.intellij.codeInsight.daemon.QuickFixBundle;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateBuilderImpl;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class RenameWrongRefFix implements IntentionAction {
  private final PsiReferenceExpression myRefExpr;
  @NonNls private static final String INPUT_VARIABLE_NAME = "INPUTVAR";
  @NonNls private static final String OTHER_VARIABLE_NAME = "OTHERVAR";
  private final boolean myUnresolvedOnly;

  public RenameWrongRefFix(@NotNull PsiReferenceExpression refExpr) {
    this(refExpr, false);
  }

  public RenameWrongRefFix(@NotNull PsiReferenceExpression refExpr, final boolean unresolvedOnly) {
    myRefExpr = refExpr;
    myUnresolvedOnly = unresolvedOnly;
  }

  @Override
  @NotNull
  public String getText() {
    return QuickFixBundle.message("rename.wrong.reference.text");
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return QuickFixBundle.message("rename.wrong.reference.family");
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    if (!myRefExpr.isValid() || !BaseIntentionAction.canModify(myRefExpr)) return false;
    PsiElement refName = myRefExpr.getReferenceNameElement();
    if (refName == null) return false;

    return !CreateFromUsageUtils.isValidReference(myRefExpr, myUnresolvedOnly);
  }

  private LookupElement @NotNull [] collectItems() {
    Set<LookupElement> items = new LinkedHashSet<>();
    boolean qualified = myRefExpr.getQualifierExpression() != null;

    if (!qualified && !(myRefExpr.getParent() instanceof PsiMethodCallExpression)) {
      PsiVariable[] vars = CreateFromUsageUtils.guessMatchingVariables(myRefExpr);
      for (PsiVariable var : vars) {
        items.add(createLookupElement(var, v-> v.getName()));
      }
    } else {
      class MyScopeProcessor implements PsiScopeProcessor {
        final ArrayList<PsiElement> myResult = new ArrayList<>();
        final boolean myFilterMethods;
        boolean myFilterStatics;

        MyScopeProcessor(PsiReferenceExpression refExpression) {
          myFilterMethods = refExpression.getParent() instanceof PsiMethodCallExpression;
          PsiExpression qualifier = refExpression.getQualifierExpression();
          if (qualifier instanceof PsiReferenceExpression) {
            PsiElement e = ((PsiReferenceExpression) qualifier).resolve();
            myFilterStatics = e instanceof PsiClass;
          } else if (qualifier == null) {
            PsiModifierListOwner scope = PsiTreeUtil.getParentOfType(refExpression, PsiModifierListOwner.class);
            myFilterStatics = scope != null && scope.hasModifierProperty(PsiModifier.STATIC);
          }
        }

        @Override
        public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
          if (element instanceof PsiNamedElement
              && element instanceof PsiModifierListOwner
              && myFilterMethods == element instanceof PsiMethod) {
            if (((PsiModifierListOwner)element).hasModifierProperty(PsiModifier.STATIC) == myFilterStatics) {
              myResult.add(element);
            }
          }
          return true;
        }

        public PsiElement[] getVariants () {
          return PsiUtilCore.toPsiElementArray(myResult);
        }
      }

      if (!ApplicationManager.getApplication().isUnitTestMode()) items.add(createLookupElement(myRefExpr, r -> r.getReferenceName()));
      MyScopeProcessor processor = new MyScopeProcessor(myRefExpr);
      myRefExpr.processVariants(processor);
      PsiElement[] variants = processor.getVariants();
      for (PsiElement variant : variants) {
        items.add(createLookupElement((PsiNamedElement)variant, v -> v.getName()));
      }
    }

    return items.toArray(LookupElement.EMPTY_ARRAY);
  }

  @NotNull
  private static <T extends PsiElement> LookupElementBuilder createLookupElement(T variant, Function<? super T, String> toPresentableElement) {
    return LookupElementBuilder.create(variant, toPresentableElement.apply(variant));
  }

  @Override
  public void invoke(@NotNull Project project, final Editor editor, PsiFile file) {
    PsiReferenceExpression[] refs = CreateFromUsageUtils.collectExpressions(myRefExpr, PsiMember.class, PsiFile.class);
    PsiElement element = PsiTreeUtil.getParentOfType(myRefExpr, PsiMember.class, PsiFile.class);
    if (element == null) return;
    LookupElement[] items = collectItems();
    ReferenceNameExpression refExpr = new ReferenceNameExpression(items, myRefExpr.getReferenceName());

    TemplateBuilderImpl builder = new TemplateBuilderImpl(element);
    for (PsiReferenceExpression expr : refs) {
      if (!expr.equals(myRefExpr)) {
        builder.replaceElement(expr.getReferenceNameElement(), OTHER_VARIABLE_NAME, INPUT_VARIABLE_NAME, false);
      }
      else {
        builder.replaceElement(expr.getReferenceNameElement(), INPUT_VARIABLE_NAME, refExpr, true);
      }
    }

    final float proportion = EditorUtil.calcVerticalScrollProportion(editor);
    editor.getCaretModel().moveToOffset(element.getTextRange().getStartOffset());

    Template template = builder.buildInlineTemplate();
    editor.getCaretModel().moveToOffset(element.getTextRange().getStartOffset());

    TemplateManager.getInstance(project).startTemplate(editor, template);

    EditorUtil.setVerticalScrollProportion(editor, proportion);
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
