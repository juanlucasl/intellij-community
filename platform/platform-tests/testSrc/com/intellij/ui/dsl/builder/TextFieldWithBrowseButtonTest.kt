// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ui.dsl.builder

import com.intellij.ui.layout.*
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class TextFieldWithBrowseButtonTest {

  var property = ""

  @Test
  fun testBindingInitialization() {
    property = UUID.randomUUID().toString()
    panel {
      row {
        val textField = textFieldWithBrowseButton()
          .bindText(::property)
        assertEquals(textField.component.text, property)
      }
    }

    var localProperty = UUID.randomUUID().toString()
    panel {
      row {
        val textField = textFieldWithBrowseButton()
          .bindText({ localProperty }, { localProperty = it })
        assertEquals(textField.component.text, localProperty)
      }
    }

    panel {
      row {
        val textField = textFieldWithBrowseButton()
          .bindText(PropertyBinding({ localProperty }, { localProperty = it }))
        assertEquals(textField.component.text, localProperty)
      }
    }
  }
}
