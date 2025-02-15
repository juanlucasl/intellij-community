// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.kotlin.idea.projectWizard.gradle

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.configuration.JdkComboBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.buttonGroup
import com.intellij.util.io.systemIndependentPath
import org.jetbrains.kotlin.tools.projectWizard.KotlinBuildSystemType
import org.jetbrains.kotlin.tools.projectWizard.KotlinNewProjectWizard
import org.jetbrains.kotlin.tools.projectWizard.plugins.buildSystem.BuildSystemType
import org.jetbrains.plugins.gradle.service.project.wizard.GradleBuildSystemStep


class GradleKotlinBuildSystemType : KotlinBuildSystemType {
    override val name = "Gradle"

    override fun createStep(parent: KotlinNewProjectWizard.Step) = object : GradleBuildSystemStep<KotlinNewProjectWizard.Step>(parent) {
        private var buildSystemType: BuildSystemType = BuildSystemType.GradleKotlinDsl

        override val sdkComboBox: Cell<JdkComboBox>
            get() = parentStep.sdkComboBox

        override val sdk = parentStep.sdk

        override fun setupUI(builder: Panel) {
            super.setupUI(builder)
            with(builder) {
                buttonGroup(::buildSystemType) {
                    row {
                        radioButton(BuildSystemType.GradleKotlinDsl.text, BuildSystemType.GradleKotlinDsl)
                        radioButton(BuildSystemType.GradleGroovyDsl.text, BuildSystemType.GradleGroovyDsl)
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
                buildSystemType = buildSystemType,

                projectGroupId = groupId,
                artifactId = artifactId,
                version = version
            )
        }
}