package root.gui;

import root.tasks.ILanguageUpdateble;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class MyFileChooser extends JFileChooser implements ILanguageUpdateble {
    private final Set<FileFilterExt> filters = new HashSet<>();

    public MyFileChooser() {
        filters.add(new FileFilterExt("txt"));
        filters.add(new FileFilterExt("script"));
        filters.forEach(this::addChoosableFileFilter);
        setFileSelectionMode(JFileChooser.FILES_ONLY);
        updateLanguage();
    }

    @Override
    public void updateLanguage() {
        class Builder {
            String getString(String str) { return UIManager.getString("FileChooser." + str); }
            String getFilter(String str) { return getString("filter." + str); }
        } Builder builder = new Builder();
        filters.forEach(filter -> filter.description = builder.getFilter(filter.extension));

        setLocale(UIManager.getDefaults().getDefaultLocale());
        updateUI();
    }

    private class FileFilterExt extends javax.swing.filechooser.FileFilter
    {
        private final String extension;
        private String description;

        FileFilterExt(String extension) {
            this.extension = extension;
        }

        @Override
        public boolean accept(java.io.File file) {
            if (file == null) return false;
            if (file.isDirectory()) return true;
            return file.getName().endsWith("." + extension);
        }

        @Override public String getDescription() {
            return description;
        }
    }
}