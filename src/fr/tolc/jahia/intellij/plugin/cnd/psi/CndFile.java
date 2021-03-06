package fr.tolc.jahia.intellij.plugin.cnd.psi;

import javax.swing.*;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import fr.tolc.jahia.intellij.plugin.cnd.CndFileType;
import fr.tolc.jahia.intellij.plugin.cnd.CndLanguage;
import org.jetbrains.annotations.NotNull;

public class CndFile extends PsiFileBase {
    public CndFile(@NotNull FileViewProvider viewProvider) {
        super(viewProvider, CndLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return CndFileType.INSTANCE;
    }

    @Override
    public String toString() {
        return "CND File";
    }

    @Override
    public Icon getIcon(int flags) {
        return super.getIcon(flags);
    }
}