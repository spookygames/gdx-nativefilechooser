/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016-2024 Spooky Games
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

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.memFree;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_FreePath;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_GetError;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_PathSet_EnumNext;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_PathSet_Free;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_PathSet_FreeEnum;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_PathSet_FreePath;
import static org.lwjgl.util.nfd.NativeFileDialog.NFD_PathSet_GetEnum;
import static org.lwjgl.util.nfd.NativeFileDialog.nNFD_FreePath;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NFDPathSetEnum;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import games.spooky.gdx.nativefilechooser.NativeFileChooser;
import games.spooky.gdx.nativefilechooser.NativeFileChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFileChooserConfiguration;
import games.spooky.gdx.nativefilechooser.NativeFileChooserIntent;
import games.spooky.gdx.nativefilechooser.NativeFileChooserUtils;
import games.spooky.gdx.nativefilechooser.NativeFilesChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFolderChooserCallback;
import games.spooky.gdx.nativefilechooser.NativeFolderChooserConfiguration;

public class DesktopFileChooser implements NativeFileChooser {

	@Override
	public void chooseFile(NativeFileChooserConfiguration configuration, NativeFileChooserCallback callback) {

        NativeFileChooserUtils.checkNotNull(configuration, "configuration");
        NativeFileChooserUtils.checkNotNull(callback, "callback");

		NFDFilterItem.Buffer filterList = null;

        try (MemoryStack stack = stackPush()) {

			if (configuration.mimeFilter != null) {
				filterList = createFilterList(configuration.mimeFilter, stack);
			}

			PointerBuffer path = stack.mallocPointer(1);
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
            memFree(filterList);
        }
    }

	@Override
	public void chooseFiles(NativeFileChooserConfiguration configuration, NativeFilesChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		NFDFilterItem.Buffer filterList = null;

		try (MemoryStack stack = stackPush()) {

			if (configuration.mimeFilter != null) {
				filterList = createFilterList(configuration.mimeFilter, stack);
			}

			PointerBuffer path = stack.mallocPointer(1);
			int result = NativeFileDialog.NFD_OpenDialogMultiple(path, filterList, configuration.directory.path());

			switch (result) {
				case NativeFileDialog.NFD_OKAY:
					long pathSet = path.get(0);

					NFDPathSetEnum psEnum = NFDPathSetEnum.calloc(stack);
					NFD_PathSet_GetEnum(pathSet, psEnum);

					Array<FileHandle> files = new Array<>();
					while (NFD_PathSet_EnumNext(psEnum, path) == NativeFileDialog.NFD_OKAY && path.get(0) != MemoryUtil.NULL) {
						files.add(new FileHandle(Objects.requireNonNull(path.getStringUTF8(0))));
						NFD_PathSet_FreePath(path.get(0));
					}
					callback.onFilesChosen(files);

					NFD_PathSet_FreeEnum(psEnum);
					NFD_PathSet_Free(pathSet);
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
			memFree(filterList);
		}
	}

	@Override
	public void chooseFolder(NativeFolderChooserConfiguration configuration, NativeFolderChooserCallback callback) {

		NativeFileChooserUtils.checkNotNull(configuration, "configuration");
		NativeFileChooserUtils.checkNotNull(callback, "callback");

		try (MemoryStack stack = stackPush()) {

			PointerBuffer path = stack.mallocPointer(1);
			int result = NativeFileDialog.NFD_PickFolder(path, configuration.directory.path());

			switch (result) {
				case NativeFileDialog.NFD_OKAY:
					FileHandle file = new FileHandle(path.getStringUTF8(0));
					callback.onFolderChosen(file);
					nNFD_FreePath(path.get(0));
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
		}
	}

	static NFDFilterItem.Buffer createFilterList(final String mimeTypeFilter, MemoryStack stack) {

		Collection<FileFilter> filters;

		try {
			String name = mimeTypeFilter.split("/")[0];
			String types = findEligibleMimeTypes(mimeTypeFilter).stream()
					.flatMap(type -> type.getExtensions().stream().map(s -> s.substring(1)))
					.distinct()
					.collect(Collectors.joining(","));
			filters = Collections.singletonList(new FileFilter(name, types));
		} catch (MimeTypeException mimeTypeException) {
			filters = Stream.of(mimeTypeFilter.split(";")).flatMap(mimeType -> {
				String[] slashSplit = mimeTypeFilter.split("/");
				if (slashSplit.length > 1) {
					return Stream.of(new FileFilter(slashSplit[0], slashSplit[1]));
				} else {
					return Stream.empty();
				}
			}).collect(Collectors.toList());
		}

        int length = filters.size();
        if (length > 0) {
			NFDFilterItem.Buffer filterList = NFDFilterItem.malloc(length);
            int i = 0;
            for (FileFilter filter : filters) {
                filterList.get(i++)
                        .name(stack.UTF8(filter.name))
                        .spec(stack.UTF8(filter.spec));
            }
			return filterList;
        } else {
			return null;
		}
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

	private static final class FileFilter {
		final String name;
		final String spec;

        private FileFilter(String name, String spec) {
            this.name = name;
            this.spec = spec;
        }
    }
}
