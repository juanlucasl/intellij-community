// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.kotlin.tools.projectWizard

import com.intellij.ide.projectWizard.generators.IntellijBuildSystemStep
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.ui.UIBundle
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.io.systemIndependentPath
import org.jetbrains.kotlin.tools.projectWizard.plugins.buildSystem.BuildSystemType
import org.jetbrains.kotlin.tools.projectWizard.wizard.KotlinNewProjectWizardUIBundle
import org.jetbrains.kotlin.tools.projectWizard.wizard.NewProjectWizardModuleBuilder
import java.awt.event.KeyEvent
import java.awt.event.KeyListener

class IntelliJKotlinBuildSystemType : KotlinBuildSystemType {
    override val name = "IntelliJ"

    override fun createStep(parent: KotlinNewProjectWizard.Step) = object : IntellijBuildSystemStep<KotlinNewProjectWizard.Step>(parent) {
        val wizardBuilder: NewProjectWizardModuleBuilder = NewProjectWizardModuleBuilder()

        override fun setupUI(builder: Panel) {
            with(builder) {
                collapsibleGroup(KotlinNewProjectWizardUIBundle.message("additional.buildsystem.settings.kotlin.runtime")) {
                    row("${KotlinNewProjectWizardUIBundle.message("additional.buildsystem.settings.kotlin.runtime")}:") {
                        cell(wizardBuilder.wizard.jpsData.libraryOptionsPanel.simplePanel)
                    }
                }
            }
        }

        override fun setupProject(project: Project) =
            KotlinNewProjectWizard.generateProject(
                project = project,
                projectPath = parent.projectPath.systemIndependentPath,
                projectName = parent.name,
                sdk = parent.sdk,
                buildSystemType = BuildSystemType.Jps
            )
    }
}