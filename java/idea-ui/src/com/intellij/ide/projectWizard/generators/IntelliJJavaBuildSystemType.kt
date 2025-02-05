// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ide.projectWizard.generators

import com.intellij.ide.highlighter.ModuleFileType
import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.wizard.getCanonicalPath
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import java.nio.file.Paths

class IntelliJJavaBuildSystemType : JavaBuildSystemType {
  override val name = "IntelliJ"

  override fun createStep(parent: JavaNewProjectWizard.Step) =
    object : IntellijBuildSystemStep<JavaNewProjectWizard.Step>(parent) {
      override fun setupProject(project: Project) {
        val builder = JavaModuleBuilder()
        val moduleFile = Paths.get(moduleFileLocation, moduleName + ModuleFileType.DOT_DEFAULT_EXTENSION)

        builder.name = moduleName
        builder.moduleFilePath = FileUtil.toSystemDependentName(moduleFile.toString())
        builder.contentEntryPath = FileUtil.toSystemDependentName(contentRoot)
        builder.moduleJdk = parentStep.sdk

        builder.commit(project)
      }
    }
}