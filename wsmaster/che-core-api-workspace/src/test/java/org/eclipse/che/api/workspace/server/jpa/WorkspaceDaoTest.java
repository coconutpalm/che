/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.workspace.server.jpa;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.machine.server.model.impl.CommandImpl;
import org.eclipse.che.api.machine.server.model.impl.LimitsImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineConfigImpl;
import org.eclipse.che.api.machine.server.model.impl.MachineSourceImpl;
import org.eclipse.che.api.machine.server.model.impl.ServerConfImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.ProjectConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.SourceStorageImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.test.tck.TckModuleFactory;
import org.eclipse.che.commons.test.tck.repository.TckRepository;
import org.eclipse.che.commons.test.tck.repository.TckRepositoryException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests {@link WorkspaceDao} contract.
 *
 * @author Yevhenii Voevodin
 */
@Guice(moduleFactory = TckModuleFactory.class)
public class WorkspaceDaoTest {

    private static final int COUNT_OF_WORKSPACES = 5;

    @Inject
    private TckRepository<WorkspaceImpl> workspaceRepo;

    @Inject
    private WorkspaceDao workspaceDao;

    private WorkspaceImpl[] workspaces;

    @BeforeMethod
    public void createEntities() throws TckRepositoryException {
        workspaces = new WorkspaceImpl[COUNT_OF_WORKSPACES];
        for (int i = 0; i < COUNT_OF_WORKSPACES; i++) {
            // 2 workspaces share 1 namespace
            workspaces[i] = createWorkspace("workspace-" + i, "namespace-" + i / 2, "name-" + i);
        }
        workspaceRepo.createAll(asList(workspaces));
    }

    @AfterMethod
    public void removeEntities() throws TckRepositoryException {
        workspaceRepo.removeAll();
    }

    @Test
    public void shouldGetWorkspaceById() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        assertEquals(workspaceDao.get(workspace.getId()), workspace);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenGettingNonExistingWorkspaceById() throws Exception {
        workspaceDao.get("non-existing-id");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingWorkspaceByIdWhereIdIsNull() throws Exception {
        workspaceDao.get(null);
    }

    @Test
    public void shouldGetWorkspacesByNamespace() throws Exception {
        final WorkspaceImpl workspace1 = workspaces[0];
        final WorkspaceImpl workspace2 = workspaces[1];
        assertEquals(workspace1.getNamespace(), workspace2.getNamespace(), "Namespaces must be the same");

        final List<WorkspaceImpl> found = workspaceDao.getByNamespace(workspace1.getNamespace());

        assertEquals(new HashSet<>(found), new HashSet<>(asList(workspace1, workspace2)));
    }

    @Test
    public void emptyListShouldBeReturnedWhenThereAreNoWorkspacesInGivenNamespace() throws Exception {
        assertTrue(workspaceDao.getByNamespace("non-existing-namespace").isEmpty());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingWorkspaceByNullNamespace() throws Exception {
        workspaceDao.getByNamespace(null);
    }

    @Test
    public void shouldGetWorkspaceByNameAndNamespace() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        assertEquals(workspaceDao.get(workspace.getName(), workspace.getNamespace()), workspace);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenWorkspaceWithSuchNameDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        workspaceDao.get("non-existing-name", workspace.getNamespace());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenWorkspaceWithSuchNamespaceDoesNotExist() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        workspaceDao.get(workspace.getName(), "non-existing-namespace");
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenWorkspaceWithSuchNameDoesNotExistInGiveWorkspace() throws Exception {
        final WorkspaceImpl workspace1 = workspaces[0];
        final WorkspaceImpl workspace2 = workspaces[2];

        workspaceDao.get(workspace1.getName(), workspace2.getNamespace());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingWorkspaceByNameAndNamespaceWhereNameIsNull() throws Exception {
        workspaceDao.get(null, workspaces[0].getNamespace());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenGettingWorkspaceByNameAndNamespaceWhereNamespaceIsNull() throws Exception {
        workspaceDao.get(workspaces[0].getName(), null);
    }

    @Test(expectedExceptions = NotFoundException.class,
          dependsOnMethods = "shouldThrowNotFoundExceptionWhenGettingNonExistingWorkspaceById")
    public void shouldRemoveWorkspace() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        workspaceDao.remove(workspace.getId());
        workspaceDao.get(workspace.getId());
    }

    @Test
    public void shouldNotThrowExceptionWhenRemovingNonExistingWorkspace() throws Exception {
        workspaceDao.remove("non-existing-id");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void shouldThrowNpeWhenRemovingNull() throws Exception {
        workspaceDao.remove(null);
    }

    private static WorkspaceImpl createWorkspace(String id, String namespace, String name) {
        // Project Source configuration
        final SourceStorageImpl source = new SourceStorageImpl();
        source.setType("type");
        source.setLocation("somewhere");
        source.setParameters(new HashMap<>(ImmutableMap.of("param1", "value1",
                                                           "param2", "value2",
                                                           "param3", "value3")));

        // Project Configuration
        final ProjectConfigImpl pCfg = new ProjectConfigImpl();
        pCfg.setPath("/hello");
        pCfg.setType("maven");
        pCfg.setName("project-1");
        pCfg.setDescription("This is a test project");
        pCfg.setMixins(new ArrayList<>(asList("mixin1", "mixin2")));
        pCfg.setSource(source);
        pCfg.setAttributes(new HashMap<>(ImmutableMap.of("key", asList("a", "b"), "key2", asList("d", "e"))));
        pCfg.getAttributes().put("key3", asList("5", "6"));
        final List<ProjectConfigImpl> projects = new ArrayList<>(singleton(pCfg));


        // Commands
        final CommandImpl cmd1 = new CommandImpl("test", "ping eclipse.org", "bash");
        cmd1.setAttributes(ImmutableMap.of("key1", "value1",
                                           "key2", "value2",
                                           "key3", "value3"));
        final List<CommandImpl> commands = new ArrayList<>(singleton(cmd1));

        // Machine configs
        final MachineConfigImpl machineConfig = new MachineConfigImpl();
        machineConfig.setName("dev-cfg");
        machineConfig.setDev(true);
        machineConfig.setType("docker");
        machineConfig.setLimits(new LimitsImpl(2048));
        machineConfig.setEnvVariables(new HashMap<>(ImmutableMap.of("GOPATH", "~/workspace")));
        machineConfig.setServers(new ArrayList<>(singleton(new ServerConfImpl("ref", "port", "protocol", "path"))));
        machineConfig.setSource(new MachineSourceImpl("type", "location", "content"));
        final List<MachineConfigImpl> machineConfigs = new ArrayList<>(singleton(machineConfig));

        // Environments
        final EnvironmentImpl env = new EnvironmentImpl();
        env.setName("dev-env");
        env.setMachineConfigs(machineConfigs);
        final List<EnvironmentImpl> environments = new ArrayList<>(singleton(env));

        // Workspace configuration
        final WorkspaceConfigImpl wCfg = new WorkspaceConfigImpl();
        wCfg.setDefaultEnv("dev-env");
        wCfg.setDescription("This is the best workspace ever");
        wCfg.setCommands(commands);
        wCfg.setProjects(projects);
        wCfg.setEnvironments(environments);

        // Workspace
        final WorkspaceImpl workspace = new WorkspaceImpl();
        workspace.setId(id);
        workspace.setNamespace(namespace);
        workspace.setName(name);
        workspace.setAttributes(new HashMap<>(ImmutableMap.of("attr1", "value1",
                                                              "attr2", "value2",
                                                              "attr3", "value3")));
        workspace.setConfig(wCfg);

        return workspace;
    }
}
