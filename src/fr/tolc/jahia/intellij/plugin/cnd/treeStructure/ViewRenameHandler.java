/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

/*
 * Created by IntelliJ IDEA.
 * User: yole
 * Date: 26.10.2006
 * Time: 16:44:00
 */
package fr.tolc.jahia.intellij.plugin.cnd.treeStructure;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringActionHandlerFactory;
import com.intellij.refactoring.rename.RenameHandler;
import org.jetbrains.annotations.NotNull;

public class ViewRenameHandler implements RenameHandler {
  public boolean isAvailableOnDataContext(DataContext dataContext) {
    View[] views = View.DATA_KEY.getData(dataContext);
    return views != null && views.length == 1;
  }

  public boolean isRenaming(DataContext dataContext) {
    return isAvailableOnDataContext(dataContext);
  }

  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    View[] views = View.DATA_KEY.getData(dataContext);
    if (views == null || views.length != 1) {
      return;
    }
    PsiClass boundClass = views [0].getClassToBind();
    RefactoringActionHandlerFactory.getInstance().createRenameHandler().invoke(project, new PsiElement[] { boundClass },
                                                                               dataContext);
  }

  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
    invoke(project, null, null, dataContext);
  }
}