// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.idea.quickfix

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.util.application.isUnitTestMode
import org.jetbrains.kotlin.psi.CREATE_BY_PATTERN_MAY_NOT_REFORMAT
import org.jetbrains.kotlin.psi.KtCodeFragment
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.createSmartPointer

abstract class QuickFixActionBase<out T : PsiElement>(element: T) : IntentionAction {
    private val elementPointer = element.createSmartPointer()

    protected val element: T?
        get() = elementPointer.element

    open val isCrossLanguageFix: Boolean = false

    protected open fun isAvailableImpl(project: Project, editor: Editor?, file: PsiFile) = true

    final override fun isAvailable(project: Project, editor: Editor?, file: PsiFile): Boolean {
        if (isUnitTestMode()) {
            CREATE_BY_PATTERN_MAY_NOT_REFORMAT = true
        }
        try {
            val element = element ?: return false
            return element.isValid &&
                    !element.project.isDisposed &&
                    (file.manager.isInProject(file) || file is KtCodeFragment || (file is KtFile && file.isScript())) &&
                    (file is KtFile || isCrossLanguageFix) &&
                    isAvailableImpl(project, editor, file)
        } finally {
            CREATE_BY_PATTERN_MAY_NOT_REFORMAT = false
        }
    }

    override fun startInWriteAction() = true
}