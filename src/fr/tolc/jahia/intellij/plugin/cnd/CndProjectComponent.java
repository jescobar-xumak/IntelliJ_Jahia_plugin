package fr.tolc.jahia.intellij.plugin.cnd;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import fr.tolc.jahia.intellij.plugin.cnd.utils.CndPluginUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.generate.exception.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CndProjectComponent implements ProjectComponent {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CndProjectComponent.class);
    
    private static final String JAHIA_PLUGIN_LIBRARY_NAME = "jahia-plugin-base-cnd-files";
    private static final String JAHIA_PLUGIN_SUBFOLDER = "jahia";
    private static final String JAHIA_CND_JAR_NAME = "jahia-plugin-cnds.jar";

    private Project project;

    public CndProjectComponent(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "CndProjectComponent";
    }

    @Override
    public void projectOpened() {
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        //TODO: move files/jar creation to an ApplicationComponent and let the libraries mods here
                        File jahiaPluginSubFolder = CndPluginUtil.getPluginFile(JAHIA_PLUGIN_SUBFOLDER);

                        if (jahiaPluginSubFolder.exists() && jahiaPluginSubFolder.isDirectory()) {
                            //Re-generate 'fake' jar containing cnd files
                            File jarFile = CndPluginUtil.getPluginFile(JAHIA_PLUGIN_SUBFOLDER + "/" + JAHIA_CND_JAR_NAME);
                            if (jarFile != null && jarFile.exists()) {
                                jarFile.delete();
                            }
                            try {
                                CndPluginUtil.fileToJar(jahiaPluginSubFolder, jahiaPluginSubFolder.getAbsolutePath() + "/" + JAHIA_CND_JAR_NAME, "cnd");
                            } catch (Exception e) {
                               LOGGER.warn("Error generating Jahia base cnd files 'fake' jar", e);
                            }


                            //Adding it to the modules libraries
                            Set<Module> alreadyDoneModules = new HashSet<>();
                            Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, CndFileType.INSTANCE, GlobalSearchScope.allScope(project));
                            for (VirtualFile virtualFile : virtualFiles) {
                                try {
                                    Module fileModule = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(virtualFile);

                                    if (fileModule != null && !alreadyDoneModules.contains(fileModule)) {
                                        final ModifiableRootModel rootModel = ModuleRootManager.getInstance(fileModule).getModifiableModel();
                                        LibraryTable.ModifiableModel moduleLibraryTable = rootModel.getModuleLibraryTable().getModifiableModel();

                                        Library library = moduleLibraryTable.getLibraryByName(JAHIA_PLUGIN_LIBRARY_NAME);
                                        if (library != null) {
                                            moduleLibraryTable.removeLibrary(library);
                                        }
                                        Library newLibrary = moduleLibraryTable.createLibrary(JAHIA_PLUGIN_LIBRARY_NAME);

                                        File libraryJar = new File(jahiaPluginSubFolder.getAbsolutePath() + "/" + JAHIA_CND_JAR_NAME);

                                        Library.ModifiableModel modifiableModel = newLibrary.getModifiableModel();
                                        modifiableModel.addRoot("jar://" + libraryJar.getAbsolutePath() + "!/", OrderRootType.CLASSES);
                                        modifiableModel.commit();
                                        moduleLibraryTable.commit();
                                        rootModel.commit();
                                        
                                        alreadyDoneModules.add(fileModule);
                                    }
                                } catch (Exception e) {
                                    LOGGER.warn("Error while adding Jahia CND files to module(s) libraries", e);
                                }
                            }
                        } else {
                            LOGGER.error("Error finding Jahia plugin resources folder");
                            throw new PluginException("Error finding Jahia plugin resources folder", new FileNotFoundException("Missing folder " + jahiaPluginSubFolder.getPath()));
                        }
                    }
                });
            }
        });
    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @Override
    public void projectClosed() {

    }
}
