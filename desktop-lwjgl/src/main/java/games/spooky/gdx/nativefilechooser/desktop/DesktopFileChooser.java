/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2023 Spooky Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package games.spooky.gdx.nativefilechooser.desktop;

import com.badlogic.gdx.files.FileHandle;
import games.spooky.gdx.nativefilechooser.*;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memAllocPointer;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_FreePath;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_GetError;

public class DesktopFileChooser implements NativeFileChooser {

	@Override
	public void chooseFile(NativeFileChooserConfiguration configuration, NativeFileChooserCallback callback) {

        NativeFileChooserUtils.checkNotNull(configuration, "configuration");
        NativeFileChooserUtils.checkNotNull(callback, "callback");

        String mimeFilterList = "";

        if (configuration.mimeFilter != null) {
            try {
                mimeFilterList = createFilterList(configuration.mimeFilter);
                System.out.println(mimeFilterList);
            } catch (MimeTypeException ignored) {
            }
        }

        PointerBuffer path = memAllocPointer(1);

        NFDFilterItem.Buffer filterList = null;
        try (MemoryStack stack = stackPush()) {

            if (configuration.fileTypeFilter != null && !configuration.fileTypeFilter.isEmpty()) {
                mimeFilterList += configuration.fileTypeFilter;
            }

            String[] filter = mimeFilterList.split(";");
            filterList = NFDFilterItem.malloc(filter.length);
            for (int i = 0; i < filter.length; i++) {

                String[] s = filter[i].split("/");
                filterList.get(i)
                        .name(stack.UTF8(s[0]))
                        .spec(stack.UTF8(s[1]));
            }

            int result = configuration.intent == NativeFileChooserIntent.SAVE ?
                    NativeFileDialog.NFD_SaveDialog(path, filterList, configuration.directory.path(), null) :
                    NativeFileDialog.NFD_OpenDialog(path, filterList, configuration.directory.path());


            switch (result) {
                case NativeFileDialog.NFD_OKAY:
                    FileHandle file = new FileHandle(path.getStringUTF8(0));
                    callback.onFileChosen(file);
                    NFD_FreePath(path.get(0));
                    break;
                case NativeFileDialog.NFD_CANCEL:
                    callback.onCancellation();
                    break;
                case NativeFileDialog.NFD_ERROR:
                    callback.onError(new Exception(NFD_GetError()));
                    break;
            }
        } catch (Exception e) {
            callback.onError(e);
        } finally {
            memFree(path);
            memFree(filterList);
        }
    }

	static String createFilterList(final String mimeType) throws MimeTypeException {
		return mimeType.split("/")[0] + "/" + findEligibleMimeTypes(mimeType).stream()
				.flatMap(type -> type.getExtensions().stream().map(s -> s.substring(1)))
				.distinct()
				.collect(Collectors.joining(","));
	}

	static Collection<MimeType> findEligibleMimeTypes(final String mimeType) throws MimeTypeException {
		MimeTypes allMimeTypes = MimeTypes.getDefaultMimeTypes();

		if (mimeType.contains("*")) {
			final Pattern mimePattern = NativeFileChooserUtils.mimePattern(mimeType);
			return allMimeTypes.getMediaTypeRegistry().getTypes().stream()
					.map(MediaType::toString)
					.filter(typeName -> mimePattern.matcher(typeName).matches())
					.map(typeName -> {
						try {
							return allMimeTypes.getRegisteredMimeType(typeName);
						} catch (MimeTypeException e) {
							return null;
						}
					})
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
		} else {
			return Collections.singletonList(allMimeTypes.forName(mimeType));
		}
	}
}
