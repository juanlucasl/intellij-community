// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.idea.maven.importing;

import org.jetbrains.idea.maven.MavenTestCase;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ArchetypesTest extends MavenTestCase {
  @Test
  public void testGenerating() throws Exception {
    File dir = new File(myDir.getPath(), "generated");
    dir.mkdirs();

    MavenRunnerParameters params = new MavenRunnerParameters(
      false, dir.getPath(), (String)null,
      Arrays.asList("org.apache.maven.plugins:maven-archetype-plugin:RELEASE:generate"),
      Collections.emptyList()
    );

    MavenRunnerSettings settings = new MavenRunnerSettings();
    Map<String, String> props = new HashMap<>();
    props.put("archetypeGroupId", "org.apache.maven.archetypes");
    props.put("archetypeArtifactId", "maven-archetype-quickstart");
    props.put("archetypeVersion", "1.0");
    props.put("interactiveMode", "false");
    props.put("groupId", "foo");
    props.put("artifactId", "bar");

    settings.setMavenProperties(props);
    settings.setJreName(MavenRunnerSettings.USE_INTERNAL_JAVA);
    CountDownLatch latch = new CountDownLatch(1);
    MavenRunner.getInstance(myProject).run(params, settings, () -> latch.countDown());

    boolean tryAcquire = latch.await(20, TimeUnit.SECONDS);
    assertTrue("Maven execution failed", tryAcquire);
    assertTrue(new File(dir, "bar/pom.xml").exists());
  }
}
