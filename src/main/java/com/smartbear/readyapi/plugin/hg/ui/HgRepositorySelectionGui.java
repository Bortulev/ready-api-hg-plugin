package com.smartbear.readyapi.plugin.hg.ui;

import com.aragost.javahg.Repository;
import com.aragost.javahg.commands.PullCommand;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.plugins.vcs.RepositorySelectionGui;
import com.eviware.soapui.plugins.vcs.VcsIntegrationException;
import com.eviware.soapui.support.UISupport;
import com.smartbear.readyapi.plugin.hg.ReadyApiHgIntegration;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static com.eviware.soapui.support.UISupport.createLabelLink;

public class HgRepositorySelectionGui implements RepositorySelectionGui {
    private final String HG_HELP_URL = "";
    private final String HG_HELP_TEXT = "Learn about sharing projects with Hg (Mercurial)";

    private final WsdlProject project;

    private JTextField repoPath;

    public HgRepositorySelectionGui(WsdlProject project) {
        this.project = project;
    }

    @Override
    public Component getComponent() {
        JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("wrap 3", "8[]8[grow, fill]8[]8", "8[]32[]8[grow, fill]8[]8"));
        panel.add(new JLabel("Hg (Mercurial) Local Repository"), "span 3");
        panel.add(new JLabel("Repository path:"));
        repoPath = new JTextField();
        panel.add(repoPath);
        JButton browse = new JButton("Browse");
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = UISupport.getFileDialogs().openDirectory(this, "Select Hg directory", null);
                if (file.exists()) {
                    repoPath.setText(file.getAbsolutePath());
                }
            }
        });
        panel.add(browse);
        panel.add(new JPanel(), "span 3");
        panel.add(panel.add(createLabelLink(HG_HELP_URL, HG_HELP_TEXT)), "span 3");

        return panel;
    }

    @Override
    public boolean isValidInput() {
        File repoFile = new File(repoPath.getText());
        if (!repoFile.exists()) {
            return false;
        }
        Repository result = Repository.open(repoFile);

        if (result == null) {
            throw new VcsIntegrationException("Repository is not Hg");
        }
        return true;
    }

    private boolean createHGRC(String localRepositoryPath, String remoteRepositoryPath) throws IOException {
        File hgPath = new File(localRepositoryPath + File.separator + ".hg");
        if (!hgPath.exists()) {
            ReadyApiHgIntegration.logger.error("Repository (.hg) not found.");
            return false;
        }

        File hgrcPath = new File(hgPath.getPath() + File.separator + "hgrc");
        if (hgrcPath.exists()) {
            throw new VcsIntegrationException("Cannot add link to remote repository. File 'hgrc' already exists");
        }

        FileWriter writer = new FileWriter(hgrcPath.getPath());
        writer.write("[paths]\n");
        writer.write("default=" + remoteRepositoryPath);
        writer.write("\n" );
        writer.close();

        return true;
    }

    @Override
    public void initializeRepository() {
        try {
            File projectDir = new File(project.getPath());

            Repository.create(projectDir);
            createHGRC(projectDir.getPath(), repoPath.getText());

            Repository repo = Repository.open(projectDir);
            PullCommand pull = new PullCommand(repo);
            pull.cmdAppend("--update");
            pull.execute();
        } catch (IOException ioe) {
            throw new VcsIntegrationException("Initialization of repository failed");
        }
    }

    @Override
    public String getRemoteRepositoryId() {
        return repoPath.getText();
    }
}
