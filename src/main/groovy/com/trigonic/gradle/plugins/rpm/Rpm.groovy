/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trigonic.gradle.plugins.rpm

import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier

import org.freecompany.redline.header.Architecture
import org.freecompany.redline.header.Os
import org.freecompany.redline.header.RpmType
import org.freecompany.redline.payload.Directive
import org.gradle.api.internal.file.FileResolver
import org.gradle.api.internal.file.copy.CopyActionImpl
import org.gradle.api.tasks.bundling.AbstractArchiveTask

class Rpm extends AbstractArchiveTask {
    static final String RPM_EXTENSION = "rpm";

    final CopyActionImpl action
    Architecture arch = Architecture.NOARCH
    Os os = Os.UNKNOWN
    RpmType type = RpmType.BINARY
    String user
    String group
    String packageGroup = ''
    String buildHost = InetAddress.localHost.hostName
    String summary = ''
    String description = ''
    String license = ''
    String packager = System.getProperty('user.name', '')
    String distribution = ''
    String vendor = ''
    String url = ''
    String sourcePackage
    String provides
    File preInstall
    File postInstall
    File preUninstall
    File postUninstall
    List<Link> links = new ArrayList<Link>()

    Rpm() {
        action = new RpmCopyAction(getServices().get(FileResolver.class))
        extension = RPM_EXTENSION

        aliasEnumValues(Architecture.values())
        aliasEnumValues(Os.values())
        aliasEnumValues(RpmType.values())
        aliasStaticInstances(Directive.class)
    }

    private <T extends Enum<T>> void aliasEnumValues(T[] values) {
        for (T value : values) {
            assert !hasProperty(value.name())
            setProperty value.name(), value
        }
    }

    private <T> void aliasStaticInstances(Class<T> forClass) {
        for (Field field : forClass.fields) {
            if (field.type == forClass && field.hasModifier(Modifier.STATIC)) {
                assert !hasProperty(field.name)
                setProperty field.name, field.get(null)
            }
        }
    }

    CopyActionImpl getCopyAction() {
        action
    }

    String getPackageName() {
        baseName
    }

    void setPackageName(String packageName) {
        baseName = packageName
    }

    String getRelease() {
        classifier
    }

    void setRelease(String release) {
        classifier = release
    }

    String getArchiveName() {
        String.format("%s-%s-%s.%s.%s", packageName, version, release, arch.name().toLowerCase(), extension)
    }
    
    Link link(String path, String target) {
        link(path, target, -1)
    }
    
    Link link(String path, String target, int permissions) {
        Link link = new Link()
        link.path = path
        link.target = target
        link.permissions = permissions
        links.add(link)
        link
    }
    
    List<Link> getLinks() {
        links
    }

    class RpmCopyAction extends CopyActionImpl {
        public RpmCopyAction(FileResolver resolver) {
            super(resolver, new RpmCopySpecVisitor());
        }

        Rpm getTask() {
            Rpm.this
        }
    }
}