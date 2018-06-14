package com.smartbear.readyapi.plugin.hg;

import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.model.workspace.Workspace;
import com.eviware.soapui.plugins.vcs.ActivationStatus;
import com.eviware.soapui.plugins.vcs.CommitResult;
import com.eviware.soapui.plugins.vcs.HistoryEntry;
import com.eviware.soapui.plugins.vcs.ImportProjectFromVcsGui;
import com.eviware.soapui.plugins.vcs.LockHandler;
import com.eviware.soapui.plugins.vcs.LockStatus;
import com.eviware.soapui.plugins.vcs.RepositorySelectionGui;
import com.eviware.soapui.plugins.vcs.VcsIntegration;
import com.eviware.soapui.plugins.vcs.VcsIntegrationConfiguration;
import com.eviware.soapui.plugins.vcs.VcsIntegrationException;
import com.eviware.soapui.plugins.vcs.VcsRepositoryInfo;
import com.eviware.soapui.plugins.vcs.VcsUpdate;

import javax.swing.JPanel;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@VcsIntegrationConfiguration(name = "Hg", description = "Mercurial Version Control System")
public class ReadyApiHgIntegration implements VcsIntegration {

    @Override
    public Set<String> getAvailableTags(WsdlProject wsdlProject) throws VcsIntegrationException {
        return null;
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
        return null;
    }

    @Override
    public void deleteFile(WsdlProject wsdlProject, File file) throws IOException {
    }

    @Override
    public CommitResult commit(Collection<VcsUpdate> collection, String s) {
        return null;
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
        return ActivationStatus.FAILED;
    }

    @Override
    public void createTag(WsdlProject wsdlProject, String s) {
    }

    @Override
    public boolean updateFromRemoteRepository(File file, boolean overwriteLocalChanges) {
        return false;
    }

    @Override
    public Collection<VcsUpdate> getRemoteRepositoryUpdates(File file) {
        return new ArrayList<>();
    }

    @Override
    public void revert(Collection<VcsUpdate> collection) throws VcsIntegrationException {
        throw new VcsIntegrationException("Not implemented");
    }

    @Override
    public List<HistoryEntry> getFileHistory(WsdlProject wsdlProject, File file) {
        return new ArrayList<>();
    }

    @Override
    public Collection<VcsUpdate> getLocalRepositoryUpdates(WsdlProject wsdlProject) {
        return null;
    }

    @Override
    public List<HistoryEntry> getProjectHistory(WsdlProject wsdlProject) {
        return new ArrayList<>();
    }

    @Override
    public List<VcsBranch> getBranchList(WsdlProject wsdlProject) {
        return new ArrayList<>();
    }

    @Override
    public boolean switchBranch(WsdlProject wsdlProject, VcsBranch vcsBranch) {
        return false;
    }
}
