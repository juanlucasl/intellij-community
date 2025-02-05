// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.internal.ui.uiDslShowcase

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import java.awt.Dimension
import javax.swing.JComponent
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaMethod

const val BASE_URL = "https://github.com/JetBrains/intellij-community/blob/master/platform/platform-impl/src/com/intellij/internal/ui/uiDslShowcase/"
val DEMOS = arrayOf(
  ::demoComponentLabels,
  ::demoComments
)

class UiDslShowcaseAction : DumbAwareAction("UI DSL Showcase") {

  override fun actionPerformed(e: AnActionEvent) {
    UiDslShowcaseDialog(e.project).show()
  }
}

@Suppress("DialogTitleCapitalization")
private class UiDslShowcaseDialog(project: Project?) : DialogWrapper(project, null, true, IdeModalityType.IDE, false) {

  init {
    title = "UI DSL Showcase"
    init()
  }

  override fun createCenterPanel(): JComponent {
    val result = JBTabbedPane()
    result.minimumSize = Dimension(400, 300)
    result.preferredSize = Dimension(800, 600)

    for (demo in DEMOS) {
      addDemo(demo, result)
    }

    return result
  }

  private fun addDemo(demo: KFunction<DialogPanel>, tabbedPane: JBTabbedPane) {
    val annotation = demo.findAnnotation<Demo>()
    if (annotation == null) {
      throw Exception("Demo annotation is missed for ${demo.name}")
    }

    val content = panel {
      row {
        label("<html>Description: ${annotation.description}")
      }

      val simpleName = demo.javaMethod!!.declaringClass.simpleName
      val fileName = (simpleName.substring(0..simpleName.length - 3) + ".kt")
      row {
        browserLink("View source", BASE_URL + fileName)
      }.bottomGap(BottomGap.MEDIUM)

      row {
        cell(demo.call())
      }
    }

    tabbedPane.add(annotation.title, content)
  }
}
