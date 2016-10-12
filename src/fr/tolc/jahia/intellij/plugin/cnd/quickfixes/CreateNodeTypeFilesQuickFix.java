package fr.tolc.jahia.intellij.plugin.cnd.quickfixes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import fr.tolc.jahia.intellij.plugin.cnd.CndUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class CreateNodeTypeFilesQuickFix extends BaseIntentionAction {

    private String jahiaWorkFolderPath;
    private String namespace;
    private String nodeTypeName;

    public CreateNodeTypeFilesQuickFix(String jahiaWorkFolderPath, String namespace, String nodeTypeName) {
        this.jahiaWorkFolderPath = jahiaWorkFolderPath;
        this.namespace = namespace;
        this.nodeTypeName = nodeTypeName;
    }

    @NotNull
    @Override
    public String getText() {
        return "Create node type associated folders and files";
    }
    
    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Cnd";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                String finalDirectory = CndUtil.getNodeTypeDefaultViewsFolderPath(jahiaWorkFolderPath, namespace, nodeTypeName);
                createNodeTypeFiles(project, finalDirectory);
                
//                Module currentModule = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(file.getVirtualFile());
//                VirtualFile[] sourceRoots = ModuleRootManager.getInstance(currentModule).getSourceRoots();

//                if (sourceRoots.length == 1) {
//                    createNodeTypeFiles(project, sourceRoots[0], finalDirectory);
//                } else {
//                    final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
//                    descriptor.setTitle("Select Root Folder");
//                    descriptor.setRoots(sourceRoots);
//                    final VirtualFile file = FileChooser.chooseFile(descriptor, project, null);
//                    if (file != null) {
//                        createNodeTypeFiles(project, file, finalDirectory);
//                    }
//                }
            }
        });
    }
    
    private void createNodeTypeFiles(final Project project, final String directory) {
        File folder = new File(directory);
        if(!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        VirtualFile nodeTypeFolder = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(folder);

        File jsp = new File(nodeTypeFolder.getCanonicalPath(),nodeTypeName + ".jsp");
        File properties = new File(nodeTypeFolder.getCanonicalPath(),nodeTypeName + ".properties");

        //Copying default content files to create the new files
        try {
            Path defaultViewPath = Paths.get(getClass().getClassLoader().getResource("default/view.jsp").getFile().substring(1));   //Because of starting "/" /E:/...
            Files.copy(defaultViewPath, jsp.toPath());

            Path defaultPropertiesPath = Paths.get(getClass().getClassLoader().getResource("default/view.properties").getFile().substring(1));  //Because of starting "/" /E:/...
            Files.copy(defaultPropertiesPath, properties.toPath());
        } catch (IOException e) {
            throw new IncorrectOperationException(e);
        }

        //Open new files in editor
        VirtualFile propertiesFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(properties);
        FileEditorManager.getInstance(project).openFile(propertiesFile, false);
        
        VirtualFile jspFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(jsp);
        FileEditorManager.getInstance(project).openFile(jspFile, true);

        //Expand folder in Project view
        ProjectView.getInstance(project).select(null, jspFile, false);
    }
}
