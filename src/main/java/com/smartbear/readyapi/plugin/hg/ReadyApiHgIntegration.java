package com.smartbear.readyapi.plugin.hg;

import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.AddCommand;
import com.aragost.javahg.commands.Branch;
import com.aragost.javahg.commands.BranchCommand;
import com.aragost.javahg.commands.BranchesCommand;
import com.aragost.javahg.commands.CommitCommand;
import com.aragost.javahg.commands.ExecutionException;
import com.aragost.javahg.commands.ParentsCommand;
import com.aragost.javahg.commands.PullCommand;
import com.aragost.javahg.commands.PushCommand;
import com.aragost.javahg.commands.RemoveCommand;
import com.aragost.javahg.commands.StatusResult;
import com.aragost.javahg.commands.Tag;
import com.aragost.javahg.commands.TagCommand;
import com.aragost.javahg.commands.TagsCommand;
import com.aragost.javahg.commands.flags.PullCommandFlags;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.plugins.vcs.ActivationStatus;
import com.eviware.soapui.plugins.vcs.CommitResult;
import com.eviware.soapui.plugins.vcs.HistoryEntry;
import com.eviware.soapui.plugins.vcs.ImportProjectFromVcsGui;
import com.eviware.soapui.plugins.vcs.LockHandler;
import com.eviware.soapui.plugins.vcs.LockStatus;
import com.eviware.soapui.plugins.vcs.RepositorySelectionGui;
import com.eviware.soapui.plugins.vcs.VcsBranch;
import com.eviware.soapui.plugins.vcs.VcsIntegration;
import com.eviware.soapui.plugins.vcs.VcsIntegrationConfiguration;
import com.eviware.soapui.plugins.vcs.VcsIntegrationException;
import com.eviware.soapui.plugins.vcs.VcsRepositoryInfo;
import com.eviware.soapui.plugins.vcs.VcsUpdate;
import com.smartbear.readyapi.plugin.hg.ui.HgRepositorySelectionGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JPanel;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@VcsIntegrationConfiguration(name = "Hg", description = "Mercurial Version Control System")
public class ReadyApiHgIntegration implements VcsIntegration {
    private final static Logger logger = LoggerFactory.getLogger(ReadyApiHgIntegration.class);

    private WsdlProject activeProject;

    @Override
    public Set<String> getAvailableTags(WsdlProject wsdlProject) throws VcsIntegrationException {
        activeProject = wsdlProject;

        File file = new File(wsdlProject.getPath());
        Repository repo = Repository.open(file);
        TagsCommand tagsCmd = new TagsCommand(repo);
        List<Tag> tags = tagsCmd.execute();

        HashSet<String> result = new HashSet<>();
        for (Tag tag : tags) {
            result.add(tag.getName());
        }
        return result;
    }

    @Override
    public LockHandler getLockHandler() {
        return new LockHandler() {
            @Override
            public LockStatus lock(File file) {
                return LockStatus.NOT_LOCKED;
            }

            @Override
            public LockStatus unlock(File file) {
                return LockStatus.NOT_LOCKED;
            }

            @Override
            public LockStatus getLockStatusFor(File file) {
                return LockStatus.NOT_LOCKED;
            }

            @Override
            public String getLockedBy(File file) {
                return "Unknown";
            }
        };
    }

    @Override
    public RepositorySelectionGui buildRepositorySelectionGui(WsdlProject wsdlProject) {
        activeProject = wsdlProject;
        return new HgRepositorySelectionGui(wsdlProject);
    }

    @Override
    public void deleteFile(WsdlProject wsdlProject, File file) throws IOException {
        activeProject = wsdlProject;
    }

    @Override
    public CommitResult commit(Collection<VcsUpdate> collection, String s) {
        if (collection.size() == 0) {
            return new CommitResult(CommitResult.CommitStatus.FAILED, "Nothing to update!");
        }

        VcsUpdate first_item = collection.stream().findFirst().get();
        WsdlProject project = first_item.getProject();
        Repository repo = Repository.open(new File(project.getPath()));
        CommitCommand commit = new CommitCommand(repo);
        commit.message(s);
        int skippedUpdates = 0;

        StatusResult statusResult = repo.workingCopy().status();

        List<String> toRemove = new ArrayList<>();
        List<String> toAdd = new ArrayList<>();

        ArrayList<String> files = new ArrayList<>();
        for (VcsUpdate update : collection) {
            if (update.getProject() != project) {
                skippedUpdates++;
                continue;
            }

            switch (update.getType()) {
                case DELETED:
                    if (statusResult.getMissing().indexOf(update.getRelativePath()) >= 0) {
                        toRemove.add(update.getRelativePath());
                    }
                    break;
                case ADDED:
                    if (statusResult.getUnknown().indexOf(update.getRelativePath()) >= 0) {
                        toAdd.add(update.getRelativePath());
                    }
                    break;
            }
            files.add(update.getRelativePath());
        }

        if (toRemove.size() > 0) {
            RemoveCommand remove = new RemoveCommand(repo);
            List<String> removeResult = remove.execute(toRemove.toArray(new String[0]));
        }
        if (toAdd.size() > 0) {
            AddCommand add = new AddCommand(repo);
            List<String> addResult = add.execute(toAdd.toArray(new String[0]));
        }

        try {
            PullCommand pk = PullCommandFlags.on(repo);

            PullCommand pull = new PullCommand(repo);
            List<Changeset> pullResult = pull.execute();

        } catch (IOException ioe) {
            throw new VcsIntegrationException("Error in executing push command", ioe);
        }

        try {
            Changeset changeset = commit.execute(files.toArray(new String[0]));
        } catch (ExecutionException ee) {
            throw new VcsIntegrationException(ee.getMessage(), ee);
        }

        PullCommand pull = new PullCommand(repo);
        try {
            pull.execute();
        } catch (IOException ioe) {
            throw new VcsIntegrationException("Error in executing pull command", ioe);
        }

        CommitResult result;
        if (skippedUpdates != 0) {
            result = new CommitResult(CommitResult.CommitStatus.PARTIAL, "Some updates from different repositories were skipped.");
        } else {
            result = new CommitResult(CommitResult.CommitStatus.SUCCESSFUL, "Commit succesfully finished.");
        }
        return result;
    }

