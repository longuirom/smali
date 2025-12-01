
package com.android.tools.smali.dexlib2.dexbacked;

import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.iface.MultiDexContainer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DexDexContainer implements MultiDexContainer<DexBackedDexFile> {
    private final Opcodes opcodes;
    private final byte[] buf;
    private Map<String, DexBackedDexFile> entries;

    public DexDexContainer(@Nonnull File file, @Nullable Opcodes opcodes) throws IOException {
        this.opcodes = opcodes;
        this.buf = java.nio.file.Files.readAllBytes(file.toPath());
    }

    @Nonnull
    @Override
    public List<String> getDexEntryNames() throws IOException {
        return new ArrayList<>(getEntries().keySet());
    }

    private synchronized Map<String, DexBackedDexFile> getEntries() throws IOException {
        if (entries != null) return entries;
        entries = new TreeMap<>();

        int offset = 0;
        int index = 1;
        while (offset < buf.length) {
            try {
                DexBackedDexFile dex = new DexBackedDexFile(opcodes, buf, 0, true, offset);
                String classesIndex = "";
                if (index > 1){
                    classesIndex = "_classes" + index;
                }
                entries.put("smali" + classesIndex, dex);
                offset += dex.getFileSize();
                index++;
            } catch (Exception e) {
                break;
            }
        }
        return entries;
    }

    @Nullable
    @Override
    public MultiDexContainer.DexEntry<DexBackedDexFile> getEntry(@Nonnull String entryName) throws IOException {
        DexBackedDexFile dex = getEntries().get(entryName);
        if (dex == null) return null;
        return new MultiDexContainer.DexEntry<DexBackedDexFile>() {
            @Nonnull
            @Override
            public String getEntryName() {
                return entryName;
            }

            @Nonnull
            @Override
            public DexBackedDexFile getDexFile() {
                return dex;
            }

            @Nonnull
            @Override
            public MultiDexContainer<DexBackedDexFile> getContainer() {
                return DexDexContainer.this;
            }
        };
    }
}