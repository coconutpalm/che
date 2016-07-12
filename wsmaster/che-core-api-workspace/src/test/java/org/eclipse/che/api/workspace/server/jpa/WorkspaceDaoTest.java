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

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
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
import java.util.stream.Collectors;

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

    @Test(dependsOnMethods = "shouldGetWorkspaceById")
    public void shouldCreateWorkspace() throws Exception {
        final WorkspaceImpl workspace = createWorkspace("new-workspace", "new-namespace", "new-name");

        workspaceDao.create(workspace);

        assertEquals(workspaceDao.get(workspace.getId()), new WorkspaceImpl(workspace));
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldNotCreateWorkspaceWithANameWhichAlreadyExistsInGivenNamespace() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        final WorkspaceImpl newWorkspace = createWorkspace("new-id", workspace.getNamespace(), workspace.getName());

        workspaceDao.create(newWorkspace);
    }

    @Test
    public void shouldCreateWorkspaceWithNameWhichDoesNotExistInGivenNamespace() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];
        final WorkspaceImpl workspace2 = workspaces[4];

        final WorkspaceImpl newWorkspace = createWorkspace("new-id", workspace.getNamespace(), workspace2.getName());

        assertEquals(workspaceDao.create(newWorkspace), new WorkspaceImpl(newWorkspace));
    }

    @Test(expectedExceptions = ConflictException.class)
    public void shouldThrowConflictExceptionWhenCreatingWorkspaceWithExistingId() throws Exception {
        final WorkspaceImpl workspace = workspaces[0];

        final WorkspaceImpl newWorkspace = createWorkspace(workspace.getId(), "new-namespace", "new-name");

        workspaceDao.create(newWorkspace);
    }

    @Test(dependsOnMethods = "shouldGetWorkspaceById")
    public void shouldUpdateWorkspace() throws Exception {
        final WorkspaceImpl workspace = new WorkspaceImpl(workspaces[0]);

        // Remove an existing project configuration from workspace
        workspace.getConfig().getProjects().remove(1);

        // Add new project to the workspace configuration
        final SourceStorageImpl source = new SourceStorageImpl();
        source.setType("type");
        source.setLocation("somewhere");
        source.setParameters(new HashMap<>(ImmutableMap.of("param1", "value1",
                                                           "param2", "value2",
                                                           "param3", "value3")));
        final ProjectConfigImpl newProjectCfg = new ProjectConfigImpl();
        newProjectCfg.setPath("/hello2");
        newProjectCfg.setType("gradle");
        newProjectCfg.setName("project-2");
        newProjectCfg.setDescription("This is a test project2");
        newProjectCfg.setMixins(new ArrayList<>(asList("mixin3", "mixin4")));
        newProjectCfg.setSource(source);
        newProjectCfg.getAttributes().put("key3", asList("1", "2"));
        workspace.getConfig().getProjects().add(newProjectCfg);

        // Update an existing project configuration
        final ProjectConfigImpl projectCfg = workspace.getConfig().getProjects().get(0);
        projectCfg.getAttributes().clear();
        projectCfg.getSource().setLocation("new-location");
        projectCfg.getSource().setType("new-type");
        projectCfg.getSource().getParameters().put("new-param", "new-param-value");
        projectCfg.getMixins().add("new-mixin");
        projectCfg.setPath("/new-path");
        projectCfg.setDescription("new project description");

        // Remove an existing command
        workspace.getConfig().getCommands().remove(1);

        // Add a new command
        final CommandImpl newCmd = new CommandImpl();
        newCmd.setName("echo");
        newCmd.setType("bash");
        newCmd.setCommandLine("echo Hello");
        newCmd.setAttributes(new HashMap<>(ImmutableMap.of("simple", "true")));
        workspace.getConfig().getCommands().add(newCmd);

        // Update an existing command
        final CommandImpl command = workspace.getConfig().getCommands().get(0);
        command.setName("ps");
        command.setType("docker");
        command.setCommandLine("docker ps");
        command.getAttributes().clear();

        // Remove an existing machine config
        workspace.getConfig().getEnvironments().get(0).getMachineConfigs().remove(1);

        // Add a new machine config
        final MachineConfigImpl newMachineCfg = new MachineConfigImpl();
        newMachineCfg.setName("non-dev-cfg");
        newMachineCfg.setDev(false);
        newMachineCfg.setType("docker");
        newMachineCfg.setLimits(new LimitsImpl(2048));
        newMachineCfg.getEnvVariables().putAll(ImmutableMap.of("JAVA_HOME", "/usr/java/jdk"));
        newMachineCfg.setServers(new ArrayList<>(singleton(new ServerConfImpl("ref", "port", "protocol", "path"))));
        newMachineCfg.setSource(new MachineSourceImpl("type", "location", "content"));
        workspace.getConfig().getEnvironments().get(0).getMachineConfigs().add(newMachineCfg);

        // Update an existing machine configuration
        final MachineConfigImpl machineCfg = workspace.getConfig().getEnvironments().get(0).getMachineConfigs().get(0);
        machineCfg.getEnvVariables().clear();
        machineCfg.setType("new-type");
        machineCfg.setName("new-name");
        machineCfg.getLimits().setRam(512);
        machineCfg.getServers().clear();
        machineCfg.getServers().add(new ServerConfImpl("ref3", "port3", "protocol3", "path3"));
        machineCfg.getSource().setType("new-type");
        machineCfg.getSource().setLocation("new-location");
        machineCfg.getSource().setContent("new-content");

        // Remove an existing environment
        workspace.getConfig().getEnvironments().remove(1);

        // Add a new environment
        final EnvironmentImpl newEnv = new EnvironmentImpl();
        newEnv.setName("new-env");
        final MachineConfigImpl newEnvMachineCfg = new MachineConfigImpl(newMachineCfg);
        newEnvMachineCfg.setDev(true);
        newEnv.getMachineConfigs().add(newEnvMachineCfg);
        workspace.getConfig().getEnvironments().add(newEnv);

        // Update an existing environment
        final EnvironmentImpl environment = workspace.getConfig().getEnvironments().get(0);
        environment.setName("new-name");

        // Update workspace configuration
        final WorkspaceConfigImpl wCfg = workspace.getConfig();
        wCfg.setDefaultEnv(newEnv.getName());
        wCfg.setName("new-name");
        wCfg.setDescription("This is a new description");

        // Update workspace object
        workspace.setName("new-name");
        workspace.setNamespace("new-namespace");
        workspace.getAttributes().clear();

        workspaceDao.update(workspace);

        assertEquals(workspaceDao.get(workspace.getId()), new WorkspaceImpl(workspace));
    }

    private static WorkspaceImpl createWorkspace(String id, String namespace, String name) {
        // Project Sources configuration
        final SourceStorageImpl source1 = new SourceStorageImpl();
        source1.setType("type");
        source1.setLocation("somewhere");
        source1.setParameters(new HashMap<>(ImmutableMap.of("param1", "value1",
                                                            "param2", "value2",
                                                            "param3", "value3")));
        final SourceStorageImpl source2 = new SourceStorageImpl();
        source2.setType("type2");
        source2.setLocation("somewhere2");
        source2.setParameters(new HashMap<>(ImmutableMap.of("param4", "value4",
                                                            "param5", "value5",
                                                            "param6", "value6")));

        // Project Configuration
        final ProjectConfigImpl pCfg1 = new ProjectConfigImpl();
        pCfg1.setPath("/hello");
        pCfg1.setType("maven");
        pCfg1.setName("project-1");
        pCfg1.setDescription("This is a test project");
        pCfg1.setMixins(new ArrayList<>(asList("mixin1", "mixin2")));
        pCfg1.setSource(source1);
        pCfg1.setAttributes(new HashMap<>(ImmutableMap.of("key", asList("a", "b"), "key2", asList("d", "e"))));

        final ProjectConfigImpl pCfg2 = new ProjectConfigImpl();
        pCfg2.setPath("/hello2");
        pCfg2.setType("maven2");
        pCfg2.setName("project-2");
        pCfg2.setDescription("This is a test project2");
        pCfg2.setMixins(new ArrayList<>(asList("mixin3", "mixin4")));
        pCfg2.setSource(source2);
        pCfg2.setAttributes(new HashMap<>(ImmutableMap.of("key3", asList("c", "d"), "key4", asList("f", "g"))));

        final List<ProjectConfigImpl> projects = new ArrayList<>(asList(pCfg1, pCfg2));

        // Commands
        final CommandImpl cmd1 = new CommandImpl("test", "ping eclipse.org", "bash");
        cmd1.getAttributes().putAll(ImmutableMap.of("key1", "value1",
                                                    "key2", "value2",
                                                    "key3", "value3"));
        final CommandImpl cmd2 = new CommandImpl("test", "echo test", "bash");
        cmd2.getAttributes().putAll(ImmutableMap.of("key4", "value4",
                                                    "key5", "value5",
                                                    "key6", "value6"));
        final List<CommandImpl> commands = new ArrayList<>(asList(cmd1, cmd2));

        // Machine configs
        final MachineConfigImpl mCfg1 = new MachineConfigImpl();
        mCfg1.setName("dev-cfg");
        mCfg1.setDev(true);
        mCfg1.setType("docker");
        mCfg1.setLimits(new LimitsImpl(2048));
        mCfg1.getEnvVariables().putAll(ImmutableMap.of("GOPATH", "~/workspace"));
        mCfg1.getServers().addAll(singleton(new ServerConfImpl("ref", "port", "protocol", "path")));
        mCfg1.setSource(new MachineSourceImpl("type", "location", "content"));

        final MachineConfigImpl mCfg2 = new MachineConfigImpl();
        mCfg2.setName("non-dev-cfg");
        mCfg2.setDev(false);
        mCfg2.setType("docker");
        mCfg2.setLimits(new LimitsImpl(512));
        mCfg2.getEnvVariables().putAll(ImmutableMap.of("M2_HOME", "~/.m2"));
        mCfg2.getServers().add(new ServerConfImpl("ref2", "port2", "protocol2", "path2"));
        mCfg2.setSource(new MachineSourceImpl("type2", "location2", "content2"));

        final List<MachineConfigImpl> machineConfigs = new ArrayList<>(asList(mCfg1, mCfg2));

        // Environments
        final EnvironmentImpl env1 = new EnvironmentImpl();
        env1.setName("dev-env");
        env1.setMachineConfigs(machineConfigs);

        final EnvironmentImpl env2 = new EnvironmentImpl();
        env2.setName("non-dev-env");
        env2.setMachineConfigs(machineConfigs.stream()
                                             .map(MachineConfigImpl::new)
                                             .collect(Collectors.toList()));

        final List<EnvironmentImpl> environments = new ArrayList<>(asList(env1, env2));

        // Workspace configuration
        final WorkspaceConfigImpl wCfg = new WorkspaceConfigImpl();
        wCfg.setDefaultEnv("dev-env");
        wCfg.setName(name);
        wCfg.setDescription("This is the best workspace ever");
        wCfg.setCommands(commands);
        wCfg.setProjects(projects);
        wCfg.setEnvironments(environments);

        // Workspace
        final WorkspaceImpl workspace = new WorkspaceImpl();
        workspace.setStatus(WorkspaceStatus.STOPPED);
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