    @Override
    public ImportProjectFromVcsGui buildRepositoryDownloadGui(Workspace workspace) {
        return new ImportProjectFromVcsGui() {
            @Override
            public Component getComponent() {
                return new JPanel();
            }

            @Override
            public VcsRepositoryInfo downloadProjectFiles(File file) {
                return new VcsRepositoryInfo("String 1", "String 2");
            }

            @Override
            public boolean isValidInput() {
                return false;
            }
        };
    }

    @Override
    public ActivationStatus activateFor(WsdlProject wsdlProject) {
        activeProject = wsdlProject;
        return ActivationStatus.FAILED;
    }

    @Override
    public void createTag(WsdlProject wsdlProject, String s) {
        activeProject = wsdlProject;

        File file = new File(wsdlProject.getPath());
        Repository repo = Repository.open(file);
        TagCommand tagCmd = new TagCommand(repo);
        tagCmd.execute(s);

        PushCommand push = new PushCommand(repo);
        try {
            push.execute();
        } catch (IOException ioe) {
            throw new VcsIntegrationException("Error on push command");
        }
    }

    @Override
    public boolean updateFromRemoteRepository(File file, boolean overwriteLocalChanges) {
        Repository repo = Repository.open(file);
        PullCommand pull = new PullCommand(repo);
        if (overwriteLocalChanges) {
            pull.cmdAppend("--clean");
        }
        try {
            pull.execute();
        } catch (IOException ioe) {
            throw new VcsIntegrationException("Error on execution Pull command");
        }
        return false;
    }

    @Override
    public Collection<VcsUpdate> getRemoteRepositoryUpdates(File file) {
        Repository repo = Repository.open(file);
        ParentsCommand parents = new ParentsCommand(repo);
        List<Changeset> list = parents.execute();
        return new ArrayList<>();
    }

    @Override
    public void revert(Collection<VcsUpdate> collection) throws VcsIntegrationException {
        throw new VcsIntegrationException("Not implemented");
    }

    @Override
    public List<HistoryEntry> getFileHistory(WsdlProject wsdlProject, File file) {
        activeProject = wsdlProject;
        return new ArrayList<>();
    }

    @Override
    public Collection<VcsUpdate> getLocalRepositoryUpdates(WsdlProject wsdlProject) {
        activeProject = wsdlProject;

        File file = new File(wsdlProject.getPath());
        if (!file.exists()) {
            throw new VcsIntegrationException("Something wrong!");
        }
        Repository rep = Repository.open(file);

        List<VcsUpdate> updates = new ArrayList<>();

        List<String> added = rep.workingCopy().status().getAdded();
        for (String item : added) {
            updates.add(new VcsUpdate(wsdlProject, VcsUpdate.VcsUpdateType.ADDED, item, null));
        }

        List<String> clean = rep.workingCopy().status().getClean();

        Map<String, String> copied = rep.workingCopy().status().getCopied();

        List<String> ignored = rep.workingCopy().status().getIgnored();

        List<String> missing = rep.workingCopy().status().getMissing();
        for (String item : missing) {
            updates.add(new VcsUpdate(wsdlProject, VcsUpdate.VcsUpdateType.DELETED, item, null));
        }

        List<String> modified = rep.workingCopy().status().getModified();
        for (String item : modified) {
            updates.add(new VcsUpdate(wsdlProject, VcsUpdate.VcsUpdateType.MODIFIED, item, null));
        }

        List<String> removed = rep.workingCopy().status().getRemoved();
        for (String item : removed) {
            updates.add(new VcsUpdate(wsdlProject, VcsUpdate.VcsUpdateType.DELETED, item, null));
        }

        List<String> unknown = rep.workingCopy().status().getUnknown();
        for (String item : unknown) {
            updates.add(new VcsUpdate(wsdlProject, VcsUpdate.VcsUpdateType.ADDED, item, null));
        }

        return updates;
    }

    @Override
    public List<HistoryEntry> getProjectHistory(WsdlProject wsdlProject) {
        activeProject = wsdlProject;

        return new ArrayList<>();
    }

    @Override
    public List<VcsBranch> getBranchList(WsdlProject wsdlProject) {
        File file = new File(wsdlProject.getPath());
        Repository repo = Repository.open(file);
        BranchesCommand branchesCmd = new BranchesCommand(repo);
        List<Branch> branches = branchesCmd.execute();

        List<VcsBranch> result = new ArrayList<>();
        for (Branch branch : branches) {
            result.add(new VcsBranch(branch.getName()));
        }
        return result;
    }

    @Override
    public boolean switchBranch(WsdlProject wsdlProject, VcsBranch vcsBranch) {
        File file = new File(wsdlProject.getPath());
        Repository repo = Repository.open(file);
        BranchCommand branch = new BranchCommand(repo);
        branch.set(vcsBranch.getName());
        return true;
    }
}
