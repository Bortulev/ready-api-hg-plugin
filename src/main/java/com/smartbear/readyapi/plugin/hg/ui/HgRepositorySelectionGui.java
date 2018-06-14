package com.smartbear.readyapi.plugin.hg.ui;

import com.aragost.javahg.Repository;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.plugins.vcs.RepositorySelectionGui;
import com.eviware.soapui.plugins.vcs.VcsIntegrationException;
import com.eviware.soapui.support.UISupport;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

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

    @Override
    public void initializeRepository() {
    }

    @Override
    public String getRemoteRepositoryId() {
        return repoPath.getText();
    }
}
